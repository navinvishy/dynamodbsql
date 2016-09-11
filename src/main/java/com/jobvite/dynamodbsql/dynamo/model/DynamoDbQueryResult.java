package com.jobvite.dynamodbsql.dynamo.model;

import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class DynamoDbQueryResult {
    private GetItemResult getItemResult;
    private QueryResult queryResult;
    private ScanResult scanResult;
    public DynamoDbQueryResult(GetItemResult result){
        this.getItemResult = result;
    }
    public DynamoDbQueryResult(QueryResult result){
        this.queryResult = result;
    }
    public DynamoDbQueryResult(ScanResult result){
        this.scanResult = result;
    }
    public boolean isGetItemResult(){
        return getItemResult != null;
    }
    public boolean isQueryResult(){
        return queryResult != null;
    }
    public boolean isScanResult(){
        return scanResult != null;
    }
}
