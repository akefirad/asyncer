package com.akefirad.asyncer.core;

import com.akefirad.asyncer.api.Logger;
import com.akefirad.asyncer.api.SourceParser;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class JavaSourceParser implements SourceParser {
    private static final String CLASS_PATH = getProperty("java.class.path");

    private final Logger log;
    private final JavaParser parser;

    public JavaSourceParser(@NonNull Logger log, @NonNull List<File> dependencies) {
        this.log = log;
        this.parser = new JavaParser(newConfiguration(dependencies));
    }

    private ParserConfiguration newConfiguration(List<File> dependencies) {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(new JavaSymbolSolver(newCombinedTypeSolver(dependencies)));
        return configuration;
    }

    private TypeSolver newCombinedTypeSolver(@NonNull List<File> dependencies) {
        log.info("Creating combined type solver with dependencies:\n%s", Joiner.on('\n').join(dependencies));
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        directoryTypeResolvers(dependencies).forEach(solver::add);
        jarFileTypeResolvers(CLASS_PATH).forEach(solver::add);
        return solver;
    }

    private Collection<TypeSolver> directoryTypeResolvers(Collection<File> dependencies) {
        return dependencies.stream()
                .map(this::newDependencyTypeSolver)
                .collect(toList());
    }

    private TypeSolver newDependencyTypeSolver(@NonNull File dependency) {
        if (dependency.isDirectory()) {
            return new JavaParserTypeSolver(dependency);
        } else if (dependency.isFile() && dependency.getName().endsWith(".jar")) {
            return newJarTypeSolver(dependency.toString());
        } else {
            throw new IllegalArgumentException("Invalid dependency: " + dependency);
        }
    }

    @SneakyThrows
    private JarTypeSolver newJarTypeSolver(String path) {
        return new JarTypeSolver(path);
    }

    private Collection<TypeSolver> jarFileTypeResolvers(@NonNull String path) {
        List<String> jarFiles = collectJarFilesInClassPath(path);
        log.info("Creating JAR type resolver with\n%s", Joiner.on('\n').join(jarFiles));

        return jarFiles.stream()
                .map(this::newJarTypeSolver)
                .collect(toList());
    }

    private List<String> collectJarFilesInClassPath(@NonNull String path) {
        return stream(path.split(":"))
                .map(Paths::get)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(s -> s.endsWith(".jar"))
                .collect(toList());
    }

    @Override
    public CompilationUnit parse(@NonNull String source) {
        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, Providers.provider(source));

        if (result.isSuccessful()) {
            return result.getResult().orElseThrow(AssertionError::new);
        }

        throw new ParseProblemException(result.getProblems());
    }

}
