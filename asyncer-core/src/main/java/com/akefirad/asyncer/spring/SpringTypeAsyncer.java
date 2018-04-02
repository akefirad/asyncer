package com.akefirad.asyncer.spring;

import com.akefirad.asyncer.core.AbstractTypeAsyncer;
import com.akefirad.asyncer.util.SourceUtils;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;

import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SpringTypeAsyncer extends AbstractTypeAsyncer {
    private static final String DELEGATE = "delegate";

    protected NodeList<BodyDeclaration<?>> getMembers(ClassOrInterfaceDeclaration type, Options options) {
        return new NodeList<>(getDelegateFieldDeclaration(type, options), getConstructorDeclaration(type, options));
    }

    private FieldDeclaration getDelegateFieldDeclaration(ClassOrInterfaceDeclaration type, Options options) {
        ClassOrInterfaceType scope = SourceUtils.toClassOrInterfaceType(type.resolve().getPackageName());
        ClassOrInterfaceType delegateType = new ClassOrInterfaceType(scope, type.getName(), getTypeParameters(type));
        VariableDeclarator delegateVariable = new VariableDeclarator(delegateType, DELEGATE);
        return new FieldDeclaration(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL), delegateVariable);
    }

    private ConstructorDeclaration getConstructorDeclaration(ClassOrInterfaceDeclaration type, Options options) {
        ConstructorDeclaration constructor = new ConstructorDeclaration(getName(type, options));
        constructor.setModifiers(getAccessModifier(type, options));
        constructor.getParameters().add(getDelegateParameter(type));
        constructor.getBody().addStatement(new ExpressionStmt(getDelegateAssignExpr()));
        return constructor;
    }

    private EnumSet<Modifier> getAccessModifier(ClassOrInterfaceDeclaration type, Options options) {
        return options.accessSpecifier().isPresent() ?
                SourceUtils.toModifierEnumSet(options.accessSpecifier().get()) :
                SourceUtils.toModifierEnumSet(Modifier.getAccessSpecifier(type.getModifiers()));
    }

    private Parameter getDelegateParameter(ClassOrInterfaceDeclaration type) {
        ClassOrInterfaceType scope = SourceUtils.toClassOrInterfaceType(type.resolve().getPackageName());
        return new Parameter(new ClassOrInterfaceType(scope, type.getName(), getTypeParameters(type)), DELEGATE);
    }

    private AssignExpr getDelegateAssignExpr() {
        FieldAccessExpr delegateField = new FieldAccessExpr(new ThisExpr(), DELEGATE);
        MethodCallExpr requireNonNull = SourceUtils.newRequireNonNullMethodCall(new NameExpr(DELEGATE), DELEGATE);
        return new AssignExpr(delegateField, requireNonNull, AssignExpr.Operator.ASSIGN);
    }

    private NodeList<Type> getTypeParameters(ClassOrInterfaceDeclaration type) {
        List<Type> types = type.getTypeParameters().stream()
                .map(TypeParameter::getName)
                .map(SourceUtils::toClassOrInterfaceType)
                .collect(toList());
        return types.isEmpty() ? null : new NodeList<>(types);
    }

}
