package fr.krishenk.castel.commands;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.managers.GuildManager;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.internal.identity.QuantumIdentityHashMap;
import fr.krishenk.castel.utils.string.QuantumString;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class TabCompleteManager implements TabCompleter {
    private final CastelPlugin plugin;

    public TabCompleteManager(CastelPlugin plugin) {
        this.plugin = plugin;
    }

    private static @NonNull List<String> getMainSuggestions(CommandSender sender, @Nullable String starts) {
        return new SmartCompleter(CastelCommandHandler.getCommands(LanguageManager.localeOf(sender)).values(), sender, starts).process();
    }

    public static @NonNull List<String> getSubCommand(@NonNull CommandSender sender, @NonNull CastelParentCommand command, @NonNull String[] args) {
        if (args.length == 0) {
            return new ArrayList<String>();
        }
        String starts = args[0];
        return new SmartCompleter(command.getChildren(LanguageManager.localeOf(sender)), sender, starts).process();
    }

    public static @NonNull List<String> getPlayers(@NonNull String starts) {
        return TabCompleteManager.getPlayers(starts, null);
    }

    public static @NonNull List<String> getPlayers(@NonNull String starts, boolean admin) {
        return TabCompleteManager.getPlayers(starts, null);
    }

    public static @NonNull List<String> getPlayers(@NonNull String starts, Predicate<OfflinePlayer> filter) {
        return TabCompleteManager.getPlayers(Bukkit.getOnlinePlayers(), starts, false, filter);
    }

    public static @NonNull List<String> getPlayers(Collection<? extends OfflinePlayer> players, @NonNull String starts, boolean admin, Predicate<OfflinePlayer> filter) {
        ArrayList<String> names = new ArrayList<String>(players.size());
        boolean empty = starts.isEmpty();
        if (!empty) {
            starts = starts.toLowerCase(Locale.ENGLISH);
        }
        for (OfflinePlayer offlinePlayer : players) {
            if (!admin && offlinePlayer instanceof Player && ServiceHandler.isVanished((Player)offlinePlayer)) continue;
            String name = PlayerUtils.validateOfflineName(offlinePlayer);
            if (!empty && !StringUtils.toLatinLowerCase(name).startsWith(starts) || filter != null && !filter.test(offlinePlayer)) continue;
            names.add(name);
        }
        return names;
    }
    public static @NonNull List<String> getGuildPlayers(@NonNull Guild guild, @NonNull String starts) {
        return TabCompleteManager.getPlayers(guild.getPlayerMembers(), starts, true, null);
    }

    public static @NonNull List<String> getGuildPlayers(@NonNull Guild guild, @NonNull String starts, Predicate<OfflinePlayer> filter) {
        return TabCompleteManager.getPlayers(guild.getPlayerMembers(), starts, true, filter);
    }

    public static @NonNull List<String> getGuilds(@Nullable String starts) {
        return TabCompleteManager.getGuilds(starts, null);
    }

    private static @NonNull List<String> getGroups(@NonNull Map<QuantumString, UUID> names, @Nullable QuantumString starts, @Nullable Predicate<String> predicate) {
        ArrayList<String> groups = new ArrayList<String>(Math.max(names.size() / Math.max(1, starts.length()), 30));
        String startsqBit = starts.getQuantum();
        for (QuantumString name : names.keySet()) {
            String quantum = name.getQuantum();
            if (predicate != null && !predicate.test(quantum)) continue;
            if (starts.isEmpty()) {
                groups.add(quantum);
                continue;
            }
            if (!name.getQuantum().startsWith(startsqBit)) continue;
            groups.add(quantum);
        }
        return groups;
    }

    public static @NonNull List<String> getGuilds(@Nullable String starts, @Nullable Predicate<String> predicate) {
        return TabCompleteManager.getGroups(GuildManager.getNames(), GuildManager.toQuantumName(starts), predicate);
    }

    public @NonNull List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) {
            return TabCompleteManager.getMainSuggestions(sender, args[0]);
        }
        CastelCommandHandler.CommandInformation info = CastelCommandHandler.getCommand(LanguageManager.localeOf(sender), args);
        CastelCommand command = info.command;
        if (command == null) {
            return new ArrayList<>();
        }
        if (!info.hasPermission(sender)) {
            return new ArrayList<>();
        }
        String[] commandArguments = info.getCommandArguments(args);
        if (command instanceof CastelParentCommand && args.length == info.cmdIndex) {
            return TabCompleteManager.getSubCommand(sender, (CastelParentCommand) command, commandArguments);
        }
        List<String> result = command.tabComplete(sender, commandArguments);
        if (result != null) {
            return result;
        }
        return command.tabComplete(new CommandTabContext(this.plugin, command, sender, commandArguments));
    }

    private static final class SmartCompleter {
        private final Collection<CastelCommand> source;
        private final List<String> commands = new ArrayList<String>(10);
        private final Set<CastelCommand> addedCommands = Collections.newSetFromMap(new QuantumIdentityHashMap<>());
        private final CommandSender sender;
        private final @Nullable String input;
        private final boolean isAdmin;
        private final SupportedLanguage locale;

        private SmartCompleter(Collection<CastelCommand> source, CommandSender sender, @Nullable String input) {
            this.source = source;
            this.sender = sender;
            this.isAdmin = !(sender instanceof Player) || CastelPlayer.getCastelPlayer((OfflinePlayer) sender).isAdmin();
            this.locale = LanguageManager.localeOf(sender);
            this.input = input.toLowerCase(this.locale.getLocale());
        }

        public List<String> process() {
            this.addSuggestionLevel(false, false);
            if (this.commands.size() <= 5) {
                this.addSuggestionLevel(false, true);
            }
            if (this.commands.size() <= 10) {
                this.addSuggestionLevel(true, false);
            }
            if (this.commands.size() <= 15) {
                this.addSuggestionLevel(true, true);
            }
            return this.commands;
        }

        private void addSuggestion(CastelCommand cmd, String name) {
            if (this.addedCommands.add(cmd)) {
                this.commands.add(name);
            }
        }

        private void addSuggestionLevel(boolean alias, boolean contains) {
            for (CastelCommand cmd : this.source) {
                List<String> commands;
                if (!this.isAdmin && !cmd.hasPermission(this.sender)) continue;
                if (alias) {
                    commands = cmd.aliases.get((Object)this.locale);
                    if (commands.isEmpty()) {
                        continue;
                    }
                } else {
                    commands = Collections.singletonList(cmd.getDisplayName().getMessageObject(this.locale).buildPlain(MessageBuilder.DEFAULT));
                }
                for (String name : commands) {
                    String lowerName = name.toLowerCase(this.locale.getLocale());
                    if (contains) {
                        if (!lowerName.contains(this.input)) continue;
                        this.addSuggestion(cmd, name);
                        continue;
                    }
                    if (!lowerName.startsWith(this.input)) continue;
                    this.addSuggestion(cmd, name);
                }
            }
        }
    }
}

