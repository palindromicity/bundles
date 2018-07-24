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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import com.github.palindromicity.bundles.BundleThreadContextClassLoaderTest.WithPropertiesConstructor;
import com.github.palindromicity.bundles.bundle.Bundle;
import com.github.palindromicity.bundles.util.BundleProperties;
import com.github.palindromicity.bundles.util.FileSystemManagerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileSystemManager;
import com.github.palindromicity.bundles.util.ResourceCopier;
import com.github.palindromicity.parsers.interfaces.MessageParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BundleSystemTest {

  @AfterClass
  public static void after() {
    BundleClassLoaders.reset();
    ExtensionManager.reset();
  }

  @After
  public void afterTest() {
    ExtensionManager.reset();
    BundleClassLoaders.reset();
  }

  @BeforeClass
  public static void copyResources() throws IOException {
    ResourceCopier.copyResources(Paths.get("./src/test/resources"), Paths.get("./target"));
  }

  @Test
  public void createInstance() throws Exception {
    BundleProperties properties = BundleProperties
        .createBasicBundleProperties("src/test/resources/bundle.properties", null);

    properties.setProperty(BundleProperties.BUNDLE_LIBRARY_DIRECTORY, "src/test/resources/BundleMapper/lib");
    BundleSystem bundleSystem = new BundleSystemBuilder().withBundleProperties(properties)
        .withExtensionClasses(
            Arrays.asList(AbstractFoo.class)).build();
    Assert.assertNotNull(bundleSystem.createInstance(WithPropertiesConstructor.class.getName(),
        WithPropertiesConstructor.class));

  }

  @Test(expected = IllegalArgumentException.class)
  public void createInstanceFail() throws Exception {
    BundleSystem bundleSystem = new BundleSystemBuilder().build();
  }

  @Test
  public void testAddBundle() throws Exception {
    BundleProperties properties = BundleProperties
        .createBasicBundleProperties("target/bundle.properties", null);

    properties.setProperty(BundleProperties.BUNDLE_LIBRARY_DIRECTORY,
        "target/BundleMapper/lib");
    File f = new File("src/test/resources/BundleMapper/lib/foo-lib-bundle-0.1.0.bundle");
    File t = new File(
        "target/BundleMapper/lib/foo-lib-bundle-0.1.0.bundle");
    if (t.exists()) {
      t.delete();
    }
    FileSystemManager fileSystemManager = FileSystemManagerFactory
        .createFileSystemManager(new String[]{properties.getArchiveExtension()});
    BundleSystem bundleSystem = new BundleSystemBuilder()
        .withFileSystemManager(fileSystemManager)
        .withBundleProperties(properties).withExtensionClasses(
            Arrays.asList(AbstractFoo.class, MessageParser.class)).build();
    Assert.assertTrue(
        bundleSystem.createInstance(WithPropertiesConstructor.class.getName(),
            WithPropertiesConstructor.class) != null);
    // copy the file into bundles library
    FileUtils.copyFile(f, t);
    bundleSystem.addBundle("foo-lib-bundle-0.1.0.bundle");

    Assert.assertEquals(1, BundleClassLoaders.getInstance().getBundles().size());
    for (Bundle thisBundle : BundleClassLoaders.getInstance().getBundles()) {
      Assert.assertTrue(
          thisBundle.getBundleDetails().getCoordinates().getCoordinates()
              .equals("com.github.palindromicity:bar-lib-bundle:0.1.0")
              ||
              thisBundle.getBundleDetails().getCoordinates().getCoordinates()
                  .equals("com.github.palindromicity:foo-lib-bundle:0.1.0")

      );
    }
  }
}
