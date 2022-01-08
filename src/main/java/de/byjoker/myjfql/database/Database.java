package de.byjoker.myjfql.database;

import de.byjoker.jfql.util.ID;
import de.byjoker.myjfql.exception.FileException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Database implements TableService {

    private final String name;
    private String id;
    private Map<String, Table> tables;

    public Database(String name) {
        this.id = ID.generateString().toString();
        this.name = name;
        this.tables = new HashMap<>();
    }

    public Database(String id, String name) {
        this.id = id;
        this.name = name;
        this.tables = new HashMap<>();
    }

    @Override
    public void regenerateId() {
        this.id = ID.generateString().toString();
    }

    @Override
    public void createTable(Table table) {
        if (getTable(table.getName()) != null)
            throw new FileException("Table already exists in database!");

        saveTable(table);
    }

    @Override
    public void saveTable(Table table) {
        tables.put(table.getName(), table);
    }

    @Override
    public boolean existsTable(String name) {
        return tables.containsKey(name);
    }

    @Override
    public void deleteTable(String name) {
        tables.remove(name);
    }

    @Override
    public Table getTable(String name) {
        return tables.get(name);
    }

    @Override
    public Collection<Table> getTables() {
        return tables.values();
    }

    public void setTables(Map<String, Table> tables) {
        this.tables = tables;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Database database = (Database) o;
        return Objects.equals(id, database.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Database{" +
                "id='" + id + '\'' +
                ", tables=" + tables.values() +
                ", name='" + name + '\'' +
                '}';
    }
}

