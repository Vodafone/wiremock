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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Returns log normally distributed values, capped at a maximum value to eliminate long tales. Takes three
 * parameters, the median (50th percentile) of the lognormal, the standard deviation of the underlying
 * normal distribution and the maximum value at which to cap the response delay.
 *
 * @see <a href="https://www.wolframalpha.com/input/?i=lognormaldistribution%28log%2890%29%2C+0.1%29">lognormal example</a>
 */
public class CappedLogNormal extends LogNormal {

  @JsonProperty("maxValue")
  private final double maxValue;

  /**
   * @param median 50th percentile of the distribution in millis
   * @param sigma standard deviation of the distribution, a larger value produces a longer tail
   */
  @JsonCreator
  public CappedLogNormal(
      @JsonProperty("median") double median,
      @JsonProperty("sigma") double sigma,
      @JsonProperty("maxValue") double maxValue) {
    super(median, sigma);
    this.maxValue = maxValue;

    if (maxValue < median) {
      throw new IllegalArgumentException("The max value has to be at least greater than the median");
    }
  }

  @Override
  public long sampleMillis() {

    long generatedValue = super.sampleMillis();

    // Rather than capping the value at the max, if it's over the max value, then resample, but only do that a few times
    int i = 0;
    while (generatedValue > maxValue && i < 10) {
      generatedValue = super.sampleMillis();
      i++;
    }

    // Belt and braces, in the unlikely event the generated value is still over the max, cap it at the max
    return Math.round(Math.min(maxValue, generatedValue));
  }
}