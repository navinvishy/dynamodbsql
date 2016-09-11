package com.jobvite.dynamodbsql.dynamo.model;

import java.util.List;

/**
 * A representation of the parts of a query: the table name, the projection list, and the where conditions
 * @author Navin.Viswanath
 *
 */
public class DynamoDbQueryModel {
    private List<String> projectionList;
    private ConditionExpr conditions;
    private String tableName;
    private String indexName;
    public DynamoDbQueryModel(){
        
    }
    public DynamoDbQueryModel(String tableName, List<String> projectionList, ConditionExpr conditions){
        this.tableName = tableName;
        this.projectionList = projectionList;
        this.conditions = conditions;
    }
    public DynamoDbQueryModel(String tableName, String indexName, List<String> projectionList, ConditionExpr conditions){
        this.indexName = indexName;
        this.tableName = tableName;
        this.projectionList = projectionList;
        this.conditions = conditions;
    }
    @Override
    public String toString() {
        return "DynamoDbQueryModel [projectExpression=" + projectionList + ", conditions=" + conditions
                + ", tableName=" + tableName + "]";
    }
    public ConditionExpr getConditions() {
        return conditions;
    }
    public void setConditions(ConditionExpr conditions) {
        this.conditions = conditions;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<String> getProjectionList() {
        return projectionList;
    }
    public void setProjectionList(List<String> projectionList) {
        this.projectionList = projectionList;
    }
    public String getIndexName() {
        return indexName;
    }
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
}
