package com.johnp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

@Mojo(name = "SBFL", defaultPhase = LifecyclePhase.TEST)
public class SBFLMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    private File outputDirectory;
    private Runner runner;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        runner = new Runner();
        try {
            getLog().info("LOLOL :outputDirectory" + project.getBuild().getDirectory());
            runner.runSbfl(project.getBuild().getDirectory() + "/per-test-coverage", 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
