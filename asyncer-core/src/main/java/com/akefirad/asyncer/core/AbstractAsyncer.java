package com.akefirad.asyncer.core;

import com.akefirad.asyncer.api.*;
import com.akefirad.asyncer.spring.SpringReturnType;
import com.akefirad.asyncer.util.Constants.Parameters;
import com.akefirad.asyncer.util.Constants.Placeholders;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithPublicModifier;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public abstract class AbstractAsyncer implements Asyncer {

    @Getter
    @NonNull
    private final SourceParser parser;

    @Override
    public Optional<String> make(@NonNull String source, @NonNull Map<String, ?> parameters) {
        checkArgument(StringUtils.isNotBlank(source), "Source cannot be empty!");
        return make(parser.parse(source), parameters).map(Node::toString);
    }

    private Optional<CompilationUnit> make(CompilationUnit syncPackage, Map<String, ?> parameters) {
        AsyncerContext ctxPackageAsynchronization = newPackageAsynchronizationContext(syncPackage);
        Optional<CompilationUnit> asyncPackage = asynchronizePackage(syncPackage, parameters, ctxPackageAsynchronization);

        if (asyncPackage.isPresent() == false) {
            return Optional.empty();
        }

        AsyncerContext ctxTypeAsynchronization = newTypeAsynchronizationContext(ctxPackageAsynchronization, asyncPackage.get());
        List<TypeDeclaration<?>> asyncTypes = asynchronizeTypes(syncPackage, parameters, ctxTypeAsynchronization);

        if (asyncTypes.isEmpty()) {
            return Optional.empty();
        }

        asyncPackage.get().setTypes(new NodeList<>(asyncTypes));

        return asyncPackage;
    }

    private AsyncerContext newPackageAsynchronizationContext(CompilationUnit unit) {
        return AsyncerContext.builder()
                .originalPackage(unit.getPackageDeclaration())
                .build();
    }

    private Optional<CompilationUnit> asynchronizePackage(CompilationUnit unit, Map<String, ?> parameters, AsyncerContext context) {
        return newPackageAsyncer().make(unit, newPackageAsyncerOptions(parameters, context));
    }

    protected abstract PackageAsyncer newPackageAsyncer();

    protected PackageAsyncer.Options newPackageAsyncerOptions(@NonNull Map<String, ?> parameters, @NonNull AsyncerContext context) {
        String pattern = ofNullable(StringUtils.trimToNull((String) parameters.get(Parameters.GENERATED_PACKAGE_NAME)))
                .orElse(Placeholders.ORIGINAL_PACKAGE_NAME);

        return PackageAsyncer.Options.builder()
                .packagePredicate(p -> true)
                .namePattern(pattern)
                .asyncerContext(context)
                .build();
    }

    private AsyncerContext newTypeAsynchronizationContext(AsyncerContext context, CompilationUnit unit) {
        return context.toBuilder()
                .generatedPackage(unit.getPackageDeclaration())
                .build();
    }

    private List<TypeDeclaration<?>> asynchronizeTypes(CompilationUnit unit, Map<String, ?> parameters, AsyncerContext context) {
        List<ClassOrInterfaceDeclaration> types = unit.getTypes().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .collect(toList());

        if (types.isEmpty()) {
            return emptyList();
        }

        TypeAsyncer typeAsyncer = newTypeAsyncer();
        List<TypeDeclaration<?>> list = new ArrayList<>();

        for (ClassOrInterfaceDeclaration type : types) {
            AsyncerContext ctxTypeAsynchronization = newTypeAsynchronizationContext(context, type);
            TypeAsyncer.Options asyncerOptions = newTypeAsyncerOptions(parameters, ctxTypeAsynchronization);
            Optional<ClassOrInterfaceDeclaration> asyncType = typeAsyncer.make(type, asyncerOptions);

            if (asyncType.isPresent() == false) {
                continue;
            }

            AsyncerContext ctxMethodAsynchronization = newMethodAsynchronizationContext(ctxTypeAsynchronization, asyncType.get());
            List<BodyDeclaration<?>> asyncMethods = makeAsyncMethods(type, parameters, ctxMethodAsynchronization);

            if (asyncMethods.isEmpty()) {
                continue;
            }

            asyncType.get().getMembers().addAll(asyncMethods);

            list.add(asyncType.get());
        }

        return list;
    }

    private AsyncerContext newTypeAsynchronizationContext(AsyncerContext context, ClassOrInterfaceDeclaration type) {
        return context.toBuilder()
                .originalType(Optional.of(type))
                .build();
    }

    protected abstract TypeAsyncer newTypeAsyncer();

    protected TypeAsyncer.Options newTypeAsyncerOptions(@NonNull Map<String, ?> parameters, @NonNull AsyncerContext context) {
        String pattern = ofNullable(StringUtils.trimToNull((String) parameters.get(Parameters.GENERATED_TYPE_NAME)))
                .orElse(Placeholders.ORIGINAL_TYPE_NAME);

        return TypeAsyncer.Options.builder()
                .typePredicate(t -> true)
                .namePattern(pattern)
                .asyncerContext(context)
                .accessSpecifier(empty())
                .annotations(ImmutableList.of())
                .build();
    }

    private AsyncerContext newMethodAsynchronizationContext(AsyncerContext context, ClassOrInterfaceDeclaration type) {
        return context.toBuilder()
                .generatedType(Optional.of(type))
                .build();
    }

    private List<BodyDeclaration<?>> makeAsyncMethods(ClassOrInterfaceDeclaration type, Map<String, ?> parameters, AsyncerContext context) {
        List<MethodDeclaration> methods = type.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .collect(toList());

        if (methods.isEmpty()) {
            return emptyList();
        }

        MethodAsyncer mtdAsyncer = newMethodAsyncer();
        List<BodyDeclaration<?>> list = new ArrayList<>();

        for (MethodDeclaration method : methods) {
            AsyncerContext contextWithOriginalMethodName = newMethodAsynchronizationContext(context, method);
            MethodAsyncer.Options asyncerOptions = newMethodAsyncerOptions(parameters, contextWithOriginalMethodName);
            Optional<MethodDeclaration> make = mtdAsyncer.make(method, asyncerOptions);

            if (make.isPresent() == false) {
                continue;
            }

            list.add(make.get());
        }

        return list;
    }

    protected abstract MethodAsyncer newMethodAsyncer();

    private AsyncerContext newMethodAsynchronizationContext(AsyncerContext context, MethodDeclaration method) {
        return context.toBuilder()
                .originalMethod(Optional.of(method))
                .build();
    }

    protected MethodAsyncer.Options newMethodAsyncerOptions(@NonNull Map<String, ?> parameters, @NonNull AsyncerContext context) {
        String pattern = ofNullable(StringUtils.trimToNull((String) parameters.get(Parameters.GENERATED_METHOD_NAME)))
                .orElse(Placeholders.ORIGINAL_METHOD_NAME);

        return MethodAsyncer.Options.builder()
                .methodPredicate(m -> true)
                .namePattern(pattern)
                .accessSpecifier(empty())
                .returnType(SpringReturnType.COMPLETABLE_FUTURE)
                .annotations(ImmutableList.of())
                .asyncerContext(context)
                .build();
    }

    @Override
    public Optional<File> make(@NonNull Path source, @NonNull Path dirDestination, @NonNull Map<String, ?> parameters) throws IOException {
        CompilationUnit syncUnit = parser.parse(new String(Files.readAllBytes(source), UTF_8));
        Optional<CompilationUnit> asyncUnit = make(syncUnit, parameters);

        if (asyncUnit.isPresent() == false) {
            return empty();
        }

        CompilationUnit compilationUnit = asyncUnit.get();
        Path destination = Paths.get(dirDestination.toString(), extractPackages(compilationUnit))
                .resolve(Paths.get(extractFileName(compilationUnit, source)));
        Files.createDirectories(destination.getParent());

        //FIXME: make me configuration
        Files.write(destination, compilationUnit.toString().getBytes(UTF_8), CREATE);
        return Optional.of(destination.toFile());
    }

    private String extractFileName(CompilationUnit compilationUnit, Path defaultValue) {
        return compilationUnit
                .getTypes()
                .stream()
                .filter(NodeWithPublicModifier::isPublic)
                .findFirst()
                .map(NodeWithSimpleName::getNameAsString)
                .map(name -> name + ".java")
                .orElseGet(() -> defaultValue.getFileName().toString());
    }

    private String[] extractPackages(CompilationUnit compilationUnit) {
        String packageName = compilationUnit
                .getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");
        return StringUtils.split(packageName, '.');
    }

}
