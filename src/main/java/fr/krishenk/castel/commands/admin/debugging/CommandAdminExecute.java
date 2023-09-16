package fr.krishenk.castel.commands.admin.debugging;

import com.google.common.base.Enums;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CommandAdminExecute extends CastelParentCommand {
    public CommandAdminExecute(CastelParentCommand parent) {
        super("execute", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertArgs(3)) context.sendMessage(Lang.COMMAND_ADMIN_EXECUTE_USAGE);
        else {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                Executor executor = Enums.getIfPresent(Executor.class, context.arg(1).toUpperCase(Locale.ENGLISH)).orNull();
                if (executor == null) {
                    context.sendError(Lang.COMMAND_ADMIN_EXECUTE_UNKNOWN_COMMAND, "executor", context.arg(1));
                } else {
                    Boolean filter = null;
                    String thirdArg = context.arg(2);
                    String command;
                    if (thirdArg.charAt(0) != '/') {
                        thirdArg = thirdArg.toLowerCase(Locale.ENGLISH);
                        if (thirdArg.equals("online")) filter = true;
                        else {
                            if (!thirdArg.equals("offline")) {
                                context.sendError(Lang.COMMAND_ADMIN_EXECUTE_UNKNOWN_FILTER, "filter", context.arg(2));
                                return;
                            }

                            filter = false;
                            if (executor == Executor.MEMBERS) {
                                context.sendError(Lang.COMMAND_ADMIN_EXECUTE_MEMBERS_OFFLINE, "filter", context.arg(2));
                                return;
                            }
                        }

                        command = context.joinArgs(" ", 3);
                    } else command = context.joinArgs(" ", 2);

                    if (executor == Executor.MEMBERS && filter == null) {
                        context.sendError(Lang.COMMAND_ADMIN_EXECUTE_MEMBERS_NO_FILTER, "filter", context.arg(2));
                    } else {
                        if (command.charAt(0) == '/') command = command.substring(1);

                        int mainIndex = command.indexOf(32);
                        String mainCmd = command.substring(0, mainIndex < 0 ? command.length() : mainIndex);
                        PluginCommand cmd = Bukkit.getPluginCommand(mainCmd);
                        if (cmd == null)
                            context.sendMessage(Lang.COMMAND_ADMIN_EXECUTE_UNKNOWN_COMMAND, "command", mainCmd, "guild", guild.getName());

                        context.sendMessage(Lang.COMMAND_ADMIN_EXECUTE_EXECUTED, "command", cmd == null ? "~" : cmd.getName(), "plugin", cmd == null ? " server" : cmd.getPlugin().getName(), "guild", guild.getName());

                        Iterator<OfflinePlayer> it = guild.getPlayerMembers().iterator();

                        while (true) {
                            OfflinePlayer member;
                            do {
                                if (!it.hasNext()) return;
                                member = it.next();
                            } while (filter != null && filter != member.isOnline());

                            CommandSender currentExecutor;
                            switch (executor) {
                                case SELF:
                                    currentExecutor = context.getSender();
                                    break;
                                case MEMBERS:
                                    currentExecutor = member.getPlayer();
                                    break;
                                default:
                                    throw new AssertionError();
                            }

                            Bukkit.dispatchCommand(Objects.requireNonNull(currentExecutor), MessageCompiler.compile(command).buildPlain(new MessageBuilder().withContext(member.getPlayer())));
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) return context.getGuilds(0);
        if (context.isAtArg(1)) return tabComplete("self", "members");
        if (context.isAtArg(2)) return context.arg(1).equalsIgnoreCase("members") ? tabComplete("online") : tabComplete("online", "offline");
        if (context.isAtArg(3) && context.argIsAny(2, "online", "offline")) return tabComplete("<command>");
        if (context.assertArgs(4)) {
            String command = context.arg(3);
            int mainIndex = command.indexOf(32);
            String mainCmd = command.substring(0, mainIndex < 0 ? command.length() : mainIndex);
            PluginCommand cmd = Bukkit.getPluginCommand(mainCmd);
            if (cmd == null) return emptyTab();
            else {
                String[] cmdArgs = Arrays.copyOfRange(context.getArgs(), 3, context.getArgs().length);
                return cmd.tabComplete(context.getSender(), mainCmd, cmdArgs);
            }
        }
        return emptyTab();
    }

    private enum Executor {
        SELF,
        MEMBERS
    }
}
