package com.akefirad.asyncer.api;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.function.Predicate;

public interface MethodAsyncer {

    Optional<MethodDeclaration> make(MethodDeclaration method, Options options);

    @Data
    @Accessors(fluent = true)
    @Builder(toBuilder = true)
    class Options {

        @NonNull
        private final Predicate<MethodDeclaration> methodPredicate;

        @NonNull
        private final String namePattern;

        @NonNull
        private final AsyncerContext asyncerContext;

        @NonNull
        private final Optional<AccessSpecifier> accessSpecifier;

        @NonNull
        private final com.akefirad.asyncer.api.ReturnType returnType;

        @NonNull
        private final ImmutableList<AnnotationExpr> annotations;

        private final boolean noFutureVoid;

    }

}
