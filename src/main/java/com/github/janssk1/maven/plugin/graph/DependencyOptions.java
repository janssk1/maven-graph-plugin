package com.github.janssk1.maven.plugin.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: janssk1
 * Date: 12/2/11
 * Time: 1:23 PM
 */
public class DependencyOptions {

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

    private GraphType graphType = GraphType.COMPILE;
    private boolean includeAllTransitiveDependencies = true;


    public GraphType getGraphType() {
        return graphType;
    }

    public DependencyOptions setGraphType(GraphType graphType) {
        this.graphType = graphType;
        return this;
    }

    public boolean isIncludeAllTransitiveDependencies() {
        return includeAllTransitiveDependencies;
    }

    public DependencyOptions setIncludeAllTransitiveDependencies(boolean includeAllTransitiveDependencies) {
        this.includeAllTransitiveDependencies = includeAllTransitiveDependencies;
        return this;
    }
}
