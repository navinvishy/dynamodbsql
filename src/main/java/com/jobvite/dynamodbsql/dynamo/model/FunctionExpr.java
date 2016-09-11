package com.jobvite.dynamodbsql.dynamo.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jobvite.dynamodbsql.translator.visitor.ExprVisitor;

public class FunctionExpr extends ConditionExpr {
    private String functionName;
    private List<String> args;
    public FunctionExpr(){
        
    }
    public FunctionExpr(String functionName, String... args){
        this.functionName = functionName;
        this.args = Arrays.asList(args);
    }
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
    public String getFunctionName() {
        return functionName;
    }
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    public List<String> getArgs() {
        return args;
    }
    public void setArgs(List<String> args) {
        this.args = args;
    }
    @Override
    public String toString() {
        String arrStr = args.toString();
        arrStr = StringUtils.stripStart(arrStr, "[");
        arrStr = StringUtils.stripEnd(arrStr, "]");
        return functionName + "(" + arrStr + ")";
    }
    @Override
    public boolean isAncestorOf(ConditionExpr expr) {
        return false;
    }
}
