package com.akefirad.asyncer.api;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;

public interface ReturnType {

    Type getReturnType(Type type);

    ReturnStmt getReturnStatement(Expression expression);

}
