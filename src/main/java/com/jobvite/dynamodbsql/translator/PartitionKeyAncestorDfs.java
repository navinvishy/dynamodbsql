package com.jobvite.dynamodbsql.translator;

import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;

/**
 * Performs a DFS on the expr tree to find the parent and grandparent nodes of a ComparisonExpr involving the partition key and EQ ComparisonOp
 * @author Navin.Viswanath
 *
 */
public class PartitionKeyAncestorDfs {
    private AndExpr parent;
    private AndExpr grandParent;
    private ComparisonExpr expr;
    private String partitionKey;
    
    public PartitionKeyAncestorDfs(String partitionKey){
        this.partitionKey = partitionKey;
    }
    
    public void dfs(ConditionExpr root, AndExpr parent, AndExpr grandParent){
        if(root instanceof ComparisonExpr){
            ComparisonExpr comparisonExpr = (ComparisonExpr) root;
            if(comparisonExpr.getKey().equals(partitionKey) && comparisonExpr.getOp().equals(ComparisonOperator.EQ)){
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
