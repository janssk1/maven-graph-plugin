package com.sf.maven.plugin.graph.domain;

/**
 * User: janssk1
 * Date: 8/6/11
 * Time: 9:20 PM
 */
public class ArtifactIdentifier implements Cloneable {
    private final String artifactId;
    private final String groupId;
    private final String classifier;

    public ArtifactIdentifier(String artifactId, String groupId) {
        this(artifactId, groupId, null);
    }

    public ArtifactIdentifier(String artifactId, String groupId, String classifier) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.classifier = classifier;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getClassifier() {
        return classifier;
    }

    private String createStringRepresentation() {
        return getGroupId() + ':' + getArtifactId() + (classifier != null ? ':' + classifier : "");
    }

    public String toString() {
        return createStringRepresentation();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ArtifactIdentifier) {
            ArtifactIdentifier tmp = (ArtifactIdentifier) obj;
            return createStringRepresentation().equals(tmp.createStringRepresentation());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return createStringRepresentation().hashCode();
    }
}
