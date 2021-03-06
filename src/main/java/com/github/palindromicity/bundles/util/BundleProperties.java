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

package com.github.palindromicity.bundles.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The BundleProperties class holds all properties which are needed for various values to be
 * available at runtime. The properties contains keys and values.
 */
public abstract class BundleProperties {

  // core properties
  public static final String PROPERTIES_FILE_PATH = "bundle.properties.file.path";
  public static final String BUNDLE_LIBRARY_DIRECTORY = "bundle.library.directory";
  public static final String BUNDLE_LIBRARY_DIRECTORY_PREFIX = "bundle.library.directory.";
  public static final String ARCHIVE_EXTENSION = "bundle.archive.extension";
  public static final String META_ID_PREFIX = "bundle.meta.id.prefix";
  public static final String BUNDLE_EXTENSION_TYPE_PREFIX = "bundle.extension.type.";

  // defaults
  public static final String DEFAULT_ARCHIVE_EXTENSION = "bundle";
  public static final String DEFAULT_BUNDLE_LIBRARY_DIR = "./lib/";
  public static final String DEFAULT_META_ID_PREFIX = "Bundle";

  /**
   * Retrieves the property value for the given property key.
   *
   * @param key the key of property value to lookup
   * @return value of property at given key or null if not found
   */
  public abstract String getProperty(String key);

  public String getProperty(final String key, final String defaultValue) {
    final String value = getProperty(key);
    return (value == null || value.trim().isEmpty()) ? defaultValue : value;
  }

  public abstract void setProperty(String key, String value);

  public abstract void unSetProperty(String key);

  public abstract boolean match(BundleProperties other);

  public abstract void storeProperties(OutputStream outputStream, String comments)
      throws IOException;

  /**
   * Retrieves all known property keys.
   *
   * @return all known property keys
   */
  public abstract Set<String> getPropertyKeys();



  // getters for core properties //

  private static URI getConcatedDirUri(URI original, String dir) throws URISyntaxException {
    String uri = original.toString();
    StringBuilder builder = new StringBuilder(uri);
    if (uri.endsWith("/") == false) {
      builder.append("/");
    }
    builder.append(dir);
    return new URI(builder.toString());
  }

  public URI getBundleLibraryDirectory() throws URISyntaxException {
    String bundleLib = getProperty(BUNDLE_LIBRARY_DIRECTORY);
    return getUri(bundleLib);
  }

  /**
   * Returns a {@link List} of URI's to all of the defined Bundle Library Directories.
   * These are defined using BUNDLE_LIBRARY_DIRECTORY + zero or more BUNDLE_LIBRARY_DIRECTORY_PREFIX
   * properties.
   * @return List
   * @throws URISyntaxException if there is an error building any of the URIs
   */
  public List<URI> getBundleLibraryDirectories() throws URISyntaxException {

    List<URI> bundleLibraryPaths = new ArrayList<>();

    // go through each property
    for (String propertyName : getPropertyKeys()) {
      // determine if the property is a bundle library path
      if (StringUtils.startsWith(propertyName, BUNDLE_LIBRARY_DIRECTORY_PREFIX)
          || BUNDLE_LIBRARY_DIRECTORY.equals(propertyName)) {
        // attempt to resolve the path specified
        String bundleLib = getProperty(propertyName);
        if (!StringUtils.isBlank(bundleLib)) {
          bundleLibraryPaths.add(getUri(bundleLib));
        }
      }
    }

    if (bundleLibraryPaths.isEmpty()) {
      bundleLibraryPaths.add(getUri(DEFAULT_BUNDLE_LIBRARY_DIR));
    }

    return bundleLibraryPaths;
  }

  /**
   * Returns a Map of Bundle Extension Types
   * The bundle extension types are defined as properties prefixed with
   * BUNDLE_EXTENSION_TYPE_PREFIX, whose values are {@link Class} names.
   * The map ties the type to the class or interface for that type.
   * @return Map of type names to class or interface names
   */
  public Map<String, String> getBundleExtensionTypes() {
    HashMap<String, String> extensionTypeMap = new HashMap<>();

    // go through each property
    for (String propertyName : getPropertyKeys()) {
      // determine if the property is an extention type
      if (StringUtils.startsWith(propertyName, BUNDLE_EXTENSION_TYPE_PREFIX)) {
        // attempt to resolve class name
        String className = getProperty(propertyName);
        if (!StringUtils.isBlank(className)) {
          // get the extension name
          String extensionName = StringUtils.substringAfterLast(propertyName, ".");
          if (!StringUtils.isBlank(extensionName)) {
            extensionTypeMap.put(extensionName, className);
          }
        }
      }
    }
    return extensionTypeMap;
  }

  /**
   * Creates a URI from a path String.
   * @param path The path as a String.  This path may be a URI as a String, a path or relative path
   * @return URI
   * @throws URISyntaxException if there is a problem building the URI
   */
  public static URI getUri(String path) throws URISyntaxException {
    // we may have URI's or paths or relative paths
    //
    // if it is not a URI string then use Paths.get().getUri()
    if (path.matches("^[A-Za-z].*//.*$")) {
      return new URI(path);
    }
    return Paths.get(path).toUri();
  }

  public String getMetaIdPrefix() {
    return getProperty(META_ID_PREFIX, DEFAULT_META_ID_PREFIX);
  }

  public String getArchiveExtension() {
    return getProperty(ARCHIVE_EXTENSION, DEFAULT_ARCHIVE_EXTENSION);
  }

  /**
   * Creates an instance of BundleProperties from an {@link InputStream}.
   * @param inStream the InputStream
   * @param additionalProperties Additional properties to be added to the returned BundleProperties.
   *     These properties can add new properties, or overload existing properties.
   * @return BundleProperties
   */
  public static BundleProperties createBasicBundleProperties(final InputStream inStream,
      final Map<String, String> additionalProperties) {
    final Map<String, String> addProps =
        (additionalProperties == null) ? new HashMap<>() : additionalProperties;
    final Properties properties = new Properties();
    try {
      properties.load(inStream);
    } catch (final Exception ex) {
      throw new RuntimeException("Cannot load properties file due to "
          + ex.getLocalizedMessage(), ex);
    } finally {
      if (null != inStream) {
        try {
          inStream.close();
        } catch (final Exception ex) {
          /**
           * do nothing *
           */
        }
      }
    }
    addProps.entrySet().stream().forEach((entry) -> {
      properties.setProperty(entry.getKey(), entry.getValue());
    });
    return new BundleProperties() {
      @Override
      public String getProperty(String key) {
        return properties.getProperty(key);
      }

      @Override
      public Set<String> getPropertyKeys() {
        return properties.stringPropertyNames();
      }

      @Override
      public void setProperty(String key, String value) {
        properties.setProperty(key, value);
      }

      @Override
      public void unSetProperty(String key) {
        properties.remove(key);
      }

      @Override
      public boolean match(BundleProperties other) {
        return properties.equals(other);
      }

      @Override
      public void storeProperties(OutputStream outputStream, String comments) throws IOException {
        properties.store(outputStream, comments);
      }
    };

  }

  /**
   * Creates an instance of BundleProperties from a String path
   * @param propertiesFilePath String path
   * @param additionalProperties Additional properties to be added to the returned BundleProperties.
   *     These properties can add new properties, or overload existing properties.
   * @return BundleProperties
   */
  public static BundleProperties createBasicBundleProperties(final String propertiesFilePath,
      final Map<String, String> additionalProperties) {
    final String bundlePropertiesFilePath = (propertiesFilePath == null)
        ? System.getProperty(BundleProperties.PROPERTIES_FILE_PATH)
        : propertiesFilePath;
    if (bundlePropertiesFilePath != null) {
      final File propertiesFile = new File(bundlePropertiesFilePath.trim());
      if (!propertiesFile.exists()) {
        throw new RuntimeException("Properties file doesn't exist \'"
            + propertiesFile.getAbsolutePath() + "\'");
      }
      if (!propertiesFile.canRead()) {
        throw new RuntimeException("Properties file exists but cannot be read \'"
            + propertiesFile.getAbsolutePath() + "\'");
      }
      InputStream inStream = null;
      try {
        inStream = new BufferedInputStream(new FileInputStream(propertiesFile));
        return createBasicBundleProperties(inStream, additionalProperties);
      } catch (final Exception ex) {
        throw new RuntimeException("Cannot load properties file due to "
            + ex.getLocalizedMessage(), ex);
      } finally {
        if (null != inStream) {
          try {
            inStream.close();
          } catch (final Exception ex) {
            /**
             * do nothing *
             */
          }
        }
      }
    }
    return null;
  }
}
