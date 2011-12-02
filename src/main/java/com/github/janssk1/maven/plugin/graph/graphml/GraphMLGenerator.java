package com.github.janssk1.maven.plugin.graph.graphml;

import com.github.janssk1.maven.plugin.graph.GraphSerializer;
import com.github.janssk1.maven.plugin.graph.RenderOptions;
import com.github.janssk1.maven.plugin.graph.graph.Edge;
import com.github.janssk1.maven.plugin.graph.graph.Graph;
import com.github.janssk1.maven.plugin.graph.graph.Vertex;

import java.io.*;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 10:01 PM
 */
public class GraphMLGenerator implements GraphSerializer {

    private class Builder {

        private final static String TMPL_BASE = "com/github.janssk1/maven/plugin/graph/graphml";
        private final Writer out;

        public Builder(Writer out) {
            this.out = out;
        }

        private String fetch(BufferedReader in) throws IOException {
            StringBuilder sb = new StringBuilder();
            String str;
            try {
                while (true) {
                    str = in.readLine();
                    if (str == null) break;
                    sb.append(str);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }

            str = sb.toString();

            return str;
        }

        private String fetchTemplate(String tmplName, String name) throws IOException {
            return fetchTemplate(tmplName, name, "#FFCC00");
        }

        private String fetchTemplate(String tmplName, String name, String color) throws IOException {
            InputStream is = getClass().getClassLoader().getResourceAsStream(TMPL_BASE + "/" + tmplName);

            String text = fetch(new BufferedReader(new InputStreamReader(is)));
            if (name != null) {
                text = text.replaceAll("@NAME@", name);
            }
            if (color != null) {
                text = text.replaceAll("@COLOR@", color);
            }

            return text;
        }

        private void beginGraph(String id) throws IOException {
            out.write(fetchTemplate("head.tmpl", null));
            out.write("<graph id='" + id + "' edgedefault='undirected'>");
        }

        private void endGraph() throws IOException {
            out.write("</graph>\n");
            out.write("</graphml>\n");
        }

        private void beginLeafNode(String id, String name, String color) throws IOException {
            out.write("<node id='" + id + "'>");
            out.write(fetchTemplate("simplenode.tmpl", name, color));
        }

        private void beginLeafNode(String id, String name) throws IOException {
            out.write("<node id='" + id + "'>");
            out.write(fetchTemplate("simplenode.tmpl", name));
        }

        private void endLeafNode() throws IOException {
            out.write("</node>\n");
        }

        private void edge(String fromId, String toId, String name) throws IOException {
            edge(null, fromId, toId, name);
        }

        private void edge(String id, String fromId, String toId, String name) throws IOException {
            String idx = "";
            if (id != null) {
                idx = "id='" + id + "' ";
            }

            out.write("<edge " + idx + "source='" + fromId + "' target='" + toId + "'>");
            out.write(fetchTemplate("edgenode.tmpl", name));
            out.write("</edge>\n");
        }

    }

    private static String encode(String text) {
        if (text == null) {
            text = "";
        }
        return text.replaceAll("[<]", "&lt;").replaceAll("[>]", "&gt;").replaceAll("[:]", "\n:");
    }

    public void serialize(Graph graph, Writer writer, RenderOptions options) throws IOException {
        Builder gen = new Builder(writer);

        try {
            gen.beginGraph(graph.getRoot().getArtifactIdentifier().toString());

            for (Vertex vertex : graph.getVertices()) {
                RenderOptions.VertexInfo vertexInfo = options.getVertexRenderer().renderVertex(vertex);
                gen.beginLeafNode("" + vertex.getArtifactIdentifier().toString(), encode(vertexInfo.label), "#" + Integer.toHexString( vertexInfo.color.getRGB() & 0x00ffffff));
                gen.endLeafNode();
            }
            for (Vertex vertex : graph.getVertices()) {
                List<Edge> edges = vertex.getEdges();
                for (Edge edge : edges) {
                    String declaredVersion = edge.originalDependency.getId().getVersion();
                    String actualVersion = edge.to.getArtifactIdentifier().getVersion();
                    String edgeLabel = edge.originalDependency.getScope();
                    if (!declaredVersion.equals(actualVersion)) {
                        edgeLabel += " (" + declaredVersion + ")";
                    }
                    gen.edge("" + edge.from.getArtifactIdentifier(), "" + edge.to.getArtifactIdentifier(), edgeLabel);
                }
            }

            gen.endGraph();
        } finally {
            writer.close();
        }
    }

}
