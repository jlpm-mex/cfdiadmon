package net.ellapiz.admoncfdiprov.service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.stereotype.Service;

@Service
public class SqlSecurityService {

    public String injectTenantId(String rawSql, Long tenantId) throws JSQLParserException {
        // Limpiar posible formato markdown devuelto por el LLM
        String cleanSql = rawSql.replaceAll("```sql", "").replaceAll("```", "").trim();

        Statement statement = CCJSqlParserUtil.parse(cleanSql);

        if (!(statement instanceof Select)) {
            throw new SecurityException("Operación no permitida: Solo se permiten sentencias SELECT.");
        }

        Select selectStatement = (Select) statement;
        PlainSelect plainSelect = selectStatement.getPlainSelect();

        // Crear expresión: tenant_id = <tenantId>
        EqualsTo tenantFilter = new EqualsTo();
        tenantFilter.setLeftExpression(new Column("tenant_id"));
        tenantFilter.setRightExpression(new LongValue(tenantId));

        // Combinar con el WHERE existente si lo hay
        Expression existingWhere = plainSelect.getWhere();
        if (existingWhere == null) {
            plainSelect.setWhere(tenantFilter);
        } else {
            plainSelect.setWhere(new AndExpression(existingWhere, tenantFilter));
        }

        return selectStatement.toString();
    }
}