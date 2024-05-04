package alchemystar.freedom.meta;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;

import alchemystar.freedom.engine.Database;
import alchemystar.freedom.sql.parser.SelectVisitor;
import alchemystar.freedom.sql.select.TableFilter;

/**
 * @Author lizhuyang
 */
public class TableManager {

    //这是一个静态映射，存储以表名为键的 Table 对象实例。这个映射作为 TableManager 管理的所有表实例的中心存储
    public static Map<String, Table> tableMap = new HashMap<String, Table>();

    //这个方法使用 SQLExprTableSource 和 SelectVisitor 创建一个新的 TableFilter 实例。从 SQLExprTableSource 提取表名，
    // 用于从 tableMap 检索相应的 Table，并设置筛选条件为访问器的 where 条件
    public static TableFilter newTableFilter(SQLExprTableSource sqlExprTableSource, SelectVisitor selectVisitor) {
        TableFilter tableFilter = new TableFilter();
        String tableName = sqlExprTableSource.getExpr().toString();
        Table table = tableMap.get(tableName);
        tableFilter.setTable(table);
        tableFilter.setSelectVisitor(selectVisitor);
        tableFilter.setAlias(sqlExprTableSource.getAlias());
        tableFilter.setFilterCondition(selectVisitor.getWhereCondition());
        return tableFilter;
    }

    //使用直接的 SQLExpr 来设置筛选条件，而不是使用 SelectVisitor。这为定义和应用筛选条件提供了灵活性
    public static TableFilter newTableFilter(SQLExprTableSource sqlExprTableSource, SQLExpr whereExpr) {
        TableFilter tableFilter = new TableFilter();
        String tableName = sqlExprTableSource.getExpr().toString();
        Table table = tableMap.get(tableName);
        tableFilter.setTable(table);
        tableFilter.setAlias(sqlExprTableSource.getAlias());
        tableFilter.setFilterCondition(whereExpr);
        return tableFilter;
    }

    public static Table getTable(String tableName) {
        Table table = tableMap.get(tableName);
        if (table == null) {
            throw new RuntimeException("not found this table , tableName = " + tableName);
        }
        return table;
    }

    public static Table getTableWithNoException(String tableName) {
        return tableMap.get(tableName);
    }

    public static void addTable(Table table, boolean isPersist) {
        if (tableMap.get(table.getName()) != null) {
            throw new RuntimeException("table " + table.getName() + " already exists");
        }
        if (isPersist) {
            // 先落盘,再写入
            Database.getInstance().getTableLoader().writeTableMeta(table);
        }
        tableMap.put(table.getName(), table);
    }

    public static Map<String, Table> getTableMap() {
        return tableMap;
    }

    public static void setTableMap(Map<String, Table> tableMap) {
        TableManager.tableMap = tableMap;
    }
}
