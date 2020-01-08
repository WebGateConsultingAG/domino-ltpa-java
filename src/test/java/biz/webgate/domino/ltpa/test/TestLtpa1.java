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
package biz.webgate.domino.ltpa.test;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import biz.webgate.domino.ltpa.Ltpa1;
import biz.webgate.domino.ltpa.LtpaConfiguration;
import biz.webgate.domino.ltpa.LtpaToken;

public class TestLtpa1 {

  private LtpaConfiguration debugConfig = new LtpaConfiguration() {
    @Override
    public boolean traceEnabled() {
      return true;
    }
  };
  
  private LtpaConfiguration config = new LtpaConfiguration() {
    @Override
    public Map<String, String> secrets() {
      return Collections.singletonMap("reddit.com", "SpecialSecretTokenValue");
    }
  };
  
  @Test
  public void testTokenGeneration() {
    
    Ltpa1 gen = new Ltpa1(config);
    
    gen.generate("Peter Maffay", "reddit.com", Instant.now())
      .ifPresent( token -> {
        assertThat(token).isNotNull();
        System.out.println(token.getToken());
        assertThat(token.getToken()).isNotEmpty();
      });
  }
  
  @Test
  public void testParseToken() {
    Ltpa1 ltpa = new Ltpa1(config);
    
    ltpa.generate("Peter Maffay", "reddit.com", Instant.now())
    .ifPresent( token -> {
      assertThat(token).isNotNull();
      assertThat(token.getToken()).isNotEmpty();
      Optional<LtpaToken> parsedOpt = ltpa.parse(token.getToken());
      assertThat(parsedOpt).isPresent();
      assertThat(parsedOpt.get().getToken()).isEqualTo(token.getToken());
    });
  }
  
  @Test
  public void testParseToken_garbageArgs() {
    Ltpa1 ltpa = new Ltpa1(config);
    
    assertThat(ltpa.parse(null)).isNotPresent();
    assertThat(ltpa.parse("")).isNotPresent();
    
    assertThat(ltpa.parse("1234asdasc c")).isNotPresent();
  }
  
  @Test
  public void testValidate() {
    Ltpa1 ltpa = new Ltpa1(config);
    
    ltpa.generate("Peter Maffay", "reddit.com", Instant.now())
    .ifPresent( token -> {
      assertThat(token).isNotNull();
      assertThat(token.getToken()).isNotEmpty();
      assertThat(ltpa.validate(token)).isTrue();
    });
  }
  
  @Test
  public void testValidate_Invalid() {
    Ltpa1 ltpa = new Ltpa1(config);
    
    ltpa.generate("Peter Maffay", "reddit.com", Instant.now())
    .ifPresent( token -> {
      assertThat(token).isNotNull();
      assertThat(token.getToken()).isNotEmpty();
      String decoded = new String(Base64.decodeBase64(token.getToken()));
      decoded = decoded.replace("Peter", "Hubert");
      assertThat(ltpa.validate(token.withToken(Base64.encodeBase64String(decoded.getBytes())))).isFalse();
    });
  }
  
}
