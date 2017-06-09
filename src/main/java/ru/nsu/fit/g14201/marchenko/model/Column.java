package ru.nsu.fit.g14201.marchenko.model;


/**
 */
public class Column {
    private String name;
    private String type;
    private boolean notNull;

    public Column(String name) {
        this.name = name;
    }
    public Column(String name, String type, boolean notNull) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public boolean isNotNull() {
        return notNull;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(" (type: ");
        builder.append(type);
        if (notNull) {
            builder.append(", NOT NULL)");
        } else
            builder.append(")");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column)) return false;

        Column column = (Column) o;

        if (name != null ? !name.equals(column.name) : column.name != null) return false;
        return type != null ? type.equals(column.type) : column.type == null;
    }
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
