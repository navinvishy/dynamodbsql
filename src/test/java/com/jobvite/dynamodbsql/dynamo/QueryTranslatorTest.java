package com.jobvite.dynamodbsql.dynamo;

import org.junit.Test;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQuery;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
import com.jobvite.dynamodbsql.translator.DynamoDbSchemaFactory;
import com.jobvite.dynamodbsql.translator.QueryRequestBuilder;
import com.jobvite.dynamodbsql.translator.QueryTranslator;

import junit.framework.Assert;

public class QueryTranslatorTest {
    private AmazonDynamoDBClient client;
    private DynamoDbSchema schema;
    {
        DefaultAWSCredentialsProviderChain credentials = new DefaultAWSCredentialsProviderChain();
        client = new AmazonDynamoDBClient(credentials);
        schema = DynamoDbSchemaFactory.create(client);
    }
    @Test
    public void testOnlyPartitionKey(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from \"navin-books\" where  isbn = '12345'", schema);
        Assert.assertEquals(query.isQueryRequest(), true);
        Assert.assertEquals("isbn =:isbn_0", query.getQueryRequest().getKeyConditionExpression());
        Assert.assertNull(query.getQueryRequest().getFilterExpression());
    }
    @Test
    public void testNoPartitionKey(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from \"navin-books\" where  author = '12345'", schema);
        Assert.assertEquals(query.isScanRequest(), true);
        Assert.assertEquals("author =:author_0", query.getScanRequest().getFilterExpression());
    }
    @Test
    public void testQuery(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from praveenrequestLog where leaseKey='shardId-000000000000'", schema);
            if(query.isQueryRequest()){
            System.out.println("Query = " + query.getQueryRequest());
            QueryResult result = client.query(query.getQueryRequest());
            System.out.println(result);
        } else {
            System.out.println("Scan = " + query.getScanRequest());
            ScanResult result = client.scan(query.getScanRequest());
            System.out.println(result);
        }
    }
    @Test
    public void testAndQuery(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from \"navin-books\" where  isbn = '12345' and author = 'a'", schema);
        Assert.assertEquals(query.isQueryRequest(), true);
        Assert.assertEquals("isbn =:isbn_0", query.getQueryRequest().getKeyConditionExpression());
        Assert.assertEquals("author =:author_1",query.getQueryRequest().getFilterExpression());
    }
    @Test
    public void testOrQuery(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from \"navin-books\" where  isbn = '12345' or author = 'a'", schema);
        Assert.assertEquals(query.isScanRequest(), true);
        Assert.assertEquals("isbn =:isbn_0 OR author =:author_1",query.getScanRequest().getFilterExpression());
    }
    @Test
    public void testOrWithClauseModificationQuery(){
        QueryRequestBuilder builder = new QueryRequestBuilder();
        QueryTranslator qt = new QueryTranslator(builder);   
        DynamoDbQuery query = qt.translate("select * from \"navin-books\" where  isbn = '12345' and author = 'a' and title > 'x'", schema);
        Assert.assertEquals(query.isQueryRequest(), true);
        Assert.assertEquals("isbn =:isbn_0 AND title >:title_2",query.getQueryRequest().getKeyConditionExpression());
        Assert.assertEquals("author =:author_1",query.getQueryRequest().getFilterExpression());
    }
}
