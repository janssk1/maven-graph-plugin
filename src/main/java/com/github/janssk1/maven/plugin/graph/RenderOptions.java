package com.github.janssk1.maven.plugin.graph;

import com.github.janssk1.maven.plugin.graph.graph.Vertex;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: janssk1
 * Date: 12/2/11
 * Time: 1:27 PM
 */
public class RenderOptions {

    public static class VertexInfo {
        public final String label;
        public final Color color;

        public VertexInfo(String label, Color color) {
            this.label = label;
            this.color = color;
        }
    }
    public static interface VertexRenderer {
          VertexInfo renderVertex(Vertex vertex);
    }


    public static final VertexRenderer DEFAULT_VERTEX_RENDERER = new VertexRenderer() {
        public VertexInfo renderVertex(Vertex vertex) {
            return new VertexInfo(vertex.getArtifactIdentifier().toString(), Color.yellow);
        }
    };

    private VertexRenderer vertexRenderer = DEFAULT_VERTEX_RENDERER;

    public RenderOptions setVertexRenderer(VertexRenderer vertexRenderer) {
        this.vertexRenderer = vertexRenderer;
        return this;
    }

    public VertexRenderer getVertexRenderer() {
        return vertexRenderer;
    }
}
