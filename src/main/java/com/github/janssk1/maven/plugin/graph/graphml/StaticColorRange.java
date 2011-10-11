package com.sf.maven.plugin.graph.graphml;

import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * User: janssk1
 * Date: 10/5/11
 * Time: 2:07 PM
 */
public class StaticColorRange implements ColorRange {

    private static class ColorPair implements Comparable<ColorPair> {
        private final Color color;
        private final int max;

        private ColorPair(Color color, int max) {
            this.color = color;
            this.max = max;
        }

        public int compareTo(ColorPair o) {
            return new Integer(max).compareTo(o.max);
        }
    }

    private SortedSet<ColorPair> colorPairs =new TreeSet<ColorPair>();

    public StaticColorRange addColor(Color c, int max) {
        colorPairs.add(new ColorPair(c, max));
        return this;
    }

    public Color getColor(long value) {
        for (ColorPair colorPair : colorPairs) {
            if (value <= colorPair.max) {
                return colorPair.color;
            }
        }
        return colorPairs.last().color;
    }
}
