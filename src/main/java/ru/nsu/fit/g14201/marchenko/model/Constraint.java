package ru.nsu.fit.g14201.marchenko.model;


/**
 */
public class Constraint {
    public enum ConstraintType { UNIQUE, PRIMARY_KEY, FOREIGN_KEY }

    protected String name;
    protected ConstraintType type;
    protected Column[] columns;

    public Constraint(ConstraintType type, String name, Column column) {
        this.name = name;
        this.type = type;
        columns = new Column[1];
        columns[0] = column;
    }
    public Constraint(ConstraintType type, String name, Column[] columns) {
        this.name = name;
        this.type = type;
        this.columns = columns;
    }

    public ConstraintType getType() {
        return type;
    }
    public Column[] getColumns() {
        return columns;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);

        switch (type) {
            case UNIQUE:
                builder.append(" UNIQUE (");
                break;
            case PRIMARY_KEY:
                builder.append(" PRIMARY KEY (");
                break;
        }

        for (Column column : columns) {
            builder.append(column.getName());
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(")");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constraint)) return false;

        Constraint that = (Constraint) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return type == that.type;
    }
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
