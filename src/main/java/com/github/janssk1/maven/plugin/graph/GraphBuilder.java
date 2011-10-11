package com.sf.maven.plugin.graph;

import com.sf.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import com.sf.maven.plugin.graph.graph.Graph;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:30 PM
 */
public interface GraphBuilder {

    Graph buildGraph(ArtifactRevisionIdentifier artifact);
}