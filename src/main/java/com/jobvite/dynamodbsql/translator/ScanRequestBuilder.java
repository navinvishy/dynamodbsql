package com.jobvite.dynamodbsql.translator;



import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQuery;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
import com.jobvite.dynamodbsql.translator.visitor.ExprNameGeneratingVisitor;
import com.jobvite.dynamodbsql.translator.visitor.FilterExpressionGeneratingVisitor;
/**
 * A naive optimizer that always generates a ScanRequest. You can pass in a null schema since this optimizer ignores the schema
 * @author Navin.Viswanath
 *
 */
public class ScanRequestBuilder implements RequestBuilder {
    /**
     * This optimizer ignores the schema
     */
    @Override
    public DynamoDbQuery build(DynamoDbQueryModel model, DynamoDbSchema schema) {
        // First pass through name generating visitor
        ExprNameGeneratingVisitor exprNameGeneratingVisitor = new ExprNameGeneratingVisitor();
        ConditionExpr expr = model.getConditions();
        expr.accept(exprNameGeneratingVisitor);
        // Second pass through filter expression generating visitor
        FilterExpressionGeneratingVisitor filterExpressionGeneratingVisitor = new FilterExpressionGeneratingVisitor(exprNameGeneratingVisitor.getExprAttributeNames());
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.withTableName(model.getTableName()).withExpressionAttributeValues(exprNameGeneratingVisitor.getExprAttributeValues())
            .withFilterExpression(expr.accept(filterExpressionGeneratingVisitor));
        List<String> projections = model.getProjectionList();
        if(projections != null && !projections.contains("*")){
            scanRequest.setProjectionExpression(StringUtils.join(projections, ","));
        }
        return new DynamoDbQuery(scanRequest);
    }
    
}
