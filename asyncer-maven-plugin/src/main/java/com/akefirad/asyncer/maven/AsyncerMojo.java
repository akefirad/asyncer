package com.akefirad.asyncer.maven;

import com.akefirad.asyncer.maven.Configuration.Paths;
import com.akefirad.maven.MavenLogger;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

@Mojo(name = "asyncer", defaultPhase = GENERATE_SOURCES)
public class AsyncerMojo extends AbstractMojo {

    @Parameter(required = true)
    private String type;

    @Parameter(required = true)
    private Paths paths;

    @Parameter(required = true)
    private Map<String, String> parameters;

    @Parameter(defaultValue = "${settings.localRepository}", required = true, readonly = true)
    private String repositoryPath;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @SneakyThrows
    public void execute() throws MojoExecutionException {
        try {
            newMavenAsyncer().make();
        } catch (Exception e) {
            getLog().error("Asynchronization failed due to " + e, e);
            throw new MojoExecutionException("Asynchronization failed due to " + e, e);
        }
    }

    private MavenAsyncer newMavenAsyncer() {
        return MavenAsyncer.builder()
                .log(new MavenLogger(this::getLog))
                .type(getType(type))
                .paths(getPaths(paths))
                .parameters(getParameters(parameters))
                .build();
    }

    private String getType(@NonNull String type) {
        checkArgument(StringUtils.isNotBlank(type), "Asyncer type is missing!");
        return type;
    }

    private Paths getPaths(@NonNull Paths paths) {
        checkArgument(nonNull(paths.getSource()), "Source directory is missing!");
        checkArgument(paths.getSource().isDirectory(), "Not a directory: %s", paths.getSource());
        checkArgument(nonNull(paths.getDestination()), "Destination directory is missing!");
        checkArgument(paths.getIncludes() != null && paths.getIncludes().length > 0, "Include patterns are missing!");
        checkArgument(paths.getExcludes() != null, "Invalid exclude patterns!");
        return paths;
    }

    private Map<String, String> getParameters(@NonNull Map<String, String> parameters) {
        String packageName = parameters.get("patterns.generatedPackageName");
        String typeName = parameters.get("patterns.generatedTypeName");
        checkArgument(ObjectUtils.anyNotNull(packageName, typeName), "Either package or type name should be given!");
        return parameters;
    }

}
