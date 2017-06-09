package ru.nsu.fit.g14201.marchenko.model;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.model.Constraint;
import ru.nsu.fit.g14201.marchenko.model.Table;

import java.sql.SQLException;
import java.util.List;

/**
 */
public interface TableInfoGetter {
    Table getTable(String tableName) throws SQLException;
    List<String> getTablespaceTables(String tablespace) throws SQLException;
    List<Column> getTableColumns(String tableName) throws SQLException;
    List<Constraint> getConstraints(String tableName) throws SQLException;
}
