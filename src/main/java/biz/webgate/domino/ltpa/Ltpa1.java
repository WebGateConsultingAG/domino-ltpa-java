/*
 * Copyright 2020 Webgate Consulting AG

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package biz.webgate.domino.ltpa;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;

public class Ltpa1 {

  private final LtpaConfiguration config;
  private final static int DIGEST_SIZE = 20;
  private final static byte[] HEADER = new byte[] {0, 1, 2, 3};

  public Ltpa1(LtpaConfiguration config) {
    this.config = config;
  }

  public Optional<LtpaToken> generate(String username, String domain) {
    return generate(username, domain, Instant.now());
  }

  public Optional<LtpaToken> generate(String username, String domain, Instant issueingTime) {
    if (!config.secrets().containsKey(domain)) {
      throw new RuntimeException("FATAL: Unknown LTPA Domain: " + domain + ", No Secret available!");
    }
    if ( username == null || username.isEmpty()) {      
      throw new IllegalArgumentException("FATAL: Username is required to generate LTPA Token!");
    }

    try {
      Instant creation = issueingTime.minusMillis(config.graceMillis());
      Instant expires = issueingTime.plusMillis(config.validMillis() + config.graceMillis());
      
      byte[] creationHexBytes = Long.toHexString(creation.getEpochSecond()).getBytes();
      byte[] expiresHexBytes = Long.toHexString(expires.getEpochSecond()).getBytes();
      byte[] name = username.getBytes(Charset.forName("IBM850"));
  
      byte[] rawTokenNoDigest = ByteBuffer.allocate(HEADER.length + creationHexBytes.length + expiresHexBytes.length + name.length)
        .put(HEADER)
        .put(creationHexBytes)
        .put(expiresHexBytes)
        .put(name)
        .array();
      
      byte[] rawToken = ByteBuffer.allocate(rawTokenNoDigest.length + DIGEST_SIZE)
        .put(rawTokenNoDigest)
        .put(sha1(rawTokenNoDigest, config.secrets().get(domain)))
        .array();
      return Optional.of(new LtpaToken(Base64.encodeBase64String(rawToken), domain, username, creation, expires));
    } catch ( Exception e) {
      if ( config.traceEnabled()) {
        e.printStackTrace();
      }
      return Optional.empty();
    }
  }
  
  public boolean validate(LtpaToken token) {
    return validate(token.getToken(), token.getDomain());
  }
  
  public boolean validate(String ltpaToken, String domain) {
    if ( domain == null || !config.secrets().containsKey(domain)) {
      return false;
    }
    try {
      byte[] raw = Base64.decodeBase64(ltpaToken);
      byte[] digest = Arrays.copyOfRange(raw, raw.length - 20, raw.length);
      return Arrays.equals(digest, sha1(Arrays.copyOfRange(raw, 0, raw.length - 20), config.secrets().get(domain)));
    } catch ( Exception e) {
      if ( config.traceEnabled()) {
        e.printStackTrace();
      }
      return false;
    }
  }
  
  public Optional<LtpaToken> parse(String ltpaToken) {
    if ( ltpaToken == null || ltpaToken.isEmpty()) {
      return Optional.empty();
    }
    try {
      byte[] raw = Base64.decodeBase64(ltpaToken);
      long creation = Long.parseUnsignedLong(new String(Arrays.copyOfRange(raw, 4, 12)), 16);
      long expires = Long.parseUnsignedLong(new String(Arrays.copyOfRange(raw, 12, 20)), 16);
      String username = new String(Arrays.copyOfRange(raw, 20, raw.length - 20));
      
      return Optional.of(new LtpaToken(ltpaToken, null, username, Instant.ofEpochSecond(creation), Instant.ofEpochSecond(expires)));
    } catch( Exception e) {
      if ( config.traceEnabled()) {
        e.printStackTrace();
      }
      return Optional.empty();
    }
  }
  
  private byte[] sha1(byte[] array, String secret) {
    try {
      MessageDigest sha1;
      sha1 = MessageDigest.getInstance("SHA-1");
      sha1.update(array);
      return sha1.digest(secret.getBytes());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("LTPA FATAL: Cannot load MessageDigest for [SHA-1]");
    }
  }
}
