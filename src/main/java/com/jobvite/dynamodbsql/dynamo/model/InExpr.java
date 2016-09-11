package com.jobvite.dynamodbsql.dynamo.model;

import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

public class InExpr extends ConditionExpr {
    private String key;
    private List<AttributeValue> values;
    public InExpr(){
    }
    public InExpr(String key, List<AttributeValue> values){
        this.key = key;
        this.values = values;
    }
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public List<AttributeValue> getValues() {
        return values;
    }
    public void setValues(List<AttributeValue> values) {
        this.values = values;
    }
    @Override
    public String toString() {
        String valueStr = "";
        String delimiter = "";
        for(AttributeValue value : values){
            valueStr = valueStr.concat(delimiter).concat(value.toString());
            delimiter = ",";
        }
        return key + "IN (" + valueStr + ")";
    }
    @Override
    public boolean isAncestorOf(ConditionExpr expr) {
        return false;
    }
}
