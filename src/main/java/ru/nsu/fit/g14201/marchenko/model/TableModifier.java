package ru.nsu.fit.g14201.marchenko.model;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.model.Constraint;

import java.sql.SQLException;

/**
 */
public interface TableModifier {
    void addColumn(String string, Column column) throws SQLException;
    void removeColumn(String tableName, Column column) throws SQLException;
    void dropConstraint(String tableName, String constrName) throws SQLException;
    void applyConstraint(String tableName, Constraint constraint) throws SQLException;

}
