package fr.krishenk.castel.utils.commands;

import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class ConfigCommand {
    private final ExecutorType executorType;
    private final MessageObject command;
    private static final MessageCompilerSettings COMPILER_SETTINGS = (new MessageCompilerSettings()).colorize().translatePlaceholders();

    public ConfigCommand(ExecutorType executorType, MessageObject command) {
        this.executorType = executorType;
        this.command = command;
    }

    public static List<ConfigCommand> parse(Collection<String> commands) {
        if (commands == null) return Collections.emptyList();
        List<ConfigCommand> list = new ArrayList<>(commands.size());

        String command;
        ExecutorType executorType;
        for (Iterator<String> it = commands.iterator(); it.hasNext(); list.add(new ConfigCommand(executorType, MessageCompiler.compile(command, COMPILER_SETTINGS)))) {
            command = it.next();
            int index = command.indexOf(58);
            if (index != -1) {
                String option = StringUtils.toLatinUpperCase(command.substring(0, index));
                command = command.substring(index + 1);
                if (option.equals("CONSOLE")) {
                    executorType = ExecutorType.CONSOLE;
                } else {
                    if (!option.equals("OP"))
                        throw new IllegalArgumentException("Unknown command executor '" + option + "' in command: " + command);
                    executorType = ExecutorType.OP;
                }
            } else {
                executorType = ExecutorType.NORMAL;
            }

            if (command.charAt(0) == '/') command = command.substring(1);
        }

        return list;
    }

    public static void execute(Player player, Collection<ConfigCommand> commands, MessageBuilder settings, boolean ignoreIfPlayerNull) {
        Consumer<ConfigCommand> checkPlayer = cmd -> Objects.requireNonNull(player, "An online player must be present to execute the command '" + cmd.command.buildPlain(settings) + "' of Type " + cmd.executorType);

        Iterator<ConfigCommand> it = commands.iterator();
        while (it.hasNext()) {
            ConfigCommand command = it.next();
            String parsedCmd = command.command.buildPlain(settings);
            switch (command.executorType) {
                case OP:
                    if (!ignoreIfPlayerNull) {
                        checkPlayer.accept(command);
                        player.setOp(true);
                        Bukkit.dispatchCommand(player, parsedCmd);
                        player.setOp(false);
                    }
                    break;
                case CONSOLE:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
                    break;
                case NORMAL:
                    if (!ignoreIfPlayerNull) {
                        checkPlayer.accept(command);
                        Bukkit.dispatchCommand(player, parsedCmd);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private enum ExecutorType {
        OP,
        CONSOLE,
        NORMAL;
    }
}
