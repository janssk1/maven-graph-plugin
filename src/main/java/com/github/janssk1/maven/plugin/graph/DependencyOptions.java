package com.github.janssk1.maven.plugin.graph;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: janssk1
 * Date: 12/2/11
 * Time: 1:23 PM
 */
public class DependencyOptions {

    private static Pattern REPORTS_PATTERN = Pattern.compile("([A-Z]+)(-TRANSITIVE)?");

    public static enum GraphType {
        PACKAGE("test", "runtime", "provided"), COMPILE("test", "runtime"), TEST, RUNTIME("test");

        private Set<String> excludedScopes = new HashSet<String>();

        GraphType(String... excludedScopes) {
           this.excludedScopes.addAll(Arrays.asList(excludedScopes));
        }

        public boolean isExcluded(String scope) {
            return excludedScopes.contains(scope);
        }
    }

    public static List<DependencyOptions> parseReportDefinitions(String reportsString) {
        List<DependencyOptions> reportDefinitions = new LinkedList<DependencyOptions>();
        try {
            Matcher matcher = REPORTS_PATTERN.matcher(reportsString);
            while (matcher.find()) {
                String scope = matcher.group(1);
                GraphType type = GraphType.valueOf(scope);
                boolean transitive = matcher.group(2) != null;
                DependencyOptions option = new DependencyOptions(type, transitive);
                reportDefinitions.add(option);
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse report definitions: " + reportsString, e);
        }
        return reportDefinitions;
        /*
        String[] report = reportDefinitions.split(",");
        for (String s : report) {
            String[] split = s.split(":");
            String graphType = split[0];
            graphType.
        } */
    }

    private final GraphType graphType;
    private final boolean includeAllTransitiveDependencies;

    public DependencyOptions(GraphType type, boolean includeAllTransitiveDependencies) {
        this.graphType = type;
        this.includeAllTransitiveDependencies = includeAllTransitiveDependencies;
    }

    public GraphType getGraphType() {
        return graphType;
    }

    public boolean isIncludeAllTransitiveDependencies() {
        return includeAllTransitiveDependencies;
    }

}
