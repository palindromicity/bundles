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

public class ExtensionManagerContextTest {
  static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

  @AfterClass
  public static void after() {
    BundleClassLoaders.reset();
  }

  @Test
  public void merge() throws Exception {
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
    ExtensionManagerContext context = new ExtensionManagerContext.Builder().withClasses(classes).withSystemBundle(systemBundle).withBundles(BundleClassLoaders.getInstance().getBundles()).build();

    List<Bundle> bundles = context.getClassNameBundleLookup().get(BundleThreadContextClassLoaderTest.WithPropertiesConstructor.class.getName());
    Assert.assertTrue(bundles.size() == 1);
    Assert.assertEquals(bundles.get(0), context.getClassLoaderBundleLookup().get(bundles.get(0).getClassLoader()));
    Assert.assertEquals(bundles.get(0), context.getBundleCoordinateBundleLookup().get(bundles.get(0).getBundleDetails().getCoordinates()));


    BundleClassLoaders.reset();

    classes = Arrays.asList(AbstractFoo2.class);

    BundleClassLoaders
        .init(fileSystemManager, TestUtil.getExtensionLibs(fileSystemManager, properties),
            properties);
    ExtensionManagerContext context2 = new ExtensionManagerContext.Builder().withClasses(classes).withSystemBundle(systemBundle).withBundles(BundleClassLoaders.getInstance().getBundles()).build();
    bundles = context2.getClassNameBundleLookup().get(WithPropertiesConstructor2.class.getName());
    Assert.assertTrue(bundles.size() == 1);
    Assert.assertEquals(bundles.get(0), context2.getClassLoaderBundleLookup().get(bundles.get(0).getClassLoader()));
    Assert.assertEquals(bundles.get(0), context2.getBundleCoordinateBundleLookup().get(bundles.get(0).getBundleDetails().getCoordinates()));

    context.merge(context2);

    bundles = context.getClassNameBundleLookup().get(BundleThreadContextClassLoaderTest.WithPropertiesConstructor.class.getName());
    Assert.assertTrue(bundles.size() == 1);
    List<Bundle> bundles2 = context2.getClassNameBundleLookup().get(WithPropertiesConstructor2.class.getName());
    Assert.assertTrue(bundles2.size() == 1);
    Assert.assertEquals(bundles.get(0), context.getClassLoaderBundleLookup().get(bundles.get(0).getClassLoader()));
    Assert.assertEquals(bundles.get(0), context.getBundleCoordinateBundleLookup().get(bundles.get(0).getBundleDetails().getCoordinates()));
    Assert.assertEquals(bundles2.get(0), context.getClassLoaderBundleLookup().get(bundles2.get(0).getClassLoader()));
    Assert.assertEquals(bundles2.get(0), context.getBundleCoordinateBundleLookup().get(bundles2.get(0).getBundleDetails().getCoordinates()));


  }
  public static class WithPropertiesConstructor2 extends AbstractFoo2 {

    public WithPropertiesConstructor2() {
    }

    public WithPropertiesConstructor2(BundleProperties properties) {
      if (properties.getProperty("fail") != null) {
        throw new RuntimeException("Intentional failure");
      }
    }
  }
}