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
import java.util.List;

/**
 * Goal which generates a set of dependency graphs
 *
 * @goal graph
 * @phase process-sources
 */
public class GraphMojo
        extends AbstractMojo {


    /**
     * A comma separated list of report definitions
     *
     * @parameter expression="${graph.reports}" default-value="PACKAGE,COMPILE,RUNTIME,TEST,COMPILE-TRANSITIVE"
     */

    private String reports;

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
     * @readonly
     */
    private String groupId;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.version}"
     * @required
     * @readonly
     */
    private String version;

     /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    /**
     * Maven's local repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    public void execute()
            throws MojoExecutionException {


        getLog().info("Using graph.reports=" + reports);
        List<DependencyOptions> reportDefinitions = DependencyOptions.parseReportDefinitions(reports);
        ArtifactResolver artifactResolver = new MavenArtifactResolver(getLog(), localRepository, this.artifactFactory, mavenProjectBuilder);
        for (DependencyOptions reportDefinition : reportDefinitions) {
            buildGraph(artifactResolver, reportDefinition);
        }
    }


    private void buildGraph(ArtifactResolver artifactResolver, DependencyOptions options) throws MojoExecutionException {
        GraphBuilder graphBuilder = new BreadthFirstGraphBuilder(getLog(), artifactResolver);
        Graph graph = graphBuilder.buildGraph(new ArtifactRevisionIdentifier(artifactId, groupId, version), options);
        GraphSerializer graphSerializer = new GraphMLGenerator();
        try {
            File file = new File(outputDirectory, this.artifactId + "-" + this.version + "-" + options.getGraphType() + (options.isIncludeAllTransitiveDependencies() ? "-TRANSITIVE":"") + "-deps.graphml");
            file.getParentFile().mkdirs();
            graphSerializer.serialize(graph, new FileWriter(file), new RenderOptions().setVertexRenderer(new SizeVertexRenderer()));
            getLog().info("Created dependency graph in " + file);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to file", e);
        }
    }
}
