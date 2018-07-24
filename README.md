<!--
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
 -->
 
[![Build Status](https://travis-ci.org/palindromicity/bundles.svg?branch=master)](https://travis-ci.org/palindromicity/bundles)

# Bundles

Bundles are a derivative of the [Apache Nifi](https://nifi.apache.org) [NARs](https://nifi.apache.org/developer-guide.html).
This is an attempt to adapt the NAR system to be usable outside of [Apache Nifi](https://nifi.apache.org) in other scenarios.

## bundles-maven-plugin
The [bundles-maven-plugin](https://github.com/palindromicity/bundles-maven) is an adapted version of the jar dependency plugin whose function is to bundle a jar of jars based on the dependencies for a project.  It also creates metadata attributes.
A project's jar, and it's non-provided dependency jars are place in a /lib entry in the bundle, with the bundle itself being in jar format.

## bundles-lib 
The bundles-lib contains the functionality required to:
- discover bundles
- inspect bundles for exposed extension types
- load the bundles
- create special class loaders for bundles
- deliver instances of extension types for use

NAR exposed the bundles through many classes.  I have created the BundleSystem interface to expose a more usable, simplified api for our use cases.

### What is different 
- Adaptation of nifi-nar-utils to be used outside of the [Apache Nifi](https://nifi.apache.org) project
- Rudimentary extensibility to allow configuration and injection of service types and other things that were hard coded to nifi
- Refactored from File based to [Apache Commons VFS](https://commons.apache.org/proper/commons-vfs/) based, allowing for bundle libraries to be loaded from HDFS
- Rebranding to Bundle from Nar ( although the lib and the plugin allow that to be configured now )
- Added capability to the properties class to write to stream, adapted to uri from paths
- Added integration tests for hdfs
- Changed to be ClassIndex based instead of ServiceLoader. Service loader is slower, and Casey's ClassIndex work is great. This also removes the NAR's required manual maintenance of the service file.
- Refactored to use VFS to load the bundle/nar into the classloader AND to use VFS to load the dependency jars -> VFS as a composite filesystem. Thus going from NAR's 'working directory', exploded NARS to just loading the bundle/nar.
- Created a simple interface for interacting with the system
- Refactored such that it is possible to add new bundles to the running system without having to restart the application


## Project info

- bundles-lib The bundles library
- bundles-testing a sample project showing how to use bundles
- test-bundles source for bundles used for bundles-lib tests


## TODO items

- [ ]checkstyle
- [ ]clean up the javadoc
- [ ]refactor the namespace so it isn't so flat