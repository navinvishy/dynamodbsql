package com.jobvite.dynamodbsql.dynamo.model;
/**
 * Comparison operators allowed in comparison exprs
 * @author Navin.Viswanath
 *
 */
public enum ComparisonOperator {
    GT(">"), GE(">="),LT("<"),LE("<="),EQ("="),NE("<>");
    private String symbol;
    ComparisonOperator(String symbol){
        this.symbol = symbol;
    }
    public String getSymbol(){
        return symbol;
    }
}
