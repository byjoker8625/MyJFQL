package de.byjoker.myjfql.command;

import de.byjoker.myjfql.core.MyJFQL;
import de.byjoker.myjfql.util.Downloader;
import de.byjoker.myjfql.util.Updater;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@CommandExecutor
public class VersionCommand extends ConsoleCommand {

    public VersionCommand() {
        super("version", Arrays.asList("COMMAND", "DISPLAY", "UPDATE"));
    }

    @Override
    public void handleConsoleCommand(ConsoleCommandSender sender, Map<String, List<String>> args) {
        final Updater updater = MyJFQL.getInstance().getUpdater();
        final Downloader downloader = MyJFQL.getInstance().getDownloader();

        if (args.containsKey("DISPLAY")) {
            sender.sendAnswer(Collections.singletonList(MyJFQL.getInstance().getVersion()), new String[]{"Version"});
            return;
        }

        if (args.containsKey("UPDATE")) {
            final List<String> update = args.get("UPDATE");

            if (update.size() == 0)
                downloader.downloadLatestVersion();
            else {
                String version = formatString(update);

                if (version == null) {
                    sender.sendError("Undefined version!");
                    return;
                }

                if (!updater.getVersions().contains(version)) {
                    sender.sendError("Unknown version!");
                    return;
                }

                downloader.downloadByVersion(version);
                return;
            }

            return;
        }

        sender.sendSyntax();
    }

}
