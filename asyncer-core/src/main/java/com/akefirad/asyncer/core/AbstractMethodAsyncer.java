package com.akefirad.asyncer.core;

import com.akefirad.asyncer.api.MethodAsyncer;
import com.akefirad.asyncer.spring.SpringReturnType;
import com.akefirad.asyncer.util.JavaUtils;
import com.akefirad.asyncer.util.SourceUtils;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public abstract class AbstractMethodAsyncer implements MethodAsyncer {
    private static final String DELEGATE = "delegate";

    @Override
    public Optional<MethodDeclaration> make(@NonNull MethodDeclaration method, @NonNull Options options) {
        if (options.methodPredicate().test(method)) {
            return Optional.of(doMake(method, options));
        } else {
            return Optional.empty();
        }
    }

    protected MethodDeclaration doMake(@NonNull MethodDeclaration method, @NonNull Options options) {
        MethodDeclaration result = new MethodDeclaration()
                .setAnnotations(new NodeList<>(options.annotations()))
                .setModifiers(getAccessModifier(method, options))
                .setTypeParameters(method.getTypeParameters())
                .setType(getReturnType(method, options))
                .setName(getName(method, options))
                .setParameters(getParameters(method.getParameters()))
                .setThrownExceptions(getThrownExceptions(method.getThrownExceptions()))
                .setBody(getBody(method, options));

        return result;
    }

    private EnumSet<Modifier> getAccessModifier(MethodDeclaration method, Options options) {
        return options.accessSpecifier().isPresent() ?
                SourceUtils.toModifierEnumSet(options.accessSpecifier().get()) :
                SourceUtils.toModifierEnumSet(Modifier.getAccessSpecifier(method.getModifiers()));
    }

    private Type getReturnType(MethodDeclaration method, Options options) {
        return options.noFutureVoid() && method.getType().isVoidType() ?
                SpringReturnType.VOID.getReturnType(method.getType()) :
                options.returnType().getReturnType(SourceUtils.getFullyQualifiedNamedBoxedType(method.getType()));
    }

    private String getName(MethodDeclaration method, Options options) {
        String name = JavaUtils.newStrSubstitutor(options.asyncerContext().names()).replace(options.namePattern());
        checkArgument(StringUtils.isNotBlank(name), "Method name cannot be blank: %s", name);
        return name;
    }

    private NodeList<Parameter> getParameters(List<Parameter> parameters) {
        return parameters.stream()
                .map(this::toFullyQualifiedParameter)
                .collect(collectingAndThen(toList(), NodeList::new));
    }

    private Parameter toFullyQualifiedParameter(Parameter parameter) {
        return new Parameter(SourceUtils.getFullyQualifiedNamedType(parameter.getType()), parameter.getNameAsString());
    }

    protected NodeList<ReferenceType> getThrownExceptions(List<ReferenceType> thrownExceptions) {
        return thrownExceptions.stream()
                .map(this::toFullyQualifiedParameter)
                .collect(collectingAndThen(toList(), NodeList::new));
    }

    private ReferenceType toFullyQualifiedParameter(ReferenceType type) {
        checkArgument(type.isClassOrInterfaceType(), "Invalid thrown exception: %s", type);
        return SourceUtils.toFullyQualifiedNamedReferenceType(type.asClassOrInterfaceType());
    }

    protected abstract BlockStmt getBody(MethodDeclaration method, Options options);

    protected MethodCallExpr newSyncMethodCallExpr(MethodDeclaration method, Options options) {
        NodeList<Expression> parameters = new NodeList<>();
        method.getParameters().forEach(parameter -> parameters.add(new NameExpr(parameter.getName())));
        return new MethodCallExpr(new FieldAccessExpr(new ThisExpr(), DELEGATE), method.getName(), parameters);
    }

}
