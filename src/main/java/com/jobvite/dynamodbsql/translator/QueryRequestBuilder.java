package com.jobvite.dynamodbsql.translator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQuery;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
import com.jobvite.dynamodbsql.translator.visitor.ExprNameGeneratingVisitor;
import com.jobvite.dynamodbsql.translator.visitor.FilterExpressionGeneratingVisitor;
import com.jobvite.dynamodbsql.translator.visitor.HasPartitionKeyWithEqualsVisitor;
import com.jobvite.dynamodbsql.translator.visitor.HasSortKeyWithComparisonVisitor;

/**
 * Optimizes the query to determine whether to make a QueryRequest or ScanRequest
 * @author Navin.Viswanath
 *
 */
public class QueryRequestBuilder implements RequestBuilder {
    
    public DynamoDbQuery build(DynamoDbQueryModel model, DynamoDbSchema schema){
        // First pass through name generating visitor
        ExprNameGeneratingVisitor exprNameGeneratingVisitor = new ExprNameGeneratingVisitor();
        ConditionExpr expr = model.getConditions();
        expr.accept(exprNameGeneratingVisitor);
        // Second pass through filter expression generating visitor
        FilterExpressionGeneratingVisitor filterExpressionGeneratingVisitor = new FilterExpressionGeneratingVisitor(exprNameGeneratingVisitor.getExprAttributeNames());
        String tableName = model.getTableName();
        String partitionKey = getPartitionKey(tableName, schema);
        String sortKey = getSortKey(tableName, schema);
        List<String> projections = model.getProjectionList();
        if(usesPartitionKey(partitionKey, expr, schema)){
            PartitionKeyAncestorDfs partitionKeyAncestorDfs = new PartitionKeyAncestorDfs(partitionKey);
            partitionKeyAncestorDfs.dfs(expr, null, null);
            if(partitionKeyAncestorDfs.getParent() != null){
                ConditionExpr keyExpr = null;
                AndExpr nonKeyExpr = null;
                // It is part of an AND, so its possible that it makes use of the sort key as well...
                if(usesSortKey(sortKey, expr, schema)){
                    SortKeyAncestorDfs sortKeyAncestorDfs = new SortKeyAncestorDfs(sortKey);
                    sortKeyAncestorDfs.dfs(expr, null, null);
                    groupKeyExprs(partitionKeyAncestorDfs, sortKeyAncestorDfs);
                    // Grouping might have changed structure, find key exprs again
                    partitionKeyAncestorDfs.dfs(expr, null, null);
                    keyExpr = partitionKeyAncestorDfs.getParent();
                    nonKeyExpr = partitionKeyAncestorDfs.getGrandParent();
                    if(nonKeyExpr != null){
                        if(nonKeyExpr.getLeft() == keyExpr){
                            nonKeyExpr.setLeft(null);
                        } else {
                            nonKeyExpr.setRight(null);
                        }
                    }
                } else {
                    // Primary key is part of an AND but does not involve sort key, need to cut the ComparisonExpr by itself
                    keyExpr = partitionKeyAncestorDfs.getExpr();
                    nonKeyExpr = partitionKeyAncestorDfs.getParent();
                    if(nonKeyExpr.getLeft() == keyExpr){
                        nonKeyExpr.setLeft(null);
                    } else {
                        nonKeyExpr.setRight(null);
                    }
                }
                  
                String keyExprStr = keyExpr.accept(filterExpressionGeneratingVisitor);
                QueryRequest request = new QueryRequest();
                request.setTableName(tableName);
                request.setKeyConditionExpression(keyExprStr);
                if(nonKeyExpr != null){
                    String nonKeyExprStr = nonKeyExpr.accept(filterExpressionGeneratingVisitor);
                    request.setFilterExpression(nonKeyExprStr);
                }
                request.setExpressionAttributeValues(exprNameGeneratingVisitor.getExprAttributeValues());
                if(projections != null && !projections.contains("*")){
                    request.setProjectionExpression(StringUtils.join(projections, ","));
                }
                return new DynamoDbQuery(request);
            } else {
                // It is a comparison expr by itself
                ComparisonExpr comparisonExpr = partitionKeyAncestorDfs.getExpr();
                String comparisonExprStr = comparisonExpr.accept(filterExpressionGeneratingVisitor);
                QueryRequest request = new QueryRequest();
                request.setTableName(tableName);
                request.setKeyConditionExpression(comparisonExprStr);
                request.setExpressionAttributeValues(exprNameGeneratingVisitor.getExprAttributeValues());
                if(projections != null && !projections.contains("*")){
                    request.setProjectionExpression(StringUtils.join(projections, ","));
                }
                return new DynamoDbQuery(request);
            }
        } else {
            // If the query does not use a partition key, fall back to generating a scan request
            ScanRequestBuilder builder = new ScanRequestBuilder();
            return builder.build(model, schema);
        }
    }
    /**
     * Group key exprs under the same AndExpr node. This is done by swapping out children of corresponding AndExpr nodes.
     * NOTE: this does not change semantics, since we're already assured that there are only AndNode exprs on the path from root to these nodes
     * @param partitionKeyAncestorDfs
     * @param sortKeyAncestorDfs
     */
    public void groupKeyExprs(PartitionKeyAncestorDfs partitionKeyAncestorDfs, SortKeyAncestorDfs sortKeyAncestorDfs){
        ComparisonExpr partitionKeyExpr = partitionKeyAncestorDfs.getExpr();
        AndExpr partitionKeyExprParent = partitionKeyAncestorDfs.getParent();
        ComparisonExpr sortKeyExpr = sortKeyAncestorDfs.getExpr();
        AndExpr sortKeyExprParent = sortKeyAncestorDfs.getParent();
        ConditionExpr partitionNonKeyExpr = null;
        if(partitionKeyExprParent.getLeft() == partitionKeyExpr){
            partitionNonKeyExpr = partitionKeyExprParent.getRight();
            if(!partitionNonKeyExpr.isAncestorOf(sortKeyExpr)){
                if(sortKeyExprParent.getLeft() == sortKeyExpr){
                    partitionKeyExprParent.setRight(sortKeyExprParent.getLeft());
                    sortKeyExprParent.setLeft(partitionNonKeyExpr);   
                } else {
                    // The sort key expr is the right child
                    partitionKeyExprParent.setRight(sortKeyExprParent.getRight());
                    sortKeyExprParent.setRight(partitionNonKeyExpr);   
                }
            } else {
                if(sortKeyExprParent.getLeft() == sortKeyExpr){
                    partitionKeyExprParent.setLeft(sortKeyExprParent.getRight());
                    sortKeyExprParent.setRight(partitionKeyExpr);
                } else {
                    partitionKeyExprParent.setLeft(sortKeyExprParent.getLeft());
                    sortKeyExprParent.setLeft(partitionKeyExpr);
                }
            }
        } else {
            partitionNonKeyExpr = partitionKeyExprParent.getLeft();
            if(!partitionNonKeyExpr.isAncestorOf(sortKeyExpr)){
                if(sortKeyExprParent.getLeft() == sortKeyExpr){
                    partitionKeyExprParent.setLeft(sortKeyExprParent.getLeft());
                    sortKeyExprParent.setLeft(partitionNonKeyExpr);   
                } else {
                    // The sort key expr is the right child
                    partitionKeyExprParent.setLeft(sortKeyExprParent.getRight());
                    sortKeyExprParent.setRight(partitionNonKeyExpr);   
                }
            } else {
                if(sortKeyExprParent.getLeft() == sortKeyExpr){
                    partitionKeyExprParent.setRight(sortKeyExprParent.getRight());
                    sortKeyExprParent.setRight(partitionKeyExpr);
                } else {
                    partitionKeyExprParent.setRight(sortKeyExprParent.getLeft());
                    sortKeyExprParent.setLeft(partitionKeyExpr);
                }
            }
        }      
    }
    /**
     * Get sort key for a table if it has one, null otherwise
     * @param tableName
     * @param schema
     * @return
     */
    public String getSortKey(String tableName, DynamoDbSchema schema){
        Map<String,TableDescription> tableMap = schema.getTableDescriptionMap();
        TableDescription tableDescription = tableMap.get(tableName);
        List<KeySchemaElement> keySchemaElements = tableDescription.getKeySchema();
        List<String> sortKeys = keySchemaElements.stream()
                .filter(k -> k.getKeyType().equals(KeyType.RANGE.name()))
                .map(k -> k.getAttributeName()).collect(Collectors.toList());
        String sortKey = null;
        if(sortKeys != null && sortKeys.size() > 0){
            sortKey = sortKeys.get(0);
        }
        return sortKey;
    }
    /**
     * Get partition key for table
     * @param tableName
     * @param schema
     * @return
     */
    public String getPartitionKey(String tableName, DynamoDbSchema schema){
        Map<String,TableDescription> tableMap = schema.getTableDescriptionMap();
        TableDescription tableDescription = tableMap.get(tableName);
        List<KeySchemaElement> keySchemaElements = tableDescription.getKeySchema();
        List<String> partitionKeys = keySchemaElements.stream().filter(k -> k.getKeyType().equals(KeyType.HASH.name())).map(k -> k.getAttributeName()).collect(Collectors.toList());
        return partitionKeys.get(0);
    }
    /**
     * Determine whether the expr uses the partition key of the table
     * @param model
     * @param schema
     * @return
     */
    public boolean usesPartitionKey(String partitionKey, ConditionExpr expr, DynamoDbSchema schema){
        HasPartitionKeyWithEqualsVisitor partitionKeyVisitor = new HasPartitionKeyWithEqualsVisitor(partitionKey);
        return expr.accept(partitionKeyVisitor);
    }
    /**
     * Determine whether the expr uses the sort key of the table
     * @param model
     * @param schema
     * @return
     */
    public boolean usesSortKey(String sortKey, ConditionExpr expr, DynamoDbSchema schema){
        HasSortKeyWithComparisonVisitor sortKeyVisitor = new HasSortKeyWithComparisonVisitor(sortKey);
        return expr.accept(sortKeyVisitor);
    }
}
