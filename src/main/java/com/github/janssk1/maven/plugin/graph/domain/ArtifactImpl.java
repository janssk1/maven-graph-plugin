package com.sf.maven.plugin.graph.domain;

import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:53 PM
 */
public class ArtifactImpl implements Artifact {
    private MavenProject model;
    private List<ArtifactDependency> dependencies;
    private List<ArtifactDependency> dependencyManagerDependencies = new ArrayList<ArtifactDependency>();
    private long size;

    public ArtifactImpl(MavenProject model) {
        this.model = model;
    }

    public List<ArtifactDependency> getDependencies() {
        return dependencies;
    }

    public List<ArtifactDependency> getDependencyManagerDependencies() {
        return dependencyManagerDependencies;
    }

    public void setDependencies(List<ArtifactDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

}
