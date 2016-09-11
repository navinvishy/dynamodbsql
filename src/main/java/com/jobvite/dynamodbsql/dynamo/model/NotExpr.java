package com.jobvite.dynamodbsql.dynamo.model;

import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

public class NotExpr extends ConditionExpr {
    private ConditionExpr conditionExpr;
    public NotExpr(ConditionExpr expr){
        this.conditionExpr = expr;
    }
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
    public ConditionExpr getConditionExpr() {
        return conditionExpr;
    }
    public void setConditionExpr(ConditionExpr conditionExpr) {
        this.conditionExpr = conditionExpr;
    }
    @Override
    public String toString() {
        return "NOT ( " + conditionExpr.toString() + " )";
    }
    @Override
    public boolean isAncestorOf(ConditionExpr expr) {
        if(expr == conditionExpr){
            return true;
        }
        return conditionExpr.isAncestorOf(expr);
    }
}
