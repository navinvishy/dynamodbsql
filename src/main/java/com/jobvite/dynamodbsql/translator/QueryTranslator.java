package com.jobvite.dynamodbsql.translator;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQuery;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbSchema;
import com.jobvite.dynamodbsql.translator.visitor.SqlNodeVisitor;
/**
 * Translate a SQL query into a DynamoDB query model
 * @author Navin.Viswanath
 *
 */
public class QueryTranslator {
    public RequestBuilder queryBuilder;
    private static Log log = LogFactory.getLog(QueryTranslator.class);
    public QueryTranslator(RequestBuilder queryBuilder){
        this.queryBuilder = queryBuilder;
    }
    /**
     * Translate a sql query into a DynamoDB query model
     * @param sql
     * @return
     */
    public DynamoDbQuery translate(String sql, DynamoDbSchema schema){
        Config config = SqlParser.configBuilder().setCaseSensitive(true).setQuotedCasing(Casing.UNCHANGED).setUnquotedCasing(Casing.UNCHANGED).build();
        SqlParser parser = SqlParser.create(sql, config);
        try {
            SqlSelect select = (SqlSelect) parser.parseQuery();
            SqlNodeVisitor visitor = new SqlNodeVisitor();
            DynamoDbQueryModel model = select.accept(visitor);
            return queryBuilder.build(model, schema);
        } catch (SqlParseException e) {
            log.error("Only SELECT..FROM..WHERE statements are supported");
            e.printStackTrace();
        }
        return null;
    }
}
