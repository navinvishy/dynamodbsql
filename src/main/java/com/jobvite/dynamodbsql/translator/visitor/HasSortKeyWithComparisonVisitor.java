package com.jobvite.dynamodbsql.translator.visitor;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * A visitor to determine if the expr contains a ComparisonExpr with the sort key of the table as the key
 * and EQ,GT,GE,LT, or LE as the comparison operator. The ComparisonExpr must either be the root or only have AND exprs as ancestors
 * @author Navin.Viswanath
 *
 */
public class HasSortKeyWithComparisonVisitor implements ExprVisitor<Boolean> {
    private String sortKey;
    
    public HasSortKeyWithComparisonVisitor(String sortKey) {
        this.sortKey = sortKey;
    }
    @Override
    public Boolean visit(ComparisonExpr expr) {
        return expr.getKey().equals(sortKey) && (expr.getOp().equals(ComparisonOperator.EQ)
                || expr.getOp().equals(ComparisonOperator.GE)
                || expr.getOp().equals(ComparisonOperator.GT)
                || expr.getOp().equals(ComparisonOperator.LE)
                || expr.getOp().equals(ComparisonOperator.LT));
    }

    @Override
    public Boolean visit(FunctionExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(InExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(NotExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(AndExpr expr) {
        return expr.getLeft().accept(this) || expr.getRight().accept(this);
    }

    @Override
    public Boolean visit(OrExpr expr) {
        return false;
    }
    
}
