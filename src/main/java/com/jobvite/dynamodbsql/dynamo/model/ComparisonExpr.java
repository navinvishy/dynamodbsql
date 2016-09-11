package com.jobvite.dynamodbsql.dynamo.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

public class ComparisonExpr extends ConditionExpr {
    private String key;
    private ComparisonOperator op;
    private AttributeValue value;
    public ComparisonExpr(){
        
    }
    public ComparisonExpr(String key, ComparisonOperator op, AttributeValue value){
        this.key = key;
        this.op = op;
        this.value = value;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public ComparisonOperator getOp() {
        return op;
    }
    public void setOp(ComparisonOperator op) {
        this.op = op;
    }
    public AttributeValue getValue() {
        return value;
    }
    public void setValue(AttributeValue value) {
        this.value = value;
    }
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public String toString() {
        String literal = null;
        if(value.getN() != null){
            literal = value.getN();
        }
        if(value.getS() != null){
            literal = value.getS();
        }
        return key + " " + op.getSymbol() + " " + literal;
    }
    @Override
    public boolean isAncestorOf(ConditionExpr expr) {
        return false;
    }
}
