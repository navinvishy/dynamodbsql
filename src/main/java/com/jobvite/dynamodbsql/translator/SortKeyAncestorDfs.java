package com.jobvite.dynamodbsql.translator;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;

/**
 * Performs a DFS on the expr tree to find the parent and the grandparent of a ComparisonExpr node involving the sort key and a ComparisonOp
 * of EQ,GT,GE,LT, or LE
 * @author Navin.Viswanath
 *
 */
public class SortKeyAncestorDfs {
    private String sortKey;
    private AndExpr parent;
    private AndExpr grandParent;
    private ComparisonExpr expr;
    
    public SortKeyAncestorDfs(String sortKey){
        this.sortKey = sortKey;
    }
    
    public void dfs(ConditionExpr root, AndExpr parent, AndExpr grandParent){
        if(root instanceof ComparisonExpr){
            ComparisonExpr comparisonExpr = (ComparisonExpr) root;
            if(comparisonExpr.getKey().equals(sortKey) && (
                    comparisonExpr.getOp().equals(ComparisonOperator.EQ)
                    || comparisonExpr.getOp().equals(ComparisonOperator.GE)
                    || comparisonExpr.getOp().equals(ComparisonOperator.GT)
                    || comparisonExpr.getOp().equals(ComparisonOperator.LE)
                    || comparisonExpr.getOp().equals(ComparisonOperator.LT)
                    )){
                this.parent = parent;
                this.grandParent = grandParent;
                this.expr = comparisonExpr;
            }
        }
        if(root instanceof AndExpr){
            AndExpr andExpr = (AndExpr) root;
            grandParent = parent;
            parent = andExpr;
            dfs(andExpr.getLeft(), parent, grandParent);
            dfs(andExpr.getRight(), parent, grandParent);
        }
    }

    public AndExpr getParent() {
        return parent;
    }

    public AndExpr getGrandParent() {
        return grandParent;
    }

    public ComparisonExpr getExpr() {
        return expr;
    }
    
}
