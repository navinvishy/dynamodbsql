package com.jobvite.dynamodbsql.dynamo.model;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.TableDescription;

/**
 * Schema for a DynamoDB "database"
 * @author Navin.Viswanath
 *
 */
public class DynamoDbSchema {
    private Map<String,TableDescription> tableDescriptionMap;
    public Map<String, TableDescription> getTableDescriptionMap() {
        return tableDescriptionMap;
    }
    public void setTableDescriptionMap(Map<String, TableDescription> tableDescriptionMap) {
        this.tableDescriptionMap = tableDescriptionMap;
    }
    
}
