package de.byjoker.myjfql.command;

import de.byjoker.myjfql.core.MyJFQL;
import de.byjoker.myjfql.database.Database;
import de.byjoker.myjfql.database.DatabaseService;
import de.byjoker.myjfql.database.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@CommandExecutor
public class StructureCommand extends Command {

    public StructureCommand() {
        super("structure", Arrays.asList("COMMAND", "ADD", "SET", "REMOVE", "OF", "MARK-PRIMARY", "PRIMARY-KEY"));
    }

    @Override
    public void handleCommand(CommandSender sender, Map<String, List<String>> args) {
        final DatabaseService databaseService = MyJFQL.getInstance().getDatabaseService();
        final Database database = MyJFQL.getInstance().getDBSession().getDirectlyDatabase(sender.getName());

        if (database == null) {
            sender.sendError("No database is in use for this user!");
            return;
        }

        if (args.containsKey("OF")) {
            final String name = formatString(args.get("OF"));

            if (name == null) {
                sender.sendError("Undefined table!");
                return;
            }

            if (!database.existsTable(name)) {
                sender.sendError("Table doesn't exists!");
                return;
            }

            final String databaseName = database.getName();

            if ((sender.hasPermission("-use.table." + name + "." + databaseName) || sender.hasPermission("-use.table.*." + databaseName))
                    || (!sender.hasPermission("use.table." + name + "." + databaseName) && !sender.hasPermission("use.table.*." + databaseName))) {
                sender.sendForbidden();
                return;
            }

            final Table table = database.getTable(name);
            final List<String> structure = table.getStructure();
            final String primary = table.getPrimary();

            if (!args.containsKey("ADD") && !args.containsKey("REMOVE") && !args.containsKey("SET") && !args.containsKey("MARK-PRIMARY")) {
                if (args.containsKey("PRIMARY-KEY")) {
                    sender.sendAnswer(Collections.singletonList(primary), new String[]{"Primary"});
                    return;
                }

                sender.sendAnswer(structure, new String[]{"Structure"});
                return;
            }

            if (args.containsKey("ADD")) {
                final String argument = formatString(args.get("ADD"));

                if (structure.contains(argument)) {
                    sender.sendError("Key already contains in structure of table!");
                    return;
                }

                structure.add(argument);
                sender.sendSuccess();

                table.setStructure(structure);
                database.saveTable(table);
                databaseService.saveDatabase(database);
                return;
            }

            if (args.containsKey("REMOVE")) {
                final String argument = formatString(args.get("REMOVE"));

                if (!structure.contains(argument)) {
                    sender.sendError("Key doesn't contains in structure of table!");
                    return;
                }

                if (primary.equals(argument)) {
                    sender.sendError("Primary key can't be removed!");
                    return;
                }

                structure.remove(argument);

                sender.sendSuccess();

                table.setStructure(structure);
                database.saveTable(table);
                databaseService.saveDatabase(database);
                return;
            }

            if (args.containsKey("MARK-PRIMARY")) {
                final String argument = formatString(args.get("MARK-PRIMARY"));

                if (!structure.contains(argument)) {
                    sender.sendError("Key doesn't contains in structure of table!");
                    return;
                }

                if (primary.equals(argument)) {
                    sender.sendError("Key is already the primary key!");
                    return;
                }

                table.setPrimary(argument);

                sender.sendSuccess();

                database.saveTable(table);
                databaseService.saveDatabase(database);
                return;
            }

            if (args.containsKey("SET")) {
                final List<String> newStructure = formatList(args.get("SET"));

                if (newStructure.size() == 0) {
                    sender.sendError("Structures size cant be 0!");
                    return;
                }

                if (structure.equals(newStructure)) {
                    sender.sendError("Structure is the same as before!");
                    return;
                }

                table.setStructure(newStructure);

                if (args.containsKey("PRIMARY-KEY")) {
                    final String key = formatString(args.get("PRIMARY-KEY"));

                    if (key == null) {
                        sender.sendError("Key can't be null!");
                        return;
                    }

                    if (!structure.contains(key)) {
                        sender.sendError("Unknown key!");
                        return;
                    }

                    table.setPrimary(key);
                } else {
                    table.setPrimary(newStructure.get(0));
                }


                sender.sendSuccess();

                database.saveTable(table);
                databaseService.saveDatabase(database);
                return;
            }

            return;
        }

        sender.sendSyntax();
    }
}
