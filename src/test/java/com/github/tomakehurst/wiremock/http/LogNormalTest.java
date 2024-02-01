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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class LogNormalTest {

  private double median = 90.0;
  private double sigma = 0.39;

  @Test
  public void samplingLogNormalHasExpectedMean() {
    LogNormal distribution = new LogNormal(median, sigma);
    samplingLogNormalHasExpectedMean(distribution, 97.1115);
  }

  @Test
  public void samplingCappedLogNormalWithHighCapHasExpectedMean() {
    samplingCappedLogNormalHasExpectedMean(150, 88.15);
  }

  @Test
  public void samplingCappedLogNormalWithLowerCapHasExpectedMean() {
    samplingCappedLogNormalHasExpectedMean(130, 83.6);
  }

  @Test
  public void samplingCappedLogNormalWithCapSameAsMaxHasExpectedMean() {
    // This test should, on occasion, exercise the resampling of the distribution value when the initial generated
    // value(s) are higher than the max.
    samplingCappedLogNormalHasExpectedMean((long) median, 67.82);
  }

  @Test
  public void samplingCappedLogNormalFailsIfMaxLessThanMedian() {
    try {
      new CappedLogNormal(median, sigma, median);
    } catch (IllegalArgumentException ex) {
      // Fail - max = media is okay
      Assert.fail("A maxValue matching median should not throw an exception");
    }

    try {
      new CappedLogNormal(median, sigma, median - 1);
      // Fail - max = media is okay
      Assert.fail("A maxValue less than median should throw an exception");
    } catch (IllegalArgumentException ex) {
      // Exception expected
    }
  }


  private void samplingCappedLogNormalHasExpectedMean(long maxCapValue, double expectedMean) {
    LogNormal distribution = new CappedLogNormal(median, sigma, maxCapValue);
    samplingLogNormalHasExpectedMean(distribution, expectedMean);
  }

  // To test properly we would need something like a normality test.
  // For our purposes, a simple verification is sufficient.
  private void samplingLogNormalHasExpectedMean(LogNormal distribution, double expectedMean) {

    int n = 10000;

    long sum = 0;
    for (int i = 0; i < n; i++) {
      long val = distribution.sampleMillis();
      sum += val;
    }

    assertEquals(expectedMean, sum / (double) n, 5.0);
  }
}
