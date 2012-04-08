package com.github.janssk1.maven.plugin.graph;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.janssk1.maven.plugin.graph.domain.ArtifactRevisionIdentifier;
import com.github.janssk1.maven.plugin.graph.graph.Graph;
import com.github.janssk1.maven.plugin.graph.graphml.GraphMLGenerator;
import com.github.janssk1.maven.plugin.graph.graphml.SizeVertexRenderer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which generates a dependency graph
 *
 * @goal graph
 * @phase process-sources
 */
public class GraphMojo
        extends AbstractMojo {


    /**
     * Set to false to skip transitive report generation
     *
     * @parameter expression="${graph.generateTransitiveReports}" default-value="true"
     */

    private boolean generateTransitiveReports;

    /**
     * @component
     * @required
     * @readonly
     */
    private MavenProjectBuilder mavenProjectBuilder;


    /**
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.groupId}"
     * @required
     */
    private String groupId;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

     /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    /**
     * Maven's local repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    public void execute()
            throws MojoExecutionException {

        getLog().info("Options used during graph calculation: generateTransitiveReports=" + generateTransitiveReports);
        ArtifactResolver artifactResolver = new MavenArtifactResolver(getLog(), localRepository, this.artifactFactory, mavenProjectBuilder);
        buildGraph(artifactResolver, DependencyOptions.GraphType.PACKAGE, false);
        buildGraph(artifactResolver, DependencyOptions.GraphType.COMPILE, false);
        buildGraph(artifactResolver, DependencyOptions.GraphType.RUNTIME, false);
        buildGraph(artifactResolver, DependencyOptions.GraphType.TEST, false);
        if (generateTransitiveReports) {
            buildGraph(artifactResolver, DependencyOptions.GraphType.COMPILE, true);
            buildGraph(artifactResolver, DependencyOptions.GraphType.RUNTIME, true);
            buildGraph(artifactResolver, DependencyOptions.GraphType.TEST, true);
        }
        //.System.out.println("graph = " + graph);
    }

    private void buildGraph(ArtifactResolver artifactResolver, DependencyOptions.GraphType graphType, boolean transitive) throws MojoExecutionException {
        GraphBuilder graphBuilder = new BreadthFirstGraphBuilder(getLog(), artifactResolver);
        DependencyOptions options = new DependencyOptions();
        options.setIncludeAllTransitiveDependencies(transitive);
        options.setGraphType(graphType);
        Graph graph = graphBuilder.buildGraph(new ArtifactRevisionIdentifier(artifactId, groupId, version), options);
        GraphSerializer graphSerializer = new GraphMLGenerator();
        try {
            File file = new File(outputDirectory, this.artifactId + "-" + this.version + "-" + graphType + (options.isIncludeAllTransitiveDependencies() ? "-TRANSITIVE":"") + "-deps.graphml");
            graphSerializer.serialize(graph, new FileWriter(file), new RenderOptions().setVertexRenderer(new SizeVertexRenderer()));
            getLog().info("Created dependency graph in " + file);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to file", e);
        }
    }
}
