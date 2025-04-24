/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.FileBasedDistributionMap;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileBasedResponseDelayAcceptanceTest extends AcceptanceTestBase {

  @BeforeAll
  public static void setupServer() {
    setupServer(getOptions());
  }

  private static WireMockConfiguration getOptions() {
    WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
    wireMockConfiguration.dynamicPort();
    wireMockConfiguration.withRootDirectory(defaultTestFilesRoot());
    wireMockConfiguration.fileBasedDistributionsConfigFiles(
        Collections.singletonList("__files/file-based-distributions.json"));
    return wireMockConfiguration;
  }

  @Test
  public void responseWithFileBasedFixedDelay() {
    testFileBasedDistribution("/file_based_distribution/fixed", 50);
  }

  @Test
  public void responseWithFileBasedUniformDelay() {
    testFileBasedDistribution("/file_based_distribution/uniform", 25);
  }

  @Test
  public void responseWithFileBasedLogNormalDelay() {
    testFileBasedDistribution("/file_based_distribution/log_normal", 60);
  }

  @Test
  public void responseWithFileBasedTruncatedLogNormalDelay() {
    testFileBasedDistribution("/file_based_distribution/truncated_log_normal", 60);
  }

  @Test
  public void fileBasedDistributionExistanceCheck() {
    setupStub("/file_based_distribution/truncated_log_normal");

    FileBasedDistributionMap.checkDistributions(wireMockServer);
  }

  @Test
  public void fileBasedDistributionNonExistanceCheck() {
    setupStub("/file_based_distribution/distribution_key_missing_from_config");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FileBasedDistributionMap.checkDistributions(wireMockServer);
        });
  }

  private void testFileBasedDistribution(String urlPath, int minExpectedResponseTime) {
    setupStub(urlPath);

    long start = System.currentTimeMillis();
    testClient.get(urlPath);
    int duration = (int) (System.currentTimeMillis() - start);

    assertThat(duration, greaterThanOrEqualTo(minExpectedResponseTime));
  }

  private void setupStub(String urlPath) {
    stubFor(
        get(urlEqualTo(urlPath))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("Content for: " + urlPath)
                    .withFileBasedDelay("GET:" + urlPath)));
  }
}
