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

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.*;

/**
 * A response distribution latency management class that can be instructed to load distributions
 * from a config file, which are then used from <code>FileBasedDistribution</code> instances to
 * lookup the correct distribution instance by key to use.
 *
 * @see <a
 *     href="http://wiremock.org/docs/simulating-faults/">http://wiremock.org/docs/simulating-faults/</a>
 * @see FileBasedDistribution
 */
public class FileBasedDistributionMap {

  /**
   * The map of distribution key names to distribution. The variable is static so that it is
   * accessible from all of the DelayDistribution implementations.
   */
  private static Map<String, DelayDistribution> DISTRIBUTION_MAP;

  /**
   * Initialise the distribution map from the specified configuration files.
   *
   * @param fileSource The WireMock file source where the distribution configuration files are
   *     expected to reside
   * @param distributionFilenames A List of distribution configuration filenames
   */
  public static void configureFromFiles(FileSource fileSource, List<String> distributionFilenames) {
    if (distributionFilenames == null) {
      return;
    }

    DISTRIBUTION_MAP = new HashMap<>();
    for (String distributionFilename : distributionFilenames) {
      TextFile textFile = fileSource.getTextFileNamed(distributionFilename);
      TypeReference<HashMap<String, DelayDistribution>> typeRef =
          new TypeReference<HashMap<String, DelayDistribution>>() {};
      DISTRIBUTION_MAP.putAll(Json.read(textFile.readContentsAsString(), typeRef));
    }
  }

  /**
   * Get the specified distribution from the map for the given key.
   *
   * @param key The key to look up the distribution by
   * @return The distribution, if it exists under the specified key, or <code>null</code> otherwise.
   */
  public static DelayDistribution getDistribution(String key) {
    return DISTRIBUTION_MAP != null ? DISTRIBUTION_MAP.get(key) : null;
  }

  /**
   * Utility method to iterate over the configured WireMock server mappings and check that for any
   * using file based distributions, a distribution exists with the given key.
   *
   * @param wireMockServer The WireMock server instance whose mapping files are to be checked.
   * @throws IllegalArgumentException if any mapping files existing using file based distribution
   *     keys that don't exist.
   */
  public static void checkDistributions(WireMockServer wireMockServer) {

    Set<String> missingFileBasedDistributionKeys = new HashSet<>();

    // Iterate over all the mappings checking for any file based distributions...
    for (StubMapping nextMapping : wireMockServer.getStubMappings()) {
      DelayDistribution nextDistribution = nextMapping.getResponse().getDelayDistribution();

      // If it's file based, check the key exists in the config map.
      if (nextDistribution instanceof FileBasedDistribution) {
        try {
          // Easiest way to check the distribution key exists in the distribution config JSON is to
          // invoke it.
          nextDistribution.sampleMillis();
        } catch (NullPointerException ex) {
          missingFileBasedDistributionKeys.add(((FileBasedDistribution) nextDistribution).getKey());
        }
      }
    }

    if (missingFileBasedDistributionKeys.size() != 0) {
      throw new IllegalArgumentException(
          String.format(
              "The following distribution keys are configured in mappings "
                  + "but do not exist in the distribution config file: %s",
              missingFileBasedDistributionKeys));
    }
  }

  /** Clears the distribution map of all entries. */
  public static void clearDistributions() {
    DISTRIBUTION_MAP = null;
  }
}
