/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.palindromicity.bundles;

import static com.github.palindromicity.bundles.util.TestUtil.loadSpecifiedProperties;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.palindromicity.bundles.bundle.Bundle;
import com.github.palindromicity.bundles.util.BundleProperties;
import com.github.palindromicity.bundles.util.FileSystemManagerFactory;
import org.apache.commons.vfs2.FileSystemManager;
import com.github.palindromicity.bundles.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class BundleClassLoadersTest {

  static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

  @AfterClass
  public static void after() {
    BundleClassLoaders.reset();
  }

  @Test
  public void testReset() throws Exception {
    // this test is to ensure that we can
    // reset the BundleClassLoaders and initialize
    // a second time
    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        EMPTY_MAP);

    assertEquals("src/test/resources/BundleMapper/lib/",
        properties.getProperty("bundle.library.directory"));
    assertEquals("src/test/resources/BundleMapper/lib2/",
        properties.getProperty("bundle.library.directory.alt"));

    String altLib = properties.getProperty("bundle.library.directory.alt");
    properties.unSetProperty("bundle.library.directory.alt");

    FileSystemManager fileSystemManager = FileSystemManagerFactory
        .createFileSystemManager(new String[] {properties.getArchiveExtension()});

    BundleClassLoaders.init(fileSystemManager, TestUtil.getExtensionLibs(fileSystemManager, properties),
            properties);

    Set<Bundle> bundles = BundleClassLoaders.getInstance().getBundles();

    Assert.assertEquals(1, bundles.size());
    for (Bundle thisBundle : bundles) {
      Assert.assertEquals("com.github.palindromicity:foo-lib-bundle:0.1.0",
          thisBundle.getBundleDetails().getCoordinates().getCoordinates());
    }



    properties.setProperty("bundle.library.directory", altLib);
    boolean thrown = false;
    try {
      BundleClassLoaders.getInstance()
          .init(fileSystemManager, TestUtil.getExtensionLibs(fileSystemManager, properties),
              properties);
    } catch (IllegalStateException ise){
      thrown = true;
    }
    Assert.assertTrue(thrown);

    BundleClassLoaders.reset();

    BundleClassLoaders
        .init(fileSystemManager, TestUtil.getExtensionLibs(fileSystemManager, properties),
            properties);

    bundles = BundleClassLoaders.getInstance().getBundles();

    Assert.assertEquals(1, bundles.size());
    for (Bundle thisBundle : bundles) {
      Assert.assertEquals("com.github.palindromicity:bar-lib-bundle:0.1.0",
          thisBundle.getBundleDetails().getCoordinates().getCoordinates());
    }
  }
}