package com.akefirad.asyncer.spring;

import com.akefirad.asyncer.core.AbstractMethodAsyncer;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

public class SpringMethodAsyncer extends AbstractMethodAsyncer {

    @Override
    protected BlockStmt getBody(MethodDeclaration method, Options options) {
        NodeList<Statement> statements = new NodeList<>();

        boolean isVoid = method.getType().isVoidType();
        MethodCallExpr methodCall = newSyncMethodCallExpr(method, options);
        if (isVoid) {
            statements.add(new ExpressionStmt(methodCall));
            if (options.returnType() != SpringReturnType.VOID && options.noFutureVoid() == false) {
                statements.add(options.returnType().getReturnStatement(new NullLiteralExpr()));
            }
        } else {
            statements.add(options.returnType().getReturnStatement(methodCall));
        }

        return new BlockStmt(statements);
    }

}
