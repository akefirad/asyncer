package com.akefirad.asyncer.api;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.function.Predicate;

public interface PackageAsyncer {

    Optional<CompilationUnit> make(CompilationUnit unit, Options options);

    @Data
    @Accessors(fluent = true)
    @Builder(toBuilder = true)
    class Options {

        @NonNull
        private final Predicate<CompilationUnit> packagePredicate;

        @NonNull
        private final String namePattern;

        @NonNull
        private final AsyncerContext asyncerContext;

    }
}
