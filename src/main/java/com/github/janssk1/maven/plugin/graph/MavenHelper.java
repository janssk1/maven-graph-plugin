package com.github.janssk1.maven.plugin.graph;

import com.github.janssk1.maven.plugin.graph.domain.ArtifactDependency;
import com.github.janssk1.maven.plugin.graph.domain.ArtifactIdentifier;
import com.github.janssk1.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 12:32 AM
 */
class MavenHelper {

    public static List<ArtifactDependency> resolveDependencies(MavenProject model) {
        ArrayList<ArtifactDependency> dependencies;
        dependencies = new ArrayList<ArtifactDependency>(model.getDependencies().size());
        for (Dependency d : (List<Dependency>) model.getDependencies()) {
            dependencies.add(createArtifactDependency(d));
        }
        return dependencies;
    }

    public static ArtifactDependency createArtifactDependency(Dependency d) {
        ArtifactDependency dependency = new ArtifactDependency(
                new ArtifactRevisionIdentifier(d),
                d.getScope()
        );
        dependency.setClassifier(d.getClassifier());
        dependency.setOptional(d.isOptional());
        for (Exclusion ex : d.getExclusions()) {
            ArtifactIdentifier id = new ArtifactIdentifier(ex.getArtifactId(), ex.getGroupId());
            dependency.getExclusions().add(id);
        }
        return dependency;
    }

}
