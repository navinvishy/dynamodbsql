package com.jobvite.dynamodbsql.translator.visitor;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;

/**
 * A visitor to determine if the expr contains a ComparisonExpr with the partition key of the table as the key
 * and EQ as the comparison operator. The ComparisonExpr must either be the root or only have AND exprs as ancestors
 * @author Navin.Viswanath
 *
 */
public class HasPartitionKeyWithEqualsVisitor implements ExprVisitor<Boolean>{
    private String partitionKey;
    
    public HasPartitionKeyWithEqualsVisitor(String partitionKey) {
        this.partitionKey = partitionKey;
    }
    
    @Override
    public Boolean visit(ComparisonExpr expr) {
        return expr.getKey().equals(partitionKey) && expr.getOp().equals(ComparisonOperator.EQ);
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
