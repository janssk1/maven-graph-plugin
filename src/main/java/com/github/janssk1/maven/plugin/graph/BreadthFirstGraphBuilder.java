package com.sf.maven.plugin.graph;

import com.sf.maven.plugin.graph.domain.Artifact;
import com.sf.maven.plugin.graph.domain.ArtifactDependency;
import com.sf.maven.plugin.graph.domain.ArtifactIdentifier;
import com.sf.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import com.sf.maven.plugin.graph.graph.Graph;
import com.sf.maven.plugin.graph.graph.Vertex;
import org.apache.maven.plugin.logging.Log;

import java.text.MessageFormat;
import java.util.*;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:30 PM
 */
public class BreadthFirstGraphBuilder implements GraphBuilder {

    private static final DependencyAttributeRetriever SCOPE = new DependencyAttributeRetriever() {
        public String getAttributeValue(ArtifactDependency dep) {
            return dep.getScope();
        }
    };

    private static final DependencyAttributeRetriever VERSION = new DependencyAttributeRetriever() {
        public String getAttributeValue(ArtifactDependency dep) {
            return dep.getId().getVersion();
        }
    };
    private final ArtifactResolver artifactResolver;

    private final Log logger;

    public BreadthFirstGraphBuilder(Log logger, ArtifactResolver artifactResolver) {
        this.logger = logger;
        this.artifactResolver = artifactResolver;
    }


    private class ArtifactToResolve {
        private final Vertex vertex;
        private final int depth;

        private final ArtifactDependency incoming;
        private final ArtifactToResolve parent;
        private final NearestDependencySet nearestDependencySet;
        private final Map<ArtifactIdentifier, ArtifactDependency> dependencyMgnt = new HashMap<ArtifactIdentifier, ArtifactDependency>();


        private ArtifactToResolve(Vertex vertex, ArtifactToResolve parent, int depth, ArtifactDependency incoming, NearestDependencySet nearestDependencySet) {
            this.vertex = vertex;
            this.parent = parent;
            this.depth = depth;
            this.incoming = incoming;
            this.nearestDependencySet = nearestDependencySet;
        }

        public void print() {
            StringBuffer offset = new StringBuffer();
            for (int i = 0; i < depth; i++) {
                offset.append("\t");
            }
            logger.debug(
                    MessageFormat.format(offset + "Visiting {0}, current depth = {1}", vertex.getArtifactIdentifier(), depth));
        }

        private String getScope(ArtifactDependency dep, String scope) {
              if (scope.equals("compile")) {
                //check if it's overwritten by dependencyMgnt of incoming deps. Overwriting is only done for provided scope..
                  ArtifactToResolve parent= this.parent;
                  while (parent != null) {
                      ArtifactDependency overriden = parent.dependencyMgnt.get(dep.getId().getArtifactIdentifier());
                      if (overriden != null && "provided".equals(overriden.getScope())) {
                          return "provided";
                      }
                      parent = parent.parent;
                  }
              }
            return scope;
        }
        public ArtifactToResolve createDependency(ArtifactDependency dependency) {
            String scope = getScope(dependency, dependency.getScope());//dependency, SCOPE);

            if (!isOptionalOrExcluded(dependency, scope)) {
                nearestDependencySet.add(dependency.getId().getArtifactIdentifier(), getOverriddenDependencyValue(dependency, VERSION));
                ArtifactDependency nearestDependency = nearestDependencySet.getNearest(dependency, scope);
                if (isDependencyShown(scope)) {
                    Vertex depVertex = vertex.addDependency(nearestDependency.getId(), dependency);
                    return new ArtifactToResolve(depVertex, this, depth + 1, dependency, nearestDependencySet);
                }
            }
            return null;
        }

        private boolean isOptionalOrExcluded(ArtifactDependency artifactDependency, String scope) {
            return scope.equals("test") || scope.equals("system") || artifactDependency.isOptional() || isExcluded(artifactDependency.getId().getArtifactIdentifier());
        }

        private boolean isExcluded(ArtifactIdentifier artifactIdentifier) {
            return incoming != null && (incoming.getExclusions().contains(artifactIdentifier) || parent.isExcluded(artifactIdentifier));
        }

        private String getOverriddenDependencyValue(ArtifactDependency dep, DependencyAttributeRetriever attribute) {
            String value = parent != null ? parent.getOverriddenDependencyValue(dep.getId().getArtifactIdentifier(), attribute) : null;
            return value != null ? value : attribute.getAttributeValue(dep);
        }

        private String getOverriddenDependencyValue(ArtifactIdentifier artifactId, DependencyAttributeRetriever attribute) {
            if (parent != null) {
                String scope = parent.getOverriddenDependencyValue(artifactId, attribute);
                if (scope != null) {
                    return scope;
                }
            }
            ArtifactDependency override = dependencyMgnt.get(artifactId);
            return override != null ? attribute.getAttributeValue(override) : null;
        }

    }

    private static interface DependencyAttributeRetriever {
        String getAttributeValue(ArtifactDependency dep);
    }


    private void getAllDependencies(Vertex vertex) {
        Queue<ArtifactToResolve> artifactQueue = new LinkedList<ArtifactToResolve>();
        artifactQueue.add(new ArtifactToResolve(vertex, null, 0, null, new NearestDependencySet()));
        while (!artifactQueue.isEmpty()) {
            ArtifactToResolve artifactToResolve = artifactQueue.poll();
            artifactToResolve.print();
            Vertex v = artifactToResolve.vertex;
            if (v.getArtifact() == null) {
                //if (v.getArtifactIdentifier().g)
                Artifact dependent = artifactResolver.resolveArtifact(v.getArtifactIdentifier());
                v.setArtifact(dependent);
                for (ArtifactDependency artifactDependency : dependent.getDependencyManagerDependencies()) {
                    artifactToResolve.dependencyMgnt.put(artifactDependency.getId().getArtifactIdentifier(), artifactDependency);
                }
                final List<ArtifactDependency> artifactDependencyList = dependent.getDependencies();
                for (ArtifactDependency dependency : artifactDependencyList) {
                    ArtifactToResolve toResolve = artifactToResolve.createDependency(dependency);
                    if (toResolve != null) {
                        artifactQueue.add(toResolve);
                    }
                }
            }
        }
    }

    public Graph buildGraph(ArtifactRevisionIdentifier artifact) {
        //getAllDependencies(artifact);
        Graph graph = new Graph(artifact);
        getAllDependencies(graph.getRoot());
        return graph;
    }


    private class NearestDependencySet {

        private final Map<ArtifactIdentifier, String> idToVersionMap = new HashMap<ArtifactIdentifier, String>();

        public ArtifactDependency getNearest(ArtifactDependency dependency, String scope) {
            ArtifactRevisionIdentifier id = dependency.getId();
            ArtifactIdentifier artifactId = id.getArtifactIdentifier();
            String nearestVersion = idToVersionMap.get(artifactId);
            ArtifactDependency newDep = new ArtifactDependency(new ArtifactRevisionIdentifier(id.getArtifactId(), id.getGroupId(), nearestVersion, id.getClassifier()), scope);
            newDep.setOptional(dependency.isOptional());
            return newDep;
        }

        public void add(ArtifactIdentifier identifier, String version) {
            if (!idToVersionMap.containsKey(identifier)) {
                idToVersionMap.put(identifier, version);
            }
        }
    }

    private static boolean isDependencyShown(String scope) {
        return !scope.equals("provided");
    }


}