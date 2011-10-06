package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.Artifact;
import net.sf.maven.plugin.graph.domain.ArtifactIdentifier;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 12:19 AM
 */
public interface ArtifactResolver {
    Artifact resolveArtifact(ArtifactIdentifier identifier);
}
