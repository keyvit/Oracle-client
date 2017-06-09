package ru.nsu.fit.g14201.marchenko.model;


import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;

/**
 */
public class DatabaseController implements TableInfoGetter, TableModifier {
    private Connection connection = null;
    private PreparedStatement getTableColumns = null;
    private List<String> tablespaces = null;

    public void connect(String IP, int port, String username, String password)
            throws ClassNotFoundException, SQLException {
        System.out.println("-------- Oracle JDBC Connection Testing ------");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            throw e;
        }

        System.out.println("Oracle JDBC Driver Registered!");
        connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@" + IP + ":" + port + ":xe",
                    username, password);
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            throw e;
        }


        if (connection != null) {
            System.out.println("You made it, take control your database now!");
            prepareStatements();
        } else {
            System.out.println("Failed to make connection!");
            throw new SQLException();
        }
    }

    public Map<String, List<String>> configureTableList() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT DISTINCT TABLESPACE_NAME FROM USER_TABLES");
            tablespaces = new ArrayList<>();
            while (rs.next()) {
                String tablespace = rs.getString("TABLESPACE_NAME");
                if (tablespace != null)
                    tablespaces.add(tablespace);
            }

            ResultSet ts;
            Map<String, List<String>> tablesInTablespace = new HashMap<>();
            for (String tablespace : tablespaces) {
                ts = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES " +
                        "WHERE TABLESPACE_NAME = '" + tablespace+ "'");
                ArrayList<String> tables = new ArrayList<>();
                while (ts.next()) {
                    tables.add(ts.getString("TABLE_NAME"));
                }
                tablesInTablespace.put(tablespace, tables);
            }

            return tablesInTablespace;
        }
    }

    public List<Object[]> loadTableBody(String tableName, List<Column> columns) throws SQLException {
        List<Object[]> records = new ArrayList<>();

        try ( Statement statement = connection.createStatement() ) {
            StringBuilder query = new StringBuilder("SELECT ");
            if (columns.size() == 1)
                query.append(columns.get(0).getName());
            else {
                for (int i = 0; i < columns.size() - 1; i++) {
                    query.append(columns.get(i).getName());
                    query.append(", ");
                }
                query.append(columns.get(columns.size() - 1).getName());
            }
            query.append(" FROM ");
            query.append(tableName);

            ResultSet rs = statement.executeQuery(query.toString());
            while (rs.next()) {
                Object[] record = new Object[columns.size()];
                for (int i = 1; i <= columns.size(); i++) {
                    record[i - 1] = rs.getObject(i);
                }
                records.add(record);
            }
        }
        return records;
    }
    public List<String> getTablespaces() {
        return tablespaces;
    }

    @Override
    public Table getTable(String tableName) throws SQLException {
        List<Column> columns = getTableColumns(tableName);
        Table table = new Table(tableName, columns);
        List<String> tablespaces = getTablespaces();
        for (String tablespace : tablespaces) {
            List<String> tables = getTablespaceTables(tablespace);
            if (tables.contains(tableName)) {
                table.setTablespace(tablespace);
                break;
            }
        }
        return table;
    }

    @Override
    public List<String> getTablespaceTables(String tablespace) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            ResultSet rs = statement.executeQuery("SELECT TABLE_NAME FROM USER_TABLES " +
                    "WHERE TABLESPACE_NAME = '" + tablespace+ "'");
            ArrayList<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            return tables;
        }
    }
    @Override
    public List<Column> getTableColumns(String tableName) throws SQLException {
        getTableColumns.setString( 1, tableName);
        ResultSet rs = getTableColumns.executeQuery();
        List<Column> columns = new ArrayList<>();
        while (rs.next()) {
            columns.add(new Column(
                    rs.getString("COLUMN_NAME"),
                    rs.getString("DATA_TYPE"),
                    rs.getBoolean("NULLABLE")
            ));
        }
        return columns;
    }

    public void createTable(Table table) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder("CREATE TABLE ");
            builder.append(table.getName());
            builder.append(" ( ");
            List<Column> columns = table.getColumns();
            for (Column column : columns) {
                builder.append(column.getName());
                builder.append(" ");
                builder.append(column.getType());
                if (column.isNotNull())
                    builder.append(" NOT NULL");
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append(" )");
            String tablespace = table.getTablespace();
            if (tablespace != null) {
                builder.append(" TABLESPACE ");
                builder.append(table.getTablespace());
            }
            statement.executeUpdate(builder.toString());
        }
    }
    public void dropTable(String tableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE " + tableName);
        }
    }

    @Override
    public void applyConstraint(String tableName, Constraint constraint) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder("ALTER TABLE ");
            builder.append(tableName);
            builder.append(" ADD CONSTRAINT ");
            builder.append(constraint.toString());
            statement.executeUpdate(builder.toString());
        }
    }
    @Override
    public void dropConstraint(String tableName, String constrName) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder("ALTER TABLE ");
            builder.append(tableName);
            builder.append(" DROP CONSTRAINT ");
            builder.append(constrName);
            statement.executeUpdate(builder.toString());
        }
    }
    @Override
    public List<Constraint> getConstraints(String tableName) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder(
                    "SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE ");
            builder.append("FROM USER_CONSTRAINTS ");
            builder.append("WHERE TABLE_NAME = '");
            builder.append(tableName);
            builder.append("'");
            ResultSet rs = statement.executeQuery(builder.toString());

            Map<String, String> rsResult = new HashMap<>();
            while (rs.next()) {
                rsResult.put(rs.getString("CONSTRAINT_NAME"),
                        rs.getString("CONSTRAINT_TYPE"));
            }

            List<Constraint> constraints = new ArrayList<>();
            for (Map.Entry<String, String> rsRes : rsResult.entrySet()) {

                if (rsRes.getValue().equals("R")) {
                    builder = new StringBuilder("SELECT DISTINCT R_CONSTRAINT_NAME ");
                    builder.append("FROM USER_CONSTRAINTS ");
                    builder.append("WHERE TABLE_NAME = '");
                    builder.append(tableName);
                    builder.append("'");
                    ResultSet subRs = statement.executeQuery(builder.toString());
                    String refConstr = null;
                    while (subRs.next())
                        refConstr = subRs.getString("R_CONSTRAINT_NAME");

                    builder = new StringBuilder("SELECT COLUMN_NAME ");
                    builder.append("FROM USER_CONSTRAINTS NATURAL JOIN USER_CONS_COLUMNS ");
                    builder.append("WHERE TABLE_NAME = '");
                    builder.append(tableName);
                    builder.append("' AND CONSTRAINT_NAME = '");
                    builder.append(rsRes.getKey());
                    builder.append("'");

                    subRs = statement.executeQuery(builder.toString());
                    List<Column> columnList = new ArrayList<>();
                    while (subRs.next()) {
                        columnList.add(new Column(subRs.getString("COLUMN_NAME")));
                    }
                    Object[] colObjArr = columnList.toArray();
                    Column[] colArr = new Column[colObjArr.length];
                    for (int i = 0; i < colObjArr.length; i++)
                        colArr[i] = (Column) colObjArr[i];
                    constraints.add(new ForeignKey(rsRes.getKey(), colArr, refConstr, null));


                } else {
                    Constraint.ConstraintType type =
                            (rsRes.getValue().equals("P")) ?
                                    Constraint.ConstraintType.PRIMARY_KEY
                                    : Constraint.ConstraintType.UNIQUE;
                    builder = new StringBuilder("SELECT COLUMN_NAME ");
                    builder.append("FROM USER_CONSTRAINTS NATURAL JOIN USER_CONS_COLUMNS ");
                    builder.append("WHERE TABLE_NAME = '");
                    builder.append(tableName);
                    builder.append("' AND CONSTRAINT_NAME = '");
                    builder.append(rsRes.getKey());
                    builder.append("'");

                    ResultSet subRs = statement.executeQuery(builder.toString());
                    List<Column> columnList = new ArrayList<>();
                    while (subRs.next()) {
                        columnList.add(new Column(subRs.getString("COLUMN_NAME")));
                    }
                    Object[] colObjArr = columnList.toArray();
                    Column[] colArr = new Column[colObjArr.length];
                    for (int i = 0; i < colObjArr.length; i++)
                        colArr[i] = (Column) colObjArr[i];
                    constraints.add(new Constraint(type, rsRes.getKey(), colArr));
                }
            }

            return constraints;
        }
    }

    @Override
    public void addColumn(String tableName, Column column) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder("ALTER TABLE ");
            builder.append(tableName);
            builder.append(" ADD ");
            builder.append(column.getName());
            builder.append(" ");
            builder.append(column.getType());
            statement.executeUpdate(builder.toString());
        }
    }
    @Override
    public void removeColumn(String tableName, Column column) throws SQLException {
        try (Statement statement = connection.createStatement() ) {
            StringBuilder builder = new StringBuilder("ALTER TABLE ");
            builder.append(tableName);
            builder.append(" DROP COLUMN ");
            builder.append(column.getName());
            statement.executeUpdate(builder.toString());
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
        if (getTableColumns != null)
            getTableColumns.close();
    }

    private void prepareStatements() throws SQLException {
        getTableColumns = connection.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, NULLABLE " +
                "FROM USER_TAB_COLUMNS " +
                "WHERE TABLE_NAME = ?");
    }

    public void removeRow(String tableName, Map<String, Object> fields)
                            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            StringBuilder builder = new StringBuilder("DELETE FROM ");
            builder.append(tableName);
            where(fields, builder);

            statement.executeUpdate(builder.toString());
        }
    }
    public void replaceValue(String tableName, Map<String, Object> fields, String changingColumn,
                             Object newValue) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            StringBuilder builder = new StringBuilder("UPDATE ");
            builder.append(tableName);
            builder.append(" SET ");
            builder.append(changingColumn);
            builder.append(" = ");
            builder.append(newValue);
            where(fields, builder);

            statement.executeUpdate(builder.toString());
        }
    }
    private void where(Map<String, Object> fields, StringBuilder builder) {
        builder.append(" WHERE ");
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            builder.append(field.getKey());
            builder.append("=");
            builder.append(field.getValue());
            builder.append(" AND ");
        }
        builder.delete(builder.length() - 5, builder.length());
    }

    public void addRecord(String tableName, Map<String, Object> fields)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);

            StringBuilder columnList = new StringBuilder(" ("),
                    valueList = new StringBuilder(" (");
            for (Map.Entry<String, Object> field : fields.entrySet()) {
                columnList.append(field.getKey());
                columnList.append(", ");
                valueList.append(field.getValue());
                valueList.append(", ");
            }
            columnList.delete(columnList.length() - 2, columnList.length());
            valueList.delete(valueList.length() - 2, valueList.length());
            columnList.append(") ");
            valueList.append(") ");

            builder.append(columnList);
            builder.append("VALUES");
            builder.append(valueList);

            statement.executeUpdate(builder.toString());
        }
    }

    public DefaultTableModel executeQuery(String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return buildTableModel(statement.executeQuery(query));
        }
    }

    private static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        return new DefaultTableModel(data, columnNames);
    }
}
