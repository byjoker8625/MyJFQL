package de.byjoker.myjfql.command;

import de.byjoker.myjfql.core.MyJFQL;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CommandExecutor
public class ShutdownCommand extends ConsoleCommand {

    public ShutdownCommand() {
        super("shutdown", Collections.singletonList("COMMAND"));
    }

    @Override
    public void handleConsoleCommand(ConsoleCommandSender sender, Map<String, List<String>> args) {
        MyJFQL.getInstance().shutdown();
    }

}