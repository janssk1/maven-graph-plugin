package com.github.janssk1.maven.plugin.graph.graphml;

import com.github.janssk1.maven.plugin.graph.RenderOptions;
import com.github.janssk1.maven.plugin.graph.graph.Vertex;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: janssk1
 * Date: 12/2/11
 * Time: 1:36 PM
 */
public class SizeVertexRenderer implements RenderOptions.VertexRenderer {

    private static final int K = 1000;

/*        StaticColorRange jarSizeColorRange = new StaticColorRange()
                .addColor(Color.BLUE, 50 * K)
                .addColor(Color.GREEN, 100 * K)
                .addColor(Color.YELLOW, 500 * K)
                .addColor(Color.ORANGE, 3000 * K)
                .addColor(Color.RED, 50000 * K);


                  private Color[] getGreenToRedRange() {
        Color[] colors = new Color[] {new Color(255, 0, 0), new Color(255, 51, 0), new Color(255, 102, 0), new Color(255, 153, 0), new Color(255, 204, 0), new Color(255, 255, 0), new Color(204, 255, 0), new Color(153, 255, 0), new Color(102, 255, 0), new Color(51, 255, 0), new Color(0, 255, 0)};
        LinkedList<Color> list = new LinkedList<Color>(Arrays.asList(colors));
        Collections.reverse(list);
        colors = list.toArray(colors);
        return colors;
    }
  */


    private static ColorRange createColorRange() {
        Color[] colors = new Color[240];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(255, colors.length - i, colors.length - i);
        }
        return new FixedIntervalColorRange(0, 2000 * K, colors);

    }

    public SizeVertexRenderer() {
        this(createColorRange());
    }

    public SizeVertexRenderer(ColorRange colorRange) {
        this.colorRange = colorRange;
    }

    private final ColorRange colorRange;

    public RenderOptions.VertexInfo renderVertex(Vertex vertex) {
        long jarSize = vertex.getArtifact() != null ? vertex.getArtifact().getSize() : 0;
        Color color = colorRange.getColor(jarSize);
        return new RenderOptions.VertexInfo(vertex.getArtifactIdentifier().toString() + " (" + jarSize + ")", color);
    }
}
