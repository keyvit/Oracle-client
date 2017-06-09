package ru.nsu.fit.g14201.marchenko.model;

/**
 */
public class ForeignKey extends Constraint {
    private String refTable;
    private Column[] refColumns;

    public ForeignKey(String name, Column[] columns, String refTable, Column[] refColumns) {
        super(ConstraintType.FOREIGN_KEY, name, columns);

        this.refTable = refTable;
        this.refColumns = refColumns;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(" FOREIGN KEY (");

        for (Column column : columns) {
            builder.append(column.getName());
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());

        builder.append(") REFERENCES ");
        builder.append(refTable);
        if (refColumns != null) {
            builder.append(" (");
            for (Column column : refColumns) {
                builder.append(column.getName());
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append(")");
        }

        return builder.toString();
    }
}
