package com.jobvite.dynamodbsql.dynamo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class DynamoDbManager {
    private AmazonDynamoDB dynamoDbClient;
    private DynamoDB dynamoDb;
    public DynamoDbManager(AmazonDynamoDB client){
        this.dynamoDbClient = client;
        this.dynamoDb = new DynamoDB(client);
    }
    /**
     * Get a list of tables
     * @return
     */
    public List<Table> getTables(){
        TableCollection<ListTablesResult> coll = dynamoDb.listTables();
        List<Table> tables = new ArrayList<>();
        Iterator<Table> it = coll.iterator();
        while(it.hasNext()){
            tables.add(it.next());
        }
        return tables;
    }
    /**
     * Get description for a table
     * @param table
     * @return
     */
    private TableDescription getKeys(Table table){
        return table.describe();
    }
}
