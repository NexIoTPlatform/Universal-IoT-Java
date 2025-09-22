/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.rule.parser;

import cn.universal.rule.enums.ParserFormat;
import cn.universal.rule.model.RuleParserResult;
import cn.universal.rule.model.RuleParserResult.RuleField;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * sql解析策略 @Author Aleo
 *
 * @since 2025/12/3 9:16
 */
@Component
public class SqlModelParserStrategy implements RuleModelParserStrategy {

  @Override
  public ParserFormat getFormat() {
    return ParserFormat.sql;
  }

  @Override
  public RuleParserResult parse(String modelDefineString) {
    RuleParserResult ruleParserResult = new RuleParserResult();

    SQLStatement sqlStatement = SQLUtils.parseSingleStatement(modelDefineString, DbType.mock);
    SQLSelectStatement statement = (SQLSelectStatement) sqlStatement;
    SQLSelectQueryBlock query = (SQLSelectQueryBlock) statement.getSelect().getQuery();

    List<RuleField> ruleFields =
        query.getSelectList().stream()
            .map(
                item ->
                    new RuleField(
                        item.getExpr().toString().trim(),
                        Objects.isNull(item.getAlias()) ? null : item.getAlias().trim()))
            .collect(Collectors.toList());
    ruleParserResult.setFields(ruleFields);

    List<String> topics =
        Stream.of(query.getFrom().toString().split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    ruleParserResult.setTopics(topics);

    String condition = convertWhereSql(query.getWhere());
    ruleParserResult.setCondition(condition);

    return ruleParserResult;
  }

  public static String convertWhereSql(SQLExpr expr) {
    String sql = "";
    if (expr instanceof SQLBinaryOpExpr) {
      SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
      if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.Equality)) {
        sql = binaryOpExpr.getLeft() + " == " + binaryOpExpr.getRight();
      } else if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.Is)) {
        sql = binaryOpExpr.getLeft() + " == null";
      } else if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.IsNot)) {
        sql = binaryOpExpr.getLeft() + " != null";
      } else if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.LessThanOrGreater)) {
        sql = binaryOpExpr.getLeft() + " != " + binaryOpExpr.getRight();
      } else if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.BooleanAnd)) {
        sql = getAndOrSql(binaryOpExpr, SQLBinaryOperator.BooleanAnd);
      } else if (binaryOpExpr.getOperator().equals(SQLBinaryOperator.BooleanOr)) {
        sql = getAndOrSql(binaryOpExpr, SQLBinaryOperator.BooleanOr);
      } else {
        sql =
            String.format(
                "%s %s %s",
                binaryOpExpr.getLeft(),
                binaryOpExpr.getOperator().name.toLowerCase(),
                binaryOpExpr.getRight());
      }
    } else {
      if (expr instanceof SQLInListExpr) {
        SQLInListExpr inListExpr = (SQLInListExpr) expr;
        sql =
            String.format(
                "%s %s in %s",
                inListExpr.getExpr(), inListExpr.isNot() ? "not" : "", inListExpr.getTargetList());
      } else {
        if (Objects.nonNull(expr)) {
          sql = expr.toString();
        }
      }
    }

    return sql;
  }

  private static String getAndOrSql(
      SQLBinaryOpExpr binaryOpExpr, SQLBinaryOperator binaryOperator) {
    String sql;
    String leftSql = convertWhereSql(binaryOpExpr.getLeft());
    String rightSql = convertWhereSql(binaryOpExpr.getRight());
    String baseSql = StringUtils.trimToEmpty(binaryOpExpr.toString());
    if (baseSql.startsWith("(")) {
      leftSql = String.format("(%s)", leftSql);
    }
    if (baseSql.endsWith(")")) {
      rightSql = String.format("(%s)", rightSql);
    }
    sql = String.format("%s %s %s", leftSql, binaryOperator.name.toLowerCase(), rightSql);
    return sql;
  }

  public static void main(String[] args) {
    String sql = "select a,abc as aaa,b,c,d from 'app:*',c where qwe is null ";
    RuleParserResult parse = new SqlModelParserStrategy().parse(sql);
    System.out.println(parse);
  }
}
