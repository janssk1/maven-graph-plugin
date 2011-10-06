package net.sf.maven.plugin.graph.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:51 PM
 */
public class ArtifactDependency {
    private ArtifactIdentifier dependent;
    private ArtifactIdentifier dependency;
    private String scope;
    private boolean optional;
    private boolean excluded;
    private List<ArtifactIdentifier> exclusions;
    private String classifier;

    public ArtifactDependency(ArtifactIdentifier dependent, ArtifactIdentifier dependency, String scope) {
        this.dependent = dependent;
        this.dependency = dependency;
        if (scope == null || scope.equals("")) {
            this.scope = org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
        } else {
            this.scope = scope;
        }
        optional = false;
        excluded = false;
        exclusions = new ArrayList<ArtifactIdentifier>();
    }

    public ArtifactIdentifier getDependent() {
        return dependent;
    }

    public ArtifactIdentifier getDependency() {
        return dependency;
    }

    public String getScope() {
        return scope;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public List<ArtifactIdentifier> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<ArtifactIdentifier> exclusions) {
        this.exclusions = exclusions;
    }


    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getClassifier() {
        return classifier;
    }
}
