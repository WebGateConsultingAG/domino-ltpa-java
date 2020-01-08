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

import java.util.Collections;
import java.util.Map;

public interface LtpaConfiguration {
  /**
   * How long the Ltpa token should be valid, in millisecond
   * @return the millisecond value, default 5400
   */
  default int validMillis() {
    return 5400;
  }
  
  /**
   * Gracemargin for issueingTime and expireTime, in milliseconds
   * @return the millisecond value, default 300
   */
  default int graceMillis() {
    return 300;
  }

  /**
   * Secrets to generate the SHA-1 Hahes, mapped by domain.
   * @return The mapped secrest, default empty map
   */
  default Map<String, String> secrets() {
    return Collections.emptyMap();
  }
  
  /**
   * Enables the printing of stacktraces inside the ltpa generation
   * @return true / false, indicating wether to print stacktraces
   */
  default boolean traceEnabled() {
    return false;
  }
}