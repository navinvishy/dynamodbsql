package com.jobvite.dynamodbsql.translator.visitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * A visitor that finds the attributes in the where clauses to determine what kind of query can be used for DynamoDB
 * @author Navin.Viswanath
 *
 */
public class QueryOptimizingVisitor implements ExprVisitor<Void>{
    private Set<String> whereAttributes = new HashSet<>();
    @Override
    public Void visit(ComparisonExpr expr) {
        whereAttributes.add(expr.getKey());
        return null;
    }

    @Override
    public Void visit(FunctionExpr expr) {
        List<String> args = expr.getArgs();
        // The first arg always refers to a path
        if(args != null && args.size() > 0){
            whereAttributes.add(args.get(0));
        }
        return null;
    }

    @Override
    public Void visit(InExpr expr) {
        whereAttributes.add(expr.getKey());
        return null;
    }

    @Override
    public Void visit(NotExpr expr) {
        ConditionExpr child = expr.getConditionExpr();
        child.accept(this);
        return null;
    }

    @Override
    public Void visit(AndExpr expr) {
        ConditionExpr left = expr.getLeft();
        left.accept(this);
        ConditionExpr right = expr.getRight();
        right.accept(this);
        return null;
    }

    @Override
    public Void visit(OrExpr expr) {
        ConditionExpr left = expr.getLeft();
        left.accept(this);
        ConditionExpr right = expr.getRight();
        right.accept(this);
        return null;
    }

    public Set<String> getWhereAttributes() {
        return whereAttributes;
    }
    
}
