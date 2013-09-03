Documentation can be found at https://github.com/janssk1/maven-graph-plugin/wiki/Manual


*Example*

<plugin>
	<groupId>com.github.janssk1</groupId>
	<artifactId>maven-dependencygraph-plugin</artifactId>
	<version>1.2-SNAPSHOT</version>
	<configuration>
		<reports>COMPILE</reports>
		<excludedArtifactIds>
			<excludedArtifactId>.*I.want.to.exclude.this.artifactId.regex.*</excludedArtifactId>
			<excludedArtifactId>I-want-to-exclude-this-artifactId</excludedArtifactId>
		</excludedArtifactIds>
		<excludedGroupIds>
			<excludedGroupId>.*I.want.to.exclude.this.groupId.regex.*</excludedGroupId>
			<excludedGroupId>I-want-to-exclude-this-groupId</excludedGroupId>
		</excludedGroupIds>
		<showVersion>false</showVersion>
		<showEdgeLabels>false</showEdgeLabels>
	</configuration>
</plugin>