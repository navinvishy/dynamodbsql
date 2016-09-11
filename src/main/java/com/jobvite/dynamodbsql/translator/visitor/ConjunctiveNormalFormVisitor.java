package com.jobvite.dynamodbsql.translator.visitor;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * Converts an expr into CNF
 * @author Navin.Viswanath
 *
 */
public class ConjunctiveNormalFormVisitor implements ExprVisitor<ConditionExpr>{

    @Override
    public ConditionExpr visit(ComparisonExpr expr) {
        return expr;
    }

    @Override
    public ConditionExpr visit(FunctionExpr expr) {
        return expr;
    }

    @Override
    public ConditionExpr visit(InExpr expr) {
        return expr;
    }

    @Override
    public ConditionExpr visit(NotExpr expr) {
        ConditionExpr child = expr.getConditionExpr();
        ConditionExpr cnfChild = child.accept(this);
        if(cnfChild instanceof AndExpr){
            AndExpr andChild = (AndExpr) cnfChild;
            return new OrExpr(new NotExpr(andChild.getLeft()), new NotExpr(andChild.getRight()));
        }
        if(cnfChild instanceof OrExpr){
            OrExpr orChild = (OrExpr) cnfChild;
            return new AndExpr(new NotExpr(orChild.getLeft()), new NotExpr(orChild.getRight()));
        }
        if(cnfChild instanceof NotExpr){
            NotExpr cnfNotChild = (NotExpr)cnfChild;
            return cnfNotChild.getConditionExpr();
        }
        return expr;
    }

    @Override
    public ConditionExpr visit(AndExpr expr) {
        ConditionExpr cnfLeft = expr.getLeft().accept(this);
        ConditionExpr cnfRight = expr.getRight().accept(this);
        return new AndExpr(cnfLeft,cnfRight);
    }

    @Override
    public ConditionExpr visit(OrExpr expr) {
        ConditionExpr left = expr.getLeft().accept(this);
        ConditionExpr right = expr.getRight().accept(this);
        //form A v (B ^ C) = (A v B) ^ (A v C)
        if(right instanceof AndExpr){
            AndExpr andRight = (AndExpr) right;
            return new AndExpr(new OrExpr(left, andRight.getLeft()), new OrExpr(left, andRight.getRight()));
        } else if(left instanceof AndExpr){
            //form (A ^ B) v C = (A v C) ^ (B v C)
            AndExpr andLeft = (AndExpr) left;
            return new AndExpr(new OrExpr(andLeft.getLeft(),right), new OrExpr(andLeft.getRight(),right));
        } else {
            return new OrExpr(left,right);
        }
    }
    
}
