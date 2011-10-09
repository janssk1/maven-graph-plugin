package net.sf.maven.plugin.graph;

import net.sf.maven.plugin.graph.domain.Artifact;
import net.sf.maven.plugin.graph.domain.ArtifactImpl;
import net.sf.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import net.sf.maven.plugin.graph.domain.MockArtifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Relocation;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import java.io.File;
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

    private MavenProject getMavenProject(ArtifactRevisionIdentifier artifactIdentifier) throws ProjectBuildingException {
        logger.debug("Fetching artifact " + artifactIdentifier);

        org.apache.maven.artifact.Artifact artifact = artifactFactory.createArtifact(artifactIdentifier.getGroupId(), artifactIdentifier.getArtifactId(), artifactIdentifier.getVersion(), org.apache.maven.artifact.Artifact.SCOPE_COMPILE, POM_TYPE);

        return mavenProjectBuilder.buildFromRepository(
                artifact, Collections.emptyList(), localRepository, false);
    }

    public Artifact resolveArtifact(ArtifactRevisionIdentifier identifier) {
        try {
            MavenProject mavenProject;
            mavenProject = getMavenProject(identifier);

            return createArtifact(identifier, mavenProject);
        } catch (ProjectBuildingException e) {
            logger.warn(e);
            return new MockArtifact();
        }
    }

    private void configureArtifact(ArtifactImpl artifact, MavenProject mavenProject) {
        artifact.getDependencyManagerDependencies().clear();
        if (mavenProject.getDependencyManagement() != null) {
            List<Dependency> dependencies = mavenProject.getDependencyManagement().getDependencies();
            for (Dependency dependency : dependencies) {
                artifact.getDependencyManagerDependencies().add(MavenHelper.createArtifactDependency(dependency));
            }
        }
    }

    private Artifact createArtifact(ArtifactRevisionIdentifier id, MavenProject mavenProject) {
        ArtifactImpl artifact = new ArtifactImpl(mavenProject);
        File path = getArtifactFile(id, mavenProject);
        long fileLength = path.length();
        artifact.setSize(fileLength);
        artifact.setDependencies(MavenHelper.resolveDependencies(mavenProject));
        configureArtifact(artifact, mavenProject);
        return artifact;
    }

    private File getArtifactFile(ArtifactRevisionIdentifier id, MavenProject mavenProject) {
        id = applyRelocation(id, mavenProject);
        org.apache.maven.artifact.Artifact mainArtifact = mavenProject.getArtifact();

        //add classifier..
        ArtifactHandler artifactHandler = new MyArtifactHandler(mainArtifact.getArtifactHandler());
        mainArtifact = new DefaultArtifact(id.getArtifactIdentifier().getGroupId(), id.getArtifactIdentifier().getArtifactId(), VersionRange.createFromVersion(id.getVersion()), mainArtifact.getScope(), mainArtifact.getType(), id.getClassifier(), artifactHandler);

        String relativePath = localRepository.pathOf(mainArtifact);
        return new File(localRepository.getBasedir(), relativePath);
    }

    private ArtifactRevisionIdentifier applyRelocation(ArtifactRevisionIdentifier id, MavenProject mavenProject) {
        if (mavenProject.getDistributionManagement() != null) {
            Relocation relocation = mavenProject.getDistributionManagement().getRelocation();
            if (relocation != null) {
                String groupId = relocation.getGroupId() != null ? relocation.getGroupId() : id.getGroupId();
                String artifactId = relocation.getArtifactId() != null ? relocation.getArtifactId() : id.getArtifactId();
                String version = relocation.getVersion() != null ? relocation.getVersion() : id.getVersion();
                return new ArtifactRevisionIdentifier(artifactId, groupId, version, id.getClassifier());
            }
        }
        return id;
    }

    private static class MyArtifactHandler implements ArtifactHandler {
        private final ArtifactHandler originalHandler;

        public MyArtifactHandler(ArtifactHandler originalHandler) {
            this.originalHandler = originalHandler;
        }

        public String getExtension() {
            String extension = originalHandler.getExtension();
            if (extension.equals("bundle")) {
                extension = "jar";
            }
            return extension;
        }

        public String getDirectory() {
            return originalHandler.getDirectory();
        }

        public String getClassifier() {
            return originalHandler.getClassifier();
        }

        public String getPackaging() {
            return originalHandler.getPackaging();
        }

        public boolean isIncludesDependencies() {
            return originalHandler.isIncludesDependencies();
        }

        public String getLanguage() {
            return originalHandler.getLanguage();
        }

        public boolean isAddedToClasspath() {
            return originalHandler.isAddedToClasspath();
        }
    }
}
