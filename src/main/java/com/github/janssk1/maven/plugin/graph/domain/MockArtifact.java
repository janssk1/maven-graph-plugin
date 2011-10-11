package com.sf.maven.plugin.graph.domain;

import java.util.Collections;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 12:36 AM
 */
public class MockArtifact implements Artifact {

    public List<ArtifactDependency> getDependencyManagerDependencies() {
        return Collections.emptyList();
    }

    public List<ArtifactDependency> getDependencies() {
        return Collections.emptyList();
    }

    public long getSize() {
        return 0;
    }

}
