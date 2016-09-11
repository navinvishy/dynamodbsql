package com.jobvite.dynamodbsql.translator;

import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQuery;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
/**
 * Builder contract to generate a query given a query model and a schema
 * @author Navin.Viswanath
 *
 */
public interface RequestBuilder {
    public DynamoDbQuery build(DynamoDbQueryModel model, DynamoDbSchema schema);
}
