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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.palindromicity.bundles.bundle.Bundle;
import com.github.palindromicity.bundles.util.BundleProperties;
import com.github.palindromicity.bundles.util.FileSystemManagerFactory;
import org.apache.commons.vfs2.FileSystemManager;
import com.github.palindromicity.bundles.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionManagerTest {
  static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

  @AfterClass
  public static void after() {
    BundleClassLoaders.reset();
  }

  @Test
  public void testExtensionManagerFunctions() throws Exception{
    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        EMPTY_MAP);

    assertEquals("src/test/resources/BundleMapper/lib/",
        properties.getProperty("bundle.library.directory"));
    assertEquals("src/test/resources/BundleMapper/lib2/",
        properties.getProperty("bundle.library.directory.alt"));

    FileSystemManager fileSystemManager = FileSystemManagerFactory
        .createFileSystemManager(new String[] {properties.getArchiveExtension()});
    List<Class> classes = Arrays.asList(AbstractFoo.class);

    BundleClassLoaders
        .init(fileSystemManager, TestUtil.getExtensionLibs(fileSystemManager, properties),
            properties);

    Bundle systemBundle = ExtensionManager.createSystemBundle(fileSystemManager, properties);
    ExtensionManager.init(classes, systemBundle, BundleClassLoaders.getInstance().getBundles());

    List<Bundle> bundles = ExtensionManager.getInstance().getBundles(BundleThreadContextClassLoaderTest.WithPropertiesConstructor.class.getName());
    Assert.assertTrue(bundles.size() == 1);

    Assert.assertEquals(bundles.get(0), ExtensionManager.getInstance().getBundle(bundles.get(0).getClassLoader()));

    Assert.assertEquals(bundles.get(0), ExtensionManager.getInstance().getBundle(bundles.get(0).getBundleDetails().getCoordinates()));

  }
}