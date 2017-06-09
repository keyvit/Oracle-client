package ru.nsu.fit.g14201.marchenko.view;

import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public interface TableRecordsChangeListener {
    void removeRow(String tableName, Map<String, Object> fields);
    void replaceValue(String tableName, Map<String, Object> fields, String changingColumn,
                      Object newValue) throws SQLException;
    void addRow(String tableName, Map<String, Object> fields) throws SQLException;

}
