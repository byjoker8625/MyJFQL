package de.byjoker.myjfql.database

data class DatabaseRepresentation(val id: String, val name: String, val tables: List<String>, val type: DatabaseType) {

    constructor(database: Database) : this(
        database.id,
        database.name,
        database.tables.map { table -> table.name },
        database.type
    )
}
