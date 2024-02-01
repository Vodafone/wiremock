/*
 * Copyright (C) 2015-2024 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response latency distribution whose configuration is looked up by key in a JSON configuration
 * file in order that:
 *
 * <p>a) response latencies can be switched out wholesale b) response latencies can more easily be
 * automatically generated/updated
 *
 * <p>Benefits of being able to easily switch latencies are that you can reduce (or remove entirely)
 * latencies for testing types where the latency isn't relevant e.g. integration testing, most
 * regression testing, smoke testing, most development etc, and also simulate different downstream
 * states (e.g. switching between BAU response times and peak response times etc
 *
 * @see FileBasedDistributionMap
 */
public class FileBasedDistribution implements DelayDistribution {

  @JsonProperty("key")
  private final String key;

  /**
   * Construct a file based distribution where the given key is used to look up the distribution
   * definition in the configured distribution JSON file.
   *
   * @see FileBasedDistributionMap
   * @param key The key used to look up the distribution definition in the <code>
   *     FileBasedDistributionMap</code> config file.
   */
  public FileBasedDistribution(@JsonProperty("key") String key) {
    this.key = key;
  }

  /**
   * Get the generated distribution response time, be that fixed, uniform, log normal or capped log
   * normal.
   *
   * @return the generated/configured response delay in milliseconds
   * @throws IllegalArgumentException if the key doesn't exist in the distribution config file.
   */
  @Override
  public long sampleMillis() {
    DelayDistribution delayDistribution = FileBasedDistributionMap.getDistribution(key);
    if (delayDistribution == null) {
      throw new IllegalArgumentException(
          String.format("Cannot find file based distribution with key %s", key));
    }
    return delayDistribution.sampleMillis();
  }

  /**
   * Get the key used to look up the distribution definition in the config file.
   *
   * @return
   */
  public String getKey() {
    return key;
  }
}
