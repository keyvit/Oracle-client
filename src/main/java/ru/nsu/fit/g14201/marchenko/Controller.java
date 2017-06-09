package ru.nsu.fit.g14201.marchenko;

import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.model.Constraint;
import ru.nsu.fit.g14201.marchenko.model.DatabaseController;
import ru.nsu.fit.g14201.marchenko.model.Table;
import ru.nsu.fit.g14201.marchenko.view.*;
import ru.nsu.fit.g14201.marchenko.view.newTableView.TableFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Controller implements DatabaseActionListener, TableRecordsChangeListener {
    private DatabaseController databaseController;
    private DatabaseFrame databaseFrame;

    void init() {
        try {
            databaseController = new DatabaseController();
            AuthorizationWindow authWindow = new AuthorizationWindow(new byte[] {127, 0, 0, 1}, 49161,
                    "system", "oracle");
            authWindow.setVisible(true);
            authWindow.setDatabaseActionListener(this);
        } catch (Exception e) {
            try {
                databaseController.closeConnection();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void showErrorWindow(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onAuthorize(String IP, int port, String username, String password) {
        try {
            databaseController.connect(IP, port, username, password);
            databaseFrame = new DatabaseFrame(databaseController.configureTableList(), databaseController,
                    databaseController);
            databaseFrame.setDatabaseActionListener(this);
            databaseFrame.setVisible(true);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLoadTable(String tableName) {
        try {
            List<Column> columns = databaseController.getTableColumns(tableName);
            Table table = new Table(tableName, columns);
            table.setTableRecordsChangeListener(this);
            table.setData(databaseController.loadTableBody(tableName, columns));
            databaseFrame.addTable(table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequireTablespaces() {
        TableFrame tableFrame = new TableFrame(databaseController.getTablespaces());
        tableFrame.setTableInfoGetter(databaseController);
        tableFrame.setDatabaseActionListener(this);
        tableFrame.setVisible(true);
    }

    @Override
    public void onCreateTable(String name, String tablespace, List<Column> columns,
                              Constraint[] constraints) {
        try {
            Table table = new Table(name, columns);
            if (constraints != null)
                for (int i = 0; i < constraints.length; i++)
                    table.addConstraint(constraints[i]);

            table.setTableRecordsChangeListener(this);
            table.setTablespace(tablespace);
            databaseController.createTable(table);

            if (constraints != null)
                for (int i = 0; i < constraints.length; i++)
                    databaseController.applyConstraint(name, constraints[i]);

            databaseFrame.insertIntoHierarchy(tablespace, name);
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
        }
    }
    @Override
    public void onDropTable(String name) {
        try {
            databaseController.dropTable(name);
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean onExecuteQuery(String query) {
        try {
            DefaultTableModel tableModel = databaseController.executeQuery(query);
            ReportFrame reportFrame = new ReportFrame(tableModel);
            reportFrame.setVisible(true);
            return true;
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void removeRow(String tableName, Map<String, Object> fields) {
        try {
            databaseController.removeRow(tableName, fields);
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
        }
    }
    @Override
    public void addRow(String tableName, Map<String, Object> fields)
            throws SQLException {
        try {
            databaseController.addRecord(tableName, fields);
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
            throw e;
        }
    }@Override
    public void replaceValue(String tableName, Map<String, Object> fields, String changingColumn, Object newValue)
            throws SQLException {
        try {
            databaseController.replaceValue(tableName, fields, changingColumn, newValue);
        } catch (SQLException e) {
            showErrorWindow(e.getLocalizedMessage());
            throw e;
        }
    }
}
