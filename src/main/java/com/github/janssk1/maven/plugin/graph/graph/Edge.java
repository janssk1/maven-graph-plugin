package com.sf.maven.plugin.graph.graph;

import com.sf.maven.plugin.graph.domain.ArtifactDependency;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 10:00 PM
 */
public class Edge {

    public final ArtifactDependency dependency;

    public final Vertex from;
    public final Vertex to;

    public Edge(ArtifactDependency dependency, Vertex from, Vertex to) {
        this.dependency = dependency;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return to.getArtifactIdentifier().toString();
    }
}
