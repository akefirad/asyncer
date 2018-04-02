package com.akefirad.asyncer.api;

import com.github.javaparser.ast.CompilationUnit;

public interface SourceParser {

    CompilationUnit parse(String source);

}
