package net.sf.maven.plugin.graph.graphml;

import java.awt.*;

/**
 * User: janssk1
 * Date: 10/5/11
 * Time: 2:07 PM
 */
public class FixedIntervalColorRange implements ColorRange {

    private int min;
    private int max;

    private Color[] colors;

    public FixedIntervalColorRange(int min, int max, Color... colors) {
        this.min = min;
        this.max = max;
        this.colors = colors;
    }

    public Color getColor(long value) {
        if (value >= max) {
            value = max -1;
        }
        if (value < min) {
            value = min;
        }
        double v = value - min;
        double bucketSize = (((max - min) * 1.) / colors.length);
        v = v/bucketSize;
        return colors[((int) Math.floor(v))];
    }
}
