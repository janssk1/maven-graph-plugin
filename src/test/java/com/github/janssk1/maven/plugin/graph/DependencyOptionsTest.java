package com.github.janssk1.maven.plugin.graph;

import junit.framework.TestCase;

import java.util.List;

import static com.github.janssk1.maven.plugin.graph.DependencyOptions.GraphType.*;

/**
 * User: janssk1
 * Date: 4/8/12
 * Time: 10:28 PM
 */
public class DependencyOptionsTest extends TestCase {

    public void testValidParse() {
        expectDefinitions("COMPILE,TEST,TEST-TRANSITIVE,RUNTIME"
                , new DependencyOptions(COMPILE, false), new DependencyOptions(TEST, false), new DependencyOptions(TEST, true), new DependencyOptions(RUNTIME, false)
        );

    }

    public void testInvalidFormat() {
        try {
            DependencyOptions.parseReportDefinitions("J,TEST,TEST-TRANSITIVE, RUNTIME");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    private void expectDefinitions(String reportDefinitions, DependencyOptions... expectedReports) {
        List<DependencyOptions> defs = DependencyOptions.parseReportDefinitions(reportDefinitions);
        assertEquals(expectedReports.length, defs.size());
        for (int i = 0; i < expectedReports.length;i++) {
            assertEquals(expectedReports[i].getGraphType(), defs.get(i).getGraphType());
            assertEquals(expectedReports[i].isIncludeAllTransitiveDependencies(), defs.get(i).isIncludeAllTransitiveDependencies());
        }
    }
}
