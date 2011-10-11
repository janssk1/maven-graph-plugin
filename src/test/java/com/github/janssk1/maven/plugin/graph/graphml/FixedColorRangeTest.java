package com.github.janssk1.maven.plugin.graph.graphml;

import junit.framework.TestCase;

import java.awt.*;

/**
 * User: janssk1
 * Date: 10/5/11
 * Time: 2:11 PM
 */
public class FixedColorRangeTest extends TestCase {

    private FixedIntervalColorRange rangeInterval = new FixedIntervalColorRange(40, 70, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED);

    public void testValueHigherThanMaxReturnsMax() {
        assertEquals(Color.RED, rangeInterval.getColor(80));
    }

    public void testValueLowerThanMinReturnsMin() {
        assertEquals(Color.GREEN, rangeInterval.getColor(10));
    }

    public void testMinReturnsMin() {
        assertEquals(Color.GREEN, rangeInterval.getColor(40));
    }

    public void testSomeMoreValues() {
        assertEquals(Color.GREEN, rangeInterval.getColor(44));
        assertEquals(Color.GREEN, rangeInterval.getColor(47));
        assertEquals(Color.YELLOW, rangeInterval.getColor(48));
        assertEquals(Color.ORANGE, rangeInterval.getColor(56));
        assertEquals(Color.RED, rangeInterval.getColor(64));
        assertEquals(Color.RED, rangeInterval.getColor(70));
    }
}
