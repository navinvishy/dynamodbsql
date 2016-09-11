package com.jobvite.dynamodbsql.translator.visitor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;
/**
 * A visitor that determines if the ConditionExpr involves only and EQ operator on the partition key
 * @author Navin.Viswanath
 *
 */
public class GetItemVisitor implements ExprVisitor<Boolean>{
    private DynamoDbSchema schema;
    private DynamoDbQueryModel model;
    
    @Override
    public Boolean visit(ComparisonExpr expr) {
        String tableName = model.getTableName();
        Map<String,TableDescription> tableMap = schema.getTableDescriptionMap();
        TableDescription tableDescription = tableMap.get(tableName);
        List<KeySchemaElement> keySchemaElements = tableDescription.getKeySchema();
        Set<String> primaryKey = keySchemaElements.stream().filter(k -> k.getKeyType().equals(KeyType.HASH)).map(k -> k.getAttributeName()).collect(Collectors.toSet());
        return primaryKey.contains(expr.getKey()) && expr.getOp().equals(ComparisonOperator.EQ);
    }

    @Override
    public Boolean visit(FunctionExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(InExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(NotExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(AndExpr expr) {
        return false;
    }

    @Override
    public Boolean visit(OrExpr expr) {
        return false;
    }
    
}
