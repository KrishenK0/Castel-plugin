package fr.krishenk.castel.commands;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.SimpleMessenger;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.common.value.qual.IntRange;

import java.util.Locale;
import java.util.StringJoiner;

public class CommandContext extends SimpleMessenger {
    public final CastelPlugin plugin;
    public final String[] args;
    private final CastelCommand command;
    private static final int MAX_ARGS = 5;
    private int argPosition;

    public CommandContext(CastelPlugin plugin, CastelCommand command, CommandSender sender, String[] args) {
        super(sender, new MessageBuilder().withContext(sender));
        this.settings.lang(this.isPlayer() ? this.getCastelPlayer().getLanguage() : SupportedLanguage.EN);
        this.plugin = plugin;
        this.command = command;
        this.args = args;
    }

    public String arg(@IntRange(from=0L, to=5L) int index) {
        return this.args[index];
    }

    public boolean argIsAny(@IntRange(from=0L, to=5L) int index, String ... options) {
        String arg = this.arg(index).toLowerCase();
        for (String option : options) {
            if (!arg.equals(option)) continue;
            return true;
        }
        return false;
    }

    public String joinArgs() {
        return this.joinArgs(" ");
    }

    public String joinArgs(String delimiter) {
        return String.join(delimiter, this.args);
    }

    public String joinArgs(String delimiter, @IntRange(from=0L, to=5L) int index) {
        StringJoiner joiner = new StringJoiner(delimiter);
        while (index < this.args.length) {
            joiner.add(this.args[index++]);
        }
        return joiner.toString();
    }

    public CastelCommand getCommand() {
        return this.command;
    }

    public Integer parseInt(int index, Messenger argName, boolean allowNegative) {
        String arg = this.arg(index);
        this.settings.raw("arg", argName);
        try {
            int num = Integer.parseInt(arg);
            if (allowNegative) {
                return num;
            }
            if (num < 0) {
                this.sendMessage(this.command.getUsage(), this.settings);
                this.sendError(Lang.INVALID_NUMBER_NEGATIVE);
                return null;
            }
            return num;
        }
        catch (NumberFormatException ex) {
            this.sendMessage(this.command.getUsage(), this.settings);
            this.sendError(Lang.INVALID_NUMBER);
            return null;
        }
    }

    public Boolean parseBool(@IntRange(from=0L, to=5L) int index) {
        String str = this.arg(index).toLowerCase(Locale.ENGLISH);
        if (str.equals("true") || Lang.TRUE.parse(this.sender).toLowerCase().equals(str)) {
            return true;
        }
        if (str.equals("false") || Lang.FALSE.parse(this.sender).toLowerCase().equals(str)) {
            return false;
        }
        this.sendError(Lang.INVALID_BOOLEAN, "arg", this.arg(index));
        return null;
    }

    public String arg(@IntRange(from=0L, to=5L) int index, String def) {
        return this.assertArgs(index + 1) ? this.args[index] : def;
    }

    public CommandContext debug(String msg) {
        CLogger.info(msg);
        return this;
    }

    public int nextArg() {
        return this.argPosition++;
    }

    public <T extends Group> T generalSelector(boolean targetNation) {
        CastelPlayer cp;
        Guild guild;
        OfflinePlayer player;
        Guild guild2;
        String playerTag = Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS.parse(this.sender).toLowerCase();
        String guildTag = Lang.COMMANDS_TAGS_IDENTIFIER_GUILDS.parse(this.sender).toLowerCase();
//        String nationTag = Lang.COMMANDS_TAGS_IDENTIFIER_NATIONS.parse(this.sender, new Object[0]).toLowerCase();
        int from = this.nextArg();
        String identifier = this.args[from].toLowerCase();
        String second = this.args.length - 1 < from + 1 ? null : this.args[this.nextArg()];
        if (identifier.equals(playerTag) || identifier.equals(Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS.parse(SupportedLanguage.EN, new MessageBuilder()).toLowerCase())) {
            if (second == null) {
                this.wrongUsage();
                return null;
            }
            OfflinePlayer player2 = this.getOfflinePlayer(from + 1);
            if (player2 == null) {
                return null;
            }
            CastelPlayer kp2 = CastelPlayer.getCastelPlayer(player2);
            Guild group = kp2.getGuild();
            if (group == null) {
                this.sendError(Lang.NOT_FOUND_PLAYER_NO_GUILD);
                return null;
            }
//            Group group = guild3;
//            if (group == null) {
//                this.sendError(Lang.NOT_FOUND_PLAYER_NO_NATION);
//                return null;
//            }
            return (T)group;
        }
        if (identifier.equals(guildTag) || identifier.equals(Lang.COMMANDS_TAGS_IDENTIFIER_GUILDS.parse(SupportedLanguage.EN, new MessageBuilder()).toLowerCase())) {
            if (second == null) {
                this.wrongUsage();
                return null;
            }
            Guild guild4 = this.getGuild(from + 1);
            if (guild4 == null) {
                return null;
            }
            Group group = guild4;
            if (group == null) {
                this.sendError(Lang.NO_NATION_OTHER);
                return null;
            }
            return (T)group;
        }
        if (second != null) {
            this.wrongUsage();
            return null;
        }
        identifier = this.arg(from);
        Group group = null;
        if ((guild2 = Guild.getGuild(identifier)) != null) {
            group = guild2;
        }
        if (group == null && (player = PlayerUtils.getOfflinePlayer(identifier)) != null && (guild = CastelPlayer.getCastelPlayer(player).getGuild()) != null) {
            group = guild;
        }
        if (group == null) {
            this.settings.raw("arg", identifier);
            if (targetNation) {
                this.sendError(Lang.NOT_FOUND_PLAYER_OR_GUILD_OR_NATION);
            } else {
                this.sendError(Lang.NOT_FOUND_PLAYER_OR_GUILD);
            }
        }
        return (T)group;
    }

    public CommandResult fail(Messenger lang, Object ... edits) {
        super.sendError(lang, edits);
        return CommandResult.FAILED;
    }

    public void wrongUsage() {
        this.sendError(this.command.getUsage());
    }

    /**
     * Check if the command sender is a player or not.
     * Send a {@link Lang#COMMANDS_PLAYERS_ONLY} error to the sender if he is not a Player for a commands <b>player only</b>.
     * @return true if not a player; false if not
     */
    public boolean assertPlayer() {
        if (this.isPlayer()) {
            return false;
        }
        Lang.COMMANDS_PLAYERS_ONLY.sendError(this.sender);
        return true;
    }

    public CastelPlugin getPlugin() {
        return this.plugin;
    }

    public String[] getArgs() {
        return this.args;
    }

    /**
     * Get the sender as a {@link Player} instance
     * @return sender {@link Player} object
     */
    public Player senderAsPlayer() {
        return (Player)this.sender;
    }

    public boolean assertArgs(@IntRange(from=1L, to=5L) int len) {
        return this.args.length >= len;
    }

    /**
     * Check if the sender has sended the same number of args as {@code len}.
     * @param len
     * @return true if equals; false if not
     */
    public boolean requireArgs(@IntRange(from=1L, to=5L) int len) {
        if (!this.assertArgs(len)) {
            this.wrongUsage();
            return true;
        }
        return false;
    }

    /**
     * Check if the command sender has a guild.
     * Send a {@link Lang#NO_GUILD_DEFAULT} error to the player has no guild.
     * @return false if member of a guild; true if not
     */
    public boolean assertHasGuild() {
        if (!this.getCastelPlayer().hasGuild()) {
            this.sendError(Lang.NO_GUILD_DEFAULT);
            return true;
        }
        return false;
    }

    public Messenger lang(String ... entries) {
        return this.command.lang(entries);
    }

    public boolean argsLengthEquals(int len) {
        return this.args.length == len;
    }

    public boolean isAtArg(@IntRange(from=0L, to=5L) int index) {
        return this.args.length == index + 1;
    }

    public int intArg(@IntRange(from=0L, to=5L) int index) {
        return Integer.parseInt(this.arg(index));
    }

    public boolean isNumber(int index) {
        return StringUtils.isNumeric(this.arg(index));
    }

    public Double getDouble(int index) {
        try {
            return Double.parseDouble(this.arg(index));
        }
        catch (NumberFormatException ignored) {
            this.settings.raw("arg", this.args[1]).raw("needed", "argument");
            this.sendError(Lang.INVALID_NUMBER);
            return null;
        }
    }

    public Integer getInt(int index) {
        try {
            return Integer.parseInt(this.arg(index));
        }
        catch (NumberFormatException ignored) {
            this.settings.raw("arg", this.args[1]).raw("needed", "argument");
            this.sendError(Lang.INVALID_NUMBER);
            return null;
        }
    }

    public CastelPlayer getCastelPlayer() {
        return CastelPlayer.getCastelPlayer(this.senderAsPlayer());
    }

    public Guild getGuild() {
        return this.getCastelPlayer().getGuild();
    }

    public Guild getGuild(int index) {
        Guild guild = Guild.getGuild(this.arg(index));
        if (guild == null) {
            this.settings.raw("guild", this.arg(index));
            this.sendError(Lang.NOT_FOUND_GUILD);
        }
        return guild;
    }

    public boolean argEquals(int index, Lang lang) {
        return this.argEquals(index, lang.parse());
    }

    public boolean argEquals(int index, String str) {
        return this.args.length > index && this.arg(index).equals(str);
    }

    @Override
    public CommandSender getSender() {
        return this.sender;
    }

    public boolean hasPermission(CastelPluginPermission permission) {
        return this.hasPermission(permission, false);
    }

    public boolean hasPermission(CastelPluginPermission permission, boolean checkAdmin) {
        return permission.hasPermission(this.sender, checkAdmin);
    }

    public boolean isAdmin() {
        return !this.isPlayer() || this.getCastelPlayer().isAdmin();
    }

    public Player getPlayer(int index) {
        return this.getPlayer(index, true);
    }

    public Player getPlayer(int index, boolean exact) {
        String name = this.arg(index);
        Player player = PlayerUtils.getPlayer(name, exact);
        if (player == null) {
            this.settings.raw("name", name);
            this.sendError(Lang.NOT_FOUND_PLAYER);
        }
        return player;
    }

    public OfflinePlayer getOfflinePlayer(int index) {
        String name = this.arg(index);
        OfflinePlayer player = PlayerUtils.getOfflinePlayer(name);
        if (player == null) {
            this.settings.raw("name", name);
            this.sendError(Lang.NOT_FOUND_PLAYER);
        }
        return player;
    }
}


