package com.jobvite.dynamodbsql.dynamo.model;

import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

public class AndExpr extends ConditionExpr {
    private ConditionExpr left;
    private ConditionExpr right;
    public AndExpr(ConditionExpr left, ConditionExpr right){
        this.left = left;
        this.right = right;
    }
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
    public ConditionExpr getLeft() {
        return left;
    }
    public void setLeft(ConditionExpr left) {
        this.left = left;
    }
    public ConditionExpr getRight() {
        return right;
    }
    public void setRight(ConditionExpr right) {
        this.right = right;
    }
    @Override
    public String toString() {
        return left.toString() + " AND " + right.toString();
    }
    @Override
    public boolean isAncestorOf(ConditionExpr expr) {
        if(expr == left || expr == right){
            return true;
        }
        return left.isAncestorOf(expr) || right.isAncestorOf(expr);
    }
}
