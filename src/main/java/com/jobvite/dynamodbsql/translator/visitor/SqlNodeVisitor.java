package com.jobvite.dynamodbsql.translator.visitor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.calcite.sql.SqlBinaryStringLiteral;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.SqlBasicVisitor;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jobvite.dynamodbsql.dynamo.model.AndExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonExpr;
import com.jobvite.dynamodbsql.dynamo.model.ComparisonOperator;
import com.jobvite.dynamodbsql.dynamo.model.ConditionExpr;
import com.jobvite.dynamodbsql.dynamo.model.DynamoDbQueryModel;
import com.jobvite.dynamodbsql.dynamo.model.FunctionExpr;
import com.jobvite.dynamodbsql.dynamo.model.InExpr;
import com.jobvite.dynamodbsql.dynamo.model.NotExpr;
import com.jobvite.dynamodbsql.dynamo.model.OrExpr;

public class SqlNodeVisitor extends SqlBasicVisitor<DynamoDbQueryModel>{
    
    @Override
    public DynamoDbQueryModel visit(SqlCall sqlCall) {
        List<String> projectionList = new ArrayList<>();
        String tableName = null;
        String indexName = null;
        ConditionExpr conditions = null;
        if(sqlCall instanceof SqlSelect){
            SqlSelect select = (SqlSelect) sqlCall;
            SqlNode from = select.getFrom();
            SqlNodeList projection = select.getSelectList();
            SqlNode where = select.getWhere();
            String colExprStr = null;
            for(SqlNode col : projection){
                if(col.getKind() == SqlKind.IDENTIFIER){
                    colExprStr = col.toString();
                    
                } else {
                    if(col.getKind() == SqlKind.OTHER_FUNCTION){
                        SqlCall colExpr = (SqlCall)col;
                        List<SqlNode> parts = colExpr.getOperandList();
                        if(parts.size() > 1){
                            colExprStr = parts.get(0) + "[" + parts.get(1) + "]";
                        } else {
                            colExprStr = parts.get(0).toString();
                        }
                    }
                }
                projectionList.add(colExprStr);
            }
            if(from.getKind() == SqlKind.IDENTIFIER){
                String fromName = from.toString();
                if(!fromName.contains(".")){
                    tableName = fromName;
                } else {
                    String[] names = fromName.split("\\.");
                    tableName = names[0];
                    indexName = names[1];
                }
            }
            conditions = getWhereConditions((SqlCall)where);
        }
        return indexName == null ? new DynamoDbQueryModel(tableName,projectionList,conditions) 
                : new DynamoDbQueryModel(tableName,indexName,projectionList,conditions);
    }
    /**
     * Transforms the where clause to a ConditionExpr
     * @param where
     * @return
     */
    public ConditionExpr getWhereConditions(SqlCall where){
        SqlKind kind = where.getKind();
        // Base cases
        if(kind == SqlKind.EQUALS || kind == SqlKind.GREATER_THAN || kind == SqlKind.LESS_THAN || kind == SqlKind.LESS_THAN_OR_EQUAL 
                || kind == SqlKind.GREATER_THAN_OR_EQUAL || kind == SqlKind.NOT_EQUALS){
            return getComparisonWhereCondition(where);
        }
        if(kind == SqlKind.IN){
            List<SqlNode> operands = where.getOperandList();
            String key = operands.get(0).toString();
            SqlNodeList vals = (SqlNodeList) operands.get(1);
            List<AttributeValue> literalVals = new ArrayList<>();
            for(SqlNode val : vals){
                literalVals.add(getValueForLiteral((SqlLiteral)val));     
            }
            return new InExpr(key, literalVals);
        }
        if(kind == SqlKind.OTHER_FUNCTION){
            String function = where.getOperator().getName();
            List<SqlNode> operands = where.getOperandList();
            List<String> operandList = operands.stream().map(op -> op.toString()).collect(Collectors.toList());
            String[] operandArray = new String[operandList.size()];
            operandList.toArray(operandArray);
            return new FunctionExpr(function, operandArray);
        }
        // Recursive cases
        if(kind == SqlKind.NOT){
            SqlCall operand = (SqlCall) where.getOperandList().get(0);
            return new NotExpr(getWhereConditions(operand));
        }
        if(kind == SqlKind.AND){
            SqlCall left = (SqlCall) where.getOperandList().get(0);
            SqlCall right = (SqlCall) where.getOperandList().get(1);
            return new AndExpr(getWhereConditions(left), getWhereConditions(right));
        }
        if(kind == SqlKind.OR){
            SqlCall left = (SqlCall) where.getOperandList().get(0);
            SqlCall right = (SqlCall) where.getOperandList().get(1);
            return new OrExpr(getWhereConditions(left), getWhereConditions(right));
        }
        return null;
    }
    /**
     * Convert a list of literals into an AttributeValue
     * @param literals
     * @return
     */
    public AttributeValue getValueForLiterals(List<SqlLiteral> literals){
        SqlLiteral firstLiteral = (SqlLiteral) literals.get(0);
        AttributeValue value = new AttributeValue();
        if(firstLiteral.getTypeName().equals(SqlTypeName.CHAR)){
            List<String> values = new ArrayList<>();
            for(SqlLiteral literal : literals){
                values.add((String)literal.getValue());
            }
            value.setSS(values);
        }
        if(firstLiteral.getTypeName().equals(SqlTypeName.DECIMAL)){
            List<String> values = new ArrayList<>();
            for(SqlLiteral literal : literals){
                values.add((String)literal.getValue());
            }
            value.setNS(values);
        }
        if(firstLiteral.getTypeName().equals(SqlTypeName.BINARY)){
            List<ByteBuffer> values = new ArrayList<>();
            for(SqlLiteral literal : literals){
                SqlBinaryStringLiteral binaryLiteral = (SqlBinaryStringLiteral)literal;
                ByteBuffer buffer = ByteBuffer.wrap(binaryLiteral.getBitString().getAsByteArray());
                values.add(buffer);
            }
            value.setBS(values);
        }
        return value;
    }
    /**
     * Convert a constant into an AttributeValue
     * @param literal
     * @return
     */
    public AttributeValue getValueForLiteral(SqlLiteral literal){
        AttributeValue value = new AttributeValue();
        if(literal.getTypeName().equals(SqlTypeName.CHAR)){
            value.setS(literal.toValue());
        }
        if(literal.getTypeName().equals(SqlTypeName.DECIMAL) || literal.getTypeName().equals(SqlTypeName.DOUBLE)){
            value.setN(literal.toString());
        }
        if(literal.getTypeName().equals(SqlTypeName.BINARY)){
            SqlBinaryStringLiteral binaryLiteral = (SqlBinaryStringLiteral)literal;
            ByteBuffer buffer = ByteBuffer.wrap(binaryLiteral.getBitString().getAsByteArray());
            value.setB(buffer);
        }
        return value;
    }
    /**
     * Get where condition for comparison operators
     * @param where
     * @return
     */
    public ComparisonExpr getComparisonWhereCondition(SqlCall where){
        ComparisonExpr c = new ComparisonExpr();
        AttributeValue v = null;
        String identifier = null;
        List<SqlNode> operands = where.getOperandList();
        if(operands.size() > 1){
            for(SqlNode operand : operands){
                if(operand.getKind() == SqlKind.LITERAL){
                    v = getValueForLiteral((SqlLiteral)operand);
                }
                if(operand.getKind() == SqlKind.IDENTIFIER){
                    identifier = operand.toString();
                }
            }
            
        }
        c.setKey(identifier);
        c.setValue(v);
        switch(where.getKind()){  
            case EQUALS:
                c.setOp(ComparisonOperator.EQ);
                break;
            case GREATER_THAN:
                c.setOp(ComparisonOperator.GT);
                break;
            case LESS_THAN:
                c.setOp(ComparisonOperator.LT);
                break;
            case GREATER_THAN_OR_EQUAL:
                c.setOp(ComparisonOperator.GE);
                break;
            case LESS_THAN_OR_EQUAL:
                c.setOp(ComparisonOperator.LE);
                break;
            case NOT_EQUALS:
                c.setOp(ComparisonOperator.NE);
                break;
        }
        return c;
    }
}
