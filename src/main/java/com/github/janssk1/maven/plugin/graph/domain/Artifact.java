package com.sf.maven.plugin.graph.domain;

import java.util.List;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:51 PM
 */
public interface Artifact {

    List<ArtifactDependency> getDependencies();

    List<ArtifactDependency> getDependencyManagerDependencies();

    long getSize();

}