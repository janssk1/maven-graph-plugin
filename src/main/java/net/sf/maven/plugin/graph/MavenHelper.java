package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.ArtifactDependency;
import net.sf.maven.plugin.graph.domain.ArtifactIdentifier;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 12:32 AM
 */
class MavenHelper {

    public static List<ArtifactDependency> resolveDependencies(ArtifactIdentifier id, MavenProject model, List<ArtifactIdentifier> depExclusions) {
        ArrayList<ArtifactDependency> dependencies;
        dependencies = new ArrayList<ArtifactDependency>(model.getDependencies().size());
        for (Dependency d : (List<Dependency>) model.getDependencies()) {
            dependencies.add(createArtifactDependency(id, depExclusions, d));
        }
        return dependencies;
    }

    public static ArtifactDependency createArtifactDependency(ArtifactIdentifier id, List<ArtifactIdentifier> depExclusions, Dependency d) {
        ArtifactIdentifier dai;
        boolean isExcluded;
        List<ArtifactIdentifier> exclusions;
        dai = new ArtifactIdentifier(d);
        isExcluded = shouldBeExcluded(dai, depExclusions);
        exclusions = resolveExclusions(d);

        ArtifactDependency dependency = new ArtifactDependency(
                id,
                dai,
                d.getScope()
        );
        dependency.setClassifier(d.getClassifier());
        dependency.setOptional(d.isOptional());
        dependency.setExcluded(isExcluded);
        dependency.setExclusions(exclusions);
        return dependency;
    }

    private static List<ArtifactIdentifier> resolveExclusions(Dependency d) {
        List<ArtifactIdentifier> exclusions = new ArrayList<ArtifactIdentifier>(d.getExclusions().size());
        for (Exclusion ex : d.getExclusions()) {
            ArtifactIdentifier id = new ArtifactIdentifier(ex.getArtifactId(), ex.getGroupId(), "");
            exclusions.add(id);
        }
        return exclusions;
    }

    private static boolean shouldBeExcluded(ArtifactIdentifier dai, Collection<ArtifactIdentifier> depExclusions) {
        for (ArtifactIdentifier depExc : depExclusions) {
            if (depExc.getGroupId().equals(dai.getGroupId()) && depExc.getArtifactId().equals(dai.getArtifactId()))
                return true;
        }
        return false;
    }
}
