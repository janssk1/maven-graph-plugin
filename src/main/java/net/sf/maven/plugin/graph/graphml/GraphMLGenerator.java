package net.sf.maven.plugin.graph.graphml;

import net.sf.maven.plugin.graph.GraphSerializer;
import net.sf.maven.plugin.graph.graph.Edge;
import net.sf.maven.plugin.graph.graph.Graph;
import net.sf.maven.plugin.graph.graph.Vertex;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 10:01 PM
 */
public class GraphMLGenerator implements GraphSerializer {

    private class Builder {

        private final static String TMPL_BASE = "net/sf/maven/plugin/graph/graphml";
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

    private static final int K = 1000;

    public void serialize(Graph graph, Writer writer) throws IOException {
        Builder gen = new Builder(writer);

        Color[] colors = new Color[240];
        for (int i = 0;i < colors.length;i++) {
            colors[i] = new Color(255, colors.length - i, colors.length - i);
        }
        FixedIntervalColorRange greenToRed = new FixedIntervalColorRange(0, 2000 *K, colors);
/*        StaticColorRange jarSizeColorRange = new StaticColorRange()
                .addColor(Color.BLUE, 50 * K)
                .addColor(Color.GREEN, 100 * K)
                .addColor(Color.YELLOW, 500 * K)
                .addColor(Color.ORANGE, 3000 * K)
                .addColor(Color.RED, 50000 * K);
  */
        try {
            gen.beginGraph(graph.getRoot().getArtifactIdentifier().toString());

            for (Vertex vertex : graph.getVertices()) {
                long jarSize = vertex.getArtifact() != null ? vertex.getArtifact().getSize() : 0;
                Color color = greenToRed.getColor(jarSize);
                String colorCode = "#" + Integer.toHexString( color.getRGB() & 0x00ffffff);
                gen.beginLeafNode("" + vertex.getArtifactIdentifier().toString(), encode(vertex.getArtifactIdentifier().toString() + " (" + jarSize + ")"), colorCode);
                gen.endLeafNode();
            }
            for (Vertex vertex : graph.getVertices()) {
                List<Edge> edges = vertex.getEdges();
                for (Edge edge : edges) {
                    String declaredVersion = edge.dependency.getId().getVersion();
                    String actualVersion = edge.to.getArtifactIdentifier().getVersion();
                    String edgeLabel = edge.dependency.getScope();
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

    private Color[] getGreenToRedRange() {
        Color[] colors = new Color[] {new Color(255, 0, 0), new Color(255, 51, 0), new Color(255, 102, 0), new Color(255, 153, 0), new Color(255, 204, 0), new Color(255, 255, 0), new Color(204, 255, 0), new Color(153, 255, 0), new Color(102, 255, 0), new Color(51, 255, 0), new Color(0, 255, 0)};
        LinkedList<Color> list = new LinkedList<Color>(Arrays.asList(colors));
        Collections.reverse(list);
        colors = list.toArray(colors);
        return colors;
    }

}
