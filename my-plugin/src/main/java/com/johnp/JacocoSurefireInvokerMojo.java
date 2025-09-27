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

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "myCoverage", defaultPhase = LifecyclePhase.TEST)
public class JacocoSurefireInvokerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Set system properties to help with classloading
        System.setProperty("surefire.useSystemClassLoader", "false");
        System.setProperty("surefire.useManifestOnlyJar", "false");

        getLog().info("Running JaCoCo prepare-agent goal...");
        executeMojo(
                plugin(
                        groupId("org.jacoco"),
                        artifactId("jacoco-maven-plugin"),
                        version("0.8.11")
                ),
                goal("prepare-agent"),
                configuration(
                        element(name("destFile"), "${project.build.directory}/jacoco-per-test.exec"),
                        element(name("sessionId"), "${maven.build.timestamp}"),
                        element(name("append"), "false")
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        );

        getLog().info("Running Surefire tests...");
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-surefire-plugin"),
                        version("3.2.5"),
                        dependencies(
                                dependency(
                                        groupId("org.apache.maven.surefire"),
                                        artifactId("surefire-junit4"),
                                        version("3.2.5")
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
                        element(name("forkCount"), "1"),
                        element(name("reuseForks"), "false"),
                        element(name("properties"),
                                element(name("property"),
                                        element(name("name"), "listener"),
                                        element(name("value"), "com.johnp.PerTestCoverageListener")
                                )
                        ), element(name("includes"),
                                element(name("include"), "**/*Test.java"),
                                element(name("include"), "**/*Tests.java")
                        ),
                        element(name("testClassesDirectory"), "${project.build.testOutputDirectory}"),
                        element(name("classesDirectory"), "${project.build.outputDirectory}"),
                        element(name("classpathDependencyExcludes"), ""),
                        element(name("additionalClasspathElements"),
                                element(name("additionalClasspathElement"), "${project.build.testOutputDirectory}"))
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        );

    }
}
