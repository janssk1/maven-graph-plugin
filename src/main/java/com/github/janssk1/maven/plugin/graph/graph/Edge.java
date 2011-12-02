package com.github.janssk1.maven.plugin.graph.graph;

import com.github.janssk1.maven.plugin.graph.domain.ArtifactDependency;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 10:00 PM
 */
public class Edge {

    public final ArtifactDependency originalDependency;
    public final String scope;

    public final Vertex from;
    public final Vertex to;

    public Edge(ArtifactDependency dependency, Vertex from, Vertex to, String scope) {
        this.originalDependency = dependency;
        this.from = from;
        this.to = to;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return to.getArtifactIdentifier().toString();
    }
}
