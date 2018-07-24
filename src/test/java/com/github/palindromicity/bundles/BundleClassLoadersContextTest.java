/*
 * Copyright 2018 bundles authors
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.palindromicity.bundles;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.github.palindromicity.bundles.bundle.Bundle;
import com.github.palindromicity.bundles.util.BundleProperties;
import com.github.palindromicity.bundles.util.FileSystemManagerFactory;
import com.github.palindromicity.bundles.util.TestUtil;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class BundleClassLoadersContextTest {
  static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

  @AfterClass
  public static void after() {
    BundleClassLoaders.reset();
  }

  @Test
  public void merge() throws Exception {
    BundleProperties properties = TestUtil.loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        EMPTY_MAP);

    assertEquals("src/test/resources/BundleMapper/lib/",
        properties.getProperty("bundle.library.directory"));
    assertEquals("src/test/resources/BundleMapper/lib2/",
        properties.getProperty("bundle.library.directory.alt"));

    String altLib = properties.getProperty("bundle.library.directory.alt");
    String lib = properties.getProperty("bundle.library.directory");
    properties.unSetProperty("bundle.library.directory.alt");

    FileSystemManager fileSystemManager = FileSystemManagerFactory
        .createFileSystemManager(new String[] {properties.getArchiveExtension()});

    BundleClassLoadersContext firstContext = new BundleClassLoadersContext.Builder().withFileSystemManager(fileSystemManager)
        .withExtensionDirs(TestUtil.getExtensionLibs(fileSystemManager,properties)).withBundleProperties(properties).build();

    Assert.assertEquals(1, firstContext.getBundles().size());
    for (Bundle thisBundle : firstContext.getBundles().values()) {
      Assert.assertEquals("com.github.palindromicity:foo-lib-bundle:0.1.0",
          thisBundle.getBundleDetails().getCoordinates().getCoordinates());
    }

    // set the lib again so the utils will pickup the other directory
    properties.setProperty("bundle.library.directory", altLib);

    BundleClassLoadersContext secondContext = new BundleClassLoadersContext.Builder().withFileSystemManager(fileSystemManager)
        .withExtensionDirs(TestUtil.getExtensionLibs(fileSystemManager,properties)).withBundleProperties(properties).build();


    Assert.assertEquals(1, secondContext.getBundles().size());
    for (Bundle thisBundle : secondContext.getBundles().values()) {
      Assert.assertEquals("com.github.palindromicity:bar-lib-bundle:0.1.0",
          thisBundle.getBundleDetails().getCoordinates().getCoordinates());
    }

    // ok merge together

    firstContext.merge(secondContext);
    Assert.assertEquals(2, firstContext.getBundles().size());
    for (Bundle thisBundle : firstContext.getBundles().values()) {
      Assert.assertTrue(
          thisBundle.getBundleDetails().getCoordinates().getCoordinates()
              .equals("com.github.palindromicity:foo-lib-bundle:0.1.0")
              ||
              thisBundle.getBundleDetails().getCoordinates().getCoordinates()
                  .equals("com.github.palindromicity:bar-lib-bundle:0.1.0")

      );
    }

    // merge a thirds, with duplicates
    // set both dirs
    properties.setProperty("bundle.library.directory.alt",lib);

    BundleClassLoadersContext thirdContext = new BundleClassLoadersContext.Builder().withFileSystemManager(fileSystemManager)
        .withExtensionDirs(TestUtil.getExtensionLibs(fileSystemManager,properties)).withBundleProperties(properties).build();

    Assert.assertEquals(2, thirdContext.getBundles().size());
    for (Bundle thisBundle : thirdContext.getBundles().values()) {
      Assert.assertTrue(
          thisBundle.getBundleDetails().getCoordinates().getCoordinates()
              .equals("com.github.palindromicity:foo-lib-bundle:0.1.0")
              ||
              thisBundle.getBundleDetails().getCoordinates().getCoordinates()
                  .equals("com.github.palindromicity:bar-lib-bundle:0.1.0")

      );
    }

    // merge them
    firstContext.merge(thirdContext);
    Assert.assertEquals(2, firstContext.getBundles().size());
    for (Bundle thisBundle : firstContext.getBundles().values()) {
      Assert.assertTrue(
          thisBundle.getBundleDetails().getCoordinates().getCoordinates()
              .equals("com.github.palindromicity:foo-lib-bundle:0.1.0")
              ||
              thisBundle.getBundleDetails().getCoordinates().getCoordinates()
                  .equals("com.github.palindromicity:bar-lib-bundle:0.1.0")

      );
    }
  }
}