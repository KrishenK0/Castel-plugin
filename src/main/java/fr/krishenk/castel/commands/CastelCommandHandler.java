package fr.krishenk.castel.commands;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.admin.CommandAdmin;
import fr.krishenk.castel.commands.general.claims.CommandClaim;
import fr.krishenk.castel.commands.general.claims.CommandUnclaim;
import fr.krishenk.castel.commands.general.election.CommandElection;
import fr.krishenk.castel.commands.general.home.CommandHome;
import fr.krishenk.castel.commands.general.home.CommandSetHome;
import fr.krishenk.castel.commands.general.home.CommandUnsetHome;
import fr.krishenk.castel.commands.general.invitations.*;
import fr.krishenk.castel.commands.general.misc.*;
import fr.krishenk.castel.commands.general.misc.mails.CommandMail;
import fr.krishenk.castel.commands.general.misc.map.CommandMap;
import fr.krishenk.castel.commands.general.ranking.CommandDemote;
import fr.krishenk.castel.commands.general.ranking.CommandLeader;
import fr.krishenk.castel.commands.general.ranking.CommandPromote;
import fr.krishenk.castel.commands.general.relation.*;
import fr.krishenk.castel.commands.general.resourcepoints.CommandResourcePoints;
import fr.krishenk.castel.commands.general.teleports.CommandTpa;
import fr.krishenk.castel.commands.general.teleports.CommandTpaAccept;
import fr.krishenk.castel.commands.general.teleports.CommandTpaReject;
import fr.krishenk.castel.commands.general.text.CommandLore;
import fr.krishenk.castel.commands.general.text.CommandRename;
import fr.krishenk.castel.commands.general.text.CommandTag;
import fr.krishenk.castel.commands.general.visualizer.CommandVisualize;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.internal.ProxyBytecodeManipulator;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumMap;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CastelCommandHandler implements CommandExecutor {
        private static final Cooldown<UUID> COOLDOWN;
        protected static Map<SupportedLanguage, Map<String, CastelCommand>> COMMANDS;
        private static CommandHelp HELP_COMMAND;

        static {
            COOLDOWN = new Cooldown<>();
            CastelCommandHandler.initializeCommandsMap();
        }

        private final CastelPlugin plugin;

    public CastelCommandHandler(CastelPlugin plugin) {
            this.plugin = plugin;
//            PluginCommand command = ProxyBytecodeManipulator.registerCommand(plugin, CastelConfig.COMMAND_NAME.getManager().getString(), CastelConfig.COMMAND_ALIASES.getManager().getStringList());
            PluginCommand command = ProxyBytecodeManipulator.registerCommand(plugin, "castel", Collections.singletonList("c"));
            command.setExecutor(this);
            command.setTabCompleter(new TabCompleteManager(plugin));
            CastelCommandHandler.registerCommands();
        }

        private static void unregisterPermissions(Collection< CastelCommand > commands) {
            for (CastelCommand cmd : commands) {
                cmd.unregisterPermissions();
                if (cmd instanceof CastelParentCommand) {
                    unregisterPermissions(((CastelParentCommand) cmd).getChildren(SupportedLanguage.EN));
                }
            }
        }

        private static void initializeCommandsMap() {
            COMMANDS = new QuickEnumMap<>(SupportedLanguage.values());
        }

        public static void reload() {
            CastelCommandHandler.unregisterPermissions(CastelCommandHandler.getCommands(SupportedLanguage.EN).values());
            CastelCommandHandler.initializeCommandsMap();
            CastelCommandHandler.registerCommands();
        }

        public static void registerCommands() {
            HELP_COMMAND = new CommandHelp();
            new CommandAbout();
//            new CommandUpdates();
            new CommandReload();
            new CommandCreate();
            new CommandRename();
            new CommandTag();
            new CommandLore();
            new CommandDisband();
//            new CommandExtractor();
//            new CommandGUI();
            new CommandColor();
            new CommandTpa();
            new CommandTpaAccept();
            new CommandTpaReject();
            new CommandShow();
            new CommandMap();
//            new CommandInvade();
//            new CommandRansack();
//            new CommandChallenge();
//            new CommandTeleport();
//            new CommandSurrender();
            new CommandLeave();
            new CommandKick();
            new CommandVault();
            new CommandPromote();
            new CommandDemote();
            new CommandLeader();
//            new CommandChat();
//            new CommandMute();
//            new CommandUnmute();
//            new CommandBroadcast();
            new CommandMail();
            new CommandHome();
            new CommandSetHome();
            new CommandUnsetHome();
//            new CommandNexus();
            new CommandDonate();
            new CommandPvP();
//            new CommandLanguage();
            new CommandRelations();
            new CommandRevoke();
            new CommandAlly();
            new CommandEnemy();
            new CommandTruce();
            new CommandRejectRelation();
            new CommandResourcePoints();
            new CommandBank();
//            new CommandTradable();
//            new CommandSell();
            new CommandClaim();
            new CommandUnclaim();
//            new CommandRedo();
//            new CommandUndo();
            new CommandVisualize();
            new CommandInvite();
            new CommandInvites();
            new CommandInviteCodes();
            new CommandAccept();
            new CommandDecline();
            new CommandJoin();
            new CommandRequestJoin();
            new CommandJoinRequests();
            new CommandFly();
            new CommandSneak();
            new CommandTop();
//            new CommandBook();
//            new CommandShield();
//            new CommandInventory();
            new CommandElection();
//            new CommandMerge();
//            new CommandNation();
            new CommandInviteCancel();
            CommandAdmin.setInstance(new CommandAdmin());
            CastelCommandHandler.gatherPermissions();
        }

        private static void gatherPermissions() {
            ArrayList<Permission> permissions = new ArrayList<>(50);
            for (CastelCommand value : CastelCommandHandler.getCommands(SupportedLanguage.EN).values()) {
                permissions.add(value.getPermission());
            }
            for (CastelPluginPermission value : CastelPluginPermission.values()) {
                permissions.add(value.getPermission());
            }
            ProxyBytecodeManipulator.injectPermissions(CastelPlugin.getInstance(), permissions);
        }

        public static Map<SupportedLanguage, Map<String, CastelCommand>> getCommands() {
            return Collections.unmodifiableMap(COMMANDS);
        }

        public static Map<String, CastelCommand> getCommands(SupportedLanguage locale){
            return Objects.requireNonNull(COMMANDS.get(locale), () -> "No commands exist for language: " + locale);
        }

        public static CommandInformation getCommand(SupportedLanguage lang, String[] args){
            CastelParentCommand last = null;
            int index = 0;
            for (String arg : args) {
                String lowerArg = arg.toLowerCase(lang.getLocale());
                CastelCommand current = last == null ? CastelCommandHandler.getCommands(lang).get(lowerArg) : last.children.get(lang).get(lowerArg);
                if (current == null) {
                    return new CommandInformation(last, last == null ? index - 1 : index);
                }
                ++index;
                if (!(current instanceof CastelParentCommand)) {
                    return new CommandInformation(current, index);
                }
                last = (CastelParentCommand) current;
            }
            return new CommandInformation(last, index);
        }

        public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String[]args){
            CommandContext ctx;
            CommandResult res;
            if (args.length == 0) {
                HELP_COMMAND.execute(new CommandContext(this.plugin, HELP_COMMAND, sender, args));
                if (sender instanceof Player) {
                    XSound.ITEM_BOOK_PAGE_TURN.play((Player) sender);
                }
                return true;
            }
            SupportedLanguage lang = LanguageManager.localeOf(sender);
            CommandInformation info = CastelCommandHandler.getCommand(lang, args);
            CastelCommand command = info.command;
            if (command == null) {
                CastelLang.COMMANDS_UNKNOWN_COMMAND.sendError(sender, "cmd", String.join(" ", args));
                return false;
            }
            if (sender instanceof Player) {
                long cooldown;
                Player player = (Player) sender;
                if (!info.hasPermission(sender)) {
                    CastelLang.COMMANDS_INSUFFICIENT_PERMISSION.sendError(sender);
                    return false;
                }
                if (!command.getDisabledWorlds().isEmpty() && command.getDisabledWorlds().contains(player.getWorld().getName()) && !command.canBypassDisabledWorlds(player)) {
                    CastelLang.COMMANDS_DISABLED_WORLD.sendError(sender, "cmd", command.getDisplayName(), "world", player.getWorld().getName());
                    return false;
                }
                if (command.getCooldown() > 0L && (cooldown = COOLDOWN.getTimeLeft(player.getUniqueId())) > 0L && !command.canBypassCooldown(player)) {
                    CastelLang.COMMANDS_COOLDOWN.sendError(sender, "cmd", command.getDisplayName(), "cooldown", TimeFormatter.of(cooldown));
                    return false;
                }
            }
            if ((res = command.executeX(ctx = new CommandContext(this.plugin, command, sender, info.getCommandArguments(args)))) == CommandResult.NOT_IMPLEMENTED) {
                command.execute(ctx);
                res = CommandResult.SUCCESS;
            }
            if (res == CommandResult.SUCCESS && sender instanceof Player) {
                COOLDOWN.add(((Player) sender).getUniqueId(), command.getCooldown());
            }
            return res == CommandResult.SUCCESS;
        }

        public static final class CommandInformation {
            public final CastelCommand command;
            final int cmdIndex;

            public CommandInformation(CastelCommand command, int cmdIndex) {
                this.command = command;
                this.cmdIndex = cmdIndex;
            }

            public String[] getCommandArguments(String[] args) {
                String[] cmdArgs = new String[args.length - this.cmdIndex];
                System.arraycopy(args, this.cmdIndex, cmdArgs, 0, cmdArgs.length);
                return cmdArgs;
            }

            public boolean hasPermission(CommandSender sender) {
                return !(sender instanceof Player) || this.command.hasPermission(sender) || CastelPlayer.getCastelPlayer((Player) sender).isAdmin();
            }
        }
    }

