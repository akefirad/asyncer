package com.akefirad.asyncer.util;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class SourceUtils {
    private static final ClassOrInterfaceType JAVA_LANG = new ClassOrInterfaceType(new ClassOrInterfaceType(null, "java"), "lang");
    private static final FieldAccessExpr JAVA_UTIL_OBJECT = new FieldAccessExpr(newExpressionScope("java.util"), "Objects");

    private static final Type BOXED_VOID = new ClassOrInterfaceType(null, Void.class.getSimpleName());
    private static final EnumSet<Modifier> NO_MODIFIERS = EnumSet.noneOf(Modifier.class);

    public static EnumSet<Modifier> toModifierEnumSet(@NonNull AccessSpecifier specifier) {
        return toModifier(specifier).map(EnumSet::of).orElse(NO_MODIFIERS);
    }

    private static Optional<Modifier> toModifier(@NonNull AccessSpecifier specifier) {
        return specifier == AccessSpecifier.DEFAULT ? Optional.empty() :
                Optional.of(Modifier.valueOf(specifier.toString()));
    }

    public static ClassOrInterfaceType toClassOrInterfaceType(@NonNull String name) {
        if (name.contains(".")) {
            String parent = StringUtils.substringBeforeLast(name, ".");
            String child = StringUtils.substringAfterLast(name, ".");
            return new ClassOrInterfaceType(toClassOrInterfaceType(parent), child);
        } else {
            return new ClassOrInterfaceType(null, name);
        }
    }

    public static ClassOrInterfaceType toGenericType(@NonNull Class<?> mainType, Type... genericTypes) {
        return toGenericType(mainType.getPackage().getName(), mainType.getSimpleName(), genericTypes);
    }

    public static ClassOrInterfaceType toGenericType(String packageName, String typeName, Type... genericTypes) {
        NodeList<Type> typeArguments = new NodeList<>(toBoxedTypes(genericTypes));
        return new ClassOrInterfaceType(toClassOrInterfaceType(packageName), new SimpleName(typeName), typeArguments);
    }

    private static List<Type> toBoxedTypes(Type[] types) {
        return stream(types).map(SourceUtils::toBoxedType).collect(toList());
    }

    public static Type toBoxedType(Type type) {
        return type.isVoidType() ? BOXED_VOID : type.isPrimitiveType() ? type.asPrimitiveType().toBoxedType() : type;
    }

    public static Expression newExpressionScope(@NonNull String name) {
        checkArgument(StringUtils.isNotBlank(name), "Name should not be blank!");
        if (name.contains(".")) {
            String parent = StringUtils.substringBeforeLast(name, ".");
            String child = StringUtils.substringAfterLast(name, ".");
            return new FieldAccessExpr(newExpressionScope(parent), child);
        } else {
            return new NameExpr(name);
        }
    }

    public static MethodCallExpr newRequireNonNullMethodCall(@NonNull Expression expression, @NonNull String message) {
        NodeList<Expression> arguments = new NodeList<>(expression, new StringLiteralExpr(message));
        return new MethodCallExpr(JAVA_UTIL_OBJECT, "requireNonNull", arguments);
    }

    public static ClassOrInterfaceType toClassOrInterfaceType(@NonNull SimpleName name) {
        return new ClassOrInterfaceType(null, name, null);
    }

    public static boolean isObjectMethod(@NonNull MethodDeclaration method) {
        return StringUtils.equalsAny(method.getNameAsString(), "clone", "equals", "finalize", "hashCode", "toString");
    }

    public static Type getFullyQualifiedNamedBoxedType(@NonNull Type type) {
        return type.isArrayType() ? toFullyQualifiedNamedArrayType((ArrayType) type) :
                type.isReferenceType() ? toFullyQualifiedNamedReferenceType((ReferenceType) type) : toFullyQualifiedNamedBoxedType(type);
    }

    public static Type getFullyQualifiedNamedType(@NonNull Type type) {
        return type.isArrayType() ? toFullyQualifiedNamedArrayType((ArrayType) type) :
                type.isReferenceType() ? toFullyQualifiedNamedReferenceType((ReferenceType) type) : type;
    }

    private static ArrayType toFullyQualifiedNamedArrayType(ArrayType type) {
        return new ArrayType(getFullyQualifiedNamedType(type.asArrayType().getComponentType()));
    }

    public static ReferenceType toFullyQualifiedNamedReferenceType(ReferenceType type) {
        return SourceUtils.toClassOrInterfaceType(tryResolveFullyQualifiedName(type));
    }

    private static String tryResolveFullyQualifiedName(Type type) {
        try {
            return type.resolve().describe();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to resolve " + type);
            e.printStackTrace();
            return type.asClassOrInterfaceType().getNameAsString();
        }
    }

    private static Type toFullyQualifiedNamedBoxedType(Type type) {
        checkArgument(type.isReferenceType() == false, "Invalid type: %s", type);
        return new ClassOrInterfaceType(JAVA_LANG, SourceUtils.toBoxedType(type).asString());
    }

    public static Name toName(@NonNull String name) {
        checkArgument(StringUtils.isNotBlank(name), "Name should not be blank!");
        if (name.contains(".")) {
            String parent = StringUtils.substringBeforeLast(name, ".");
            String child = StringUtils.substringAfterLast(name, ".");
            return new Name(toName(parent), child);
        } else {
            return new Name(name);
        }
    }

    public static AnnotationExpr newAnnotationWithValue(Name annotation, Optional<String> value) {
        return value.map(StringLiteralExpr::new)
                .<AnnotationExpr>map(expr -> new SingleMemberAnnotationExpr(annotation, expr))
                .orElseGet(() -> new MarkerAnnotationExpr(annotation));
    }

    public static boolean isPublicMethod(TypeDeclaration<?> type, MethodDeclaration method) {
        boolean isInterface = type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().isInterface();
        return method.isPublic() || isInterface && method.isPrivate() == false && method.isProtected() == false;
    }
}
