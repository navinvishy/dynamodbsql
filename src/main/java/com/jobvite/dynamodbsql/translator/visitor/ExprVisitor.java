package com.jobvite.dynamodbsql.translator.visitor;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * Visitor pattern for ConditionExpr. Use java.lang.Void as type parameter if you dont want to return anything
 * @author Navin.Viswanath
 *
 * @param <T>
 */
public interface ExprVisitor<T> {
    public T visit(ComparisonExpr expr);
    public T visit(FunctionExpr expr);
    public T visit(InExpr expr);
    public T visit(NotExpr expr);
    public T visit(AndExpr expr);
    public T visit(OrExpr expr);
}
