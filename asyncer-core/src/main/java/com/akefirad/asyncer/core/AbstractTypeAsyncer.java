package com.akefirad.asyncer.core;

import com.akefirad.asyncer.api.TypeAsyncer;
import com.akefirad.asyncer.util.JavaUtils;
import com.akefirad.asyncer.util.SourceUtils;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractTypeAsyncer implements TypeAsyncer {

    @Override
    public Optional<ClassOrInterfaceDeclaration> make(ClassOrInterfaceDeclaration type, Options options) {
        if (options.typePredicate().test(type)) {
            return Optional.of(doMake(type, options));
        } else {
            return Optional.empty();
        }
    }

    protected ClassOrInterfaceDeclaration doMake(ClassOrInterfaceDeclaration type, Options options) {
        ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration()
                .setAnnotations(new NodeList<>(options.annotations()))
                .setModifiers(getAccessModifier(type, options))
                .setTypeParameters(type.getTypeParameters())
                .setName(getName(type, options))
                .setMembers(getMembers(type, options));

        return result;
    }

    private EnumSet<Modifier> getAccessModifier(ClassOrInterfaceDeclaration type, Options options) {
        return options.accessSpecifier().isPresent() ?
                SourceUtils.toModifierEnumSet(options.accessSpecifier().get()) :
                SourceUtils.toModifierEnumSet(Modifier.getAccessSpecifier(type.getModifiers()));
    }

    protected String getName(ClassOrInterfaceDeclaration type, Options options) {
        String name = JavaUtils.newStrSubstitutor(options.asyncerContext().names()).replace(options.namePattern());
        checkArgument(StringUtils.isNotBlank(name), "Type name cannot be blank: %s", name);
        return name;
    }

    protected abstract NodeList<BodyDeclaration<?>> getMembers(ClassOrInterfaceDeclaration type, Options options);

}
