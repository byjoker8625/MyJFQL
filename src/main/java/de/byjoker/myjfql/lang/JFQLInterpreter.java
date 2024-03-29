package de.byjoker.myjfql.lang;

import de.byjoker.myjfql.core.MyJFQL;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JFQLInterpreter implements Interpreter {

    @Override
    public Map<String, List<String>> interpretCommand(String command) {
        if (command == null)
            return null;

        final String[] cmd = command.split(" ");

        final List<String> strings = new ArrayList<>();
        StringBuilder builder = null;

        for (int i = 0; i < cmd.length; i++) {
            String current = cmd[i];

            if (i == 0) {
                strings.add("COMMAND");
                strings.add(current);
            } else {
                if (current.startsWith("'") && current.endsWith("'") && !current.equals("'")) {
                    strings.add(current);
                } else if ((current.equals("'") && builder == null) || (current.startsWith("'") && !current.endsWith("'"))) {
                    builder = new StringBuilder(current);
                } else if (current.equals("'") || (!current.startsWith("'") && current.endsWith("'"))) {
                    if (builder == null) {
                        strings.add(current);
                    } else {
                        builder.append(" ").append(current);
                        strings.add(builder.toString());
                        builder = null;
                    }
                } else {
                    if (builder == null) {
                        strings.add(current);
                    } else {
                        builder.append(" ").append(current);
                    }
                }
            }

        }

        List<String> syntax;

        try {
            syntax = MyJFQL.getInstance().getCommandService().getCommand(strings.get(1)).getSyntax();
        } catch (Exception ex) {
            return null;
        }

        final Map<String, List<String>> arguments = new HashMap<>();
        String section = null;

        for (String current : strings) {
            if (syntax.contains(current.toUpperCase())) {
                section = current.toUpperCase();
                arguments.put(section, new ArrayList<>());
            } else {
                if (section == null) {
                    break;
                }

                if (!arguments.containsKey(section)) {
                    arguments.put(section, Collections.singletonList(current));
                } else {
                    arguments.get(section).add(current);
                }
            }
        }

        return arguments;
    }

    @Override
    public final String parseString(List<String> strings) {
        if (strings.size() == 0)
            return null;

        return IntStream.range(1, strings.size()).mapToObj(i -> " " + strings.get(i)).collect(Collectors.joining("", strings.get(0), "")).replace("'", "");
    }

    @Override
    public final List<String> parseList(List<String> strings) {
        if (strings.size() == 0)
            return new ArrayList<>();

        return strings.stream().map(s -> s.replace("'", "")).collect(Collectors.toList());
    }

    @Override
    public final int parseInteger(List<String> strings) {
        if (strings.size() == 0)
            return -1;

        return Integer.parseInt(IntStream.range(1, strings.size()).mapToObj(i -> " " + strings.get(i)).collect(Collectors.joining("", strings.get(0), "")).replace("'", ""));
    }

}
