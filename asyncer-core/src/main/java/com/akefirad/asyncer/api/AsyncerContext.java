package com.akefirad.asyncer.api;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.text.StrLookup;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;

@Data
@ToString
@Accessors(fluent = true)
@Builder(builderMethodName = "emptyBuilder", toBuilder = true)
public class AsyncerContext {

    @NonNull
    private final Optional<PackageDeclaration> originalPackage;

    @NonNull
    private final Optional<PackageDeclaration> generatedPackage;

    @NonNull
    private final Optional<TypeDeclaration<?>> originalType;

    @NonNull
    private final Optional<TypeDeclaration<?>> generatedType;

    @NonNull
    private final Optional<MethodDeclaration> originalMethod;

    @NonNull
    private final Optional<MethodDeclaration> generatedMethod;

    public static AsyncerContextBuilder builder() {
        return AsyncerContext.emptyBuilder()
                .originalPackage(empty())
                .originalType(empty())
                .originalMethod(empty())
                .generatedPackage(empty())
                .generatedType(empty())
                .generatedMethod(empty())
                ;
    }

    public StrLookup<String> names() {
        return new StrLookup<String>() {
            @Override
            public String lookup(String key) {
                switch (key) {
                    case "original.package.name":
                        return originalPackage.orElseThrow(valueNotSetException("original package")).getNameAsString();
                    case "generated.package.name":
                        return generatedPackage.orElseThrow(valueNotSetException("generated package")).getNameAsString();
                    case "original.type.name":
                        return originalType.orElseThrow(valueNotSetException("original type")).getNameAsString();
                    case "generated.type.name":
                        return generatedType.orElseThrow(valueNotSetException("generated type")).getNameAsString();
                    case "original.method.name":
                        return originalMethod.orElseThrow(valueNotSetException("original method")).getNameAsString();
                    case "generated.method.name":
                        return generatedMethod.orElseThrow(valueNotSetException("generated method")).getNameAsString();
                    default:
                        throw new IllegalArgumentException("Invalid variable: " + key);
                }
            }
        };
    }

    private Supplier<IllegalArgumentException> valueNotSetException(String message) {
        return () -> new IllegalArgumentException(message + " not set!");
    }

}
