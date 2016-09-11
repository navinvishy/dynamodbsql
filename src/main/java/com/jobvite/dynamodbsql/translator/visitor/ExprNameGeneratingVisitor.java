package com.jobvite.dynamodbsql.translator.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * A visitor to generate filter expressions
 * @author Navin.Viswanath
 *
 */
public class ExprNameGeneratingVisitor implements ExprVisitor<Void>{
    private Map<ConditionExpr,List<String>> exprAttributeNames = new HashMap<>();
    private Map<String,AttributeValue> exprAttributeValues = new HashMap<>();
    private int counter;
    
    @Override
    public Void visit(ComparisonExpr expr) {
        String exprName = ":".concat(expr.getKey()).concat("_").concat(Integer.toString(counter));
        counter++;
        exprAttributeNames.put(expr, Collections.singletonList(exprName));
        exprAttributeValues.put(exprName, expr.getValue());
        return null;
    }

    @Override
    public Void visit(FunctionExpr expr) {
        List<String> args = expr.getArgs();
        if(args != null && args.size() > 1){
            AttributeValue value = new AttributeValue();
            String arg = args.get(1);
            String name = args.get(0);
            name = StringUtils.stripStart(name, "'");
            name = StringUtils.stripEnd(name, "'");
            arg = StringUtils.stripStart(arg, "'");
            arg = StringUtils.stripEnd(arg, "'");
            String exprName = ":".concat(name).concat("_").concat(Integer.toString(counter));
            counter++;
            // Function arguments can only be of String type
            value.withS(arg);
            exprAttributeNames.put(expr, Collections.singletonList(exprName));
            exprAttributeValues.put(exprName, value);
        }
        return null;
    }

    @Override
    public Void visit(InExpr expr) {
        List<String> exprNames = new ArrayList<>();
        for(AttributeValue value : expr.getValues()){
            String exprName = ":".concat(expr.getKey()).concat("_").concat(Integer.toString(counter));
            exprNames.add(exprName);
            counter++;
            exprAttributeValues.put(exprName, value);
        }
        exprAttributeNames.put(expr, exprNames);
        return null;
    }

    @Override
    public Void visit(NotExpr expr) {
        expr.getConditionExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(AndExpr expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return null;
    }

    @Override
    public Void visit(OrExpr expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return null;
    }

    public Map<ConditionExpr, List<String>> getExprAttributeNames() {
        return exprAttributeNames;
    }

    public Map<String, AttributeValue> getExprAttributeValues() {
        return exprAttributeValues;
    }
    
}
