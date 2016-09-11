package com.jobvite.dynamodbsql.dynamo.model;

import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

/**
 * Represents an expression that defines the filter criteria in a DynamoDB query
 * @author Navin.Viswanath
 *
 */
public abstract class ConditionExpr {
    public abstract <T> T accept(ExprVisitor<T> visitor);
    public abstract boolean isAncestorOf(ConditionExpr expr);
}
