package com.github.janssk1.maven.plugin.graph.domain;

import org.apache.maven.model.Dependency;

/**
 * User: janssk1
 * Date: 8/6/11
 * Time: 9:20 PM
 */
public class ArtifactRevisionIdentifier {
    private final ArtifactIdentifier artifactIdentifier;
    private final String version;


     public ArtifactRevisionIdentifier(ArtifactIdentifier artifactIdentifier, String version) {
        this.artifactIdentifier = artifactIdentifier;
         this.version = version;
    }

    public ArtifactRevisionIdentifier(String artifactId, String groupId, String version) {
        this(artifactId, groupId, version, null);
    }

    public ArtifactRevisionIdentifier(String artifactId, String groupId, String version, String classifier) {
        this(new ArtifactIdentifier(artifactId, groupId, classifier), version);
    }

    public ArtifactRevisionIdentifier(Dependency dep) {
        this(dep.getArtifactId(), dep.getGroupId(), dep.getVersion(), dep.getClassifier());
    }

    public ArtifactIdentifier getArtifactIdentifier() {
        return artifactIdentifier;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtifactRevisionIdentifier that = (ArtifactRevisionIdentifier) o;

        return artifactIdentifier.equals(that.artifactIdentifier) && version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = artifactIdentifier.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return artifactIdentifier.toString() + ":" + version;
    }
    
    public String getIdentifierWithoutVersion() {
      return artifactIdentifier.toString();
    }

    public String getGroupId() {
        return artifactIdentifier.getGroupId();
    }


    public String getArtifactId() {
        return artifactIdentifier.getArtifactId();
    }

    public String getClassifier() {
        return artifactIdentifier.getClassifier();
    }
}
