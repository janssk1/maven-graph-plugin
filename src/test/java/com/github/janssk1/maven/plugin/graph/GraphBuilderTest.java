package com.github.janssk1.maven.plugin.graph;

import com.github.janssk1.maven.plugin.graph.domain.ArtifactDependency;
import com.github.janssk1.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import com.github.janssk1.maven.plugin.graph.graph.Edge;
import com.github.janssk1.maven.plugin.graph.graph.Graph;
import com.github.janssk1.maven.plugin.graph.graph.Vertex;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;

import java.net.URL;

/**
 * User: janssk1
 * Date: 8/15/11
 * Time: 9:55 PM
 */
public class GraphBuilderTest extends PlexusTestCase {

    private GraphBuilder builder;
    private Graph expectedGraph;

    private <T> T getComponent(Class<T> t) throws Exception {
        return (T) lookup(t.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        builder = createGraphBuilder();
    }

    public void testGraphOfALeafNodeReturnsThatNode() throws Exception {
        expectGraph("a:1.0");
        checkGraph();
    }

    public void testANodeThatHasAJarFileReturnsANonNullSize() {
        Graph graph = buildGraph("a:1.0");
        assertTrue(graph.getRoot().getArtifact().getSize() > 0);
    }

    public void testANodeThatHasARelocatedJarFileReturnsANonNullSize() {
        Graph graph = buildGraph("b:1.0");
        assertTrue(graph.getRoot().getArtifact().getSize() > 0);
    }

    public void testANodeWithoutAJarFileReturnsANullSize() {
        Graph graph = buildGraph("b:1.0-withunknowna");
        assertEquals(0, graph.getRoot().getArtifact().getSize());
    }

    public void testClassifierIsTakenIntoAccountWhenDownloadingArtifacts() {
        expectGraph("b:1.0-withclassifier");
        expectEdge("b:1.0-withclassifier", "a:1.1", "compile:1.1");
        Graph graph = buildGraph("b:1.0-withclassifier");
        Vertex a = graph.getRoot().getEdges().get(0).to;
        assertTrue(a.getArtifact().getSize() > 0);
    }

    public void testProvidedScopeInDepMgntOverridesNestedDependencyScopesToProvided() {
        expectGraph("c:1.0-withprovideda");
        expectEdge("c:1.0-withprovideda", "b:1.0", "compile:1.0");
        //expectEdge("b:1.0", "a:1.0", "provided:1.0");
        checkGraph();
    }

    public void testGraphOfANodeWithOneDependency() throws Exception {
        expectGraph("b:1.0");
        expectEdge("b:1.0", "a:1.0", "compile:1.0");
        checkGraph();
    }

    public void testGraphOfANodeWithOneProvidedDependency() throws Exception {
        expectGraph("b:1.0-withprovideda");
        // expectEdge("b:1.0-withprovideda", "a:1.0", "provided:1.0");
        checkGraph();
    }

    public void testGraphOfANodeWithAnUnknownDependency() throws Exception {
        expectGraph("b:1.0-withunknowna");
        expectEdge("b:1.0-withunknowna", "a:unknown", "compile:unknown");
        checkGraph();
    }

    public void testGraphOfANodeWithTransitiveCompileDependency() throws Exception {
        expectGraph("c:1.0");
        expectEdge("c:1.0", "b:1.0", "compile:1.0");
        expectEdge("b:1.0", "a:1.0", "compile:1.0");
        checkGraph();
    }

    public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsExcluded() throws Exception {
        expectGraph("c:1.0-withexcludeda");
        expectEdge("c:1.0-withexcludeda", "b:1.0", "compile:1.0");
        checkGraph();
    }


    public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsOverriddenInDependencyMgnt() throws Exception {
        expectGraph("c:1.0-withexplicitaversion");
        expectEdge("c:1.0-withexplicitaversion", "b:1.0", "compile:1.0");
        expectEdge("b:1.0", "a:1.1", "compile:1.0");
        checkGraph();
    }

    public void testTestScopeDepDoesNotOverridesNestedDependencyScopes() {
        expectGraph("c:1.0-withtestscopeda");
        expectEdge("c:1.0-withtestscopeda", "b:1.0", "compile:1.0");
        expectEdge("b:1.0", "a:1.0", "compile:1.0");
        checkGraph();
    }

    public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsOverriddenByNearerDependency() throws Exception {
        expectGraph("c:1.0-withexplicitaversionasdep");
        expectEdge("c:1.0-withexplicitaversionasdep", "b:1.0", "compile:1.0");
        expectEdge("b:1.0", "a:1.1", "compile:1.0");
        expectEdge("c:1.0-withexplicitaversionasdep", "a:1.1", "compile:1.1");
        checkGraph();
    }

    public void testNearestDependencyInAnotherBranchIsSelectedAnyway() throws Exception {
        expectGraph("e:1.0");
        expectEdge("e:1.0", "c:1.0", "compile:1.0");
        expectEdge("c:1.0", "b:1.0", "compile:1.0");
        expectEdge("b:1.0", "a:1.1", "compile:1.0");
        expectEdge("e:1.0", "d:1.0", "compile:1.0");
        expectEdge("d:1.0", "a:1.1", "compile:1.1");
        checkGraph();
    }

    private Graph checkGraph() {
        Graph graph = builder.buildGraph(expectedGraph.getRoot().getArtifactIdentifier());
        assertEquals(expectedGraph.getRoot(), graph.getRoot());
        return graph;
        //assertEquals(expectedGraph.toString(), graph.toString());
    }

    private Graph buildGraph(String nodeId) {
        return builder.buildGraph(createArtifactId(nodeId));
    }

    private void assertEquals(Vertex expected, Vertex actual) {
        assertEquals(expected.getArtifactIdentifier(), actual.getArtifactIdentifier());
        String errorMessage = "expected: " + expected + ", got: " + actual;
        assertEquals(errorMessage, expected.getEdges().size(), actual.getEdges().size());
        for (int i = 0; i < expected.getEdges().size(); i++) {
            Edge expectedEdge = expected.getEdges().get(0);
            Edge actualEdge = actual.getEdges().get(0);
            assertEquals(actual.getArtifactIdentifier(), actualEdge.from.getArtifactIdentifier());
            assertEquals(expectedEdge.to, actualEdge.to);
            assertEquals(expectedEdge.dependency.getScope(), actualEdge.dependency.getScope());
            assertEquals(expectedEdge.dependency.getId(), actualEdge.dependency.getId());
        }
    }

    private void expectGraph(String nodeId) {
        expectedGraph = new Graph(createArtifactId(nodeId));
    }

    private void expectEdge(String fromNodeId, String toNodeId, String depInfo) {
        if (expectedGraph == null) {
            expectedGraph = new Graph(createArtifactId(fromNodeId));
        }
        ArtifactRevisionIdentifier fromArtifact = createArtifactId(fromNodeId);
        ArtifactRevisionIdentifier toArtifact = createArtifactId(toNodeId);
        String[] split = depInfo.split(":");
        String scope = split[0];
        String version = split[1];
        expectedGraph.findOrCreate(fromArtifact).addDependency(toArtifact, new ArtifactDependency(new ArtifactRevisionIdentifier(toArtifact.getArtifactId(), toArtifact.getGroupId(), version), scope));
    }

    private ArtifactRevisionIdentifier createArtifactId(String nodeId) {
        String[] split = nodeId.split(":");
        return new ArtifactRevisionIdentifier(split[0], "a", split[1]);
    }


    private GraphBuilder createGraphBuilder() throws Exception {
        org.apache.maven.artifact.factory.ArtifactFactory artifactFactory = getComponent(org.apache.maven.artifact.factory.ArtifactFactory.class);
        MavenProjectBuilder mavenProjectBuilder = getComponent(MavenProjectBuilder.class);
        URL repository = Thread.currentThread().getContextClassLoader().getResource("repository");
        ArtifactRepository localRepository = new DefaultArtifactRepository("local", repository.toString(), new DefaultRepositoryLayout());
        Log log = new SystemStreamLog();
        return new BreadthFirstGraphBuilder(log, new MavenArtifactResolver(log, localRepository, artifactFactory, mavenProjectBuilder));
    }

}
