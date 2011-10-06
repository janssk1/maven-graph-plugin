package net.sf.maven.plugin.graph.graph;

import net.sf.maven.plugin.graph.domain.ArtifactIdentifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:59 PM
 */
public class Graph {

    private final Map<ArtifactIdentifier, Vertex> vertices = new HashMap<ArtifactIdentifier, Vertex>();

    private Vertex root;

    public Graph(ArtifactIdentifier root) {
        this.root = new Vertex(this, root);
        vertices.put(root, this.root);
    }

    public Vertex getRoot() {
        return root;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }


    public Vertex findOrCreate(ArtifactIdentifier artifactId) {
        Vertex vertex = vertices.get(artifactId);
        if (vertex == null) {
            vertex = new Vertex(this, artifactId);
            vertices.put(artifactId, vertex);
        }
        return vertex;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        root.toString(buffer, 0);
        return buffer.toString();
    }
}
