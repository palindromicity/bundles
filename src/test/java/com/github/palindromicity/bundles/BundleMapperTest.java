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

import com.github.palindromicity.bundles.bundle.Bundle;
import com.github.palindromicity.bundles.util.BundleProperties;
import com.github.palindromicity.bundles.util.FileSystemManagerFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import com.github.palindromicity.parsers.interfaces.MessageParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.github.palindromicity.bundles.util.TestUtil.loadSpecifiedProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BundleMapperTest {

  static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

  @AfterClass
  public static void after() {
    ExtensionManager.reset();
    BundleClassLoaders.reset();
  }

  @After
  public void afterTest() {
    ExtensionManager.reset();
    BundleClassLoaders.reset();
  }

  @Test
  public void testUnpackBundles()
      throws FileSystemException, URISyntaxException, NotInitializedException {

    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        EMPTY_MAP);

    assertEquals("src/test/resources/BundleMapper/lib/",
        properties.getProperty("bundle.library.directory"));
    assertEquals("src/test/resources/BundleMapper/lib2/",
        properties.getProperty("bundle.library.directory.alt"));

    FileSystemManager fileSystemManager = FileSystemManagerFactory.createFileSystemManager(new String[] {properties.getArchiveExtension()});
    ArrayList<Class> classes = new ArrayList<>();
    classes.add(MessageParser.class);
    final ExtensionMapping extensionMapping = BundleMapper
        .mapBundles(fileSystemManager,
            properties);

    assertEquals(2, extensionMapping.getAllExtensionNames().size());

    assertTrue(extensionMapping.getAllExtensionNames().keySet().contains(
        "com.github.palindromicity.parsers.FooParser"));
    assertTrue(extensionMapping.getAllExtensionNames().keySet().contains(
        "com.github.palindromicity.parsers.BarParser"));
  }

  @Test
  public void testUnpackBundlesFromEmptyDir()
      throws IOException, FileSystemException, URISyntaxException, NotInitializedException {

    final File emptyDir = new File("./target/empty/dir");
    emptyDir.delete();
    emptyDir.deleteOnExit();
    assertTrue(emptyDir.mkdirs());

    final Map<String, String> others = new HashMap<>();
    others.put("bundle.library.directory.alt", emptyDir.toString());
    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        others);
    FileSystemManager fileSystemManager = FileSystemManagerFactory.createFileSystemManager(new String[] {properties.getArchiveExtension()});
    ArrayList<Class> classes = new ArrayList<>();
    classes.add(MessageParser.class);
    // create a FileSystemManager
    Bundle systemBundle = ExtensionManager.createSystemBundle(fileSystemManager, properties);
    ExtensionManager.init(classes, systemBundle, Collections.emptySet());
    final ExtensionMapping extensionMapping = BundleMapper
        .mapBundles(fileSystemManager,
             properties);

    assertNotNull(extensionMapping);
    assertEquals(1, extensionMapping.getAllExtensionNames().size());
    assertTrue(extensionMapping.getAllExtensionNames().keySet().contains(
        "com.github.palindromicity.parsers.FooParser"));
  }

  @Test
  public void testUnpackBundlesFromNonExistantDir()
      throws FileSystemException, URISyntaxException, NotInitializedException {

    final File nonExistantDir = new File("./target/this/dir/should/not/exist/");
    nonExistantDir.delete();
    nonExistantDir.deleteOnExit();

    final Map<String, String> others = new HashMap<>();
    others.put("bundle.library.directory.alt", nonExistantDir.toString());
    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        others);
    FileSystemManager fileSystemManager = FileSystemManagerFactory.createFileSystemManager(new String[] {properties.getArchiveExtension()});
    ArrayList<Class> classes = new ArrayList<>();
    classes.add(MessageParser.class);
    // create a FileSystemManager
    Bundle systemBundle = ExtensionManager.createSystemBundle(fileSystemManager, properties);
    ExtensionManager.init(classes, systemBundle, Collections.emptySet());
    final ExtensionMapping extensionMapping = BundleMapper
        .mapBundles(fileSystemManager,
             properties);

    assertTrue(extensionMapping.getAllExtensionNames().keySet().contains(
        "com.github.palindromicity.parsers.FooParser"));

    assertEquals(1, extensionMapping.getAllExtensionNames().size());
  }

  @Test
  public void testUnpackBundlesFromNonDir()
      throws IOException, FileSystemException, URISyntaxException, NotInitializedException {

    final File nonDir = new File("./target/file.txt");
    nonDir.createNewFile();
    nonDir.deleteOnExit();

    final Map<String, String> others = new HashMap<>();
    others.put("bundle.library.directory.alt", nonDir.toString());
    BundleProperties properties = loadSpecifiedProperties("/BundleMapper/conf/bundle.properties",
        others);
    // create a FileSystemManager
    FileSystemManager fileSystemManager = FileSystemManagerFactory.createFileSystemManager(new String[] {properties.getArchiveExtension()});
    ArrayList<Class> classes = new ArrayList<>();
    classes.add(MessageParser.class);
    // create a FileSystemManager
    Bundle systemBundle = ExtensionManager.createSystemBundle(fileSystemManager, properties);
    ExtensionManager.init(classes, systemBundle, Collections.emptySet());
    final ExtensionMapping extensionMapping = BundleMapper
        .mapBundles(fileSystemManager,
            properties);

    assertNull(extensionMapping);
  }
}
