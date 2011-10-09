package net.sf.maven.plugin.graph.graph;

import net.sf.maven.plugin.graph.domain.Artifact;
import net.sf.maven.plugin.graph.domain.ArtifactDependency;
import net.sf.maven.plugin.graph.domain.ArtifactRevisionIdentifier;

import java.util.LinkedList;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 10:00 PM
 */
public class Vertex {

    private final ArtifactRevisionIdentifier artifactIdentifier;
    private final List<Edge> edges = new LinkedList<Edge>();
    private Artifact artifact;
    private final Graph graph;

    Vertex(Graph graph, ArtifactRevisionIdentifier artifactIdentifier) {
        this.artifactIdentifier = artifactIdentifier;
        this.graph = graph;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public ArtifactRevisionIdentifier getArtifactIdentifier() {
        return artifactIdentifier;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Vertex addDependency(ArtifactRevisionIdentifier nearest, ArtifactDependency original) {
        Vertex target = graph.findOrCreate(nearest);
        Edge e = new Edge(original, this, target);
        edges.add(e);
        return target;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toString(buffer, 0);
        return buffer.toString();
    }

    public void toString(StringBuffer buffer, int indent) {
        addIndent(indent, buffer);
        buffer.append(artifactIdentifier.toString()).append("\n");
        for (Edge edge : edges) {
            edge.to.toString(buffer, indent+1);
        }
    }

    private void addIndent(int indent, StringBuffer res) {
        for (int i =0;i < indent;i++) {
            res.append("\t");
        }
    }
}
