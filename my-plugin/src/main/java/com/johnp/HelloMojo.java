package com.johnp;


import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;


@Mojo(name = "hello", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class HelloMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "scope")
    String scope;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(property = "surefireVersion", defaultValue = "3.5.3")
    private String surefireVersion;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Hello World from my-plugin! hello mojo");
        long dependencyCount = project.getDependencies().stream().count();
        getLog().info("dependency count: " + dependencyCount);
        getLog().info("Scope: " + scope);
        getLog().info("Hello World from my-plugin! hello mojo - END");

//        setupJacoco();
//        setupSurefire();

//        runSureFireTests();

        getLog().info("Invoking maven-surefire-plugin version: " + surefireVersion);

    }

    private void setupJacoco() throws MojoExecutionException {

        MojoExecutor.executeMojo(
                MojoExecutor.plugin(
                        MojoExecutor.groupId("org.jacoco"),
                        MojoExecutor.artifactId("jacoco-maven-plugin"),
                        MojoExecutor.version("0.8.11")
                ),
                MojoExecutor.goal("prepare-agent"),
                MojoExecutor.configuration(
                        MojoExecutor.element("destFile", "${project.build.directory}/jacoco-per-test.exec"),
                        MojoExecutor.element("sessionId", "${maven.build.timestamp}"),
                        MojoExecutor.element("append", "false")
                ),
                MojoExecutor.executionEnvironment(
                        project, mavenSession, pluginManager
                )
        );

    }

    private void setupSurefire() throws MojoExecutionException {
        // 2. Run Surefire with JUnit4 provider and Jacoco agent (uses the argLine set above)
        MojoExecutor.executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-surefire-plugin"),
                        version("2.22.2"),
                        dependencies(
                                dependency(
                                        groupId("org.apache.maven.surefire"),
                                        artifactId("surefire-junit4"),
                                        version("2.22.2")
                                ),
                                dependency(
                                        groupId("junit"),
                                        artifactId("junit"),
                                        version("4.13.2")
                                )
                        )
                ),
                goal("test"),
                configuration(
                        element("forkCount", "1"),
                        element("reuseForks", "false"),
                        element("useSystemClassLoader", "false"), // critical!
                        // use the argLine variable set by jacoco:prepare-agent
                        element("argLine", "${argLine}"),
//                        element("additionalClasspathElements",
//                                element("additionalClasspathElement", "/absolute/path/to/your/plugin.jar")
//                        ),
                        element("properties",
                                element("property",
                                        element("name", "listener"),
                                        element("value", "com.johnp.hal.PerTestCoverageListener")
                                )
                        )
                ),
                executionEnvironment(
                        project, mavenSession, pluginManager
                )
        );

        // 3. Optionally generate Jacoco report after tests
        MojoExecutor.executeMojo(
                plugin(
                        groupId("org.jacoco"),
                        artifactId("jacoco-maven-plugin"),
                        version("0.8.11")
                ),
                goal("report"),
                configuration(
                        element("dataFile", "${project.build.directory}/jacoco-per-test.exec")
                ),
                executionEnvironment(
                        project, mavenSession, pluginManager
                )
        );
    }


    private void runSureFireTests() throws MojoExecutionException {

        getLog().info("Invoking Maven Surefire Plugin version: " + surefireVersion);

        // Build the properties configuration for the listener
        Xpp3Dom properties = new Xpp3Dom("properties");
        Xpp3Dom property = new Xpp3Dom("property");

        Xpp3Dom name = new Xpp3Dom("name");
        name.setValue("listener");
        property.addChild(name);

        Xpp3Dom value = new Xpp3Dom("value");
        value.setValue("com.johnp.PerTestCoverageListener");
        property.addChild(value);

        properties.addChild(property);

        // Build additional classpath elements to include this plugin
        Xpp3Dom additionalClasspathElements = new Xpp3Dom("additionalClasspathElements");
        Xpp3Dom classpathElement = new Xpp3Dom("additionalClasspathElement");

        // Get the plugin's JAR location
        String pluginJarPath = getClass().getProtectionDomain()
                .getCodeSource().getLocation().getPath();
        classpathElement.setValue(pluginJarPath);
        additionalClasspathElements.addChild(classpathElement);

        // Build includes for test discovery
        Xpp3Dom includes = new Xpp3Dom("includes");
        Xpp3Dom include1 = new Xpp3Dom("include");
        include1.setValue("**/*Test.java");
        Xpp3Dom include2 = new Xpp3Dom("include");
        include2.setValue("**/*Tests.java");
        Xpp3Dom include3 = new Xpp3Dom("include");
        include3.setValue("**/*TestCase.java");
        includes.addChild(include1);
        includes.addChild(include2);
        includes.addChild(include3);

        // Build the full configuration
        org.codehaus.plexus.util.xml.Xpp3Dom config = MojoExecutor.configuration(
                MojoExecutor.element("skip", "false"),
                MojoExecutor.element("forkCount", "1"),
                MojoExecutor.element("reuseForks", "false"),
                MojoExecutor.element("useSystemClassLoader", "false"),
                MojoExecutor.element("useManifestOnlyJar", "false"),
                MojoExecutor.element("properties",
                        MojoExecutor.element("property",
                                MojoExecutor.element("name", "listener"),
                                MojoExecutor.element("value", "com.johnp.PerTestCoverageListener")
                        )
                ),
                MojoExecutor.element("additionalClasspathElements",
                        MojoExecutor.element("additionalClasspathElement", pluginJarPath)
                ),
                MojoExecutor.element("includes",
                        MojoExecutor.element("include", "**/*Test.java"),
                        MojoExecutor.element("include", "**/*Tests.java")
                )
        );


        // Execute the Surefire plugin's "test" goal programmatically
        try {
            MojoExecutor.executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-surefire-plugin"),
                            version(surefireVersion),
                            dependencies(
                                    dependency(
                                            groupId("org.apache.maven.surefire"),
                                            artifactId("surefire-junit4"),
                                            version(surefireVersion)
                                    ),
                                    dependency(
                                            groupId("junit"),
                                            artifactId("junit"),
                                            version("4.13.2")
                                    )
                            )
                    ),
                    goal("test"),
                    config,
                    executionEnvironment(
                            project,
                            mavenSession,
                            pluginManager
                    )
            );
            getLog().info("Surefire tests executed successfully with JaCoCo listener.");
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute Surefire plugin", e);
        }
    }

}
