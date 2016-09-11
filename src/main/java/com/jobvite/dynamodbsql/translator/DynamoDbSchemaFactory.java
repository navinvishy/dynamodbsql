package com.jobvite.dynamodbsql.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
/**
 * Factory to create a DynamoDbSchema
 * @author Navin.Viswanath
 *
 */
public class DynamoDbSchemaFactory {

    public static DynamoDbSchema create(AmazonDynamoDBClient client){
        DynamoDbSchema schema = new DynamoDbSchema();
        ListTablesResult listTablesResult = client.listTables();
        List<String> tableNames = listTablesResult.getTableNames();
        Map<String,TableDescription> tableDescriptionMap = new HashMap<>();
        for(String tableName : tableNames){
            DescribeTableRequest request = new DescribeTableRequest(tableName);
            DescribeTableResult result = client.describeTable(request);
            tableDescriptionMap.put(tableName, result.getTable());
        }
        schema.setTableDescriptionMap(tableDescriptionMap);
        return schema;
    }
}
