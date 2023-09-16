package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelCommandHandler;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.enums.CommandLang;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.CmdHelpPagination;
import fr.krishenk.castel.utils.internal.nonnull.NonNullSet;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHelp extends CastelCommand {
    public CommandHelp() {
        super("help", true);
    }

    private static int getPageNumbers(int commands) {
        return commands % 5 != 0 ? commands / 5 + 1 : commands / 5;
    }

    private static CastelCommand[] filterAccessiblePages(CommandSender sender) {
        Map<String, CastelCommand> all = CastelCommandHandler.getCommands(SupportedLanguage.EN);
        NonNullSet<CastelCommand> orderedCommands = new NonNullSet<>(new LinkedHashSet<>(all.size()));
        orderedCommands.addAll(Config.HELP_ORDER.getStringList().stream().map(x -> {
            CastelCommand res = all.get(x);
            if (res == null) {
                CLogger.warn("Unknown command for /c help order: " + x);
            }
            return res;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        orderedCommands.addAll(all.values());
        return orderedCommands.stream().filter(c -> c.hasPermission(sender)).toArray(CastelCommand[]::new);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.assertArgs(1) && !context.isNumber(0)) {
            CommandHelp.showInfoOf(context, context.arg(0), Lang.COMMAND_HELP_INFO);
            return;
        }
        new CmdHelpPagination(context, Config.HELP_COMMANDS.getInt(), () -> CommandHelp.filterAccessiblePages(context.getSender())).execute();
    }

    public static void showInfoOf(CommandContext context, String first, Messenger infoMsg) {
        CastelLang permScope;
        if (first.startsWith("/") || first.equals("c")) {
            context.sendError(CommandLang.COMMAND_HELP_BAD_START);
            return;
        }
        CastelCommandHandler.CommandInformation cmdInfo = CastelCommandHandler.getCommand(context.getSettings().getLanguage(), context.args);
        CastelCommand cmd = cmdInfo.command;
        context.var("command", context.joinArgs());
        if (cmd == null) {
            context.sendError(CommandLang.COMMAND_HELP_NOT_FOUND);
            return;
        }
        Object usage = cmd.getUsage().parse(context.getSender());
        if (usage == null) {
            usage = CastelLang.NONE;
        }
        switch (cmd.getPermission().getDefault()) {
            case TRUE: {
                permScope = CastelLang.PLUGIN_PERMISSION_SCOPE_EVERYONE;
                break;
            }
            case FALSE: {
                permScope = CastelLang.PLUGIN_PERMISSION_SCOPE_NO_ONE;
                break;
            }
            case OP: {
                permScope = CastelLang.PLUGIN_PERMISSION_SCOPE_OP;
                break;
            }
            case NOT_OP: {
                permScope = CastelLang.PLUGIN_PERMISSION_SCOPE_NOT_OP;
                break;
            }
            default: {
                CLogger.error("Unknown permission type: " + cmd.getPermission().getDefault());
                permScope = CastelLang.UNKNOWN;
            }
        }
        context.getSettings().resetPlaceholders();
        context.getSettings().raws("main-name", cmd.getName(), "parent", cmd.getParent() != null ? cmd.getParent().getName() : CastelLang.NONE, "command-displayname", cmd.getDisplayName(), "description", cmd.getDescription().parse(context.getSender()), "usage", usage, "permission", cmd.getPermission().getName(), "permission-scope", permScope, "cooldown", cmd.getCooldown() >= 0L ? CastelLang.NONE : TimeFormatter.of(cmd.getCooldown()));
        List<String> aliases = cmd.getAliases().get(context.getSettings().getLanguage());
        if (aliases.isEmpty()) {
            context.getSettings().raw("aliases", CastelLang.NONE);
        } else {
            context.getSettings().parse("aliases", StringUtils.join("&8, ", aliases, x -> "&9" + x));
        }
        if (cmd.getDisabledWorlds().isEmpty()) {
            context.getSettings().raw("disabled-worlds", CastelLang.NONE);
        } else {
            context.getSettings().parse("disabled-worlds", StringUtils.join("&8, ", cmd.getDisabledWorlds(), x -> "&9" + x));
        }
        context.sendMessage(infoMsg);
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        int maxPages = CommandHelp.getPageNumbers(context.isPlayer() ? CastelCommandHandler.getCommands(context.getSettings().getLanguage()).size() : CommandHelp.filterAccessiblePages(context.senderAsPlayer()).length);
        ArrayList<String> numbers = new ArrayList<>(maxPages);
        for (int i = 0; i < maxPages; ++i) {
            numbers.add(Integer.toString(i));
        }
        return numbers;
    }
}
