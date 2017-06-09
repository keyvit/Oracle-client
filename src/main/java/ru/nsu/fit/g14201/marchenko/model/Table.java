package ru.nsu.fit.g14201.marchenko.model;

import ru.nsu.fit.g14201.marchenko.view.TableRecordsChangeListener;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.*;

/**
 */
public class Table extends DefaultTableModel {
    private String name;
    private String tablespace = null;
    private List<Column> columns;
    private List<Object[]> data = null;

    private List<Constraint> constraints = new ArrayList<>();

    private TableRecordsChangeListener tableRecordsChangeListener = null;

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }
    public String getTablespace() {
        return tablespace;
    }
    public List<Column> getColumns() {
        return columns;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }
    public void addConstraint(Constraint constr) {
        constraints.add(constr);
    }
    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    public void setData(List<Object[]> data) {
        this.data = data;
    }
    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public void setTableRecordsChangeListener(TableRecordsChangeListener tableRecordsChangeListener) {
        this.tableRecordsChangeListener = tableRecordsChangeListener;
    }

    @Override
    public int getRowCount() {
        return (data == null) ? 0 : data.size();
    }
    @Override
    public int getColumnCount() {
        return columns.size();
    }
    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex).getName();
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (data == null)
            return null;

        Object value = data.get(rowIndex)[columnIndex];
        if (value == null) {
            if (columns.get(columnIndex).getName().equals("NUMBER"))
                return 0;
            else
                return "null";
        }
        else
            return value;
    }
    @Override
    public void removeRow(int row) {
        if (tableRecordsChangeListener == null)
            return;

        tableRecordsChangeListener.removeRow(name, getIdentifyingMap(row));
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }
    @Override
    public void addRow(Object[] fields) {
        //Сначала попробуем добавить в БД
        try {
            Map<String, Object> insertedFields = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                if (fields[i] != null)
                    insertedFields.put(columns.get(i).getName(), fields[i]);
            }
            tableRecordsChangeListener.addRow(name, insertedFields);
            data.add(fields);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        } catch (SQLException e) {}
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (tableRecordsChangeListener == null)
            return;

        //Сначала пробуем изменить строку в базе данных

        //Map<String, Object> identMap = getIdentifyingMap(row); //FIXME А если список отсортировали?
        try {
            tableRecordsChangeListener.replaceValue(name, getIdentifyingMap(row),
                    columns.get(column).getName(), aValue);
            //Если успешно, сохраняем изменение
            Object[] record = data.get(row);
            record[column] = aValue;
        } catch (SQLException e) {
            //Оставляем старое значение
        }
    }

    private Map<String, Object> getIdentifyingMap(int row) {
        Map<String, Object> fields = new HashMap<>(columns.size());

        Object[] record = data.get(row);
        for (int i = 0; i < columns.size(); i++)
            fields.put(columns.get(i).getName(), record[i]);

        return fields;
    }
}
