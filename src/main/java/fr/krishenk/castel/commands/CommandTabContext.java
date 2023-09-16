package fr.krishenk.castel.commands;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelChatChannel;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.managers.GuildManager;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.utils.string.QuantumString;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandTabContext extends CommandContext {
    public CommandTabContext(CastelPlugin plugin, CastelCommand command, CommandSender sender, String[] args) {
        super(plugin, command, sender, args);
    }

//    public static @NonNull List<String> getGuildMembers(@Nullable String starts, @Nullable Predicate<Guild> predicate) {
//        List<Guild> Castel = nation.getCastel();
//        ArrayList<String> names = new ArrayList<String>(Castel.size());
//        boolean empty = starts.isEmpty();
//        for (Guild guild : Castel) {
//            String name = guild.getName();
//            if (!empty && !name.toLowerCase(Locale.ENGLISH).startsWith(starts) || predicate != null && !predicate.test(guild)) continue;
//            names.add(name);
//        }
//        return names;
//    }

    private static @NonNull List<String> getGroups(@NonNull Map<QuantumString, UUID> names, @Nullable QuantumString starts, @Nullable Predicate<String> predicate) {
        ArrayList<String> groups = new ArrayList<>(Math.max(names.size() / Math.max(1, starts.length()), 30));
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

    public String tab(String path) {
        String obj = this.lang("tab-completion", path).parse(this.settings);
        return obj == null ? "" : obj;
    }

    public Stream<String> filter(Stream<String> stream, int index) {
        return !this.assertArgs(index - 1) ? stream : stream.filter(x -> x.startsWith(this.arg(index)));
    }

    public List<String> suggest(int index, Collection<String> items) {
        return this.suggest(index, items.toArray(new String[0]));
    }

    public List<String> suggest(int index, String ... items) {
        String starts = this.arg(index);
        if (starts.isEmpty()) {
            return Arrays.asList(items);
        }
        ArrayList<String> suggestions = new ArrayList<String>(items.length);
        starts = StringUtils.toLatinLowerCase(starts);
        for (String item : items) {
            if (!StringUtils.toLatinLowerCase(item).startsWith(starts)) continue;
            suggestions.add(item);
        }
        if (suggestions.size() <= 5) {
            for (String item : items) {
                if (!StringUtils.toLatinLowerCase(item).contains(starts)) continue;
                suggestions.add(item);
            }
        }
        return suggestions;
    }

    public List<String> getChannels(int index, Predicate<CastelChatChannel> filter) {
        return this.suggest(index, CastelChatChannel.getChannels().values().stream().filter(filter).map(x -> x.getName().buildPlain(this.getSettings())).toArray(String[]::new));
    }

    public @NonNull List<String> getPlayers(int index) {
        return this.getPlayers(index, null);
    }

    public List<String> add(List<String> list2, String ... others) {
        list2.addAll(Arrays.asList(others));
        return list2;
    }

    public @NonNull List<String> getPlayers(int index, Predicate<OfflinePlayer> filter) {
        return this.getPlayers(Bukkit.getOnlinePlayers(), this.arg(index), false, filter);
    }

    public @NonNull List<String> getPlayers(Collection<? extends OfflinePlayer> players, @NonNull String starts, boolean admin, Predicate<OfflinePlayer> filter) {
        return TabCompleteManager.getPlayers(players, starts, admin, filter);
    }

    public String currentArg() {
        return this.args[this.args.length - 1];
    }

    public @NonNull List<String> getGuildPlayers(@NonNull Guild guild, int index) {
        return this.getPlayers(guild.getPlayerMembers(), this.arg(index), true, null);
    }

    public @NonNull List<String> getGuildPlayers(@NonNull Guild guild, @NonNull String starts, Predicate<OfflinePlayer> filter) {
        return this.getPlayers(guild.getPlayerMembers(), starts, true, filter);
    }

    public @NonNull List<String> getGuilds(int index) {
        return this.getGuilds(index, null);
    }

    public @NonNull List<String> getGuilds(int index, @Nullable Predicate<String> predicate) {
        return CommandTabContext.getGroups(GuildManager.getNames(), this.assertArgs(index + 1) ? GuildManager.toQuantumName(this.arg(index)) : QuantumString.empty(), predicate);
    }

    public <T extends Group> List<String> tabCompleteGeneralSelector(boolean targetNation, boolean includeIfNotExist, Predicate<T> filter) {
        String startsWith;
        int from = this.nextArg();
        if (this.args.length - 1 < from || this.args.length - 1 > from + 1) {
            return new ArrayList<>();
        }
        String playerTag = CastelLang.COMMANDS_TAGS_IDENTIFIER_PLAYERS.parse(this.sender);
        String guildTag = CastelLang.COMMANDS_TAGS_IDENTIFIER_GUILDS.parse(this.sender);
        ArrayList<String> list2 = new ArrayList<>();
        boolean addPlayerNames = false;
        boolean addGuildNames = false;
        if (this.args.length - 1 == from) {
            startsWith = this.args[from];
            list2.add(playerTag);
            list2.add(guildTag);
            if (startsWith.isEmpty()) {
                list2.add(CastelLang.COMMANDS_TAGS_PLAYERS.parse(this.sender));
                list2.add(CastelLang.COMMANDS_TAGS_IDENTIFIER_GUILDS.parse(this.sender));
            } else {
                addGuildNames = true;
                addPlayerNames = true;
            }
        } else {
            startsWith = this.args[this.nextArg()];
            String previous = this.args[from].toLowerCase();
            if (previous.equals(playerTag.toLowerCase())) {
                addPlayerNames = true;
            } else if (previous.equals(guildTag.toLowerCase())) {
                addGuildNames = true;
            }
        }
        if (addPlayerNames) {
            list2.addAll(TabCompleteManager.getPlayers(startsWith, player -> {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Group group = cp.getGuild();
                if (group == null) {
                    return includeIfNotExist;
                }
                return filter.test((T) group);
            }));
        }
        if (addGuildNames) {
            list2.addAll(TabCompleteManager.getGuilds(startsWith, guildName -> {
                Group group = Guild.getGuild(guildName);
                if (group == null) {
                    return includeIfNotExist;
                }
                return filter.test((T) group);
            }));
        }
        return list2;
    }
}

