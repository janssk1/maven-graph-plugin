package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import net.sf.maven.plugin.graph.graph.Graph;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:30 PM
 */
public interface GraphBuilder {

    Graph buildGraph(ArtifactRevisionIdentifier artifact);
}