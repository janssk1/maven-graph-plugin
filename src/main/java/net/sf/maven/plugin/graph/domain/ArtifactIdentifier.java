package net.sf.maven.plugin.graph.domain;

import org.apache.maven.model.Dependency;

/**
 * User: janssk1
 * Date: 8/6/11
 * Time: 9:20 PM
 */
public class ArtifactIdentifier implements Cloneable {
    private final String artifactId;
    private final String groupId;
    private final String version;
    private final String classifier;


    public ArtifactIdentifier(String artifactId, String groupId, String version) {
        this(artifactId, groupId, version, null);
    }

    public ArtifactIdentifier(String artifactId, String groupId, String version, String classifier) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
        this.classifier = classifier;
    }

    public ArtifactIdentifier(Dependency dep) {
        this(dep.getArtifactId(), dep.getGroupId(), dep.getVersion(), dep.getClassifier());
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getUniqueId() {
        return getGroupId() + "@" + getArtifactId() + (classifier != null ? "@" + classifier : "");
    }

    private String createStringRepresentation() {
        return getGroupId() + ':' + getArtifactId() + ':' + getVersion();
    }

    public String toString() {
        return createStringRepresentation();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ArtifactIdentifier) {
            ArtifactIdentifier tmp = (ArtifactIdentifier) obj;
            return (
                    groupId.equals(tmp.groupId) &&
                            artifactId.equals(tmp.artifactId) &&
                            version.equals(tmp.version)
            );
        } else {
            return false;
        }
    }

    public int hashCode() {
        return createStringRepresentation().hashCode();
    }
}
