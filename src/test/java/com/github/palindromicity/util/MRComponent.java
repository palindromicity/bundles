package com.github.palindromicity.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import java.io.IOException;

public class MRComponent {
  private Configuration configuration;
  MiniDFSCluster cluster;
  private Path basePath;

  public MRComponent withBasePath(String path) {
    basePath = new Path(path);
    return this;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public Path getBasePath() {
    return basePath;
  }

  public void start()  {
    configuration = new Configuration();
    System.clearProperty(MiniDFSCluster.PROP_TEST_BUILD_DATA);
    configuration.set(YarnConfiguration.YARN_MINICLUSTER_FIXED_PORTS, "true");
    if(basePath == null) {
      throw new RuntimeException("Unable to start cluster: You must specify the basepath");
    }
    configuration.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, basePath.toString());
    try {
      cluster = new MiniDFSCluster.Builder(configuration)
          .build();
    } catch (IOException e) {
      throw new RuntimeException("Unable to start cluster", e);
    }
  }

  public void stop() {
    cluster.shutdown();
  }
}

