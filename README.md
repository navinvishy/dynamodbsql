# dynamodbsql
A SQL interface for DynamoDB

The goal is to provide a SQL interface for DynamoDB. This project provides a query translator that translates a SQL query into a DynamoDB
query or scan request. The translator supports SQL queries of the form:

**select [projection_list] from [table_name] where [boolean_condition]**

where
```
projection_list     := '*' | columns
boolean_condition   :=  column op_symbol value 
                        | function_expr 
                        | and_expr 
                        | or_expr 
                        | not_expr
op_symbol           := '=' | '>' | '<' | '>=' | '<=' | '!='
function_expr       :=  attribute_exists (path) 
                        | attribute_not_exists (path) 
                        | attribute_type (path, type) 
                        | begins_with (path, substr) 
                        | contains (path, operand)
                        | size (path)
and_expr            := boolean_condition and boolean_condition
or_expr             := boolean_condition or boolean_condition
not_expr            := not(boolean_condition)
```
Here is a simple example of use:
```java
DefaultAWSCredentialsProviderChain credentials = new DefaultAWSCredentialsProviderChain();
AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
DynamoDbSchema schema = DynamoDbSchemaFactory.create(client);
// If you need only a Scan, use RequestBuilder builder = new ScanRequestBuilder()
RequestBuilder builder = new QueryRequestBuilder();
QueryTranslator qt = new QueryTranslator(builder);
DynamoDbQuery query = qt.translate("select * from testTable where primaryKey='value'", schema);
if(query.isQueryRequest()){
  System.out.println("Query = " + query.getQueryRequest());
  QueryResult result = client.query(query.getQueryRequest());
  System.out.println(result);
} else {
  System.out.println("Scan = " + query.getScanRequest());
  ScanResult result = client.scan(query.getScanRequest());
  System.out.println(result);
}
```
The type of query generated depends on the RequestBuilder that is used. The ScanRequestBuilder will always generate a Scan request.
The QueryRequestBuilder is somewhat more intelligent about generating queries. Based on the provided DynamoDbSchema and the SQL query,
it can figure out whether a Query request can be issued. This is done by examining the AST of the where clause of the SQL query
to determine whether the clause involves conjunctions involving the partition key(and possibly the sort key). If so, a Query
request is generated. Otherwise, it generates a Scan request.

This project makes use of the SQL parser provided by [Apache Calcite](http://calcite.apache.org/)

If querying an index, the following format can be used:

**select * from table_name.index_name where primary_key='value';**

The translator also supports the functions that can be used with DynamoDB. For example:

**select * from table_name where begins_with('column','value');**
