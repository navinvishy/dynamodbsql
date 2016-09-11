package com.jobvite.dynamodbsql.translator.visitor;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * Determines if a QueryRequest can be used to answer the query
 * @author Navin.Viswanath
 *
 */
public class QueryRequestVisitor implements ExprVisitor<Boolean> {

    @Override
    public Boolean visit(ComparisonExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(FunctionExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(InExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(NotExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(AndExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(OrExpr expr) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
