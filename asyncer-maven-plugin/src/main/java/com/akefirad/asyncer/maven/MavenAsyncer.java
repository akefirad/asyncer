package com.akefirad.asyncer.maven;

import com.akefirad.asyncer.api.Asyncer;
import com.akefirad.asyncer.api.SourceParser;
import com.akefirad.asyncer.core.JavaSourceParser;
import com.akefirad.asyncer.maven.Configuration.Paths;
import com.akefirad.asyncer.util.JavaUtils;
import com.akefirad.maven.MavenLogger;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Data
@Builder
class MavenAsyncer {

    @NonNull
    private final MavenLogger log;

    @NonNull
    private final String type;

    @NonNull
    private final Paths paths;

    @NonNull
    private final Map<String, String> parameters;

    @SneakyThrows
    void make() {
        log.info("Asynchronizing using %s %s %s", type, paths, parameters);

        Asyncer asyncer = newAsyncer(type, paths.getSource(), paths.getDependencies());

        Path destination = paths.getDestination().toPath();
        Files.createDirectories(destination);

        scanSourceFiles(paths).stream()
                .peek(file -> log.debug("Including file: %s", file))
                .forEach(file -> make(asyncer, file, destination, unmodifiableMap(parameters)));
    }

    @SneakyThrows
    private Optional<File> make(Asyncer asyncer, Path source, Path dirDestination, Map<String, ?> parameters) {
        log.info("Asynchronizing %s", source);

        checkArgument(Files.isRegularFile(source), "Not a file: %s", source);
        Optional<File> asynced = asyncer.make(source, dirDestination, parameters);

        if (asynced.isPresent()) {
            log.info("Asynchronized version: %s", asynced.get());
        } else {
            log.info("Nothing is generated for %s", source);
        }

        return asynced;
    }

    private List<Path> scanSourceFiles(Paths paths) {
        File baseDirectory = JavaUtils.requireDirectory(paths.getSource());

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDirectory);
        scanner.setIncludes(paths.getIncludes());
        scanner.setExcludes(paths.getExcludes());
        scanner.scan();

        Path pathDirectory = baseDirectory.toPath();

        return stream(scanner.getIncludedFiles())
                .map(pathDirectory::resolve)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @SneakyThrows
    private Asyncer newAsyncer(String asyncerType, File sourceDirectory, File[] dependencies) {
        log.info("Creating asyncer of type %s for directory %s", asyncerType, sourceDirectory);

        Class<?> type = ClassUtils.getClass(asyncerType);
        checkArgument(Asyncer.class.isAssignableFrom(type), "%s is not of type %s", type, Asyncer.class);

        return (Asyncer) type.getConstructor(SourceParser.class)
                .newInstance(new JavaSourceParser(log, asList(dependencies)));
    }

}
