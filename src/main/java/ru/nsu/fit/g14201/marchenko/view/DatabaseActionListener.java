package ru.nsu.fit.g14201.marchenko.view;


import ru.nsu.fit.g14201.marchenko.model.Column;
import ru.nsu.fit.g14201.marchenko.model.Constraint;

import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public interface DatabaseActionListener {
    void onAuthorize(String IP, int port, String username, String password);
    void onLoadTable(String tableName);
    void onRequireTablespaces();
    void onCreateTable(String name, String tablespace, List<Column> columns, Constraint[] constraints);
    void onDropTable(String name);
    boolean onExecuteQuery(String query);
}
