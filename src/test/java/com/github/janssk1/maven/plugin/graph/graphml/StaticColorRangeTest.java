package com.sf.maven.plugin.graph.graphml;

import junit.framework.TestCase;

import java.awt.*;

/**
 * User: janssk1
 * Date: 10/5/11
 * Time: 2:37 PM
 */
public class StaticColorRangeTest extends TestCase {
    private StaticColorRange rangeInterval = new StaticColorRange().addColor(Color.GREEN, 30).addColor(Color.YELLOW, 50).addColor(Color.RED, 100);

    public void testValueHigherThanMaxReturnsMax() {
        assertEquals(Color.RED, rangeInterval.getColor(120));
    }

    public void testValueLowerThanMinReturnsMin() {
        assertEquals(Color.GREEN, rangeInterval.getColor(10));
    }

    public void testMinReturnsMin() {
        assertEquals(Color.GREEN, rangeInterval.getColor(30));
    }

    public void testSomeMoreValues() {
        assertEquals(Color.YELLOW, rangeInterval.getColor(31));
        assertEquals(Color.YELLOW, rangeInterval.getColor(50));
        assertEquals(Color.RED, rangeInterval.getColor(51));
        assertEquals(Color.RED, rangeInterval.getColor(156));
    }

}
