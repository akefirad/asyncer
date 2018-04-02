package com.akefirad.asyncer.spring;

import com.akefirad.asyncer.api.*;
import com.akefirad.asyncer.core.AbstractAsyncer;
import com.akefirad.asyncer.util.Constants.Parameters;
import com.akefirad.asyncer.util.JavaUtils;
import com.akefirad.asyncer.util.SourceUtils;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class SpringAsyncer extends AbstractAsyncer {
    public static final String ASYNC_ANNOTATION = "org.springframework.scheduling.annotation.Async";
    public static final String COMPONENT_ANNOTATION = "org.springframework.stereotype.Component";
    public static final String SERVICE_ANNOTATION = "org.springframework.stereotype.Service";
    public static final String LAZY_ANNOTATION = "org.springframework.context.annotation.Lazy";
    public static final String SPRING_ASYNC_CLASS = "CLASS";
    public static final String SPRING_ASYNC_METHOD = "METHOD";

    public SpringAsyncer(SourceParser parser) {
        super(parser);
    }

    @Override
    protected SpringPackageAsyncer newPackageAsyncer() {
        return new SpringPackageAsyncer();
    }

    @Override
    protected PackageAsyncer.Options newPackageAsyncerOptions(Map<String, ?> parameters, AsyncerContext context) {
        return super.newPackageAsyncerOptions(parameters, context);
    }

    @Override
    protected SpringTypeAsyncer newTypeAsyncer() {
        return new SpringTypeAsyncer();
    }

    @Override
    protected TypeAsyncer.Options newTypeAsyncerOptions(Map<String, ?> parameters, AsyncerContext context) {
        return super.newTypeAsyncerOptions(parameters, context)
                .toBuilder()
                .annotations(getTypeAnnotations(parameters, context))
                .build();
    }

    private ImmutableList<AnnotationExpr> getTypeAnnotations(Map<String, ?> parameters, AsyncerContext context) {
        return ImmutableList.<AnnotationExpr>builder()
                .addAll(newLazyAnnotation(parameters))
                .addAll(newComponentAnnotation(parameters, context))
                .addAll(getClassLevelAsyncAnnotation(parameters, context))
                .build();
    }

    private ImmutableList<AnnotationExpr> getClassLevelAsyncAnnotation(Map<String, ?> parameters, AsyncerContext context) {
        return SPRING_ASYNC_CLASS.equals(getAsyncType(parameters)) ?
                newAsyncAnnotation(parameters, context) : ImmutableList.of();
    }

    private List<AnnotationExpr> newLazyAnnotation(Map<String, ?> parameters) {
        return parseBoolean(ofNullable((String) parameters.get(Parameters.Spring.IS_LAZY_BEAN)).orElse("true")) ?
                singletonList(SourceUtils.newAnnotationWithValue(SourceUtils.toName(LAZY_ANNOTATION), empty())) :
                emptyList();
    }

    // Watch the default value, if you're refactoring this!
    private String getAsyncType(Map<String, ?> parameters) {
        String value = ofNullable((String) parameters.get(Parameters.Spring.ASYNC_TYPE)).orElse(SPRING_ASYNC_CLASS);
        boolean isValid = StringUtils.equalsAnyIgnoreCase(value, SPRING_ASYNC_CLASS, SPRING_ASYNC_METHOD);
        checkArgument(isValid, "Invalid async type, must be either CLASS or METHOD: %s", value);
        return value;
    }

    private ImmutableList<AnnotationExpr> newAsyncAnnotation(Map<String, ?> parameters, AsyncerContext context) {
        Optional<String> executorName = ofNullable(parameters.get(Parameters.Spring.EXECUTOR_NAME))
                .map(name -> JavaUtils.newStrSubstitutor(context.names()).replace(name));

        Name annotationType = SourceUtils.toName(ASYNC_ANNOTATION);
        return ImmutableList.of(SourceUtils.newAnnotationWithValue(annotationType, executorName));
    }

    private ImmutableList<AnnotationExpr> newComponentAnnotation(Map<String, ?> parameters, AsyncerContext context) {
        Optional<String> beanName = ofNullable(parameters.get(Parameters.Spring.BEAN_NAME))
                .map(name -> JavaUtils.newStrSubstitutor(context.names()).replace(name));

        String annotationType = ofNullable((String) parameters.get(Parameters.Spring.BEAN_TYPE)).orElse(COMPONENT_ANNOTATION);
        return ImmutableList.of(SourceUtils.newAnnotationWithValue(SourceUtils.toName(annotationType), beanName));
    }

    @Override
    protected SpringMethodAsyncer newMethodAsyncer() {
        return new SpringMethodAsyncer();
    }

    @Override
    protected MethodAsyncer.Options newMethodAsyncerOptions(Map<String, ?> parameters, AsyncerContext context) {
        return super.newMethodAsyncerOptions(parameters, context)
                .toBuilder()
                .methodPredicate(method -> isPublicNonObjectMethod(method, context))
                .annotations(getMethodAsyncAnnotation(parameters, context))
                .build();
    }

    private ImmutableList<AnnotationExpr> getMethodAsyncAnnotation(Map<String, ?> parameters, AsyncerContext context) {
        return SPRING_ASYNC_METHOD.equals(getAsyncType(parameters)) ?
                newAsyncAnnotation(parameters, context) : ImmutableList.of();
    }

    private boolean isPublicNonObjectMethod(MethodDeclaration method, AsyncerContext context) {
        TypeDeclaration<?> type = context.originalType()
                .orElseThrow(() -> new IllegalStateException("Original type is missing!"));
        return SourceUtils.isPublicMethod(type, method) && SourceUtils.isObjectMethod(method) == false;
    }

}
