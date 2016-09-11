package com.jobvite.dynamodbsql.dynamo.model;

import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;

/**
 * Represents a DynamoDb query which could be a query request or a get item request or a scan request
 * Simply wraps the request
 * @author Navin.Viswanath
 *
 */
public class DynamoDbQuery {
    private QueryRequest queryRequest;
    private ScanRequest scanRequest;
    public DynamoDbQuery(QueryRequest request){
        this.queryRequest = request;
    }
    public DynamoDbQuery(ScanRequest request){
        this.scanRequest = request;
    }
    public boolean isQueryRequest(){
        return queryRequest != null;
    }
    public boolean isScanRequest(){
        return scanRequest != null;
    }
    public QueryRequest getQueryRequest() {
        return queryRequest;
    }
    public ScanRequest getScanRequest() {
        return scanRequest;
    }
    
}
