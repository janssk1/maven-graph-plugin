package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.Artifact;
import net.sf.maven.plugin.graph.domain.ArtifactIdentifier;
import net.sf.maven.plugin.graph.domain.ArtifactImpl;
import net.sf.maven.plugin.graph.domain.MockArtifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Relocation;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: janssk1
 * Date: 8/14/11
 * Time: 12:19 AM
 */
public class MavenArtifactResolver implements ArtifactResolver{
    private final Log logger;
    private final ArtifactRepository localRepository;
    private final ArtifactFactory artifactFactory;
    private final MavenProjectBuilder mavenProjectBuilder;

    private static final String POM_TYPE = "pom";

    public MavenArtifactResolver(Log logger, ArtifactRepository localRepository, ArtifactFactory artifactFactory, MavenProjectBuilder mavenProjectBuilder) {
        this.logger = logger;
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
        this.mavenProjectBuilder = mavenProjectBuilder;
    }

    private MavenProject getMavenProject(ArtifactIdentifier artifactIdentifier) throws ProjectBuildingException {
        logger.debug("Fetching artifact " + artifactIdentifier);

        org.apache.maven.artifact.Artifact artifact = artifactFactory.createArtifact(artifactIdentifier.getGroupId(), artifactIdentifier.getArtifactId(), artifactIdentifier.getVersion(), org.apache.maven.artifact.Artifact.SCOPE_COMPILE, POM_TYPE);

        return mavenProjectBuilder.buildFromRepository(
                artifact, Collections.emptyList(), localRepository, false);
    }

    public Artifact resolveArtifact(ArtifactIdentifier identifier) {
        try {
            MavenProject mavenProject;
            mavenProject = getMavenProject(identifier);

            return createArtifact(identifier, mavenProject);
        } catch (ProjectBuildingException e) {
            logger.warn(e);
            return new MockArtifact();
        }
    }

    private void configureArtifact(ArtifactIdentifier id, ArtifactImpl artifact, MavenProject mavenProject) {
        artifact.getDependencyManagerDependencies().clear();
        if (mavenProject.getDependencyManagement() != null) {
            List<Dependency> dependencies = mavenProject.getDependencyManagement().getDependencies();
            for (Dependency dependency : dependencies) {
                artifact.getDependencyManagerDependencies().add(MavenHelper.createArtifactDependency(id, Collections.<ArtifactIdentifier>emptyList(), dependency));
            }
        }
    }

    private Artifact createArtifact(ArtifactIdentifier id, MavenProject mavenProject) {
        ArtifactImpl artifact = new ArtifactImpl(mavenProject);
        File path = getArtifactFile(id, mavenProject);
        long fileLength = path.length();
        artifact.setSize(fileLength);
        artifact.setDependencies(MavenHelper.resolveDependencies(id, mavenProject, new ArrayList<ArtifactIdentifier>()));
        configureArtifact(id, artifact, mavenProject);
        return artifact;
    }

    private File getArtifactFile(ArtifactIdentifier id, MavenProject mavenProject) {
        id = applyRelocation(id, mavenProject);
        org.apache.maven.artifact.Artifact mainArtifact = mavenProject.getArtifact();

        String type = mainArtifact.getType();
        if (type.equals("bundle")) {
            type = "jar";//some artifacts have type 'bundle', but they are just jars..
        }
        //add classifier..
        mainArtifact = new DefaultArtifact(id.getGroupId(), id.getArtifactId(), VersionRange.createFromVersion(id.getVersion()), mainArtifact.getScope(), type, id.getClassifier(), mainArtifact.getArtifactHandler());

        String relativePath = localRepository.pathOf(mainArtifact);
        return new File(localRepository.getBasedir(), relativePath);
    }

    private ArtifactIdentifier applyRelocation(ArtifactIdentifier id, MavenProject mavenProject) {
        if (mavenProject.getDistributionManagement() != null) {
            Relocation relocation = mavenProject.getDistributionManagement().getRelocation();
            if (relocation != null) {
                String groupId = relocation.getGroupId() != null ? relocation.getGroupId() : id.getGroupId();
                String artifactId = relocation.getArtifactId() != null ? relocation.getArtifactId() : id.getArtifactId();
                String version = relocation.getVersion() != null ? relocation.getVersion() : id.getVersion();
                return new ArtifactIdentifier(artifactId, groupId, version, id.getClassifier());
            }
        }
        return id;
    }
}
