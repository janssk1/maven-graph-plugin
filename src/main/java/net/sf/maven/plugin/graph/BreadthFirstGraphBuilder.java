package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.Artifact;
import net.sf.maven.plugin.graph.domain.ArtifactDependency;
import net.sf.maven.plugin.graph.domain.ArtifactIdentifier;
import net.sf.maven.plugin.graph.graph.Graph;
import net.sf.maven.plugin.graph.graph.Vertex;
import org.apache.maven.plugin.logging.Log;

import java.text.MessageFormat;
import java.util.*;

/**
 * User: janssk1
 * Date: 8/13/11
 * Time: 9:30 PM
 */
public class BreadthFirstGraphBuilder implements GraphBuilder {

    private final ArtifactResolver artifactResolver;

    private final Log logger;

    public BreadthFirstGraphBuilder(Log logger, ArtifactResolver artifactResolver) {
        this.logger = logger;
        this.artifactResolver = artifactResolver;
    }


    private class ArtifactToResolve {
        private final Vertex vertex;
        private final int depth;
        private final ExcludedDeps excludedDeps;
        private final ArtifactToResolve parent;
        private final NearestDependencySet nearestDependencySet;
        private final Map<String, String> dependencyScope = new HashMap<String, String>();


        private ArtifactToResolve(Vertex vertex, ArtifactToResolve parent, int depth, ExcludedDeps excludedDeps, NearestDependencySet nearestDependencySet) {
            this.vertex = vertex;
            this.parent = parent;
            this.depth = depth;
            this.excludedDeps = excludedDeps;
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

        public ArtifactToResolve createDependency(ArtifactDependency dependency) {
            if (!isOptionalOrExcluded(dependency, excludedDeps)) {
                String scope = getScope(dependency.getDependency().getUniqueId());


                ArtifactDependency nearestDependency = nearestDependencySet.getNearest(dependency, scope);
                if (isDependencyIncluded(nearestDependency, excludedDeps)) {
                    Vertex depVertex = vertex.addDependency(nearestDependency.getDependency(), dependency);
                    return new ArtifactToResolve(depVertex, this, depth + 1, excludedDeps.create(dependency), nearestDependencySet);
                }
            }
            return null;
        }

        private String getScope(String artifactId) {
            if (parent != null) {
                String scope = parent.getScope(artifactId);
                if (scope != null) {
                    return scope;
                }
            }
            return dependencyScope.get(artifactId);

        }

        public void add(ArtifactDependency artifactDependency) {
            if (!isOptionalOrExcluded(artifactDependency, excludedDeps)) {
                nearestDependencySet.add(artifactDependency);
            }
            dependencyScope.put(artifactDependency.getDependency().getUniqueId(), artifactDependency.getScope());
        }
    }


    private void getAllDependencies(Vertex vertex) {
        Queue<ArtifactToResolve> artifactQueue = new LinkedList<ArtifactToResolve>();
        artifactQueue.add(new ArtifactToResolve(vertex, null, 0, new ExcludedDeps(), new NearestDependencySet()));
        while (!artifactQueue.isEmpty()) {
            ArtifactToResolve artifact = artifactQueue.poll();
            artifact.print();
            Vertex v = artifact.vertex;
            if (v.getArtifact() == null) {
                Artifact dependent = artifactResolver.resolveArtifact(v.getArtifactIdentifier());
                v.setArtifact(dependent);
                final List<ArtifactDependency> artifactDependencyList = dependent.getDependencies();
                for (ArtifactDependency artifactDependency : artifactDependencyList) {
                    artifact.add(artifactDependency);
                }
                for (ArtifactDependency dependency : artifactDependencyList) {
                    ArtifactToResolve toResolve = artifact.createDependency(dependency);
                    if (toResolve != null) {
                        artifactQueue.add(toResolve);
                    }
                }
            }
        }
    }

    public Graph buildGraph(ArtifactIdentifier artifact) {
        //getAllDependencies(artifact);
        Graph graph = new Graph(artifact);
        getAllDependencies(graph.getRoot());
        return graph;
    }


    private class NearestDependencySet {

        private final Map<String, ArtifactDependency> idToVersionMap = new HashMap<String, ArtifactDependency>();

        public ArtifactDependency getNearest(ArtifactDependency id, String scope) {
            ArtifactIdentifier dependency = id.getDependency();
            String artifactId = dependency.getUniqueId();
            ArtifactDependency nearestVersion = idToVersionMap.get(artifactId);
            ArtifactDependency newDep = new ArtifactDependency(id.getDependent(), new ArtifactIdentifier(dependency.getArtifactId(), dependency.getGroupId(), nearestVersion.getDependency().getVersion(), id.getClassifier()), scope);
            newDep.setExcluded(id.isExcluded());
            newDep.setOptional(id.isOptional());
            return newDep;
        }

        public void add(ArtifactDependency dep) {
            String identifier = dep.getDependency().getUniqueId();
            if (!idToVersionMap.containsKey(identifier)) {
                idToVersionMap.put(identifier, dep);
            }
        }
    }

    private class ExcludedDeps {

        private Set<String> excluded = new HashSet<String>();
        private ExcludedDeps parent;

        public ExcludedDeps() {
        }

        private ExcludedDeps(ArtifactDependency artifactDependency) {
            List<ArtifactIdentifier> exclusions = artifactDependency.getExclusions();
            for (ArtifactIdentifier exclusion : exclusions) {
                excluded.add(exclusion.getUniqueId());
            }
        }

        public ExcludedDeps create(ArtifactDependency dep) {
            if (dep.getExclusions().size() != 0) {
                ExcludedDeps deps = new ExcludedDeps(dep);
                deps.parent = this;
                return deps;
            } else {
                return this;
            }
        }



        public boolean isExcluded(ArtifactIdentifier id) {
            return excluded.contains(id.getUniqueId()) || (parent != null && parent.isExcluded(id));
        }
    }

    private static boolean isOptionalOrExcluded(ArtifactDependency artifactDependency, ExcludedDeps excludedDeps) {
        return artifactDependency.getScope().equals("test") || artifactDependency.getScope().equals("system") || artifactDependency.isOptional() || excludedDeps.isExcluded(artifactDependency.getDependency());
    }


    private static boolean isDependencyIncluded(ArtifactDependency dependency, ExcludedDeps excludedDeps) {
        return (!isOptionalOrExcluded(dependency, excludedDeps) && !dependency.getScope().equals("test") && !dependency.getScope().equals("provided"));
    }


}