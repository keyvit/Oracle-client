package ru.nsu.fit.g14201.marchenko.model;

import java.util.List;

/**
 */
public interface TableCreator {
    boolean addColumn(Column column);
    void addPrimaryKey(String name, List<Column> columns);
    void addUniqueConstraint(String name, List<Column> column);
    void addForeignKey(String name, Column[] columns, String refTable, Column[] refColumns);
}
