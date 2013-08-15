package com.github.janssk1.maven.plugin.graph;

import static com.github.janssk1.maven.plugin.graph.DependencyOptions.GraphType.COMPILE;
import static com.github.janssk1.maven.plugin.graph.DependencyOptions.GraphType.PACKAGE;
import static com.github.janssk1.maven.plugin.graph.DependencyOptions.GraphType.TEST;

import java.net.URL;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;

import com.github.janssk1.maven.plugin.graph.domain.ArtifactDependency;
import com.github.janssk1.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import com.github.janssk1.maven.plugin.graph.graph.Edge;
import com.github.janssk1.maven.plugin.graph.graph.Graph;
import com.github.janssk1.maven.plugin.graph.graph.Vertex;

/**
 * User: janssk1
 * Date: 8/15/11
 * Time: 9:55 PM
 */
public class GraphBuilderTest extends PlexusTestCase
{

  private GraphBuilder      builder;
  private Graph             expectedGraph;
  private DependencyOptions options = new DependencyOptions(COMPILE, true);

  private <T> T getComponent(Class<T> t) throws Exception
  {
    return (T) lookup(t.getName());
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    builder = createGraphBuilder();
  }

  public void testGraphOfALeafNodeReturnsThatNode() throws Exception
  {
    expectGraph("a:1.0");
    checkGraph();
  }

  public void testANodeThatHasAJarFileReturnsANonNullSize()
  {
    Graph graph = buildGraph("a:1.0");
    assertTrue(graph.getRoot().getArtifact().getSize() > 0);
  }

  public void testANodeThatHasARelocatedJarFileReturnsANonNullSize()
  {
    Graph graph = buildGraph("b:1.0");
    assertTrue(graph.getRoot().getArtifact().getSize() > 0);
  }

  public void testANodeWithoutAJarFileReturnsANullSize()
  {
    Graph graph = buildGraph("b:1.0-withunknowna");
    assertEquals(0, graph.getRoot().getArtifact().getSize());
  }

  public void testClassifierIsTakenIntoAccountWhenDownloadingArtifacts()
  {
    expectGraph("b:1.0-withclassifier");
    expectEdge("b:1.0-withclassifier", "a:1.1", "compile:1.1");
    Graph graph = buildGraph("b:1.0-withclassifier");
    Vertex a = graph.getRoot().getEdges().get(0).to;
    assertTrue(a.getArtifact().getSize() > 0);
  }

  public void testProvidedScopeInDepMgntOverridesNestedDependencyScopesToProvided()
  {
    expectGraph("c:1.0-withprovideda");
    expectEdge("c:1.0-withprovideda", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.1", "provided", "compile:1.0");
    checkGraph();
  }

  public void testGraphOfANodeWithOneDependency() throws Exception
  {
    expectGraph("b:1.0");
    expectEdge("b:1.0", "a:1.0", "compile:1.0");
    checkGraph();
  }

  public void testProvidedNodeCanBeHidden() throws Exception
  {
    //options.setShowProvidedScope(false);
    options = new DependencyOptions(PACKAGE, true);
    expectGraph("b:1.0-withprovideda");
    checkGraph();
  }

  public void testProvidedNodeCanBeShown() throws Exception
  {
    options = new DependencyOptions(COMPILE, true);
    expectGraph("b:1.0-withprovideda");
    expectEdge("b:1.0-withprovideda", "a:1.0", "provided:1.0");
    checkGraph();
  }

  public void testGraphOfANodeWithAnUnknownDependency() throws Exception
  {
    expectGraph("b:1.0-withunknowna");
    expectEdge("b:1.0-withunknowna", "a:unknown", "compile:unknown");
    checkGraph();
  }

  public void testGraphOfANodeWithTransitiveCompileDependency() throws Exception
  {
    expectGraph("c:1.0");
    expectEdge("c:1.0", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.0", "compile:1.0");
    checkGraph();
  }

  public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsExcluded() throws Exception
  {
    expectGraph("c:1.0-withexcludeda");
    expectEdge("c:1.0-withexcludeda", "b:1.0", "compile:1.0");
    checkGraph();
  }

  public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsOverriddenInDependencyMgnt() throws Exception
  {
    expectGraph("c:1.0-withexplicitaversion");
    expectEdge("c:1.0-withexplicitaversion", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.1", "compile:1.0");
    checkGraph();
  }

  public void testTestScopeDepDoesNotOverridesNestedDependencyScopes()
  {
    expectGraph("c:1.0-withtestscopeda");
    expectEdge("c:1.0-withtestscopeda", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.0", "compile:1.0");
    checkGraph();
  }

  public void testTestScopeIsIncludedWhenTestGraphIsRequested()
  {
    options = new DependencyOptions(TEST, false);
    expectGraph("c:1.0-withtestscopeda");
    expectEdge("c:1.0-withtestscopeda", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.1", "compile:1.0");
    expectEdge("c:1.0-withtestscopeda", "a:1.1", "test:1.1");
    checkGraph();
  }

  public void testNestedTestScopeIsIgnoredWhenCompileGraphIsRequested()
  {
    expectGraph("c:1.0-withnestedtestscopeda");
    expectEdge("c:1.0-withnestedtestscopeda", "b:1.0-withtestscopeda", "compile:1.0-withtestscopeda");
    checkGraph();
  }

  public void testTestScopeDoesIncludeTransitiveCompileScopes()
  {
    options = new DependencyOptions(TEST, false);
    expectGraph("c:1.0-withtestscopedb");
    expectEdge("c:1.0-withtestscopedb", "b:1.0", "test:1.0");
    expectEdge("b:1.0", "a:1.0", "compile:1.0");
    checkGraph();
  }

  public void testTransitiveTestScopeIsIgnored()
  {
    options = new DependencyOptions(TEST, false);
    expectGraph("c:1.0-withbthathastestscopeda");
    expectEdge("c:1.0-withbthathastestscopeda", "b:1.0-withtestscopeda", "compile:1.0-withtestscopeda");
    checkGraph();
  }

  public void testGraphOfANodeWithTransitiveCompileDependencyThatGetsOverriddenByNearerDependency() throws Exception
  {
    expectGraph("c:1.0-withexplicitaversionasdep");
    expectEdge("c:1.0-withexplicitaversionasdep", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.1", "compile:1.0");
    expectEdge("c:1.0-withexplicitaversionasdep", "a:1.1", "compile:1.1");
    checkGraph();
  }

  public void testNestedProvidedScopeIsIgnored() throws Exception
  {
    options = new DependencyOptions(COMPILE, false);
    expectGraph("d:1.1");
    expectEdge("d:1.1", "c:1.0-withprovideda", "provided:1.0-withprovideda");
    expectEdge("c:1.0-withprovideda:1.0", "b:1.0", "compile:1.0");
    //expectEdge("b:1.0", "a:1.0", "provided:1.0"); ignored
    checkGraph();
  }

  public void testNearestDependencyInAnotherBranchIsSelectedAnyway() throws Exception
  {
    expectGraph("e:1.0");
    expectEdge("e:1.0", "c:1.0", "compile:1.0");
    expectEdge("c:1.0", "b:1.0", "compile:1.0");
    expectEdge("b:1.0", "a:1.1", "compile:1.0");
    expectEdge("e:1.0", "d:1.0", "compile:1.0");
    expectEdge("d:1.0", "a:1.1", "compile:1.1");
    checkGraph();
  }

  private Graph checkGraph()
  {
    Graph graph = builder.buildGraph(expectedGraph.getRoot().getArtifactIdentifier(), options);
    assertEquals(expectedGraph.getRoot(), graph.getRoot());
    return graph;
    //assertEquals(expectedGraph.toString(), graph.toString());
  }

  private Graph buildGraph(String nodeId)
  {
    return builder.buildGraph(createArtifactId(nodeId), options);
  }

  private void assertEquals(Vertex expected, Vertex actual)
  {
    assertEquals(expected.getArtifactIdentifier(), actual.getArtifactIdentifier());
    String errorMessage = "expected: " + expected + ", got: " + actual;
    assertEquals(errorMessage, expected.getEdges().size(), actual.getEdges().size());
    for (int i = 0; i < expected.getEdges().size(); i++)
    {
      Edge expectedEdge = expected.getEdges().get(0);
      Edge actualEdge = actual.getEdges().get(0);
      assertEquals(actual.getArtifactIdentifier(), actualEdge.from.getArtifactIdentifier());
      assertEquals(expectedEdge.to, actualEdge.to);
      assertEquals(expectedEdge.scope, actualEdge.scope);
      assertEquals(expectedEdge.originalDependency.getScope(), actualEdge.originalDependency.getScope());
      assertEquals(expectedEdge.originalDependency.getId(), actualEdge.originalDependency.getId());
    }
  }

  private void expectGraph(String nodeId)
  {
    expectedGraph = new Graph(createArtifactId(nodeId));
  }

  private void expectEdge(String fromNodeId, String toNodeId, String depInfo)
  {
    expectEdge(fromNodeId, toNodeId, null, depInfo);
  }

  private void expectEdge(String fromNodeId, String toNodeId, String scope, String depInfo)
  {
    if (expectedGraph == null)
    {
      expectedGraph = new Graph(createArtifactId(fromNodeId));
    }
    ArtifactRevisionIdentifier fromArtifact = createArtifactId(fromNodeId);
    ArtifactRevisionIdentifier toArtifact = createArtifactId(toNodeId);
    String[] split = depInfo.split(":");
    String originalScope = split[0];
    String originalVersion = split[1];
    expectedGraph.findOrCreate(fromArtifact).addDependency(toArtifact,
                                                           scope == null ? originalScope : scope,
                                                           new ArtifactDependency(new ArtifactRevisionIdentifier(
                                                               toArtifact.getArtifactId(), toArtifact.getGroupId(), originalVersion),
                                                               originalScope));
  }

  private ArtifactRevisionIdentifier createArtifactId(String nodeId)
  {
    String[] split = nodeId.split(":");
    return new ArtifactRevisionIdentifier(split[0], "a", split[1]);
  }

  private GraphBuilder createGraphBuilder() throws Exception
  {
    org.apache.maven.artifact.factory.ArtifactFactory artifactFactory = getComponent(org.apache.maven.artifact.factory.ArtifactFactory.class);
    MavenProjectBuilder mavenProjectBuilder = getComponent(MavenProjectBuilder.class);
    URL repository = Thread.currentThread().getContextClassLoader().getResource("repository");
    ArtifactRepository localRepository = new DefaultArtifactRepository("local", repository.toString(), new DefaultRepositoryLayout());
    Log log = new SystemStreamLog();
    return new BreadthFirstGraphBuilder(log, new MavenArtifactResolver(log, localRepository, artifactFactory, mavenProjectBuilder));
  }

}
