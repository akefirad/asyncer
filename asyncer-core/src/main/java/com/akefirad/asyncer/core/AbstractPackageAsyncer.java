package com.akefirad.asyncer.core;

import com.akefirad.asyncer.api.PackageAsyncer;
import com.akefirad.asyncer.util.JavaUtils;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractPackageAsyncer implements PackageAsyncer {

    @Override
    public Optional<CompilationUnit> make(CompilationUnit unit, Options options) {
        if (options.packagePredicate().test(unit)) {
            return Optional.of(doMake(unit, options));
        } else {
            return Optional.empty();
        }
    }

    protected CompilationUnit doMake(CompilationUnit synced, Options options) {
        CompilationUnit asynced = new CompilationUnit();

        asynced.setPackageDeclaration(generatedPackageName(synced, options));
        asynced.setImports(synced.getImports());

        return asynced;
    }

    protected String generatedPackageName(CompilationUnit unit, Options options) {
        String name = JavaUtils.newStrSubstitutor(options.asyncerContext().names()).replace(options.namePattern());
        checkArgument(StringUtils.isNotBlank(name), "Package name cannot be blank: %s", name);
        return name;
    }

}
