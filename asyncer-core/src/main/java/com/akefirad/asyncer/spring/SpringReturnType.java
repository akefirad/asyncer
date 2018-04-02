package com.akefirad.asyncer.spring;

import com.akefirad.asyncer.util.SourceUtils;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.Arrays.stream;

public enum SpringReturnType implements com.akefirad.asyncer.api.ReturnType {
    /**
     * No return type in the async method
     */
    VOID("void") {
        @Override
        public Type getReturnType(@NonNull Type type) {
            return new VoidType();
        }

        @Override
        public ReturnStmt getReturnStatement(Expression expression) {
            throw new UnsupportedOperationException("No return statement is available!");
        }
    },
    /**
     * Future return type in the async method
     */
    FUTURE(Future.class.getCanonicalName()) {
        @Override
        public Type getReturnType(@NonNull Type type) {
            return SourceUtils.toGenericType(Future.class, type);
        }

        @Override
        public ReturnStmt getReturnStatement(Expression expression) {
            String packageName = "org.springframework.scheduling.annotation";
            ClassOrInterfaceType type = SourceUtils.toGenericType(packageName, "AsyncResult");
            ObjectCreationExpr instantiation = new ObjectCreationExpr(null, type, new NodeList<>());
            instantiation.getArguments().add(expression);
            return new ReturnStmt(instantiation);
        }
    },
    /**
     * CompletableFuture return type in the async method
     */
    COMPLETABLE_FUTURE(CompletableFuture.class.getCanonicalName()) {
        @Override
        public Type getReturnType(@NonNull Type type) {
            return SourceUtils.toGenericType(CompletableFuture.class, type);
        }

        @Override
        public ReturnStmt getReturnStatement(Expression expression) {
            String typeName = CompletableFuture.class.getSimpleName();
            String packageName = CompletableFuture.class.getPackage().getName();
            Expression typeScope = SourceUtils.newExpressionScope(packageName);
            FieldAccessExpr callScope = new FieldAccessExpr(typeScope, typeName);
            MethodCallExpr callExpr = new MethodCallExpr(callScope, "completedFuture");
            callExpr.getArguments().add(expression);
            return new ReturnStmt(callExpr);
        }
    };

    private final String name;

    SpringReturnType(@NonNull String name) {
        this.name = name;
    }

    public static SpringReturnType of(@NonNull String name) {
        return stream(values())
                .filter(type -> type.name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Return Type: " + name));
    }

}
