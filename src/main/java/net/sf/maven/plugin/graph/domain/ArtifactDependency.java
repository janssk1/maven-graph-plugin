package net.sf.maven.plugin.graph.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:51 PM
 */
public class ArtifactDependency {
    private ArtifactRevisionIdentifier id;
    private String scope;
    private boolean optional;
    private Set<ArtifactIdentifier> exclusions;
    private String classifier;

    public ArtifactDependency(ArtifactRevisionIdentifier id, String scope) {
        this.id = id;
        if (scope == null || scope.equals("")) {
            this.scope = org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
        } else {
            this.scope = scope;
        }
        optional = false;
        exclusions = new HashSet<ArtifactIdentifier>();
    }

    public ArtifactRevisionIdentifier getId() {
        return id;
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

    public Set<ArtifactIdentifier> getExclusions() {
        return exclusions;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getClassifier() {
        return classifier;
    }
}
