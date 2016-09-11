package com.jobvite.dynamodbsql.translator.visitor;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;

public class FilterExpressionGeneratingVisitor implements ExprVisitor<String> {
    private Map<ConditionExpr,List<String>> exprAttributeNames;
    
    public FilterExpressionGeneratingVisitor(Map<ConditionExpr,List<String>> exprAttributeNames) {
        this.exprAttributeNames = exprAttributeNames;
    }
    @Override
    public String visit(ComparisonExpr expr) {
        String key = expr.getKey();
        List<String> values = exprAttributeNames.get(expr);
        String value = null;
        if(values != null && values.size() == 1){
            value = values.get(0);
        }
        return key.concat(StringUtils.SPACE).concat(expr.getOp().getSymbol()).concat(value);
    }

    @Override
    public String visit(FunctionExpr expr) {
        String key = expr.getFunctionName();
        StringBuilder builder = new StringBuilder();
        builder.append(key).append("(");
        String firstArg = null;
        List<String> args = expr.getArgs();
        if(args != null && args.size() > 0){
            firstArg = args.get(0);
            firstArg = StringUtils.stripStart(firstArg, "'");
            firstArg = StringUtils.stripEnd(firstArg, "'");
            builder.append(firstArg);
        }
        List<String> argValue = exprAttributeNames.get(expr);
        if(argValue != null){
            builder.append(",");
            builder.append(argValue.get(0));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visit(InExpr expr) {
        StringBuilder builder = new StringBuilder();
        builder.append(expr.getKey());
        builder.append(" IN ").append("(");
        List<String> names = exprAttributeNames.get(expr);
        String comma = "";
        for(String name : names){
            builder.append(comma).append(name);
            comma = ",";
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visit(NotExpr expr) {
        StringBuilder builder = new StringBuilder();
        if(expr != null){
            builder.append("NOT (");
            builder.append(expr.getConditionExpr().accept(this));
            builder.append(")");
        }
        return builder.toString();
    }

    @Override
    public String visit(AndExpr expr) {
        StringBuilder builder = new StringBuilder();
        if(expr != null){
            if(expr.getLeft() == null && expr.getRight() != null){
                builder.append(expr.getRight().accept(this));
            } else if(expr.getLeft() != null && expr.getRight() == null){
                builder.append(expr.getLeft().accept(this));
            } else {
                builder.append(expr.getLeft().accept(this)).append(" AND ").append(expr.getRight().accept(this));
            }
        }
        return builder.toString();
    }

    @Override
    public String visit(OrExpr expr) {
        StringBuilder builder = new StringBuilder();
        if(expr != null){
            if(expr.getLeft() == null && expr.getRight() != null){
                builder.append(expr.getRight().accept(this));
            }
            if(expr.getLeft() != null && expr.getRight() == null){
                builder.append(expr.getLeft().accept(this));
            } else {
                builder.append(expr.getLeft().accept(this)).append(" OR ").append(expr.getRight().accept(this));
            }
        }
        return builder.toString();
    }
    
}
