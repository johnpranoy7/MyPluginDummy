package com.johnp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "jacocoSurefireCoverage", defaultPhase = LifecyclePhase.TEST)
public class JacocoMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean skipTests;

    @Parameter(property = "testFailureIgnore", defaultValue = "true")
    private boolean testFailureIgnore;

    @Parameter(property = "jacocoDestFile")
    private String jacocoDestFile;

    @Parameter(property = "listenerClass", defaultValue = "com.johnp.PerTestCoverageListener")
    private String listenerClass;

    public void execute() throws MojoExecutionException {
        if (skipTests) {
            getLog().info("Tests are skipped.");
            return;
        }

        try {
            // Execute the plugin sequence
            executeJaCoCoAndTests();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute JaCoCo and tests", e);
        }
    }

    private void executeJaCoCoAndTests() throws MavenInvocationException {
        getLog().info("Starting JaCoCo and Surefire execution sequence");

        // Step 1: Prepare JaCoCo agent (CRITICAL: Must be before test execution)
        executeJaCoCoPrepareAgent();

        // Step 2: Run tests with Surefire
        executeSurefireWithSystemProperties();

        getLog().info("JaCoCo and Surefire execution completed successfully");
    }

    private void executeJaCoCoPrepareAgent() throws MavenInvocationException {
        getLog().info("=== Executing JaCoCo prepare-agent ===");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(project.getBasedir(), "pom.xml"));
        request.setGoals(Collections.singletonList("org.jacoco:jacoco-maven-plugin:0.8.11:prepare-agent"));

        // Set JaCoCo properties
        Properties props = new Properties();

        // Determine destFile path
        String destFile = jacocoDestFile != null ? jacocoDestFile :
                "${project.build.directory}/jacoco-per-test.exec";

        props.setProperty("jacoco.destFile", destFile);
        props.setProperty("jacoco.append", "false");
        props.setProperty("jacoco.sessionId", getCurrentTimestamp());

        // CRITICAL: Set property name for argLine
        props.setProperty("jacoco.propertyName", "jacocoArgLine");

        request.setProperties(props);

        executeWithInvoker(request, "JaCoCo prepare-agent");
    }

    private void executeSurefireTests() throws MavenInvocationException {
        getLog().info("=== Executing Surefire tests with listener ===");
        getLog().info("*** Configuring test listener: " + listenerClass + " ***");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(project.getBasedir(), "pom.xml"));
        request.setGoals(Collections.singletonList("org.apache.maven.plugins:maven-surefire-plugin:3.0.0:test"));

        // Set Surefire properties
        Properties props = new Properties();
        props.setProperty("maven.test.failure.ignore", String.valueOf(testFailureIgnore));
        props.setProperty("forkCount", "1");
        props.setProperty("reuseForks", "false");

        // CRITICAL FIX 1: Proper listener configuration for Surefire 3.0+
        // Use the correct property name for listeners
        props.setProperty("surefire.junit5.listeners", listenerClass);
        props.setProperty("surefire.junit4.listeners", listenerClass);

        // Alternative approach - use properties configuration
        props.setProperty("junit.platform.listeners.enable", "true");

        // CRITICAL FIX 2: Add JaCoCo argLine properly
        props.setProperty("argLine", "${jacocoArgLine}");

        // Additional Surefire configurations
        props.setProperty("surefire.useSystemClassLoader", "false");
        props.setProperty("surefire.useManifestOnlyJar", "false");
        props.setProperty("surefire.printSummary", "true");

        // Enable debug output to trace listener loading
        props.setProperty("surefire.runOrder", "filesystem");
        props.setProperty("trimStackTrace", "false");

        request.setProperties(props);

        executeWithInvoker(request, "Surefire tests");
    }

    private void executeWithInvoker(InvocationRequest request, String stepName)
            throws MavenInvocationException {

        Invoker invoker = new DefaultInvoker();

        // Set Maven home - make this configurable or auto-detect
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome == null) {
            mavenHome = "C:\\work\\apache-maven-3.9.11"; // Your fallback
        }

        invoker.setMavenHome(new File(mavenHome));
        invoker.setWorkingDirectory(project.getBasedir());

        // Configure output handlers with enhanced listener detection
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String line) throws IOException {
                // Enhanced detection for listener activity
                if (line.toLowerCase().contains("listener") ||
                        line.contains("PerTestCoverageListener") ||
                        line.contains("TestExecutionListener") ||
                        line.contains("junit") && line.toLowerCase().contains("listener")) {
                    getLog().info("*** LISTENER ACTIVITY ***: " + stepName + ": " + line);
                } else if (line.contains("jacocoArgLine") ||
                        line.toLowerCase().contains("jacoco") ||
                        line.contains("javaagent")) {
                    getLog().info("*** JACOCO ACTIVITY ***: " + stepName + ": " + line);
                } else if (line.contains("Forking") || line.contains("Fork") ||
                        line.contains("Running") || line.contains("Tests run:")) {
                    getLog().info("*** TEST EXECUTION ***: " + stepName + ": " + line);
                } else if (!line.contains("Downloading") && !line.contains("Downloaded")) {
                    getLog().info(stepName + ": " + line);
                }
            }
        });

        invoker.setErrorHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String line) throws IOException {
                if (line.contains("listener") || line.contains("Listener")) {
                    getLog().error("*** LISTENER ERROR ***: " + line);
                } else if (line.contains("jdwp") || line.contains("JDWP")) {
                    getLog().error("*** DEBUG PORT ISSUE ***: " + line);
                } else {
                    getLog().error(stepName + " Error: " + line);
                }
            }
        });

        getLog().info("Executing " + stepName + " in: " + project.getBasedir());
        getLog().info("Maven Home: " + mavenHome);
        getLog().info("Goals: " + request.getGoals());
        if (request.getProperties() != null && !request.getProperties().isEmpty()) {
            getLog().info("Properties: " + request.getProperties());
        }

        InvocationResult result = invoker.execute(request);

        if (result.getExitCode() != 0) {
            if (testFailureIgnore && stepName.contains("test")) {
                getLog().warn(stepName + " failed with exit code: " + result.getExitCode() +
                        " (ignored due to testFailureIgnore=true)");
            } else {
                throw new RuntimeException(stepName + " execution failed with exit code: " +
                        result.getExitCode());
            }
        }

        getLog().info(stepName + " completed successfully");
    }


    // Alternative method 3: Direct JVM system properties approach
    private void executeSurefireWithSystemProperties() throws MavenInvocationException {
        getLog().info("=== Executing Surefire with system properties ===");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(project.getBasedir(), "pom.xml"));
        request.setGoals(Collections.singletonList("test"));

        Properties props = new Properties();

        // Configure JaCoCo
        props.setProperty("argLine", "${jacocoArgLine}");

        // Configure listener through various system properties
        props.setProperty("systemProperties.listener", listenerClass);
        props.setProperty("systemProperties.junit.platform.listeners.enable", "true");
        props.setProperty("systemProperties.junit.jupiter.extensions.autodetection.enabled", "true");

        // For JUnit 4 compatibility
        props.setProperty("systemProperties.junit4.listeners", listenerClass);

        // Additional debug properties
        props.setProperty("systemProperties.surefire.listener.debug", "true");

        request.setProperties(props);
        executeWithInvoker(request, "Surefire with system properties");
    }

    private String getCurrentTimestamp() {
        return java.time.Instant.now().toString();
    }

}