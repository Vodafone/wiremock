/*
 * Copyright (C) 2020-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileBasedDistributionMapTest {

  @BeforeEach
  public void init() {
    FileBasedDistributionMap.clearDistributions();
  }

  @Test
  public void nullFileBasedDistributionMapReturnsNull() {
    DelayDistribution delayDistribution =
        FileBasedDistributionMap.getDistribution("non-existent-key");
    assertThat(delayDistribution, is(nullValue()));
  }

  @Test
  public void nullDistributionsFileListDoesNothing() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());

    assertDoesNotThrow(
        () -> {
          FileBasedDistributionMap.configureFromFiles(filesRoot, null);
        });
  }

  @Test
  public void emptyDistributionsFileListDoesNothing() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());

    assertDoesNotThrow(
        () -> {
          FileBasedDistributionMap.configureFromFiles(filesRoot, emptyList());
        });
  }

  @Test
  public void nonExistentDistributionsFileThrowsException() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());
    List<String> fileBasedDistributions =
        Collections.singletonList("__files/non_existent_file.json");

    assertThrows(
        FileNotFoundException.class,
        () -> {
          FileBasedDistributionMap.configureFromFiles(filesRoot, fileBasedDistributions);
        });
  }

  @Test
  public void nonExistentKeyReturnsNull() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());
    List<String> fileBasedDistributions =
        Collections.singletonList("__files/file-based-distributions.json");
    FileBasedDistributionMap.configureFromFiles(filesRoot, fileBasedDistributions);

    DelayDistribution delayDistribution =
        FileBasedDistributionMap.getDistribution("non-existent-key");

    assertThat(delayDistribution, is(nullValue()));
  }

  @Test
  public void validKeyReturnsDelayDistribution() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());
    List<String> fileBasedDistributions =
        Collections.singletonList("__files/file-based-distributions.json");
    FileBasedDistributionMap.configureFromFiles(filesRoot, fileBasedDistributions);

    DelayDistribution delayDistribution =
        FileBasedDistributionMap.getDistribution("GET:/file_based_distribution/fixed");

    assertThat(delayDistribution, is(not(nullValue())));
    assertThat(delayDistribution.sampleMillis(), equalTo(50L));
  }

  @Test
  public void validKeyReturnsOverriddenDelayDistribution() {
    FileSource filesRoot = new SingleRootFileSource(defaultTestFilesRoot());
    List<String> fileBasedDistributions =
        Arrays.asList(
            "__files/file-based-distributions.json",
            "__files/file-based-distributions2.json",
            "__files/file-based-distributions3.json");
    FileBasedDistributionMap.configureFromFiles(filesRoot, fileBasedDistributions);

    DelayDistribution delayDistribution1 =
        FileBasedDistributionMap.getDistribution(
            "GET:/file_based_distribution/fixed/override/test1");

    DelayDistribution delayDistribution2 =
        FileBasedDistributionMap.getDistribution(
            "GET:/file_based_distribution/fixed/override/test2");

    DelayDistribution delayDistribution3 =
        FileBasedDistributionMap.getDistribution(
            "GET:/file_based_distribution/fixed/override/test3");

    assertThat(delayDistribution1, is(not(nullValue())));
    assertThat(delayDistribution1.sampleMillis(), equalTo(51L));

    assertThat(delayDistribution2, is(not(nullValue())));
    assertThat(delayDistribution2.sampleMillis(), equalTo(52L));

    assertThat(delayDistribution3, is(not(nullValue())));
    assertThat(delayDistribution3.sampleMillis(), equalTo(53L));
  }
}
