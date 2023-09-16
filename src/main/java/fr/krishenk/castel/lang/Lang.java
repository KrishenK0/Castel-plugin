package fr.krishenk.castel.lang;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.AdvancedMessage;
import fr.krishenk.castel.config.Comment;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public enum Lang implements DefinedMessenger {
    PREFIX("&6[&eCastel&6] &2"),
    @Comment(value={"", "During chat inputs (mostly used in GUIs like as king for rank name, priority, etc)", "When the player writes this in the chat, it cancels the operation.", "The sustainable thing to use for hover messages is the '/cancel' command instead.", "", "The reason this doesn't support multiple words is that it'll minimize the amount of", "inputs players can choose for all the chat inputs."})
    CHAT_INPUT_CANCEL("cancel", 2),
    @Comment(value={"", "Used for boolean placeholder modifier."})
    ENABLED("{$p}\u2714"),
    DISABLED("{$e}\u2717"),
    TRUE("true"),
    FALSE("false"),
    @Comment({"", "Used in places were the data is no longer available.", "For example, a guild that sent a mail was disbanded."})
    UNKNOWN("&4Unknown"),
    DISABLED_WORLD("{$e}Guilds are disabled in {$s}%world%"),
    @Comment({"", "Used for absence of a value."})
    NONE("&4None"),
    @Comment(
            forParent = true,
            value = {"", "Used for time-based placeholders.", "The default time related placeholders like %guilds_shield_time% are shown as", "random numbers if you use it just like that, you'd have to use the timer modifier:", "%guilds_time_shield_time%"}
    )
    TIME_FORMATTER_SECONDS("00:00:%ssaf%", 2),
    TIME_FORMATTER_MINUTES("00:%mmaf%:%ssf%", 2),
    TIME_FORMATTER_HOURS("%hhaf%:%mmf%:%ssf%", 2),
    TIME_FORMATTER_DAYS("%dda% day(s), %hhf%:%mmf%:%ssf%", 2),
    TIME_FORMATTER_WEEKS("%wwwwa% week(s), %dd% day(s), %hhf%:%mmf%:%ssf%", 2),
    TIME_FORMATTER_MONTHS("%MMA% month(s), %wwww% week(s), %dd% day(s), %hhf%:%mmf%:%ssf%", 2),
    @Comment({"", "Used for date-based placeholders.", "The default time related placeholders like %guilds_since% are shown as", "random numbers if you use it just like that, you'd have to use the date modifier:", "%guilds_date_since%", "", "It uses Java's default date formatter syntax which can be found here:", "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns"})
    DATE_FORMATTER("{$s}yyyy{$sep}/{$s}MM{$sep}/{$s}dd {$s}hh{$sep}:{$s}mm{$sep}:{$s}ss"),
    @Comment({"", "Used for displaying no limit. Currently only used for invite codes."})
    UNLIMITED("&5Unlimited"),
    @Comment(value={"", "Used for /c map and /c claim line"}, forParent=true)
    CARDINAL_DIRECTIONS_NORTH("North", 2),
    CARDINAL_DIRECTIONS_EAST("East", 2),
    CARDINAL_DIRECTIONS_SOUTH("South", 2),
    CARDINAL_DIRECTIONS_WEST("West", 2),
    CARDINAL_DIRECTIONS_NORTH_EAST("North_East", 2),
    CARDINAL_DIRECTIONS_NORTH_WEST("North_West", 2),
    CARDINAL_DIRECTIONS_SOUTH_EAST("South_East", 2),
    CARDINAL_DIRECTIONS_SOUTH_WEST("South_West", 2),
    @Comment(value = {""}, forParent = true)
    PERMISSIONS_CLAIM("{$e}You can't claim lands.", 1),
    PERMISSIONS_JAIL("{$e}You can't manage jails.", 1),
    PERMISSIONS_UNCLAIM("{$e}You can't unclaim lands.", 1),
    PERMISSIONS_UNCLAIM_OWNED("{$e}You can only unclaim the lands you claimed yourself. This land was claimed by {$es}%claimer%", 1),
    PERMISSIONS_HOME("{$e}You can't use guild's home.", 1),
    PERMISSIONS_INSTANT_TELEPORT("{$e}You can't instantly teleport to your members.", 1),
    PERMISSIONS_READ_MAILS("{$e}You don't have permission to read your guild mails.", 1),
    PERMISSIONS_INVSEE("{$e}You don't have permission to see inventories of the guild members.", 1),
    PERMISSIONS_VIEW_LOGS("{$e}You don't have permission to view logs.", 1),
    PERMISSIONS_MANAGE_MAILS("{$e}You don't have permission to send mails.", 1),
    PERMISSIONS_SET_HOME("{$e}You can't set guild's home.", 1),
    PERMISSIONS_LORE("{$e}You can't change guilds' name or lore.", 1),
    PERMISSIONS_NATION("{$e}You can't manage the nation.", 1),
    PERMISSIONS_BUILD("{$e}You can't build in your guild's land.", 1),
    PERMISSIONS_BUILD_OWNED("{$e}You can't build in your guild's land.", 1),
    PERMISSIONS_NEXUS_BUILD("{$e}You can't build in your guild's nexus land.", 1),
    PERMISSIONS_NEXUS("{$e}You can't change guild's nexus.", 1),
    PERMISSIONS_BROADCAST("{$e}You can't broadcast messages.", 1),
    PERMISSIONS_ALLIANCE("{$e}You can't change guild alliance.", 1),
    PERMISSIONS_EXCLUDE_TAX("~Unused message entry", 1),
    PERMISSIONS_PROTECTION_SIGNS("~Unused message entry", 1),
    PERMISSIONS_EDIT_RANKS("{$e}You can't edit ranks & permissions.", 1),
    PERMISSIONS_RELATION_ATTRIBUTES("{$e}You can't edit relation attributes.", 1),
    PERMISSIONS_MANAGE_RANKS("{$e}You can't change ranks.", 1),
    PERMISSIONS_UPGRADE("{$e}You can't upgrade.", 1),
    PERMISSIONS_ENEMY("{$e}You can't /c enemy other guilds.", 1),
    PERMISSIONS_TRUCE("{$e}You can't /c truce other guilds.", 1),
    PERMISSIONS_FLY("{$e}You can't use guilds fly", 1),
    PERMISSIONS_SETTINGS("{$e}You can't edit guild settings.", 1),
    PERMISSIONS_DONATE("{$e}You can't manage resource points in the guild.", 1),
    PERMISSIONS_INVADE("{$e}You can't invade other guilds.", 1),
    PERMISSIONS_INVITE("{$e}You can't invite players to this guild.", 1),
    PERMISSIONS_KICK("{$e}You can't kick members.", 1),
    PERMISSIONS_NEXUS_CHEST("{$e}You can't use nexus chest.", 1),
    PERMISSIONS_OUTPOST("{$e}You can't use outposts.", 1),
    PERMISSIONS_PROTECTED_CHESTS("{$e}You can't access protected chests.", 1),
    PERMISSIONS_INTERACT("{$e}You're not allowed to interact in the guild land.", 1),
    PERMISSIONS_USE("{$e}You're not allowed to use in the guild land.", 1),
    PERMISSIONS_STRUCTURES("{$e}You can't use structures.", 1),
    PERMISSIONS_TURRETS("{$e}You can't use turrets.", 1),
    PERMISSIONS_WITHDRAW("{$e}You can't withdraw from your guild.", 1),
    AUTO_CLAIM_DISABLED_WORLD("{$e}Claiming is disabled in {$s}%world%", 1, 2),
    @AdvancedMessage(actionbar = "{$p}Auto-claim{$sep}: {$s}Enabled")
    AUTO_CLAIM_ACTIONBAR_ENABLED("{$p}Auto-claim{$sep}: {$s}Enabled", 1, 2, 3),
    @AdvancedMessage(actionbar = "{$p}Auto-claim{$sep}: {$e}Disabled")
    AUTO_CLAIM_ACTIONBAR_DISABLED("{$p}Auto-claim{$sep}: {$e}Disabled", 1, 2, 3),
    AUTO_CLAIM_SUCCESS("{$p}Claimed {$s}%x%&7, {$s}%z% {$p}as guild land.", 1, 2),
//    AUTO_CLAIM_NATION_ZONE("{$e}This land is a part of {$es}%nation%''s {$e}nation zone and cannot be claimed.", 1, 2),
    @AdvancedMessage(actionbar = "{$p}Auto-unclaim{$sep}: {$s}Enabled")
    AUTO_UNCLAIM_ACTIONBAR_ENABLED("{$p}Auto-unclaim{$sep}: {$s}Enabled", 1, 2, 3),
    @AdvancedMessage(actionbar = "{$p}Auto-unclaim{$sep}: {$e}Disabled")
    AUTO_UNCLAIM_ACTIONBAR_DISABLED("{$p}Auto-unclaim{$sep}: {$e}Disabled", 1, 2, 3),
    AUTO_UNCLAIM_SUCCESS("{$p}Unclaimed {$s}%x%&7, {$s}%z% {$p}as guild land.", 1, 2),
    INACTIVITY_ANNOUNCE_PLAYER("{$es}%player% {$e}has been kicked due to inactivity.", 2),
    INACTIVITY_ANNOUNCE_GUILD("{$es}%guild% {$s}has been disbanded due to inactivity.", 2),
    TAX_KICK("{$e}You didn''t have enough money for your guilds taxes, therefore you were kicked out.", 1),
    TAX_REMINDER("\n&4&l&nAttention{$sep}: {$e}&lDaily checks will be performed in{$sep}: {$es}%time%", 1),
    TAX_TOTAL("{$p}Your guild has gained a total of {$s}$%tax% {$p}from collecting taxes.", 1),
    TAX_PAID("{$es}$%tax% {$e}has been withdrawn from your account for your guild tax.", 1),
    TAX_EXCLUDED_PERMISSION("{$p}You did not pay guild taxes because you were excluded.", 1),
    TAX_EXCLUDED_NEW("{$p}You did not pay guild taxes because you were new.", 1),
    TAX_NOTIFICATIONS("{$p}A total of {$s}$%tax% {$p}guild tax has been paid.\n" +
            "      New Gingdom Bank Balance{$sep}: {$s}$%guilds_fancy_bank%", 1),
    TAX_KICK_ANNOUNCE("{$es}%player% {$e}has been kicked due to not being able to pay the taxes{$sep}: {$es}%tax%", 1),
    TAX_MEMBER_PAID("{$s}$%tax% {$p}has been taken from your account for guild tax.", 1),
    TAX_GUILD_AGE("&5Your guild did not pay the taxes as it''s new.", 1, 2),
    TAX_GUILD_PAID("{$p}A total of {$s}$%tax% {$p}guild tax has been paid.\n" +
            "New Guild Bank Balance{$sep}: {$s}$%guilds_fancy_bank%", 1, 2),
    TAX_GUILD_KICKED("{$es}%guilds_guild_name% {$e}guild has been kicked out of the nation due to not being able to pay the taxes{$sep}: {$es}%guilds_fancy_bank% {$sep}< {$es}$%tax%", 1, 2),
//    TAX_GUILD_KICKED_ANNOUNCE("{$es}%guilds_guild_name% {$e}guild has been kicked out of {$es}%guilds_nation_name% {$e}nation due to not being able to pay the taxes{$sep}: {$es}%guilds_fancy_bank% {$sep}< {$es}$%tax%", 1, 2),
    TAX_GUILD_DISBANDED("{$e}Your guild has been disbanded due to not being able to pay the taxes{$sep}: {$es}$%tax%", 1, 2),
    TAX_GUILD_DISBANDED_ANNOUNCE("{$es}%guild% {$e}guild has been disbanded due to not being able to pay the taxes{$sep}: {$es}%guilds_fancy_bank% {$sep}< {$es}$%tax%", 1, 2),
    LOCATIONS_NORMAL("{$s}%translated-world%{$sep}, {$s}%x%{$sep}, {$s}%y%{$sep}, {$s}%z%", 1),
    LOCATIONS_CHUNK("{$s}%translated-world%{$sep}, {$s}%x%{$sep}, {$s}%z%", 1),
    @AdvancedMessage(sound= XSound.BLOCK_NOTE_BLOCK_BELL)
    JOIN_LEAVE_MESSAGES_JOIN_GUILD("NOPREFIX|{$sep}[{$p}◆{$sep}]{$sep}[%guilds_rank_color%%guilds_rank_symbol%{$sep}] {$p}%displayname%", 3, 4),
    JOIN_LEAVE_MESSAGES_JOIN_OTHERS("NOPREFIX|{$s}%displayname% {$p}has joined the server.", 3, 4),
    @AdvancedMessage(sound=XSound.BLOCK_NOTE_BLOCK_FLUTE)
    JOIN_LEAVE_MESSAGES_LEAVE_GUILD("NOPREFIX|{$sep}[{$e}◆{$sep}]{$sep}[%guilds_rank_color%%guilds_rank_symbol%{$sep}] {$e}%displayname%", 3, 4),
    JOIN_LEAVE_MESSAGES_LEAVE_OTHERS("NOPREFIX|{$es}%displayname% {$e}has left the server.", 3, 4),
    JOIN_LANGUAGE_NOT_SUPPORTED("&cThe detected language for your client was {$es}%detected-lang% &cwhich is probably not supported by the plugin or installed by the server.\nYou can change your language later using hover:{{$es}/c language;&9Click to run;|/c language} &ccommand.", 1, 2),
    JOIN_LANGUAGE_SUPPORTED("&2Your detected language is &6%detected-lang% &8(&6%guilds_lang%&8)\n&8[&9&l!&8] &2If you believe this was a mistake, you can change it with hover:{&6/c language;&9Click to open;/c language} &2command.",1, 2),
    JOIN_INVITES("&2You have &6%invites% &2invites. Check them with &6hover:{/c invites;&9Click to open;/c invites}", 1),
    MAILS_HEADER_FORMAT_SENT("{$p}Subject{$sep}: {$s}%subject%\n" +
            "{$p}From{$sep}: {$s}%from%\n" +
            "{$p}Sender{$sep}: {$s}%sender%\n" +
            "{$p}To{$sep}: {$s}%to%\n" +
            "{$p}CC{$sep}: {$s}%cc%\n" +
            "{$p}At{$sep}: {$s}%date_sent%\n" +
            "\n" +
            "          hover:{{$sep}[{$p}Reply{$sep}];{$sep}Click to reply;/c mail reply %id%}\n" +
            "&7.......................................................", 1, 3),
    MAILS_HEADER_FORMAT_RECEIVED("{$p}Subject{$sep}: {$s}%subject%\n" +
            "{$p}From{$sep}: {$s}%from%\n" +
            "{$p}Sender{$sep}: {$s}%sender%\n" +
            "{$p}To{$sep}: {$s}%to%\n" +
            "{$p}CC{$sep}: {$s}%cc%\n" +
            "{$p}At{$sep}: {$s}%date_sent%\n" +
            "\n" +
            "          hover:{{$sep}[{$p}Reply{$sep}];{$sep}Click to reply;/c mail reply %id%}\n" +
            "&7.......................................................", 1, 3),
    MAILS_REPLY_SUBJECT_FORMATTER("{$sep}[{$p}RE{$sep}] &f%subject%", 1),
    MAILS_NOTIFICATIONS_JOIN("{$p}You have {$s}%mails% {$p}new mails. Open your mailbox to read them.", 1, 2),
    MAILS_NOTIFICATIONS_RECEIVERS("&9%player% {$p}from &9%guilds_guild_name% {$p}guild has sent you a mail{$sep}:\n" +
            "hover:{&f%subject%;&9Click to read;/c mail open %id%}", 1, 2),
    MAILS_PRIMARY_NONE("{$e}None", 1),
    MAILS_CC_EACH("{$p}%cc%", 1, 2),
    MAILS_CC_NONE("{$e}None", 1, 2),
    MAILS_CC_SEPARATOR("&7, ", 1, 2),
    MAILS_CC_REMOVED("{$e}Removed CC recipient{$sep}: {$es}%recipient%", 1, 2),
    MAILS_CC_MAX("{$e}You can''t send more than %limit% carbon copies.", 1, 2),
    MAILS_CC_ENTER("{$p}Enter the name of the guild you wish to send a carbon copy to or type hover:{{$e}cancel;{$es}Cancel;/cancel} {$p}to cancel.", 1, 2),
    MAILS_TO_ENTER("{$p}Enter the name of the guild you wish to send the mail to or type hover:{{$e}cancel;{$es}Cancel;/cancel} {$p}to cancel.", 1, 2),
    MAILS_TO_YOURSELF("{$e}You can''t send mails to your own guild.", 1, 2),
    MAILS_ALREADY_RECIPIENT("{$e}The specified guild is already a recipient.", 1),
    MAILS_SUBJECT_EMPTY("{$e}None", 1, 2),
    MAILS_SUBJECT_ENTER("{$p}Enter a subject for your mail or type hover:{{$e}cancel;{$es}Cancel;/cancel} {$p}to cancel", 1, 2),
    MAILS_SUBJECT_LIMIT("{$e}Mail subjects can only be {$es}%limit% {$e}characters long.", 1, 2),
    MAILS_SUBJECT_CHANGED("{$p}Mail subjected changed{$sep}: {$s}%subject%", 1, 2),
    MAILS_CONTENT_EMPTY("{$e}None", 1, 2),
    MAILS_CLEARED_PROPERTIES("{$p}Cleared all mail properties.", 1),
    MAILS_ENVELOPE_NO_FREE_SLOT("{$e}You don''t have any free slots in your inventory for the envelope.", 1, 2),
    MAILS_ENVELOPE_NOT_ENOUGH_RESOURCES("{$e}Your guild needs {$es}%rp% resources points {$e}and {$es}$%money% {$e}for an envelope.", 1, 2),
    MAILS_ENVELOPE_GIVE("{$p}Right-click the item while sneaking to edit special properties.", 1, 2),
    MAILS_SEND_MISSING_SUBJECT("{$e}Your mail doesn''t have a subject.", 1, 2),
    MAILS_SEND_MISSING_PRIMARY_RECIPIENT("{$e}Your mail doesn''t a primary recipient.", 1, 2),
    MAILS_SEND_MISSING_CONTENT("{$e}Your mail''s content is empty.", 1, 2),
    MAILS_SEND_TOTAL_COOLDOWN("{$e}Your guild can send another mail in{$sep}: {$es}%cooldown%", 1, 2),
    MAILS_SEND_PER_GROUP_COOLDOWN("{$e}Your guild can send another mail to {$es}%guild% {$e}in{$sep}: {$es}%cooldown%", 1, 2),
    MAILS_NOT_AN_ENVELOPE("{$e}The item is not an envelope.", 1, 2),
    COMMAND_HELP_DESCRIPTION("'&6Displays all commands", 1, 2),
    COMMAND_HELP_NAME("help", 1, 2),
    COMMAND_HELP_ALIASES("h ? --help /? -h", 1, 2),
    COMMAND_HELP_USAGE("{$usage}%command% &9[page]", 1, 2),
    COMMAND_HELP_GROUPED_HEADER("&8&m---------------=(&c&l %group% &8)=---------------", 1, 2),
    COMMAND_HELP_NEGATIVE_PAGES("&cThis isn''t a building. You can''t just go to a negative page.", 1, 2),
    COMMAND_HELP_GROUPED_COMMANDS("hover:{&8/&2k %cmd% &7- %description%;&8/&2k %cmd%;|/c %cmd%}", 1, 2),
    COMMAND_HELP_FOOTER("hover:{&6←;&6Previous Page;/c %command% %previous_page%} %pages%hover:{&6→;&6Next Page;/c %command% %next_page%}", 1, 2),
    COMMAND_HELP_FOOTER_PAGE("hover:{&8[&2%number%&8];&6Go to page &2%number%;/c %command% %number%}", 1, 2),
    COMMAND_HELP_FOOTER_CURRENT_PAGE("hover:{&8[&2&l%number%&8];&2Current Page}",1, 2),
    COMMAND_HELP_HEADER("&8&m---------------=(&c&l Castel &6%page%&7/&6%max_pages% &8&m)=---------------", 1, 2),
    COMMAND_HELP_NO_MORE_PAGES("&cThere are no more pages to load.",1, 2),
    COMMAND_HELP_COMMANDS("hover:{&8/&2k %cmd% &7- %description%;&5%usage%;|/c %cmd%}", 1, 2),
    COMMAND_HELP_BAD_START("&cYou don''t need slash or the main /c command to get the command info.", 1, 2),
    COMMAND_HELP_NOT_FOUND("&cCould not find any command matching&8: {$es}%command%", 1, 2),
    COMMAND_HELP_INFO("&8------------=( &2%main-name% &8)=------------\n" +
            "&7| &2Display Name&8: &9%command-displayname%\n" +
            "&7| &2Aliases&8: &9%aliases%\n" +
            "&7| &2Description&8: &9%description%\n" +
            "&7| &2Usage&8: &9%usage%\n" +
            "&7| &2Cooldown&8: &9%cooldown%\"", 1, 2),
    COMMAND_ADMIN_COMMAND_INFO("{$sep}------------=( {$p}%main-name% {$sep})=------------\n&7| {$p}Display Name{$sep}: &9%command-displayname%\n&7| {$p}Aliases{$sep}: &9%aliases%\n&7| {$p}Parent{$sep}: &9%parent%\n&7| {$p}Description{$sep}: &9%description%\n&7| {$p}Usage{$sep}: &9%usage%\n&7| {$p}Permission{$sep}: &9hover:{%permission%;{$p}Click to copy;|%permission%}\n&7| {$p}Default Permission Scope{$sep}: &9%permission-scope%\n&7| {$p}Cooldown{$sep}: &9%cooldown%\n&7| {$p}Disabled Worlds{$sep}: &9%disabled-worlds%\n"),
    NO_GUILD_REMINDER("{$e}Reminder{$sep}: {$es}You haven''t joined a guild yet."),
    INVALID_NUMBER("{$es}%arg% {$e}is not a number.", 1),
    INVALID_UUID("{$es}%arg% {$e}is not a valid &nhover:{UUID;&7Click to open Wikipedia page;@https://en.wikipedia.org/wiki/Universally_unique_identifier#Format}", 1),
    INVALID_NUMBER_NEGATIVE("{$e}Must be a positive number.", 1),
    INVALID_AMOUNT("{$e}Invalid amount{$sep}: {$es}%amount%", 1),
    INVALID_MATERIAL("{$e}Unknown material{$sep}: {$es}%material%", 1),
    INVALID_TIME("{$e}Invalid time{$sep}: {$es}%time% {$sep}({$e}Correct format{$sep}: {$es}<amount><time-suffix> {$e}e.g.{$sep}: {$es}1s{$sep}, {$es}50days{$sep})", 1),
    INVALID_BOOLEAN("{$es}%arg% {$e}is not a valid option. Please either use '&2true{$e}' or '&4false{$e}'", 1),
    GUILD_CREATED("{player} &7has created the guild {guild}."),
    NO_GUILD_DEFAULT("{$e}You don't have a guild.", 2),
    NO_GUILD_TARGET("{$e}The specified player doesn't have a guild.", 2),
    NO_NATION("{$e}Your guild is not in a nation."),
    NO_NATION_OTHER("{$e}The specified guild is not in a nation."),
    COMMANDS_TAGS_IDENTIFIER_PLAYERS("#PLAYER", 1, 2, 3),
    COMMANDS_TAGS_IDENTIFIER_GUILDS("#GUILD", 1, 2, 3),
    COMMANDS_TAGS_IDENTIFIER_NATIONS("#NATION", 1, 2, 3),
    COMMANDS_TAGS_PLAYERS("<player>", 1, 2),
    COMMANDS_TAGS_GUILDS("<guild>", 1, 2),
    COMMANDS_TAGS_NATIONS("<nation>", 1, 2),
    COMMANDS_PLAYERS_ONLY("{$e}Only players can use this command.", 1),
    COMMANDS_UNKNOWN_COMMAND("{$e}Unknown command!", 1),
    COMMAND_CREATE_DESCRIPTION("{$s}Creates a guild.", 1, 2),
    COMMAND_CREATE_USAGE("{$usage}create {$p}<name>", 1, 2),
    COMMAND_CREATE_ALREADY_IN_GUILD("{$e}You are already in a guild. Leave your guild to make a new one.", 1, 2),
    @AdvancedMessage(sound = XSound.UI_TOAST_CHALLENGE_COMPLETE)
    COMMAND_CREATE_SUCCESS("{$p}You have founded {$s}%guild%{$p}!", 1, 2),
    COMMAND_CREATE_PACIFIST("{$p}Your guild is a {$s}pacifist {$p}guild.", 1, 2),
    COMMAND_CREATE_AGGRESSOR("{$p}Your guild is an {$e}aggressor {$p}guild.", 1, 2),
    COMMAND_CREATE_ANNOUNCE("{$s}%player% {$p}has founded {$s}%guild% {$p}guild!", 1, 2),
    COMMAND_CREATE_COST("{$e}You need {$es}%cost% {$e}money to create a guild.", 1, 2),
    COMMAND_CREATE_CONFIRMATION("{$e}You need to pay {$es}%cost% {$e}money to create a guild.\nAre you sure you want to continue? Do the command again.", 1, 2),
    COMMAND_CREATE_NAME_LENGTH("{$e}Guild name length cannot be greater than {$es}%max% {$e}or less than {$es}%min%", 1, 2),
    COMMAND_CREATE_NAME_ENGLISH("{$e}Guild name must be in English and only contain numbers and alphabets.", 1, 2),
    COMMAND_CREATE_NAME_HAS_SYMBOLS("{$e}Guild name cannot contain symbols. Only numbers and alphabets.", 1, 2),
    COMMAND_CREATE_NAME_NUMBERS("{$e}Guild names cannot contain numbers.", 1, 2),
    COMMAND_CREATE_NAME_ALREADY_IN_USE("{$e}Another guild is already using this name.", 1, 2),
    COMMAND_LEAVE_SUCCESS("{$p}You've left {$s}%guild%", 1, 2),
    COMMAND_LEAVE_DESCRIPTION("{$s}Leave your guild.", 1, 2),
    COMMAND_LEAVE_LEADER("{$e}You can't leave your guild as a leader.\nEither disband the guild using {$es}/c disband\n{$e}Or promote someone to leader using {$es}/c leader", 1, 2),
    COMMAND_LEAVE_ANNOUNCE("{$es}%left% {$e}has left the guild!", 1, 2),
    COMMAND_DISBAND_DESCRIPTION("{$e}Disbands your guild and all the members will be kicked.", 1, 2),
    COMMAND_DISBAND_LEADER_ONLY("{$e}Only leader can disband the guild.", 1, 2),
    COMMAND_DISBAND_SUCCESS("&4Your guild has been disbanded!", 1, 2),
    COMMAND_DISBAND_CONFIRMATION("&4You're about to disband your guild.\nAll your members will be kicked and\nyour lands will be unclaimed.\nAre you sure you want to proceed?\nDo {$es}/c disband &4again to confirm.", 1, 2),
    COMMAND_DISBAND_ANNOUNCE("{$es}%player% {$s}has disbanded their guild {$es}%guilds_guild_name%", 1, 2),
    COMMAND_LORE_DESCRIPTION("{$s}Set your guild's lore.", 1, 2),
    COMMAND_LORE_USAGE("{$usage}lore {$p}<lore>", 1, 2),
    COMMAND_LORE_BLACKLISTED("{$e}Your guild's lore contains inappropriate words.", 1, 2),
    COMMAND_LORE_NAME_LENGTH("{$e}Guild lore length cannot be greater than {$es}%limit%", 1, 2, 3),
    COMMAND_LORE_NAME_ENGLISH("{$e}Guild lore must be in English and only contain numbers and alphabets.", 1, 2, 3),
    COMMAND_LORE_NAME_HAS_SYMBOLS("{$e}Guild lore name cannot contain symbols. Only numbers and alphabets.", 1, 2, 3),
    COMMAND_LORE_REMOVED("{$p}Removed guild's lore.", 1, 2),
    COMMAND_KICK_PERSON("{$e}You've been kicked out of {$es}%guild% {$e}by {$es}%kicker%"),
    COMMAND_KICK_DESCRIPTION("{$s}Kicks a member from your guild."),
    COMMAND_KICK_USAGE("{$usage}kick {$p}<player> &9[silent]", 1, 2),
    COMMAND_KICK_ANNOUNCE("{$es}%kicked% {$e}has been kicked out of the guild by {$es}%kicker%", 1, 2),
    COMMAND_KICK_SELF("{$e}You can't kick yourself. That's not physically possible, but I can help you with that. There you go.", 1, 2),
    COMMAND_KICK_SELF_SECONDARY("{$e}Please stop trying to kick yourself. If you really wanna try it, give {$es}&l&nhover:{this;Click to open link;@https://www.youtube.com/watch?v=dQw4w9WgXcQ} {$e}a try.", 1, 2),
    COMMAND_KICK_NOT_IN_GUILD("{$e}The specified player is not in your guild.", 1, 2),
    COMMAND_KICK_ELECTIONS("{$e}Can't kick players during elections.", 1, 2),
    COMMAND_KICK_CANT_KICK("{$e}Can't kick {$es}%kicked%", 1, 2),
    COMMAND_ELECTION_DESCRIPTION("{$s}An election system with guild member candidates to determine the next leader."),
    COMMAND_ELECTION_NO_ONGOING_ELECTION("{$e}There's currently no ongoing election. Next election in{$sep}: {$es}%next-election%"),
    COMMAND_ELECTION_VOTE_DESCRIPTION("{$s}Vote for a candidate.", 1, 2, 3),
    COMMAND_ELECTION_VOTE_VOTED("{$p}You've voted for {$s}%player%", 1, 2, 3),
    COMMAND_ELECTION_VOTE_ALREADY_VOTED("{$e}You've already voted for {$es}%player%", 1, 2, 3),
    COMMAND_ELECTION_VOTE_VOTED_AGAIN("{$p}You've voted for {$s}%player% {$p}You previously voted for {$s}%previous-candidate%", 1, 2, 3),
    COMMAND_ELECTION_STATEMENT_DESCRIPTION("{$s}Set your statement as the election candidate.", 1, 2, 3),
    COMMAND_ELECTION_STATEMENT_USAGE("{$usage}election statement {$p}<statement>", 1, 2, 3),
    COMMAND_ELECTION_STATEMENT_SET("{$p}Your new statement has been set to{$sep}: &r%statement%", 1, 2, 3),
    COMMAND_RENAME_DESCRIPTION("{$s}Rename your guild.", 1, 2),
    COMMAND_RENAME_LEADER_ONLY("{$e}Only leader can rename the guild.", 1, 2),
    COMMAND_RENAME_NAME_ENGLISH("{$e}Guild name must be in English and only contain numbers and alphabets.", 1, 2),
    COMMAND_RENAME_NAME_NUMBERS("{$e}Guild names cannot contain numbers.", 1, 2),
    COMMAND_RENAME_NAME_ALREADY_IN_USE("{$e}Another guild is already using this name.", 1, 2),
    COMMAND_RENAME_COOLDOWN("{$e}You need to wait {$es}%cooldown% seconds {$e}before renaming.", 1, 2),
    COMMAND_RENAME_USAGE("{$usage}rename <name>", 1, 2),
    COMMAND_RENAME_SUCCESS("{$s}%player% {$p}has changed the guild name to{$sep}: {$s}%name%", 1, 2),
    COMMAND_RENAME_COST("{$e}Your guild's bank needs {$es}%cost% {$e}money to rename your guild.", 1, 2),
    COMMAND_TAG_DESCRIPTION("{$s}Change your guild's tag.", 1, 2),
    COMMAND_TAG_NAME_LENGTH("{$e}Tags length cannot be greater than {$es}%max% {$e}or less than {$es}%min%", 1, 2),
    COMMAND_TAG_NAME_ENGLISH("{$e}Tags must be in English and only contain numbers and alphabets.", 1, 2),
    COMMAND_TAG_NAME_HAS_SYMBOLS("{$e}Tags cannot contain symbols. Only numbers and alphabets.", 1, 2),
    COMMAND_TAG_NAME_NUMBERS("{$e}Tags cannot contain numbers.", 1, 2),
    COMMAND_TAG_USAGE("{$usage}tag <name>", 1, 2),
    COMMAND_TAG_CHANGED("{$p}Your guild's tag was changed from {$s}%tag% {$p}to {$s}%guilds_guild_tag% {$p}by {$s}%player%", 1, 2),
    COMMAND_TAG_SET("{$p}Your guild's tag was set to {$s}%guilds_guild_tag% {$p}by {$s}%player%", 1, 2),
    COMMAND_TAG_COST("{$e}Your guild needs {$es}%cost% {$e}money to change your guild's tag.", 1, 2),
    INVITE_CODES_NO_LONGER_VALID("{$e}That invite code is no longer valid. It either expired or someone removed it manually.", 2),
    INVITE_CODES_MAX_USES("{$e}Maximum uses for invite code has already been reached.", 2),
    INVITE_CODES_USED("{$s}%player% {$p}has used an invite code to join the guild.", 2),
    INVITE_CODES_GUILD_DOESNT_EXIST("{$e}The guild that the invite code was made for no longer exists.", 2),
    COMMAND_INVITES_DESCRIPTION("{$s}View your current invitations.", 1, 2),
    COMMAND_INVITES_NO_INVITES("{$e}You don't have any invitations sent to you.", 1, 2),
    COMMAND_INVITE_INVITED("{$s}%inviter% {$p}has invited you to join {$s}%guild%{$p}.\n    hover:{{$p}Accept with /c accept;{$p}Accept;/c accept}\n    hover:{{$e}Decline with /c decline;{$e}Decline;/c decline}", 1, 2),
    COMMAND_INVITE_USAGE("{$usage}invite {$p}<player> <guild>", 1, 2),
    COMMAND_INVITE_ANNOUNCE("{$s}%inviter% {$p}has invited {$s}%invited% {$p}to the guild.", 1, 2),
    COMMAND_INVITE_ALREADY_JOINED("{$es}%invited% {$e}is already in your guild.", 1, 2),
    COMMAND_INVITE_DESCRIPTION("{$s}Sends an invitation to a player to join the guild.", 1, 2),
    COMMAND_INVITE_ALREADY_IN_GUILD("{$e}That player is already in a guild.", 1, 2),
    COMMAND_INVITE_ALREADY_INVITED("{$e}This player is already invited to the guild by {$es}%inviter%", 1, 2),
    COMMAND_INVITE_MAX_MEMBERS("{$e}Your guild is already full with {$es}%guilds_members% {$e}members.", 1, 2),
    COMMAND_INVITE_SELF("{$e}You must first consult with the elder gods before inviting yourself.", 1, 2),
    COMMAND_INVITE_EXPIRED("{$e}Your invite from {$es}%inviter% {$e}has expired!", 1, 2),
    COMMAND_INVITE_EXPIRED_NOTIFY("{$e}Your invite to {$es}%invited% {$e}has expired!", 1, 2),
    COMMAND_INVITECODES_DESCRIPTION("{$s}Invite codes are an alternative way of inviting players to your guild. It's similar to Discord's invite codes and allows you to codes which any player can use to join your guild. Only players with {$p}INVITE {$s}permission cancreate, redeem and delete invite codes.", 1, 2),
    COMMAND_INVITECODES_PERMISSION_REDEEM("{$e}You don't have permission to redeem invite codes.", 1, 2, 3),
    COMMAND_INVITECODES_PAPER_COST("{$e}You need {$es}%fancy_invitecode-paper-cost% resource points {$e}to generate an invite code paper.", 1, 2, 3),
    COMMAND_INVITECODES_PAPER_GIVE("{$p}Bought an invite paper for {$s}%fancy_invitecode-paper-cost% resource point", 1, 2, 3),
    COMMAND_INVITECODES_PERMISSION_DELETE("{$e}You don't have permission to delete invite codes.", 1, 2, 3),
    COMMAND_INVITECODES_PERMISSION_GET("{$e}You don't have permission to get invite code papers.", 1, 2, 3, 4),
    COMMAND_INVITECODES_PERMISSION_CREATE("{$e}You don't have permission to create invite codes.", 1, 2, 3, 4),
    COMMAND_INVITECODES_MAX("{$e}guilds can only make up to {$es}%invitecodes-max% {$e}invite codes.", 1, 2),
    COMMAND_INVITECODES_DURATION_ENTER("{$p}Please enter the expiration duration of the invite code {$sep}({$s}e.g. 1day or just 0 to never expire{$sep}) {$cancel}", 1, 2, 3, 4),
    COMMAND_INVITECODES_DURATION_OUT_OF_RANGE("{$e}Invite code durations must be longer than {$es}%duration-min%", 1, 2, 3, 4),
    COMMAND_INVITECODES_USES_OUT_OF_RANGE("{$e}Invite code uses must be equal or greater than {$es}%uses-min%", 1, 2, 3, 4),
    COMMAND_INVITECODES_USES_ENTER("{$p}Please enter the number of uses for this invite code. This limits how many players can use this particular code before it becomes useless. Set it to {$s}0 {$p}for unlimited uses {$cancel}", 1, 2, 3),
    COMMAND_INVITECODES_REDEEM_ENTER("{$p}Please enter the new expiration duration of the invite code to redeem it {$sep}({$s}e.g. 1day or just 0 to never expire{$sep}) {$cancel}", 1, 2, 3),
    COMMAND_INVITECODES_DELETED_ALL("{$p}Deleted all invite codes.", 1, 2),
    COMMAND_ACCEPT_DESCRIPTION("{$s}Accept invitations to join a guild.", 1, 2),
    COMMAND_ACCEPT_ALREADY_IN_GUILD("{$p}You're already in a guild.", 1, 2),
    COMMAND_ACCEPT_ACCEPTED("{$p}You've accepted {$s}%inviter%'s {$p}invitation to join {$es}%guild%", 1, 2),
    COMMAND_ACCEPT_JOINED("{$s}%player% {$p}has joined the guild.", 1, 2),
    COMMAND_ACCEPT_NOTIFY("{$s}%name% {$p}has accepted your invitation.", 1, 2),
    COMMAND_ACCEPT_NO_INVITES("{$e}You don't have any invitations sent to you.", 1, 2),
    COMMAND_ACCEPT_NOT_INVITED("{$e}You're not invited to {$es}%guild%", 1, 2),
    COMMAND_ACCEPT_EXPIRED("{$e}Your invitation to {$es}%guild% {$e}has already expired.", 1, 2),
    COMMAND_ACCEPT_MAX_MEMBERS("{$e}This guild is already full with {$es}%guilds_members% {$e}members.", 1, 2),
    COMMAND_ACCEPT_NO_LONGER_EXISTS("{$e}That guild no longer exists.", 1, 2),
    COMMAND_ACCEPT_MULTIPLE_INVITES("{$e}You have multiple invites, please specify the guild name that you want to join.You can also view all your current invites with {$es}hover:{/c invites;{$s}Click to run;/c invites} {$e}command.", 1, 2),
    COMMAND_DECLINE_DESCRIPTION("{$s}Decline invitations to join a guild.", 1, 2),
    COMMAND_DECLINE_DECLINED("{$e}You've declined {$s}%inviter%'s {$e}invitation to join {$es}%guild%", 1, 2),
    COMMAND_DECLINE_NOTIFY("{$es}%name% {$e}has declined your invitation.", 1, 2),
    COMMAND_DECLINE_NO_INVITES("{$e}You don't have any invitations sent to you.", 1, 2),
    COMMAND_DECLINE_NOT_INVITED("{$e}You're not invited to {$es}%guild%", 1, 2),
    COMMAND_DECLINE_EXPIRED("{$e}Your invitation to {$es}%guild% {$e}has already expired.", 1, 2),
    COMMAND_DECLINE_NO_LONGER_EXISTS("{$e}That guild no longer exists.", 1, 2),
    COMMAND_DECLINE_ALL("{$p}Declined a total of {$s}%invites% {$p}invitations.", 1, 2),
    COMMAND_DECLINE_MULTIPLE_INVITES("{$e}You have multiple invites, please specify the guild name that you want to decline the invitation to. Or {$es}* {$e}to decline all the invitations.\nYou can also view all your current invites with {$es}hover:{/c invites;{$s}Click to run;/c invites} {$e}command.", 1, 2),
    COMMAND_JOIN_DESCRIPTION("{$s}Join a guild that doesn't require an invitation to join.", 1, 2),
    COMMAND_JOIN_USAGE("{$usage}join {$p}<guild>", 1, 2),
    COMMAND_JOIN_JOINED("{$s}%player% {$p}has joined the guild.", 1, 2),
    COMMAND_JOIN_ALREADY_IN_GUILD("{$e}You're already in a guild.", 1, 2),
    COMMAND_JOIN_ELECTIONS("{$e}Can't join guilds during elections.", 1, 2),
    COMMAND_JOIN_REQUIRES_INVITE("{$e}You need to be invited to join {$es}%guild%", 1, 2),
    COMMAND_REQUESTJOIN_DESCRIPTION("{$s}Request a guild to join them.", 1, 2),
    COMMAND_REQUESTJOIN_USAGE("{$usage}requestJoin {$p}<guild>", 1, 2),
    COMMAND_REQUESTJOIN_ALREADY_SENT("{$e}You've already sent a request to this guild.", 1, 2),
    COMMAND_REQUESTJOIN_SENT("{$p}Sent a join request to {$s}%guilds_guild_name% {$p}guild.", 1, 2),
    COMMAND_REQUESTJOIN_SENT_ANNOUNCE("{$s}%player% {$p}wishes to join your guild. You can accept their request from {$s}hover:{/c joinrequests;&7Click to run;/c joinrequests}", 1, 2),
    COMMAND_JOINREQUESTS_DESCRIPTION("{$s}Shows all the players who have requested to join your guild.", 1, 2),
    COMMAND_JOINREQUESTS_ACCEPTED_PLAYER("{$s}%player% {$p}has accepted your join request to the guild.", 1, 2),
    COMMAND_JOINREQUESTS_ACCEPTED_SELF("{$p}You have accepted {$s}%player% {$p}join request to join {$s}%guilds_guild_name%", 1, 2),
    COMMAND_JOINREQUESTS_DENIED_PLAYER("{$es}%player% {$e}has denied your join request to join {$es}%guilds_guild_name%", 1, 2),
    COMMAND_JOINREQUESTS_DENIED_SELF("{$e}You have denied {$es}%player% {$e}join request to your guild.", 1, 2),
    COMMAND_COLOR_DESCRIPTION("{$s}Change the color of your guild.", 1, 2),
    COMMAND_COLOR_USAGE("&4Usage: {$es}/c color &9[color]", 1, 2),
    COMMAND_COLOR_TAB_COMPLETE_HEX("#<hex color code>", 1, 2, 4),
    COMMAND_COLOR_TAB_COMPLETE_RGB("red green blue", 1, 2, 4),
    COMMAND_PROMOTE_DESCRIPTION("{$s}Promotes a member in your guild.", 1, 2),
    COMMAND_PROMOTE_PROMOTED("{$s}%promoted% {$p}has been promoted to &9%rank% {$p}by {$s}%player%", 1, 2),
    COMMAND_PROMOTE_NOT_IN_GUILD("{$es}%promoted% {$e}is not in your guild.", 1, 2),
    COMMAND_PROMOTE_CANT_PROMOTE("{$e}You can't promote {$es}%promoted%", 1, 2),
    COMMAND_PROMOTE_LEADER("{$es}%promoted% {$e}already has the highest rank in the guild. To promote the player to leader use {$es}/c leader", 1, 2),
    COMMAND_PROMOTE_USAGE("{$usage}promote {$p}<player>", 1, 2),
    COMMAND_DEMOTE_DESCRIPTION("{$s}Demotes a member in your guild.", 1, 2),
    COMMAND_DEMOTE_DEMOTED("{$es}%demoted% {$e}has been demoted to &9%rank% {$e}by {$es}%player%", 1, 2),
    COMMAND_DEMOTE_NOT_IN_GUILD("{$es}%demoted% {$e}is not in your guild.", 1, 2),
    COMMAND_DEMOTE_CANT_DEMOTE("{$e}You can't demote {$es}%demoted%", 1, 2),
    COMMAND_DEMOTE_CANT_DEMOTE_LEADER("{$e}You can't demote the leader {$es}%demoted%", 1, 2),
    COMMAND_DEMOTE_MEMBER("{$e}This player has the member rank.", 1, 2),
    COMMAND_DEMOTE_USAGE("{$usage}demote {$p}<player>", 1, 2),
    COMMAND_LEADER_DESCRIPTION("{$s}Set your guilds new leader.", 1, 2),
    COMMAND_LEADER_ONLY_LEADER("{$s}Only leader can set the new guild leader.", 1, 2),
    COMMAND_LEADER_SET("{$s}%leader% {$p}is now the guilds new leader!", 1, 2),
    COMMAND_LEADER_NOT_IN_GUILD("{$es}%leader% {$e}is not in your guild.", 1, 2),
    COMMAND_LEADER_SELF("{$e}You're already the guild's leader.", 1, 2),
    COMMAND_LEADER_USAGE("{$usage}leader {$p}<player>", 1, 2),
    COMMAND_UNSETHOME_DESCRIPTION("{$s}Removes your guild home.", 1, 2),
    COMMAND_UNSETHOME_NOT_SET("{$e}Your guild home is not set.", 1, 2),
    COMMAND_UNSETHOME_SUCCESS("&aGuild home has been removed{$sep}: {$s}%x%&7, {$s}%y%&7, {$s}%z%", 1, 2),
    COMMAND_SETHOME_DESCRIPTION("{$s}Sets your guild home to your current location.", 1, 2),
    COMMAND_SETHOME_NOT_CLAIMED("{$e}You can only set your guild home on claimed lands.", 1, 2),
    COMMAND_SETHOME_OTHERS_LAND("{$e}You can only set your guild home on your own land!", 1, 2),
    COMMAND_SETHOME_NEXUS_LAND("{$e}You can only set your guild home on your nexus land!", 1, 2),
    COMMAND_SETHOME_SET("&aSet guild home to{$sep}: {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_HOME_DESCRIPTION("{$s}Teleport to your guild''s home.", 1, 2),
    COMMAND_HOME_USAGE("{$usage}home {$s}[guild]", 1, 2),
    COMMAND_HOME_CHANGED("{$e}Home location has been changed. Teleportation has been cancelled.", 1, 2),
    COMMAND_HOME_TELEPORTING("{$p}Teleporting in {$s}%countdown% seconds{$p}... Don''t move.", 1, 2),
    COMMAND_HOME_NOT_SET("{$e}Your guild does not have a home set!", 1, 2),
    COMMAND_HOME_NOT_SET_OTHERS("{$es}%guild% {$e}guild''s home is not set.", 1, 2),
    COMMAND_HOME_PERMISSION("{$e}You don''t have permission to teleport to other guilds'' homes.", 1, 2),
    COMMAND_HOME_NOT_PUBLIC("{$es}%guild% {$e}guild''s home is not public.", 1, 2),
    COMMAND_HOME_CANT_USE_PUBLIC_HOME("{$e}You can''t use {$es}%guild% {$e}guild''s public home.", 1, 2),
    COMMAND_HOME_BEING_INVADED("{$e}You can''t teleport to {$es}%guild%''s {$e}home as it''s being invaded.", 1, 2),
    COMMAND_HOME_SUCCESS("{$p}Teleported!", 1, 2),
    COMMAND_TELEPORT_DESCRIPTION("{$s}Teleport to the location where you guilds is being invaded.", 1, 2),
    COMMAND_TELEPORT_NOT_INVADING("{$e}There are currently no lands under attack."),
    COMMAND_TELEPORT_ALREADY_TELEPORTING("{$e}Already teleporting to home..."),
    COMMAND_TELEPORT_TELEPORTING("{$p}Teleporting in {$s}%countdown% seconds{$p}... Don''t move."),
    COMMAND_TELEPORT_TELEPORTED("{$p}You''ve been teleported to where the invasion began."),
    COMMAND_TELEPORT_CANCELLED("{$e}Teleportation has been cancelled!"),
    COMMAND_TPA_USAGE("{$usage}tpa {$p}<player>", 1, 2),
    COMMAND_TPA_NOT_FRIENDLY("$e}You can only send tp requests to players who have both {$es}ceasefire {$e}and {$es}turret ceasefire {$e}relation attributes with your guild.", 1, 2),
    COMMAND_TPA_DESCRIPTION("{$s}Teleport to your members instantly or send a request to allies to teleport to them.", 1, 2),
    COMMAND_TPA_YOURSELF("{$p}Success! Teleported you to yourself.", 1, 2),
    COMMAND_TPA_PENDING_REQUEST("{$e}You''ve already sent a tp request to {$es}%previous-request-player%", 1, 2),
    COMMAND_TPA_EXPIRED_TELEPORTER("{$e}Your tp request to {$es}%target% {$e}was expired.", 1, 2, 3),
    COMMAND_TPA_EXPIRED_TARGET("$es}%teleporter%{$e}''s tp request to you was expired.", 1, 2, 3),
    COMMAND_TPA_DISABLED_WORLD_TELEPORTER("$e}You cannot teleport from this world.", 1, 2),
    COMMAND_TPA_DISABLED_WORLD_TARGET("{$es}%target% {$e}is in a disallowed world. You cannot teleport there.", 1, 2),
    COMMAND_TPA_INSTANT_TELEPORTER("{$p}You''ve teleported to {$s}%target%", 1, 2),
    COMMAND_TPA_REQUESTED_TELEPORTER("{$p}Sent a tp request to {$s}%target%", 1, 2, 3),
    COMMAND_TPA_REQUESTED_TARGET("{$s}%teleporter% {$p}has sent a tp request to you.\n" +
            "{$sep}● {$p}Accept with {$s}hover:{/c tpaaccept %teleporter%;&7Click to accept;/c tpaaccept %teleporter%}\n" +
            "{$sep}● {$e}Reject with {$es}hover:{/c tpareject %teleporter%;&7Click to reject;/c tpareject %teleporter%}", 1, 2, 3),
    COMMAND_TPA_TARGET_NOTIFICATION("{$s}%teleporter% {$p}has teleported to you.", 1, 2),
    COMMAND_TPA_NO_REQUESTS("{$e}You don''t have any tp requests.", 1, 2),
    COMMAND_TPA_PLAYER_DIDNT_REQUEST("{$e}You don''t have any tp requests.", 1, 2),
    COMMAND_TPA_MULTIPLE_REQUESTS("{$e}You have multiple tp requests. Please specify the player''s name.", 1, 2),
    COMMAND_TPAACCEPT_DESCRIPTION("{$s}Accept a teleportation request from a player.", 1, 2),
    COMMAND_TPAACCEPT_ALREADY_TELEPORTING(1, 2),
    COMMAND_TPAACCEPT_ALREADY_TELEPORTING_TELEPORTER("{$es}%target% {$e}wanted to accept your request, but you were already teleporting somewhere else.", 1, 2, 3),
    COMMAND_TPAACCEPT_ALREADY_TELEPORTING_TARGET("{$es}%teleporter% {$e}is already teleporting to somewhere else.", 1, 2, 3),
    COMMAND_TPAACCEPT_CHANGED_RELATION("{$e}Failed to accept request due to a recent relationship change.", 1, 2),
    COMMAND_TPAACCEPT_USAGE("{$usage}tpaaccept &9[player]", 1, 2),
    COMMAND_TPAACCEPT_REQUESTER_OFFLINE("{$es}%requester% {$e}is currently offline.", 1, 2),
    COMMAND_TPAACCEPT_TARGET_ABOUT_TO_TELEPORT("{$s}%teleporter% {$e}is about to teleport.", 1, 2),
    COMMAND_TPAACCEPT_TELEPORTER_ABOUT_TO("{$s}%target% {$p}has accepted your request. Please stand still to begin teleporting.", 1, 2),
    COMMAND_TPAACCEPT_TARGET_FAILED_TO_TELEPORT("{$es}%teleporter% {$e}has failed to teleport.", 1, 2),
    COMMAND_TPAACCEPT_TELEPORTER_FAILED_TO_TELEPORT("{$e}Failed to teleport.", 1, 2),
    COMMAND_TPAACCEPT_DISABLED_WORLD_TELEPORTER("{$es}%teleporter% {$e}is currently in a disabled world and cannot teleport.", 1, 2),
    COMMAND_TPAACCEPT_DISABLED_WORLD_TELEPORTER_NOTIFY("{$es}%target% {$e}wanted to accept your tp request, but you are in a disabled world.", 1, 2),
    COMMAND_TPAACCEPT_DISABLED_WORLD_TARGET("{$e}You''re in a disabled world and cannot accept tp requests.", 1, 2),
    COMMAND_TPAACCEPT_TELEPORTED_NOTIFICATION("{$s}%teleporter% {$p}has teleported to you.", 1, 2),
    COMMAND_TPAACCEPT_CANCELLED("{$es}%teleporter%{$e}''s teleportation has been cancelled.", 1, 2),
    COMMAND_TPAACCEPT_TELEPORTING_TARGET("{$s}%teleporter% {$p}will teleport to you in {$s}%timer% seconds", 1, 2),
    COMMAND_TPAACCEPT_TELEPORTING_TELEPORTER("{$s}%target% {$p}has accepted your tp request. Please do not move while you''re teleporting...", 1, 2),
    COMMAND_TPAREJECT_DESCRIPTION("{$s}Reject a teleportation request from a player.", 1, 2),
    COMMAND_TPAREJECT_USAGE("{$usage}tpareject &9[player]", 1, 2),
    COMMAND_TPAREJECT_REJECTED("{$e}Rejected teleport request of {$es}%teleporter%", 1, 2),
    COMMAND_TPAREJECT_REJECTED_NOTIFICATION("{$es}%target% {$e}has rejected your tp request.", 1, 2),
    COMMAND_SHOW_DESCRIPTION("{$s}Shows information about a guild.", 1, 2),
    COMMAND_SHOW_USAGE("&4Usage: {$es}/c show &9[player | guild | #PLAYER] {$p}<player | guild>", 1, 2),
    COMMAND_SHOW_OTHERS_PERMISSION("{$e}You don''t have permission to see others info.", 1, 2),
    COMMAND_SHOW_MESSAGE_ADMIN("{$sep}&m---------------=({$e}&l %guilds_guild_name% {$sep}&m)=---------------\n" +
            "\n" +
            "&7| {$p}Leader{$sep}: {$s}%leader_status%\n" +
            "&7| {$p}Home{$sep}: {$s}%guilds_guild_home%\n" +
//            "&7| {$p}Nexus{$sep}: {$s}%guilds_nexus%\n" +
            "&7| {$p}Might{$sep}: {$s}%guilds_fancy_might%\n" +
            "&7| {$p}Bank{$sep}: {$s}$%guilds_fancy_bank% {$sep}| {$p}Tax{$sep}: {$s}$%guilds_fancy_tax% {$sep}- {$s}$%guilds_fancy_server_guild_tax%\n" +
            "&7| {$p}Lands{$sep}: {$s}%guilds_fancy_claims%{$sep}/{$s}%guilds_fancy_max_claims%\n" +
            "&7| {$p}Resource Points{$sep}: {$s}%guilds_fancy_resource_points%\n" +
            "&7| {$p}Since{$sep}: {$s}%guilds_date_since%\n" +
            "&7| {$p}Members{$sep}: {$s}%guilds_members%&7/{$s}%guilds_max_members%\n" +
            "%members%", 1, 2),
    COMMAND_SHOW_MESSAGE("{$sep}&m---------------=({$e}&l %guilds_guild_name% {$sep}&m)=---------------\n" +
            "\n" +
            "&7| {$p}Leader{$sep}: {$s}%leader_status%\n" +
            "&7| {$p}Might{$sep}: {$s}%guilds_might%\n" +
            "&7| {$p}Since{$sep}: {$s}%guilds_date_since%\n" +
            "&7| {$p}Lands{$sep}: {$s}%guilds_claims%{$sep}/{$s}%guilds_max_claims%\n" +
            "&7| {$p}Bank{$sep}: {$s}$%guilds_fancy_bank%\n" +
            "&7| {$p}Resource Points{$sep}: {$s}%guilds_fancy_resource_points%\n" +
            "\n" +
            "&7| {$p}Members{$sep}:\n" +
            "%members%", 1, 2),
    COMMAND_SHOW_ALLIES("&7| {$p}Allies{$sep}:\n{$es}%allies%", 1, 2),
    COMMAND_SHOW_TRUCES("&7| {$p}Truces{$sep}:\n%truces%", 1, 2),
    COMMAND_SHOW_ENEMIES("&7| {$p}Enemies{$sep}:\n{$es}%enemies%", 1, 2),
    COMMAND_SHOW_ONLINE_PREFIX("hover:{{$sep}[%guilds_rank_color%%guilds_rank_symbol%{$sep}] &a%player%;{$p}Joined{$sep}: {$s}%guilds_date_joined%\n" +
            "{$p}Donations{$sep}: {$s}%guilds_total_donations%\n" +
            "{$p}Last Donation{$sep}: {$s}%guilds_last_donation_time%\n" +
            "{$p}Rank{$sep}: {$s}%guilds_rank_name%;/c gui structures/nexus/member %player%}", 1, 2),
    COMMAND_SHOW_OFFLINE_PREFIX("hover:{{$sep}[%guilds_rank_color%%guilds_rank_symbol%{$sep}] {$e}%player%;{$p}Joined{$sep}: {$s}%guilds_date_joined%\n" +
            "{$p}Donations{$sep}: {$s}%guilds_total_donations%\n" +
            "{$p}Last Donation{$sep}: {$s}%guilds_last_donation_time%\n" +
            "{$p}Rank{$sep}: {$s}%guilds_rank_name%;/c gui structures/nexus/member %player%}\n", 1, 2),
    COMMAND_SHOW_RELATION("{$sep}◆ %guilds_relation_color%%guilds_guild_name%", 1, 2),
    COMMAND_CLAIM_DESCRIPTION("{$s}claim the land you are currently standing in.", 1, 2),
    COMMAND_CLAIM_CHUNK_USAGE("{$usage}claim [x] <z>", 1, 2, 3),
    COMMAND_CLAIM_CHUNK_PERMISSION("{$e}You don''t have permission to claim lands with chunk coordinates.", 1, 2, 3),
    COMMAND_CLAIM_CHUNK_MAX_DISTANCE("{$e}You''re trying to claim a land that''s too far away{$sep}: {$es}%distance% &7> {$es}%max%", 1, 2, 3),
    COMMAND_CLAIM_DISABLED_WORLD("{$e}Claiming is disabled in {$s}%world%", 1, 2),
    COMMAND_CLAIM_IN_REGION("{$e}You cannot claim lands in protected regions.", 1, 2),
    COMMAND_CLAIM_NEAR_REGION("{$e}You cannot claim lands near protected regions.", 1, 2),
    COMMAND_CLAIM_NOT_CONNECTED("{$e}Your land must be connected to your other guild lands.", 1, 2),
    COMMAND_CLAIM_NOT_DISTANCED("{$e}Another guild claimed the lands around the land you''re trying to claim.", 1, 2),
//    COMMAND_CLAIM_NATION_ZONE("{$e}This land is a part of {$es}%nation%''s {$e}nation zone and cannot be claimed.", 1, 2),
    COMMAND_CLAIM_SUCCESS("{$p}Claimed {$s}%x%&7, {$s}%z% {$p}as guild land.", 1, 2),
    COMMAND_CLAIM_LIST_DESCRIPTION("{$s}A list of all the claims that a guild has. For accurate spacing for columns, consider downloading a resource pack with monospace font.", 1, 2, 3),
    COMMAND_CLAIM_LIST_NO_CLAIMS("{$e}No claims to show.", 1, 2, 3),
    COMMAND_CLAIM_LIST_TELEPORT_DESCRIPTION(" &7| {$p}Structures{$sep}: {$s}%structures%\n&7| {$p}Turrets{$sep}: {$s}%turrets%", 1, 2, 3),
    COMMAND_CLAIM_LIST_TELEPORT_ADMIN_DESCRIPTION("&7| {$p}Structures{$sep}: {$s}%structures%\n" +
            "&7| {$p}Turrets{$sep}: {$s}%turrets%\n" +
            "\n" +
            "&7Click to teleport to the\n" +
            "center of the land.", 1, 2, 3),
    COMMAND_CLAIM_FILL_DESCRIPTION("{$p}Claim all the lands inside a shape marked by claims. For example, if you claim a square shaped border (instead of using {$e}/c claim square {$p}you claim the perimeter of the square only.), you'll be able to claim the area (in a mathematical sense) of the square by standing inside the square (any unclaimed land inside) and using.\n" +
            "Of course this is useless for a square, but this also works for arbitrary shapes.\n" +
            "This command considers all types of claimed lands as \"borders\", meaning if you wanted to fill a shape next to an area claimed by another guild, you could simply claim 3 other sides connected to the 4th side which belongs to the other guild.", 1, 2, 3),
    COMMAND_CLAIM_FILL_IN_CLAIMED_LAND("{$e}Cannot fill, because the land you''re standing in is claimed.", 1, 2, 3),
    COMMAND_CLAIM_FILL_MAX_ITERATIONS("{$e}Maximum fill iteration cycle reached. Your shape is either too big to fill, or not completely surrounded by claims.", 1, 2, 3),
    COMMAND_CLAIM_FILL_MAX_CLAIMS("{$e}Stopped filling process since you can''t claim more than {$es}%lands% {$e}lands. Your shape is probably either too big to fill, or not completely surrounded by claims.", 1, 2, 3),
    COMMAND_CLAIM_FILL_DONE("{$p}Do hover:{{$s}/c claim confirm;{$p}Execute;/c claim confirm} {$p}to claim the lands.", 1, 2, 3),
    COMMAND_CLAIM_EXCEEDED_MAX("{$e}You cannot claim more than {$es}%max% {$e}lands!", 1, 2),
    COMMAND_CLAIM_ALREADY_OWNED("{$e}Your guild already owns this land!", 1, 2),
    COMMAND_CLAIM_OCCUPIED_LAND("{$e}This land is occupied by another guild! Invade it with {$es}/c invade!", 1, 2),
    COMMAND_CLAIM_CANT_OVERCLAIM("{$e}You cannot overclaim this guild.", 1, 2),
    COMMAND_CLAIM_MAX_CLAIMS_PLAYER("{$e}Your rank is only allowed to claim {$es}%limit% {$e}lands.", 1, 2),
    COMMAND_CLAIM_NO_CLAIMS_PLAYER("{$e}Your rank is not allowed to claim any lands.", 1, 2),
    COMMAND_CLAIM_MAX_CLAIMS("{$e}Your guild can only claim {$s}%limit% {$e}lands. {$e}Two ways to increase this limit is to get more members or upgrade the max claims misc upgrade from your nexus.", 1, 2),
    COMMAND_CLAIM_SQUARE_DESCRIPTION("{$p}Claim a square with a specified radius around you.", 1, 2, 3),
    COMMAND_CLAIM_SQUARE_USAGE("{$usage}claim square {$p}<radius>", 1, 2, 3),
    COMMAND_CLAIM_SQUARE_RADIUS_INVALID("{$e}The specified square radius is invalid{$sep}: {$es}%radius%", 1, 2, 3, 4),
    COMMAND_CLAIM_SQUARE_RADIUS_DISALLOWED("{$e}The specified square radius is must be at least {$es}2 {$e}and less than {$es}%max% {$e}got{$sep}: {$es}%radius%", 1, 2, 3, 4),
    COMMAND_CLAIM_SQUARE_DONE("{$p}Do hover:{{$s}/c claim confirm;{$p}Execute;/c claim confirm} {$p}to claim the lands.", 1, 2, 3),
    COMMAND_CLAIM_AUTO_DESCRIPTION("{$p}Automatically claim lands as you walk.", 1, 2, 3),
    COMMAND_CLAIM_AUTO_USAGE("{$usage}claim auto &9[player]", 1, 2, 3),
    COMMAND_CLAIM_AUTO_PERMISSION("{$e}You don''t have permission to set others auto claim.", 1, 2, 3),
    COMMAND_CLAIM_AUTO_ON("{$s}Auto claim is now &aON{$s}. You''ll now claim lands as you walk.", 1, 2, 3),
    COMMAND_CLAIM_AUTO_OFF("{$s}Auto claim is now {$e}OFF{$s}.", 1, 2, 3),
    COMMAND_CLAIM_NEED_RP("{$e}You need {$es}%fancy_rp% {$e}resource points to claim land!", 1, 2),
    COMMAND_CLAIM_CONFIRMATION("{$e}Your guild has to pay {$es}%fancy_rp% {$e}resource points and {$es}%fancy_money% {$e}money.\n" +
            "Are you sure you want to continue? Do the command again.", 1, 2),
    COMMAND_CLAIM_NEED_MONEY("{$e}Your guild needs {$es}%fancy_money% {$e}money to claim land! Deposit money to your guild bank using {$es}/c bank deposit", 1, 2),
    COMMAND_CLAIM_DISALLOWED_BIOME("{$e}You can't claim in {$es}%biome% {$e}biomes.\n" +
            "Note that usually the entire chunk needs to be made out of this type of biome.", 1, 2),
    COMMAND_CLAIM_LINE_DESCRIPTION("{$p}Claim a straight line in a direction. If no direction is specified, it''ll be the direction that the player is facing.", 1, 2, 3),
    COMMAND_CLAIM_LINE_USAGE("{$usage}claim line {$s}[distance]", 1, 2, 3),
    COMMAND_CLAIM_LINE_INVALID_DIRECTION("{$e}Invalid direction{$sep}: {$es}%direction%", 1, 2, 3),
    COMMAND_CLAIM_LINE_MAX_DISTANCE("{$e}Maximum claiming distance is {$es}%max%", 1, 2, 3),
    COMMAND_CLAIM_LINE_MIN_DISTANCE("{$e}Minimum claiming distance is {$es}%min%", 1, 2, 3),
    COMMAND_CLAIM_LINE_DONE("{$p}Claimed {$s}%lands% {$p}lands in {$s}%direction% {$p}direction.\n" +
            "Do hover:{{$s}/c claim confirm;{$p}Execute;/c claim confirm} {$p}to claim the lands.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_DESCRIPTION("{$s}Modify your claim selection before confirming.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_USAGE("{$usage}claim clipboard [add/remove | clear] <x> <z>", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_DIFFERENT_WORLD("{$e}Your clipboard belongs to the world{$sep}: {$es}%world%", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_UNKNOWN_ACTION("{$e}Unknown clipboard action{$sep}: {$es}%action%", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_EMPTY("{$e}You don''t have anything in the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_ADDED("{$p}Added {$s}%x%&7, {$s}%z% {$p}to the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_ALREADY_ADDED("{$es}%x%&7, {$es}%z% {$e}was already in the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_REMOVED("{$p}Removed {$s}%x%&7, {$s}%z% {$p}from the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_NOT_FOUMD("{$es}%x%&7, {$es}%z% {$e}was not in the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_CLEARED("{$p}Your clipboard has been successfully cleared.", 1, 2, 3),
    COMMAND_CLAIM_CLIPBOARD_COST("{$p}Claiming these lands will cost {$s}%rp% resource points {$p}and {$s}$%money% money.", 1, 2, 3),
    COMMAND_CLAIM_CONFIRM_DESCRIPTION("{$s}Confirm and claim all the lands in your claim clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CONFIRM_EMPTY("{$e}You don''t have anything in the clipboard.", 1, 2, 3),
    COMMAND_CLAIM_CONFIRM_FAIL("{$e}Could not claim {$es}%x%&7, {$es}%z% {$e}due to recent changes{$sep}:\n" +
            "    {$e}%error%", 1, 2, 3),
    COMMAND_CLAIM_CONFIRM_SUCCESS("{$p}Successfully claimed all the lands.", 1, 2, 3),
    COMMAND_CLAIM_CORNER_DESCRIPTION("{$s}Set two positions as corners and claim everything as a rectangle.", 1, 2, 3),
    COMMAND_CLAIM_CORNER_USAGE("{$usage}claim corner &9[pos1 &7| &9pos2]", 1, 2, 3),
    COMMAND_CLAIM_CORNER_SET_POS1("{$p}The first corner position has been set in chunk{$sep}: {$s}%chunkX1%&7, {$s}%chunkZ1%", 1, 2, 3, 4),
    COMMAND_CLAIM_CORNER_SET_POS2("{$p}The second corner position has been set in chunk{$sep}: {$s}%chunkX2%&7, {$s}%chunkZ2%", 1, 2, 3, 4),
    COMMAND_UNCLAIM_AUTO_DESCRIPTION("{$p}Automatically claim lands as you walk.", 1, 2, 3),
    COMMAND_UNCLAIM_AUTO_USAGE("{$usage}unclaim auto &9[player]", 1, 2, 3),
    COMMAND_UNCLAIM_AUTO_ON("{$s}Auto claim is now &aON{$s}. You''ll now claim lands as you walk.", 1, 2, 3),
    COMMAND_UNCLAIM_AUTO_OFF("{$s}Auto claim is now {$e}OFF{$s}.", 1, 2, 3),
    COMMAND_UNCLAIM_LINE_DESCRIPTION("{$p}Unclaim a straight line in a direction. If no direction is specified, it''ll be the direction that the player is facing.", 1, 2, 3),
    COMMAND_UNCLAIM_LINE_USAGE("{$usage}unclaim line {$s}[distance]", 1, 2, 3),
    COMMAND_UNCLAIM_LINE_DONE("{$p}Unclaimed {$s}%lands% {$p}lands in {$s}%direction% {$p}direction.\n" +
            "Do hover:{{$s}/c unclaim confirm;{$p}Execute;/c unclaim confirm} {$p}to unclaim the lands.", 1, 2, 3),
    COMMAND_UNCLAIM_DESCRIPTION("{$s}Unclaim the land you are currently standing in.", 1, 2),
    COMMAND_UNCLAIM_SUCCESS("{$p}Unclaimed land at {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNCLAIM_DISCONNECTION("{$e}Unclaiming that land will disconnect lands at the location you''re trying to unclaim.", 1, 2),
    COMMAND_UNCLAIM_NOT_CLAIMED("{$e}This land is not claimed.", 1, 2),
    COMMAND_UNCLAIM_COOLDOWN("{$e}You can unclaim this land in {$es}%time%", 1, 2),
    COMMAND_UNCLAIM_CANNOT_REMOVE_NEXUS("{$e}Cannot unclaim a land with nexus in it.\n" +
            "Please relocate your nexus usinghover:{{$es}/c nexus;&4Click to Enable;/c nexus} {$e}command.", 1, 2),
    COMMAND_UNCLAIM_OCCUPIED_LAND("{$e}This land is occupied by another guild! Invade it with {$es}/c invade!", 1, 2),
    COMMAND_UNCLAIM_CONFIRMATION_TURRETS("{$e}This land contains some turrets. If you''re sure you want to unclaim it do {$es}/c unclaim confirm", 1, 2, 3),
    COMMAND_UNCLAIM_CONFIRMATION_STRUCTURES("{$e}This land contains some structures. If you''re sure you want to unclaim it do {$es}/c unclaim confirm", 1, 2, 3),
    COMMAND_UNCLAIM_CONFIRMATION_HOME("{$e}This land is the guilds home. If you''re sure you want to unclaim it do {$es}/c unclaim confirm", 1, 2, 3),
    COMMAND_UNCLAIM_ALL_DESCRIPTION("{$s}Unclaims all your guild lands.", 1, 2, 3),
    COMMAND_UNCLAIM_ALL_SUCCESS("{$p}Unclaimed a total of {$s}%lands% {$p}lands!", 1, 2, 3),
    COMMAND_UNCLAIM_ALL_NOT_ENOUGH_RESOURCEPOINTS("{$e}You need {$es}%rp% {$e}to unclaim all your lands.", 1, 2, 3),
    COMMAND_UNCLAIM_ALL_CONFIRM("{$e}You're about to unclaim {$es}&l%lands%{$e}.\n" +
            "All your turrets and structures will be unclaimed as well and your chests left unprotected.\n" +
            "If you want to continue do the command again.", 1, 2, 3),
    COMMAND_UNCLAIM_ALL_ANNOUNCE("{$s}%player% {$p}has unclaimed a total of {$s}%lands% {$p}lands!", 1, 2, 3),
    COMMAND_UNDO_DESCRIPTION("{$s}Undo a claim/unclaim.", 1, 2),
    COMMAND_UNDO_ALREADY_UNCLAIMED("{$s}The last land that was claimed is already unclaimed. {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNDO_ALREADY_CLAIMED("{$s}The last land that was unclaimed is already claimed. {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNDO_CLAIMED("{$p}You''ve claimed your claimed land at {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNDO_CLAIMED_NOT_OWNED("{$e}The land {$s}%x%&7, {$s}%z% {$e}was claimed by another player. {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNDO_UNCLAIMED("{$p}You''ve unclaimed your unclaimed land at {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_UNDO_NO_HISTORY("{$e}You don''t have any land history left to undo.", 1, 2),
    COMMAND_REDO_NO_HISTORY("{$e}You don''t have any land history left to redo.", 1, 2),
    COMMAND_REDO_DESCRIPTION("{$s}Redo a claim/unclaim.", 1, 2),
    COMMAND_REDO_ALREADY_CLAIMED("{$s}The last land that was claimed is already claimed. {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_REDO_ALREADY_UNCLAIMED("{$s}The last land that was unclaimed is already unclaimed. {$s}%x%&7, {$s}%z%", 1, 2),
    COMMAND_VISUALIZE_DESCRIPTION("{$s}Set permanent indicators or disable them.", 1, 2),
    COMMAND_VISUALIZE_USAGE("{$usage}visualize [permanent|toggle]", 1, 2),
    COMMAND_VISUALIZE_DISPLAY("&7| {$p}Location{$sep}: {$s}%x%&7, {$s}%z%\n" +
            "&7| {$p}Guild{$sep}: {$s}%guild%", 1, 2),
    COMMAND_VISUALIZE_ADMIN_DISPLAY("&7| {$p}Location{$sep}: {$s}%x%&7, {$s}%z%\n" +
            "&7| {$p}Guild{$sep}: {$s}%guild%\n" +
//            "&7| {$p}Structure{$sep}: \n" +
//            "{$s}%structures%\n" +
//            "&7| {$p}Turrets{$sep}:\n" +
//            "%turrets%\n" +
            "&7| {$p}Protection Signs{$sep}:\n" +
            "%protection-signs%", 1, 2),
    COMMAND_VISUALIZE_TOGGLE_DESCRIPTION("{$s}Toggle land indicators (visualizers or markers)", 1, 2, 3),
    COMMAND_VISUALIZE_TOGGLE_ENABLED("{$p}Indicators has been turned {$s}ON", 1, 2, 3),
    COMMAND_VISUALIZE_TOGGLE_DISABLED("{$p}Indicators has been turned {$e}OFF", 1, 2, 3),
    COMMAND_VISUALIZE_ALL_DESCRIPTION("{$s}Shows the visualizers for all the chunks that are claimed by your guild.", 1, 2, 3),
    COMMAND_VISUALIZE_ALL_SHOWING("{$p}Visualizing...", 1, 2, 3),
    COMMAND_VISUALIZE_MARKERS_DESCRIPTION("{$s}Change your land markers type.", 1, 2, 3),
    COMMAND_VISUALIZE_MARKERS_USAGE("{$usage}visualize markers <type>", 1, 2, 3),
    COMMAND_VISUALIZE_MARKERS_ALREADY_USING("{$e}You''re already using {$es}%markers% {$e}markers type.", 1, 2, 3),
    COMMAND_VISUALIZE_MARKERS_CHANGED("{$p}Your land markers have been changed to {$s}%markers%", 1, 2, 3),
    COMMAND_VISUALIZE_MARKERS_INVALID("{$e}Cannot find the specified land markers{$sep}: {$es}%markers%", 1, 2, 3),
    COMMAND_VISUALIZE_PERMANENT_DESCRIPTION("{$s}Toggle if the land indicator should be permanent.", 1, 2, 3),
    COMMAND_VISUALIZE_PERMANENT_ENABLED("{$p}Permanent indicators has been turned {$s}ON.", 1, 2, 3),
    COMMAND_VISUALIZE_PERMANENT_DISABLED("{$p}Permanent indicators has been turned {$e}OFF", 1, 2, 3),
    COMMAND_BANK_DESCRIPTION("{$s}Deposit or withdraw money from your guild''s bank.", 1, 2),
    COMMAND_BANK_USAGE("{$usage}bank {$p}<deposit/withdraw> <amount>", 1, 2),
    COMMAND_BANK_UNKNOWN_TRANSACTION("{$e}Unknown transaction {$es}%transaction%", 1, 2),
    COMMAND_BANK_NEGATIVE("{$e}You can''t transit negative values...", 1, 2),
    COMMAND_BANK_ZERO("{$e}You can''t transit nothing.", 1, 2),
    COMMAND_BANK_NOT_ENOUGH_GUILD_MONEY("{$e}Your guild''s bank doesn''t have {$es}%amount% {$e}money.", 1, 2, 3),
    COMMAND_BANK_NOT_ENOUGH_MONEY("{$e}You don''t have {$es}%amount% {$e}money.", 1, 2, 3),
    COMMAND_BANK_DEPOSIT_LIMIT("{$e}Your guild''s bank is full. The limit is {$es}%limit%", 1, 2, 3),
    COMMAND_BANK_DEPOSIT_SUCCESS("{$p}You've deposited {$s}%amount% {$p}money into your guild's bank.\n" +
            "Your new balance{$sep}: {$s}%fancy_balance%\n" +
            "{$p}Your guild's bank{$sep}: {$s}%guilds_fancy_bank%", 1, 2, 3),
    COMMAND_BANK_DEPOSIT_DISABLED("{$e}You cannot deposit money.", 1, 2, 3),
    COMMAND_BANK_DEPOSIT_MIN("{$e}You have to at least deposit {$es}$%min%", 1, 2, 3),
    COMMAND_BANK_WITHDRAW_MIN("{$e}You have to at least withdraw {$es}$%min%", 1, 2, 3),
    COMMAND_BANK_WITHDRAW_DISABLED("{$e}You cannot withdraw money.", 1, 2, 3),
    COMMAND_BANK_WITHDRAW_SUCCESS("{$p}You've withdrawn {$s}%amount% {$p}money from your guild's bank.\n" +
            "Your new balance{$sep}: {$s}%fancy_balance%\n" +
            "{$p}Your guild's bank{$sep}: {$s}%guilds_fancy_bank%", 1, 2, 3),
    COMMAND_GLOW_DESCRIPTION("{$s}Easily find your guild members.", 1, 2),
    COMMAND_GLOW_ENABLED("{$s}Glow mode{$sep}: {$p}Enabled", 1, 2),
    COMMAND_GLOW_DISABLED("{$s}Glow mode{$sep}: {$e}Disabled", 1, 2),
    COMMAND_DONATE_DESCRIPTION("{$s}Donate resourcepoints to a guild.", 1, 2),
    COMMAND_DONATE_USAGE("{$usage}donate <guild> <amount>", 1, 2),
    COMMAND_DONATE_DONT_HAVE("{$e}You don't have {$es}%rp% {$e}resource points.", 1, 2),
    COMMAND_DONATE_DONE("{$p}You've donated {$s}%rp% {$p}to {$s}%guild%", 1, 2),
    COMMAND_DONATE_DONATED("{$s}%guild% {$p}has donated {$s}%rp% {$p}resource points to you.", 1, 2),
    COMMAND_DONATE_INVALID("{$e}You can't donate {$es}%rp% {$e}resource points...", 1, 2),
    COMMAND_SNEAK_DESCRIPTION("{$s}Your guild relationship with another player will not disable their fly.", 1, 2),
    COMMAND_SNEAK_OTHERS_PERMISSION("{$e}You don't have permission to set others sneak mode.", 1, 2),
    COMMAND_SNEAK_ENABLED("{$s}Sneak mode{$sep}: {$p}Activated", 1, 2),
    COMMAND_SNEAK_DISABLED("{$s}Sneak mode{$sep}: {$e}Deactivated", 1, 2),
    COMMAND_TOP_DESCRIPTION("{$s}Shows the top ranking guilds.", 1, 2),
    COMMAND_TOP_LOADING("{$s}Loading data...", 1, 2),
    COMMAND_TOP_HEADER("{$sep}----------------------------------", 1, 2),
    COMMAND_TOP_FOOTER("     {$sep}[hover:{{$s}←;{$s}Previous Page;/c top %previous_page%}{$sep}] {$sep}[hover:{{$s}→;{$s}Next Page;/c top %next_page%}{$sep}]", 1, 2),
    COMMAND_TOP_NO_MORE_PAGES("{$e}No more pages to load.", 1, 2),
    COMMAND_TOP_NEGATIVE("{$e}Cannot show negative pages.", 1, 2),
    COMMAND_TOP_ENTRY("{$sep}♦ &7%rank%. hover:{{$p}%guilds_guild_name%;&7♦ {$p}Members{$sep}: {$s}%guilds_members%\n&7♦ {$p}Lands{$sep}: {$s}%guilds_claims%&7/{$s}%guilds_max_claims%\n&7♦ {$p}Resource Points{$sep}: {$s}%guilds_fancy_resource_points%\n&7♦ {$p}Bank{$sep}: {$s}$%guilds_fancy_bank%;/c show %guilds_guild_name%} &7- {$s}%guilds_fancy_might%", 1, 2),
    COMMAND_VAULT_DESCRIPTION("{$s}Open your guild chest remotely.", 1, 2),
    COMMAND_VAULT_OTHERS_PERMISSION("{$e}You can't access other guilds chest.", 1, 2),
    VAULT_BLACKLISTED_ITEM("{$e}You can't put this item into your guild's vault.", 1),
    COMMAND_FLY_DESCRIPTION("{$s}Enable flight in your own land.", 1, 2),
    COMMAND_FLY_USAGE("{$usage}fly &9[player]", 1, 2),
    COMMAND_FLY_OTHERS_PERMISSION("{$e}You don't have permission to set others fly state.", 1, 2),
    COMMAND_FLY_ENABLED("{$s}Flight Mode{$sep}: {$p}ON", 1, 2),
    COMMAND_FLY_DISABLED("{$s}Flight Mode{$sep}: &4OFF", 1, 2),
    COMMAND_FLY_NOT_ALLOWED("{$e}You can't fly in territory of {$es}%guild%", 1, 2),
    COMMAND_FLY_UNCLAIMED("{$e}You can only fly in claimed lands that allow flying.", 1, 2),
    COMMAND_FLY_OWN_ENEMY_NEARBY("{$e}You can't fly, there are enemies nearby.", 1, 2),
    COMMAND_FLY_CANT_AFFORD("{$e}You need {$es}$%money% {$e}to activate flight.", 1, 2),
    COMMAND_FLY_CANT_AFFORD_GUILD("{$e}Your guild needs {$es}%amount% {$e}resource points to activate flight.", 1, 2),
    COMMAND_PVP_DESCRIPTION("{$s}Enable guilds friendly fire.", 1, 2),
    COMMAND_PVP_OTHERS_PERMISSION("{$e}You don't have permission to set others pvp mode.", 1, 2),
    COMMAND_PVP_ON("{$s}PvP Mode{$sep}: {$p}ON", 1, 2),
    COMMAND_PVP_OFF("{$s}PvP Mode{$sep}: {$e}OFF", 1, 2),
    COMMAND_RELATIONS_DESCRIPTION("{$s}Shows all the current relation requests.", 1, 2),
    COMMAND_RELATIONS_NO_REQUESTS("{$e}Your guild doesn't have any requests.", 1, 2),
    COMMAND_ALLY_USAGE("{$usage}ally {$p}<guild>", 1, 2),
    COMMAND_ALLY_SENDER("{$p}Sent an alliance request to {$s}%guilds_other_guild_name%", 1, 2),
    COMMAND_ALLY_LIMIT("{$e}You can''t ally more than {$es}%max% {$e}guilds.", 1, 2),
    COMMAND_ALLY_ALREADY("{$e}You''re already allies with {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_ALLY_ALREADY_REQUESTED("{$e}You''ve already sent a request to {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_ALLY_SELF("{$e}You can''t make your own guild an ally.", 1, 2),
    COMMAND_ALLY_RECEIVER("{$s}%guilds_guild_name% {$p}wishes to be allies with you. Accept with hover:{{$s}/c ally %guilds_guild_name%;{$p}Accept;/c ally %guilds_guild_name%}", 1, 2),
    COMMAND_ALLY_ALLIES("{$p}You''re now allies with {$s}%guild%", 1, 2),
    COMMAND_ENEMY_DESCRIPTION("{$s}Make a guild your enemy.", 1, 2),
    COMMAND_ENEMY_USAGE("{$usage}enemy {$p}<guild>", 1, 2),
    COMMAND_ENEMY_LIMIT("{$e}You can''t be enemies with more than {$es}%max% {$e}guilds.", 1, 2),
    COMMAND_ENEMY_ALREADY("{$e}You''re already enemies with {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_ENEMY_ALREADY_REQUESTED("{$e}You''ve already sent an enemy request to {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_ENEMY_HIGHER_STRENGTH("{$e}Your guild is much more stronger than this guild. You cannot enemy them.", 1, 2),
    COMMAND_ENEMY_NOTIFY("{$es}%guilds_guild_name% {$e}has enemied %guilds_other_name% {$e}which both guilds are your allies.", 1, 2),
    COMMAND_ENEMY_SELF("{$e}You can''t make your own guild an enemy...", 1, 2),
    COMMAND_ENEMY_COST("{$e}You need {$es}%cost% resource points {$e}to enemy this guild.", 1, 2),
    COMMAND_ENEMY_ENEMIES("{$e}You''re now enemies with {$es}%guild%", 1, 2),
    COMMAND_TRUCE_DESCRIPTION("{$s}Send a truce request to a guild.", 1, 2),
    COMMAND_TRUCE_USAGE("{$usage}truce {$p}<guild>", 1, 2),
    COMMAND_TRUCE_LIMIT("{$e}You can''t be truce with more than {$es}%max% {$e}guilds.", 1, 2),
    COMMAND_TRUCE_SENDER("{$p}Sent a truce request to {$s}%guilds_other_guild_name%", 1, 2),
    COMMAND_TRUCE_ALREADY("{$e}You''re already truces with {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_TRUCE_ALREADY_REQUESTED("{$e}You''ve already sent a request to {$es}%guilds_other_guild_name%'", 1, 2),
    COMMAND_TRUCE_SELF("{$e}You can''t make your own guild a truce.", 1, 2),
    COMMAND_TRUCE_RECEIVER("{$s}%guilds_guild_name% {$p}wishes to be truce with you. Accept with hover:{{$s}/c truce %guilds_guild_name%;{$p}Accept;/c truce %guilds_guild_name%}", 1, 2),
    COMMAND_TRUCE_TRUCES("{$p}You''re now truce with {$s}%guild%", 1, 2),
    COMMAND_REJECTRELATION_DESCRIPTION("{$s}Reject a relationship request sent to your guild.", 1, 2),
    COMMAND_REJECTRELATION_USAGE("{$usage}rejectrelation {$p}<guild>", 1, 2),
    COMMAND_REJECTRELATION_NOT_REQUESTED("{$es}%guilds_guild_name% {$e}has not requested any relationship.", 1, 2),
    COMMAND_REJECTRELATION_REJECTED("{$e}Rejected the %relation% {$e}relationship request sent by {$es}%guilds_guild_name%", 1, 2),
    COMMAND_REJECTRELATION_NOTIFICATION("{$e}%guilds_guild_name% {$es}has rejected your %relation% {$e}relationship request.", 1, 2),
    COMMAND_REVOKE_DESCRIPTION("{$s}Revoke relationship with a guild.", 1, 2),
    COMMAND_REVOKE_USAGE("{$usage}revoke {$p}<guild>", 1, 2),
    COMMAND_REVOKE_SENDER("{$e}You''ve revoked relations with {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_REVOKE_NO_RELATION("{$e}You have no relations with {$es}%guilds_other_guild_name%", 1, 2),
    COMMAND_REVOKE_ALREADY_NEUTRAL("{$es}%guilds_other_guild_name% {$e}is already a neutral.", 1, 2),
    COMMAND_REVOKE_SELF("{$e}You can''t have any relationship with your own guild...", 1, 2),
    COMMAND_REVOKE_SPECIFIC_PERMISSION("{$e}You don''t have permission to revoke {$es}%relation% {$e}relationships.", 1, 2),
    COMMAND_REVOKE_RECEIVER("{$es}%guilds_guild_name% {$e}has revoked their relationship with you. You''re now neutrals.", 1, 2),
    COMMAND_REVOKE_REQUEST_SENDER("{$p}Sent a peace request to {$s}%guilds_other_guild_name%", 1, 2),
    COMMAND_REVOKE_REQUEST_RECEIVER("{$s}%guilds_guild_name% {$p}wishes to establish peace with you. Accept with hover:{{$s}/c neutral %guilds_guild_name%;{$p}Accept;/c neutral %guilds_guild_name%}", 1, 2),
    COMMAND_REVOKE_NEUTRALS("{$p}You''re now neutral with {$s}%guild%", 1, 2),
    COMMAND_RESOURCEPOINTS_WITHDRAW_DESCRIPTION("{$s}Get money by taking resource points from your guild.", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_WITHDRAW_USAGE("{$usage}rp withdraw {$p}<resource points>", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_WITHDRAW_MIN("{$e}You have to withdraw at least {$es}%min% resource points{$e}.", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_WITHDRAW_SUCCESS("{$p}You've successfully withdrawn {$s}%rp% {$p}resource points from your guild for {$s}$%worth%\n" +
            "{$p}Your new balance{$sep}: {$s}$%balance%\n" +
            "{$p}Your guild's resource points{$sep}: {$s}%guilds_fancy_resource_points%", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_DEPOSIT_DESCRIPTION("{$s}Get resource points by using your money.", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_DEPOSIT_USAGE("{$usage}rp deposit {$p}<resource points>", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_DEPOSIT_SUCCESS("{$p}You've successfully deposited {$s}%rp% resource points {$p}into your guild for {$s}$%worth%\n" +
            "{$p}Your new balance{$sep}: {$s}$%balance%", 1, 2, 3),
    COMMAND_RESOURCEPOINTS_NOT_ENOUGH_RESOURCE_POINTS("{$e}Your guild doesn''t have {$es}%rp% {$e}resource points.", 1, 2, 4),
    COMMAND_RESOURCEPOINTS_NOT_ENOUGH_MONEY("{$es}%rp% resource points {$e}needs {$es}$%worth%{$e}, but you only have {$es}$%balance%", 1, 2, 4),
    COMMAND_RESOURCEPOINTS_NEGATIVE("{$e}You can''t transit negative resource points...", 1, 2),
    COMMAND_RESOURCEPOINTS_ZERO("{$e}You can''t transit nothing.", 1, 2),
    COMMAND_MAP_DESCRIPTION("{$s}Shows a map of guilds and structures around you.", 1, 2),
    COMMAND_MAP_USAGE("{$usage}map &9[auto {$sep}| &9height] [width]", 1, 2),
    COMMAND_MAP_GUILD_PLAYER_ONLY("{$e}Only players with a guild can use the map.", 1, 2),
    COMMAND_MAP_INVALID_HEIGHT("{$e}Invalid height number {$es}%height% {$e}Using the default height.", 1, 2),
    COMMAND_MAP_INVALID_WIDTH("{$e}Invalid width number {$es}%width% {$e}Using the default width.", 1, 2),
    COMMAND_MAP_MAX_HEIGHT("{$e}The maximum allowed map height is {$es}%limit%", 1, 2),
    COMMAND_MAP_MAX_WIDTH("{$e}The maximum allowed map width is {$es}%limit%", 1, 2),
    COMMAND_MAP_AUTO_PERMISSION("{$e}You don''t have permission to use auto map.", 1, 2, 3),
    COMMAND_MAP_AUTO_ENABLED("{$p}Auto map has been turned {$s}ON", 1, 2, 3),
    COMMAND_MAP_AUTO_DISABLED("{$p}Auto map has been turned {$e}OFF", 1, 2, 3),
    COMMAND_MAP_SIZE_PERMISSION("{$e}You don''t have permission to change your map size.", 1, 2, 3),
    COMMAND_MAP_SIZE_CHANGED("{$p}Your map size has been changed from &7({$s}Height{$sep}: &9%guilds_map_height% &7| {$s}Width{$sep}: &9%guilds_map_width%&7) {$p}to &7({$s}Height{$sep}: &9%new_height% &7| {$s}Width{$sep}: &9%new_width%&7)", 1, 2, 3),
    COMMAND_MAP_SIZE_RESET("{$p}Your map size has been reset.", 1, 2, 3),
    COMMAND_MAP_SCOREBOARD_NOT_SUPPORTED("{$e}This server version doesn''t support scoreboard-based maps.", 1, 2, 3),
    COMMAND_MAP_SETTINGS_DESCRIPTION("{$s}Change your {$es}/c map {$s}settings.", 1, 2, 3),
    COMMAND_MAIL_DESCRIPTION("{$s}Mail related commands.", 1, 2),
    COMMAND_MAIL_REPLY_DESCRIPTION("{$s}Reply to a mail. This command is not intended to be used by players directly.", 1, 2, 3),
    COMMAND_MAIL_REPLY_USAGE("{$usage}mail reply {$p}<id>", 1, 2, 3),
    COMMAND_MAIL_OPEN_DESCRIPTION("{$s}Remotely open a mail. This command is not intended to be used by players directly.", 1, 2, 3),
    COMMAND_MAIL_OPEN_USAGE("{$usage}mail open {$p}<id>", 1, 2, 3),
    COMMAND_MAIL_INVALID_ID("{$e}The entered mail ID is invalid.", 1, 2, 3),
    COMMAND_MAIL_NOT_FOUND("{$e}No mail exists with that ID. It was probably deleted.", 1, 2 ,3),
    COMMAND_MAIL_NOT_YOURS("{$e}This mail doesn''t belong to your guild.", 1, 2 ,3),
    COMMAND_BOOK_DESCRIPTION("{$s}A little handbook for your guild.", 1, 2),
    COMMAND_BOOK_CHAPTER_NOT_FOUND("{$e}Could not find chapter{$sep}: {$es}%chapter%", 1, 2),
    COMMAND_BOOK_PERMISSION("{$e}You don't have permission to modify books.", 1, 2),
    COMMAND_BOOK_RENAME_DESCRIPTION("{$s}Rename a chapter in your guild book.", 1, 2, 3),
    COMMAND_BOOK_RENAME_USAGE("{$usage}book rename {$p}<chapter> <name>", 1, 2, 3),
    COMMAND_BOOK_RENAME_SAME("{$e}This chapter already had the same name.", 1, 2, 3),
    COMMAND_BOOK_RENAME_DUPLICATE("{$e}There's another chapter with the name {$es}%chapter%", 1, 2, 3),
    COMMAND_BOOK_RENAME_RENAMED("{$p}Successfully renamed {$s}%old_chapter% {$p}to {$s}%new_chapter%", 1, 2, 3),
    COMMAND_BOOK_OPEN_DESCRIPTION("{$s}Read your guild's handbook.", 1, 2, 3),
    COMMAND_BOOK_OPEN_USAGE("{$usage}book open &9[chapter]", 1, 2, 3),
    COMMAND_BOOK_PREVIEW_DESCRIPTION("{$s}Preview the formatted version of the chapter you're editing.", 1, 2, 3),
    COMMAND_BOOK_PREVIEW_NOTHING_TO_PREVIEW("{$e}You're not editing any chapters to preview.", 1, 2, 3),
    COMMAND_BOOK_PREVIEW_PREVIEWING("{$p}You're now previewing a formatted version of chapter{$sep}: {$s}%chapter%", 1, 2, 3),
    COMMAND_BOOK_DISCARD_DESCRIPTION("{$s}Discards all the changes made to a chapter.", 1, 2, 3),
    COMMAND_BOOK_DISCARD_NOTHING_TO_DISCARD("{$e}You're not editing any chapters to discard changes.", 1, 2, 3),
    COMMAND_BOOK_DISCARD_DISCARDED("{$p}All changes made to chapter {$s}%chapter% {$p}has been discarded.", 1, 2, 3),
    COMMAND_BOOK_REMOVE_DESCRIPTION("{$s}Remove a chapter from your guild's handbook.", 1, 2, 3),
    COMMAND_BOOK_REMOVE_USAGE("{$usage}book remove {$p}<chapter>", 1, 2, 3),
    COMMAND_BOOK_REMOVE_DELETED("{$p}Successfully removed chapter{$sep}: {$s}%chapter%", 1, 2, 3),
    BOOKS_CANT_MOVE("{$e}You can't move this book.", 1),
    BOOKS_USED_BY_ANOTHER_SESSION("{$e}Book input is being used by another session. Please complete that first.", 1),
    BOOKS_NO_EMPTY_SLOT("{$e}You don't have an empty slot available in your hotbar.", 1),
    COMMAND_BOOK_EDIT_DESCRIPTION("{$s}Edit your guild's handbook.", 1, 2, 3),
    COMMAND_BOOK_EDIT_USAGE("{$usage}book edit {$p}<chapter>", 1, 2, 3),
    COMMAND_BOOK_EDIT_ALREADY_EDITING("{$e}You're already editing chapter {$es}%chapter%", 1, 2, 3),
    COMMAND_BOOK_EDIT_CHANGED("{$p}Changed", 1, 2, 3),
    COMMAND_BOOK_EDIT_CREATE("{$p}That chapter didn't exist. Creating a new chapter... Do hover:{{$s}/c book cancel;/c book cancel;/c book cancel} {$p}to cancel.", 1, 2, 3),
    COMMAND_BOOK_EDIT_CREATE_LIMIT("{$e}Your guild cannot have more than {$es}%limit% {$e}chapters.", 1, 2, 3),
    COMMAND_BOOK_EDIT_CREATE_MAX_LENGTH("{$e}Chapter titles must have maximum of {$es}%max% {$e}characters.", 1, 2, 3),
    COMMAND_BOOK_EDIT_MODIFY("{$p}You're now modifying chapter{$sep}: {$s}%chapter%\n{$p}Do hover:{{$s}/c book discard;/c book discard;/c book discard} {$p}to discard any changes.", 1, 2, 3),
    COMMAND_NOT_INTENDED_FOR_DIRECT_USE("{$e}This command isn''t intended to be used by players directly, so unless you''re seeing this without using the command yourself, this is a bug.", 1),
    COMMAND_ADMIN_DESCRIPTION("{$s}Shows the available admin commands.", 1, 2),
    COMMAND_ADMIN_TRACK_DESCRIPTION("{$s}Shows you the path of messages sent to you", 1, 2),
    COMMAND_ADMIN_TRACK_ENABLED("&9Tracking Mode Mode{$sep}: {$p}Enabled {$sep}(&7You might not be able to see the path of all messages sent to you{$sep})", 1, 2),
    COMMAND_ADMIN_TRACK_DISABLED("&9Tracking Mode Mode{$sep}: {$e}Disabled", 1, 2),
    COMMAND_ADMIN_TRACK_TRACKED("{$p}The following message is sent from {$s}hover:{%path%;&7Click to open the file;/c admin openfile %file%} {$p}at line {$s}%line%\n{$p}The raw message{$sep}: &fhover:{%raw%;Click to copy;|%raw%}", 1, 2),
    COMMAND_ADMIN_MIGRATE_DESCRIPTION("{$s}Command used for handling data migrations.", 1, 2, 3),
    COMMAND_ADMIN_MIGRATE_DATABASE_DESCRIPTION("{$s}Migrates from one database type to another. You do not need to take extra steps if your server is a small community, but if you have a lot of guilds and players, please make sure to whitelist your server and remove all plugins that are not necessary for the server to function (which is almost none.)\nUnfortunately this command might not work if you have a lot of data as it requires a huge amount of RAM to process it.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_CONSOLE_ONLY("{$e}This command can only be executed from the console.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_PLEASE_WAIT("{$e}Please wait...", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_ERROR_LOADING_NEW_DATABASE("{$e}Error while loading new database settings, aborting migration operation. Please either correct the settings and run the command again, or revert them back and restart the server.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_SAME_DATABASE_TYPE("{$e}The new database method is the same as the old one {$sep}({$es}%method%{$sep}) {$e}Please make sure that you saved config.yml correctly. You do not need to reload the plugin manually.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_COOLDOWN(COMMAND_ADMIN_MIGRATE_DATABASE_DESCRIPTION.defaultValue + "\n{$e}Are you sure you want to start the migration process? If yes, enter the command again.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_START("{$p}Please go to your {$s}config.yml {$p}and configure the new settings for your new database and save the config. When you're done, run this command again.", 1, 2, 3, 4),
    COMMAND_ADMIN_MIGRATE_DATABASE_DONE("{$p}All data has been saved successfully. Your server will now restart in 10 seconds to finalize the changes.", 1, 2, 3, 4),
    COMMAND_ADMIN_PURGE_DESCRIPTION("{$s}Purges all guilds data and removes everything reliably.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_CONFIRM("{$e}Do the command again within 5 seconds to confirm that you want to purge all guilds data. &4This will delete all guilds data including all nations, guilds, lands, players (not their inventory) data. This action cannot be undone, your only solution would be to use the backups folder. This also will kick all players forcefully and whitelists the server and will automatically stop the server once it's done.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_ALREADY("{$e}Plugin is already in the process of purging data...", 1, 2, 3),
    COMMAND_ADMIN_PURGE_NOT_LOADED("{$e}Please wait for the plugin to be fully loaded before using this command.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_CONSOLE_ONLY("&4This command can only be performed from console.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_STOP("{$e}You cannot stop the server while guilds purging is in process.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_COMMAND("{$e}You cannot run commands during guilds purging process. Do {$es}/stop {$e}if you wish to restart the server.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_PURGING("{$e}Purging all guilds data... Please do not do anything including running commands or shutting down the server.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_DONE("{$p}Purging is done.", 1, 2, 3),
    COMMAND_ADMIN_PURGE_DONE_WITH_ERRORS("{$e}Purging is done with errors. Your data may or may not be purged correctly. If not, please report the error in the console.", 1, 2, 3),
    COMMAND_ADMIN_RESETCONFIGS_CONFIRM("{$e}Do the command again within 5 seconds to confirm that you want to reset all configs.\n" +
            "This will delete downloaded language packs, all GUIs, all configuration files and all time related caches such as daily checks.\n" +
            "This will not delete your data, logs or backups.", 1, 2, 3),
    COMMAND_ADMIN_RESETCONFIGS_ALREADY("{$e}Plugin is already in the process of resetting configs...", 1, 2, 3),
    COMMAND_ADMIN_RESETCONFIGS_CONSOLE_ONLY("&4This command can only be performed from console.", 1, 2, 3),
    COMMAND_ADMIN_RESETCONFIGS_REQUESTED("{$p}The plugin will reset all configs after the restart. Stopping the server in 10 seconds...", 1, 2, 3),
    COMMAND_ADMIN_RESETCONFIGS_RESETTING("{$e}Resetting all guilds configs... Please do not do anything including running commands or shutting down the server.", 1, 2, 3),
    COMMAND_ADMIN_SOUND_DESCRIPTION("{$s}Test a sound string that you're going to use in config.", 1, 2, 3),
    COMMAND_ADMIN_SOUND_ERROR("{$e}Error{$sep}: ${es}%error%", 1, 2, 3),
    COMMAND_ADMIN_SOUND_USAGE("{$usage}admin testsound {$p}<sound> &9[volume] [pitch]", 1, 2, 3),
    COMMAND_ADMIN_SOUND_PLAYING("{$p}Playing now...", 1, 2, 3),
    COMMAND_ADMIN_TEST_DESCRIPTION("{$s}Test a string with hex color codes, complex messages, line breaks and placeholders.", 1, 2, 3),
    COMMAND_ADMIN_TEST_USAGE("{$e}Usage{$sep}: {$es}/c admin test <message>", 1, 2, 3),
    COMMAND_ADMIN_EXECUTE_DESCRIPTION("{$s}Execute a command for all the members in a guild.", 1, 2, 3),
    COMMAND_ADMIN_EXECUTE_USAGE("{$usage}admin execute {$p}<guild> <executor> &9[filter] &9[/]{$p}<command>", 1, 2, 3),
    COMMAND_ADMIN_EXECUTE_UNKNOWN_EXECUTOR("{$e}Unknown executor{$sep}: {$es}%executor%\n{$e}Available executors{$sep}: {$es}self&7, {$es}console&7, {$es}members", 1, 2, 3, 4),
    COMMAND_ADMIN_EXECUTE_UNKNOWN_FILTER("{$e}Unknown filter{$sep}: {$es}%filter%\n{$e}Available filters{$sep}: {$es}online&7, {$es}offline", 1, 2, 3, 4),
    COMMAND_ADMIN_EXECUTE_UNKNOWN_COMMAND("{$e}Unknown command{$sep}: {$es}%command%", 1, 2, 3, 4),
    COMMAND_ADMIN_EXECUTE_MEMBERS_OFFLINE("{$e}Cannot execute as guild members with offline filter. Offline players cannot execute commands.", 1, 2, 3),
    COMMAND_ADMIN_EXECUTE_MEMBERS_NO_FILTER("{$e}Cannot execute as guild members with no filter.\nSome guild members might be offline. Use the {$es}online {$e}filter parameter to fix this.", 1, 2, 3),
    COMMAND_ADMIN_EXECUTE_EXECUTED("{$p}Executing the command {$s}%command% {$p}from plugin {$s}%plugin% {$p}for members of {$s}%guild% {$p}guild.", 1, 2, 3),
    COMMAND_ADMIN_EVALUATE_DESCRIPTION("{$s}Evaluates an expression with placeholders and math functions.", 1, 2, 3),
    COMMAND_ADMIN_EVALUATE_USAGE("{$e}Usage{$sep}: {$es}/c admin evaluate <expression>", 1, 2, 3),
    COMMAND_ADMIN_EVALUATE_FAILED("{$e}Translated Expression{$sep}: {$s}%translated%\n{$e}Error{$sep}: {$es}%result%", 1, 2, 3),
    COMMAND_ADMIN_EVALUATE_EVALUATED("{$p}Translated Expression{$sep}: hover:{{$s}%translated%;{$s}%object-code%\n\n" +
            "{$p}What is this?\n" +
            "&7This is the final representation that the\n" +
            "math compiler could understand from your expression.\n" +
            "You can see any errors such as operator\n" +
            "precedence and how subexpressions are grouped.\n" +
            "You might not see seme operations since they were optimized out.}\n" +
            "{$p}Evaluated Expression{$sep}: {$s}%result%", 1, 2, 3),
    COMMAND_ADMIN_CLAIM_DESCRIPTION("{$s}Forcibly claim the land you are currently standing in for a guild.", 1, 2, 3),
    COMMAND_ADMIN_CLAIM_USAGE("{$usage}admin claim {$p}<guild>", 1, 2, 3),
    COMMAND_ADMIN_CLAIM_SUCCESS("{$p}Claimed {$s}%x%&7, {$s}%z% {$p}as guild land.", 1, 2, 3),
    COMMAND_ADMIN_CLAIM_ALREADY_CLAIMED("{$e}This land is already claimed by {$es}%guilds_guild_name%", 1, 2, 3),
    COMMAND_ADMIN_RANK_DESCRIPTION("{$s}Change a players guild rank.", 1, 2, 3),
    COMMAND_ADMIN_RANK_USAGE("{$usage}admin rank {$p}<player> <rank>", 1, 2, 3),
    COMMAND_ADMIN_RANK_NOT_FOUND("{$e}Rank not found.", 1, 2, 3),
    COMMAND_ADMIN_RANK_NO_GUILD("{$e}The specified player is not in a guild.", 1, 2, 3),
    COMMAND_ADMIN_RANK_SAME_RANK("{$e}That player already has that rank.", 1, 2, 3),
    COMMAND_ADMIN_RANK_SUCCESS("{$p}Changed {$s}%player%'s {$p}rank from {$s}%previous_rank% {$p}to {$s}%rank%", 1, 2, 3),
    COMMAND_ADMIN_RANK_SUCCESS_LEADER("{$s}%guilds_guild_name% {$p}guild's new leader is{$sep}: {$s}%name%", 1, 2, 3),
    COMMAND_ADMIN_RANK_CANT_DEMOTE_LEADER("{$es}%player% {$e}is the leader of {$es}%guilds_guild_name% {$e}you need to set the leader first.", 1, 2, 3),
    COMMAND_ADMIN_TURRET_DESCRIPTION("{$s}Remove or change/update certain turrets.", 1, 2, 3),
    COMMAND_ADMIN_TURRET_USAGE("{$usage}admin turret {$p}<change | remove> <style> &9[replacement style]", 1, 2, 3),
    COMMAND_ADMIN_TURRET_UNKNOWN_STYLE("{$e}Unknown turret style{$sep}: {$es}%style%\n{$e}Look in your {$es}Turrets {$e}folder to see all the turret style names.", 1, 2, 3),
    COMMAND_ADMIN_TURRET_SUCCESS("{$p}A total of {$s}%amount% {$p}turrets have been processed.", 1, 2, 3),
    COMMAND_ADMIN_NEXUS_DESCRIPTION("{$s}Open a guilds nexus.", 1, 2, 3),
    COMMAND_ADMIN_NEXUS_USAGE("{$usage}admin nexus {$p}<guild>", 1, 2, 3),
    COMMAND_ADMIN_NEXUS_TP_NOT_SET("{$es}%guild% {$e}doesn't have their nexus set.", 1, 2, 3),
    COMMAND_ADMIN_NEXUS_TP("{$p}Teleporting to {$s}%guild%'s {$p}nexus...", 1, 2, 3),
    COMMAND_ADMIN_TOGGLE_DESCRIPTION("{$s}Toggle admin mode for yourself.", 1, 2, 3),
    COMMAND_ADMIN_TOGGLE_ON("{$s}Admin mode is now{$sep}: {$p}ON", 1, 2, 3),
    COMMAND_ADMIN_TOGGLE_OFF("{$s}Admin mode is now{$sep}: {$e}OFF", 1, 2, 3),
    COMMAND_ADMIN_TOGGLES_DESCRIPTION("{$s}Show a list of players with admin mode enabled.", 1, 2, 3),
    COMMAND_ADMIN_TOGGLES_HEADER("{$p}Players with admin mode enabled{$sep}:", 1, 2, 3),
    COMMAND_ADMIN_TOGGLES_ENTRY("{$sep}- {$s}%displayname%", 1, 2, 3),
    COMMAND_ADMIN_SPY_DESCRIPTION("{$s}Spy on guilds chat.", 1, 2, 3),
    COMMAND_ADMIN_SPY_ON("{$s}Chat spy mode{$sep}: {$p}ON", 1, 2, 3),
    COMMAND_ADMIN_SPY_OFF("{$s}Chat spy mode{$sep}: {$e}OFF", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_DESCRIPTION("{$s}Perform different actions for Dynmap.", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_RENDERING("{$p}Rendering...", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_USAGE("{$e}Usage{$sep}: {$es}/c admin dynmap <fullrender/update/remove>", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_NOT_AVAILABLE("{$e}All map supports seem to be disabled.", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_RENDERED("{$p}Rendered a total of {$s}%lands% {$p}land sections.", 1, 2, 3),
    COMMAND_ADMIN_DYNMAP_REMOVED("{$p}A total of {$s}%lands% {$p}has been removed.", 1, 2, 3),
    COMMAND_ADMIN_MASSWAR_DESCRIPTION("{$s}Manually start or end masswar.", 1, 2, 3),
    COMMAND_ADMIN_MASSWAR_USAGE("&4Usage{$sep}: {$e}/c admin masswar <start/end>", 1, 2, 3),
    COMMAND_ADMIN_MASSWAR_RUNNING("{$e}Masswar is already running.", 1, 2, 3),
    COMMAND_ADMIN_MASSWAR_NOT_RUNNING("{$e}Masswar is not running.", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_DESCRIPTION("{$s}Manually begin the daily checks cycle.", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_USAGE("{$usage}admin dailychecks &9[run &7| &9skip &7| &9resume]", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_INFO("{$p}Next cycle in{$sep}: {$s}%next%\n{$p}Exact Time{$sep}: {$s}%daily_checks%\n{$p}Current time{$sep}: {$s}%time%\n{$p}State{$sep}: %state%", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_RUN("{$p}Running daily checks... Todays daily checks {$s}will not {$p}be skipped.", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_SKIP("{$p}Skipping the next daily checks interval.", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_SKIPPED("{$e}The next daily checks is already skipped.", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_RESUME("{$p}Resuming the daily checks...", 1, 2, 3),
    COMMAND_ADMIN_DAILYCHECKS_RESUMED("{$e}Daily checks is not skipped.", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_DESCRIPTION("{$s}Add or take resourcepoints from a guild.", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_USAGE("{$usage}admin resourcepoints {$p}<guild> &9[set | add | remove] {$p}<amount>", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_PLAYER_NO_GUILD("{$e}The specified player doesn't have a guild.", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_DONE("{$s}%guild%'s {$p}new resource points{$sep}: {$s}%rp%", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_ADDED("{$p}Your new guilds resource points{$sep}: {$s}%rp%", 1, 2, 3),
    COMMAND_ADMIN_RESOURCEPOINTS_INVALID_ACTION("{$e}Invalid action {$es}%action%{$e}! Use{$sep}: {$es}add/remove/set", 1, 2, 3),
    COMMAND_ADMIN_BANK_DESCRIPTION("{$s}Add or take money from a guild bank.", 1, 2, 3),
    COMMAND_ADMIN_BANK_USAGE("{$usage}admin bank {$p}<guild> &9[set | add | remove] {$p}<amount>", 1, 2, 3),
    COMMAND_ADMIN_BANK_DONE("{$s}%guild%'s {$p}new bank{$sep}: {$s}%guilds_fancy_bank%", 1, 2, 3),
    COMMAND_ADMIN_BANK_ADDED("{$p}Your new guilds bank{$sep}: {$s}%guilds_fancy_bank%", 1, 2, 3),
    COMMAND_ADMIN_BANK_INVALID_ACTION("{$e}Invalid action {$es}%action%{$e}! Use{$sep}: {$es}add/remove/set", 1, 2, 3),
    COMMAND_ADMIN_COMMANDS_DESCRIPTION("{$s}Writes a list of commands with their permission in a file."),
    COMMAND_ADMIN_COMMANDS_DONE("{$p}Wrote command information in {$s}%output%. {$p}Check Guildss plugin folder."),
    COMMAND_ADMIN_JOIN_DESCRIPTION("{$s}Make a user join another guild.", 1, 2, 3),
    COMMAND_ADMIN_JOIN_USAGE("{$e}Usage{$sep}: {$es}/c admin join {$p}<player> <guild>", 1, 2, 3),
    COMMAND_ADMIN_JOIN_INVALID_GUILD("{$e}Could not find the guild {$es}%guild%", 1, 2, 3),
    COMMAND_ADMIN_JOIN_IN_GUILD("{$s}%player% {$p}is already in a guild named {$s}%guilds_guild_name%\n{$p}If you'd like to continue do{$sep}: hover:{{$s}/c admin join %player% %guild% confirm;{$p}Click to confirm;/c admin join %player% %guild% confirm}", 1, 2, 3),
    COMMAND_ADMIN_JOIN_LEADER("{$es}%player% {$e}is the leader of {$es}%guilds_guild_name% {$e}Consider disbanding the guild or changing the leader first.", 1, 2, 3),
    COMMAND_ADMIN_JOIN_ALREADY_IN_GUILD("{$s}%player% {$p}is already a member of {$s}%guilds_guild_name%", 1, 2, 3),
    COMMAND_ADMIN_JOIN_SUCCESS("{$s}%player% {$p}is now a member of {$s}%guilds_guild_name%", 1, 2, 3),
    COMMAND_ADMIN_HOME_DESCRIPTION("{$s}Teleport to another guild's home.", 1, 2, 3),
    COMMAND_ADMIN_HOME_USAGE("{$e}Usage{$sep}: {$es}/c admin home <guild>", 1, 2, 3),
    COMMAND_ADMIN_HOME_HOMELESS("&3%guild% {$e}guild doesn't have a home set.", 1, 2, 3),
    COMMAND_ADMIN_HOME_TELEPORTED("{$p}You have been teleported to {$s}%guild% {$p}guild home.", 1, 2, 3),
    COMMAND_ADMIN_CREATE_DESCRIPTION("{$s}Create a guild for another player.", 1, 2, 3),
    COMMAND_ADMIN_CREATE_USAGE("{$e}Usage{$sep}: {$es}/c admin create {$p}<player {$sep}| {$p}super> <name>", 1, 2, 3),
    COMMAND_ADMIN_CREATE_ALREADY_EXISTS("{$e}There is already a guild with the name{$sep}: {$es}%guild%", 1, 2, 3),
    COMMAND_ADMIN_CREATE_SUPER_CREATING("{$p}Creating a super guild...", 1, 2, 3),
    COMMAND_ADMIN_CREATE_ANY_NO_PLAYER_FOUND("{$e}Could not find any player that doesn't have a guild.", 1, 2, 3),
    COMMAND_ADMIN_CREATE_CREATED("{$p}Successfully created a guild named {$s}%guild% {$p}for player {$s}%target%", 1, 2, 3),
    COMMAND_ADMIN_PERMANENT_DESCRIPTION("{$e}Enable permanent mode for a guild.", 1, 2, 3),
    COMMAND_ADMIN_PERMANENT_USAGE("{$e}Usage{$sep}: {$es}/c admin permanent {$p}<guild>", 1, 2, 3),
    COMMAND_ADMIN_PERMANENT_ON("{$p}Permanent mode {$s}enabled.", 1, 2, 3),
    COMMAND_ADMIN_PERMANENT_OFF("{$p}Permanent mode {$e}disabled.", 1, 2, 3),
    COMMAND_ADMIN_RENAME_DESCRIPTION("{$s}Rename a guild.", 1, 2, 3),
    COMMAND_ADMIN_RENAME_USAGE("{$e}Usage{$sep}: {$es}/c admin rename <guild> <name>", 1, 2, 3),
    COMMAND_ADMIN_RENAME_ALREADY_EXISTS("{$e}There is already a guild with the name{$sep}: {$es}%guild%", 1, 2, 3),
    COMMAND_ADMIN_RENAME_RENAMED("{$p}Successfully renamed the guild {$s}%guild% {$p}to {$s}%name%", 1, 2, 3),
    COMMAND_ADMIN_DISBAND_DESCRIPTION("{$s}Disbands a guild.", 1, 2, 3),
    COMMAND_ADMIN_DISBAND_USAGE("{$e}Usage{$sep}: {$es}/c admin disband <guild> [silent]", 1, 2, 3),
    COMMAND_ADMIN_DISBAND_SUCCESS("{$s}%guild% {$p}has been disbanded.", 1, 2, 3),
    COMMAND_ADMIN_DISBAND_ANNOUNCE("{$es}%guild% {$s}has been disbanded.", 1, 2, 3),
    COMMAND_ADMIN_PACIFISM_DESCRIPTION("{$s}Changes the pacifism state of a guild.", 1, 2, 3),
    COMMAND_ADMIN_PACIFISM_USAGE("{$e}Usage{$sep}: {$es}/c admin pacifism {$p}<guild> {$sep}[{$p}true &7| &4false{$sep}]", 1, 2, 3),
    COMMAND_ADMIN_PACIFISM_ENABLED("{$s}Pacifism for {$p}%guild% guild{$sep}: {$p}Enabled", 1, 2, 3),
    COMMAND_ADMIN_PACIFISM_DISABLED("{$s}Pacifism for {$p}%guild% guild{$sep}: {$e}Disabled", 1, 2, 3),
    COMMAND_ADMIN_LAND_DESCRIPTION("{$s}Teleport to a land at the given coordinates.", 1, 2, 3),
    COMMAND_ADMIN_LAND_USAGE("{$usage}admin land [world] <x> <z>", 1, 2, 3),
    COMMAND_ADMIN_LAND_TELEPORTED("{$p}You've been teleported to land at{$sep}: {$s}%translated-world%&7, {$s}%chunk_x%&7, {$s}%chunk_z% {$sep}(&5%x%&7, &5%y%&7, &5%z%{$sep})", 1, 2, 3),
    COMMAND_ADMIN_LAND_INVALID_WORLD("{$e}Cannot find world{$sep}: {$es}%world%", 1, 2, 3),
    COMMAND_ADMIN_LAND_INVALID_COORDINATES("{$e}The entered coordinates are invalid{$sep}: {$es}%x%&7, {$es}%z%", 1, 2, 3),
    COMMAND_ADMIN_LAND_PREPARING("{$e}Preparing chunks for teleportation, this might take a minute.", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_DESCRIPTION("{$s}Display a player's information.", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_USAGE("{$usage}admin player {$p}<player>", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_INFO("&7| {$p}Name{$sep}: {$s}%player% {$sep}(hover:{&5%id%;{$p}Copy;|%id%}{$sep})\n" +
            "&7| {$p}Admin{$sep}: %admin% &7| {$p}Spy{$sep}: %spy%\n" +
            "&7| {$p}Guild{$sep}: {$s}%guilds_guild_name% {$sep}(hover:{&5%guild_id%;{$p}Copy;|%guild_id%}{$sep})\n" +
            "&7| {$p}Rank{$sep}: %guilds_rank_color%%guilds_rank_symbol% %guilds_rank_node%\n" +
//            "&7| {$p}National Rank{$sep}: %guilds_nation_rank_color%%guilds_nation_rank_symbol% %guilds_nation_rank_node%&7| {$p}Chat Channel{$sep}: %guilds_chat_channel_color%%guilds_chat_channel%\n" +
            "&7| {$p}Using Markers{$sep}: %visualizer%\n" +
            "&7| {$p}Invites{$sep}: {$s}%invites%\n" +
            "&7| {$p}Joined Guild At{$sep}: {$s}%guilds_date_joined%\n" +
            "&7| {$p}Auto Claim{$sep}: %auto_claim% &7| {$p}Auto Map{$sep}: %auto_map%\n" +
            "&7| {$p}Map Size{$sep}: {$s}%map_width% {$sep}- {$s}%map_height%\n" +
            "&7| {$p}Total Donations{$sep}: {$s}%guilds_total_donations% &7| {$p}Last Donation{$sep}: {$s}%guilds_last_donation_time%\n" +
            "&7| {$p}Claims &7({$s}%guilds_player_claims%&7){$sep}: %claims%\n" +
            "&7| {$p}Compressed Data{$sep}: {$s}%compressed%", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_FOUND("{$p}Player {$s}%name% {$sep}(&7%uuid%{$sep}) {$p}is in the following guilds{$sep}:", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_GUILD("{$sep}- {$p}%guilds_guild_name% {$sep}({$s}%uuid%{$sep})", 1, 2, 3),
    COMMAND_ADMIN_PLAYER_NO_DUPLICATES("{$e}This player's guild is not duplicated.", 1, 2, 3),
    COMMAND_ADMIN_KICK_DESCRIPTION("{$s}Kick a player out of a guild.", 1, 2, 3),
    COMMAND_ADMIN_KICK_USAGE("{$usage}admin kick {$p}<player>", 1, 2, 3),
    COMMAND_ADMIN_KICK_KICKED("{$e}Kicked {$es}%kicked% {$e}from {$es}%guild%", 1, 2, 3),
    COMMAND_ADMIN_KICK_KICKED_LEADER("{$e}Kicked {$es}%kicked% {$e}from {$es}%guild% {$e}the new leader is now {$sep}: {$es}%leader%", 1, 2, 3),
    COMMAND_ADMIN_KICK_KICKED_DISBANDED("{$e}Disbanded {$es}%guild% guild because {$es}%kicked% {$e}was the only member in the guild.", 1, 2, 3),
    COMMAND_ADMIN_KICK_NOT_IN_GUILD("{$e}That player is not in a guild.", 1, 2, 3),
    COMMAND_ADMIN_KICK_LEADER("{$e}The player you're trying to a kick is the guild leader. You need to either disband the guild, or set a new leader.", 1, 2),
    COMMAND_ADMIN_HOLOGRAM_DESCRIPTION("{$s}Remove bugged holograms around you.", 1, 2, 3),
    COMMAND_ADMIN_HOLOGRAM_USAGE("{$e}Usage{$sep}: {$es}/c admin hologram <radius>", 1, 2, 3),
    COMMAND_ADMIN_HOLOGRAM_REMOVED("{$p}Removed a total of {$s}%removed% {$p}hologram(s).", 1, 2, 3),
    COMMAND_ADMIN_ENTITY_DESCRIPTION("{$s}Finds all the entities around the player.", 1, 2, 3),
    COMMAND_ADMIN_UNCLAIM_DESCRIPTION("{$s}Unclaim any lands.", 1, 2, 3),
    COMMAND_ADMIN_UNCLAIM_SUCCESS("{$p}Unclaimed land at {$s}%x%&7, {$s}%z% {$p}from {$s}%guild%", 1, 2, 3),
    COMMAND_ADMIN_UNCLAIM_NOT_CLAIMED("{$e}This land is not claimed.", 1, 2, 3),
    COMMAND_ADMIN_MAXLANDMODIFIER_DESCRIPTION("{$s}Change the max lands modifier of a guild.", 1, 2, 3),
    COMMAND_ADMIN_MAXLANDMODIFIER_USAGE("{$usage}admin maxLandModifier {$p}<guild> &9[set | add | remove] &9<amount>", 1, 2, 3),
    COMMAND_ADMIN_MAXLANDMODIFIER_SUCCESS("{$p}Successfully changed max lands modifier for {$s}%guild% {$p}to {$s}%amount%", 1, 2, 3),
    COMMAND_ADMIN_MAXLANDMODIFIER_INVALID_ACTION("{$e}Invalid action {$es}%action%{$e}! Use{$sep}: {$es}add/remove/set", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_DESCRIPTION("{$s}Change the shield duration for a guild.", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_USAGE("{$usage}admin shield {$p}<guild> &9[set | add | remove] {$p}<time>", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_SUCCESS("{$p}Successfully changed shield duration for {$s}%guild% {$p}to{$sep}: {$s}%amount%", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_INVALID_ACTION("{$e}Invalid action {$es}%action%{$e}! Use{$sep}: {$es}add/remove/set", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_INVALID_GUILD("{$e}Could not find the guild {$es}%guild%", 1, 2, 3),
    COMMAND_ADMIN_SHIELD_INVALID_TIME("{$e}Invalid time unit{$sep}: {$es}%time%", 1, 2, 3),
    COMMAND_ADMIN_DEBUG_NOT_ENABLED("{$e}Debugging is not enabled. Please go to {$es}config.yml {$p}and set {$es}debug {$p}to {$es}true {$p}and then restart the server.", 1, 2, 3),
    COMMAND_ADMIN_DEBUG_DESCRIPTION("{$s}Commands related to debugging.", 1, 2, 3),
    COMMAND_ADMIN_DEBUG_UNKNOWN_DEBUG("{$e}Unknown debugging group{$sep}: {$es}%debug%", 1, 2, 3),
    COMMAND_ADMIN_DEBUG_TOGGLE_ADDED("{$p}Added to debug list{$sep}: {$s}%debug%", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_TOGGLE_REMOVED("{$p}Removed from the debug list{$sep}: {$s}%debug%", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_SPECIAL_DESCRIPTION("{$s}Used for development purposes. Not even meant to be used by admins.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_STACKTRACE_DESCRIPTION("{$s}Toggle printing the whole stacktrace for included debugs.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_STACKTRACE_ADDED("{$p}Added {$s}%debug% {$p} to stacktrace list.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_STACKTRACE_REMOVED("{$p}Removed {$s}%debug% {$p} to stacktrace list.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_STACKTRACE_ENABLED("{$s}Debug Stack Trace{$sep}: {$p}Whitelist", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_STACKTRACE_DISABLED("{$s}Debug Stack Trace{$sep}: {$e}Blacklist", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_LIST_DESCRIPTION("{$s}Make the debug list a whitelist or a blacklist.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_LIST_WHITELIST("{$s}The debug list is now a {$p}whitelist{$s}.", 1, 2, 3, 4),
    COMMAND_ADMIN_DEBUG_LIST_BLACKLIST("{$s}The debug list is now a {$e}blacklist{$s}.", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_DESCRIPTION("{$s}Custom resource point item related commands.", 1, 2, 3),
    COMMAND_ADMIN_ITEM_NEEDS_GUILD("{$e}Guild items require the target player to have a guild in order to construct the item.", 1, 2, 3),
    COMMAND_ADMIN_ITEM_STYLE_HAS_NO_ITEM("{$e}The guild item has no associated item. It's only accessible in the form of placed blocks.", 1, 2, 3),
    COMMAND_ADMIN_ITEM_RESOURCEPOINTS_DESCRIPTION("{$s}Give a custom item defined in the config that contains rp.", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_RESOURCEPOINTS_USAGE("{$usage}item give {$p}<name> &9[amount] [player]", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_RESOURCEPOINTS_UNKNOWN_ITEM("{$e}Unknown custom item named{$sep}: {$es}%item%", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_RESOURCEPOINTS_INVALID_AMOUNT("{$e}Invalid amount{$sep}: {$es}%amount%", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_RESOURCEPOINTS_DONE("{$p}Successfully gave {$s}%amount% %item% {$p}worth of {$s}%rp% resource points {$p}to {$s}%target%", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_INJECT_DESCRIPTION("{$s}Permanently inject resource points into an item.", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_INJECT_USAGE("{$usage}item inject {$p}<resource points>", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_INJECT_NO_ITEM("{$e}You're not holding any item to inject resource points into.", 1, 2, 3, 4),
    COMMAND_ADMIN_ITEM_INJECT_DONE("{$p}Successfully injected {$s}%rp% resource points {$p}to {$s}%item%", 1, 2, 3, 4),
    COMMAND_ADMIN_OPENFILE_DESCRIPTION("{$s}Opens a file relative to guild's plugin folder in the default text editor program that the server is running on. This is not intended to beused directly by admins, but for {$p}/c admin track {$s}command to easily access files for you. The player accessing this command needs to be opped. This can also be used directly if you have a GitHub link from GuildsX repository. If you're running on a server which doesn't have a graphical interface, then this command obviously won't work.", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_PERMISSION("{$e}You need to be opped in order to use this functionality.", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_USAGE("{$usage}admin openfile {$p}[file path relative to guilds folder]", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_NOT_SUPPORTED("{$e}The platform that you're running your server on, doesn't support this operation.", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_ERROR("{$e}Failed to open file{$sep}: {$es}%error%", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_UNKNOWN_LINK("{$e}Unknown link. The only recognized links are official GuildsX GitHub links{$sep}: {$es}%link%", 1, 2, 3),
    COMMAND_ADMIN_OPENFILE_GUI_WARNING("{$sep}[{$es}!{$sep}] {$es}Warning{$sep}: {$es}The file directs to a GUI file. If you have multiple languages installed, you might want to refer to hover:{{$e}updates {$sep}-> {$e}synchronize-guis;{$sep}Click to view option;@https://github.com/CryptoMorin/GuildsX/blob/1ed7ea77cf2f7773f9dcad682ac95261b6a4094e/core/src/main/resources/config.yml#L87}\n" +
            "{$es}config section for synchronization issues." ,1,2,3),
    COMMAND_ADMIN_OPENFILE_OPENED("{$p}Opened file{$sep}: {$s}%file%", 1, 2, 3),
    COMMAND_ADMIN_COMMAND_DESCRIPTION("{$e}Get all info related to a command.", 1, 2, 3),
    COMMAND_ADMIN_FILES_DESCRIPTION("{$s}For debugging purposes. Writes a list of all the files inside Guilds folder to a file.", 1, 2, 3),
    COMMAND_ADMIN_FILES_DONE("{$p}The file tree has been written to {$s}hover:{%output%;&7Click to open;/c admin openfile %sanitized_output%}", 1, 2, 3),
    COMMAND_ADMIN_FSCK_DESCRIPTION("Attempts to fix corrupted data.", 1, 2, 3),
    COMMAND_ADMIN_FSCK_SCANNING("{$p}Loading and scanning all data files... This might take a minute", 1, 2, 3),
    COMMAND_ADMIN_FSCK_CORRUPTION_LAND_UNKNOWN_WORLD("{$sep}\u26ab {$e}Unknown world for land{$sep}: {$es}%world%{$sep}, {$es}%x%{$sep}, {$es}%z%", 1, 2, 3, 4, 5),
    COMMAND_ADMIN_FSCK_ERROR("{$e}An error occurred while scanning guild data files, please check your console.", 1, 2, 3),
    COMMAND_ADMIN_FSCK_DONE_NO_CORRUPTION("{$p}Scan done. Found no corrupted data. Took {$s}%time%ms{$p}. Please restart your server to prevent performance issues.", 1, 2, 3, 4),
    COMMAND_ADMIN_FSCK_DONE_CORRUPTED("{$e}Scan done. Took {$es}%time%ms{$e}. Fixed a total of {$es}%corrupted% {$e}corrupted data. &lPlease restart your server with /stop now to save all data.", 1, 2, 3, 4),
    COMMAND_ADMIN_COMMAND_USAGE("{$usage}admin command {$p}<command>", 1, 2, 3),
    ELECTIONS_DEFAULT_STATEMENT("{$p}Click to vote for\nthis candidate.", 1),
    ELECTIONS_RESULTS("{$s}%candidate% {$p}was chosen as the new leader with {$s}%votes% {$p}votes.", 1),
    ELECTIONS_BEGIN("{$p}Elections has now begun. Vote for your favorite candidate with {$s}hover:{/c election vote;{$p}Click to vote;/c election vote}", 1),
    ELECTIONS_JOIN_NOTIFY("{$p}There's an ongoing election. Vote for your favorite candidate with {$s}hover:{/c election vote;{$p}Click to vote;/c election vote}", 1),
    ELECTIONS_NOT_ENOUGH_DATA("{$e}No new leader has been chosen because less than {$es}50% {$e}of your guild voted.", 1),
    GUILD_NAME_BLACKLISTED("{$e}Your guild's name contains blacklisted words.", 1),
    GUILD_TAG_BLACKLISTED("{$e}Your tag contains blacklisted words.", 1),
    NEXUS_SETTINGS_COLOR_ENTER_VALUE("{$s}Please enter the new hex color code in chat. Or type hover:{{$e}Cancel;{$e}Cancel;/cancel} {$s}to cancel.\nYou can get the hex code for colors from hover:{{$p}here;{$p}Click to Open;@https://htmlcolorcodes.com/}", 1, 2, 3),
    NEXUS_SETTINGS_COLOR_WRONG_HEX("{$e}Invalid hex color{$sep}: {$es}%color%", 1, 2, 3),
    NEXUS_SETTINGS_COLOR_WRONG_RGB("{$e}Invalid RGB color{$sep}: {$es}%color%", 1, 2, 3),
    NEXUS_SETTINGS_COLOR_RANGE_BLACKLISTED("{$e}The color you've chosen is not in the range of available colors.", 1, 2, 3),
    NEXUS_SETTINGS_COLOR_SET("{$p}Your guild's color was changed to{$sep}: {$s}%color%", 1, 2, 3),
    TELEPORTS_MOVED("{$e}Teleportation has been canceled because you moved.", 1),
    TELEPORTS_ALREADY_TELEPORTING("{$e}You''re already teleporting somewhere else.", 1),
    @AdvancedMessage( actionbar = "{$p}Teleporting in{$sep}: &9%timer%")
    TELEPORTS_TIMER("{$p}Teleporting in{$sep}: &9%timer%", 1),
    @AdvancedMessage(actionbar = "&9Teleported")
    TELEPORTS_TELEPORTED("&9Teleported", 1),
    POWER_DEATH("{$e}You died and lost {$es}%lost% {$e}power.", 1),
    NOT_FOUND_GUILD("{$e}The specified guild {$es}%guild% {$e}was not found.",2),
    NOT_FOUND_NATION("{$e}The specified nation was not found.", 2),
    NOT_FOUND_PLAYER("{$e}The specified player was not found.", 2),
    NOT_FOUND_PLAYER_NO_GUILD("{$e}The specified player has no guild.", 2),
    NOT_FOUND_PLAYER_NO_NATION("{$e}The specified player''s guild isn''t a part of a nation.", 2),
    NOT_FOUND_PLAYER_OR_GUILD("{$e}No guild or player was found with that name.", 2),
    NOT_FOUND_PLAYER_OR_GUILD_OR_NATION("{$e}No nation, guild or player was found with that name.", 2),
    NOT_FOUND_IN_YOUR_GUILD("{$e}That player isn''t in your guild.", 2),
    LANDS_BUILD_OWN_ONLY("{$e}You can only build in the lands you claimed.", 1),
    LANDS_PORTAL_PROTECTION("{$e}Your portal teleportation was cancelled because this portal leads to an unfriendly guild.", 1),
    LANDS_ENDER_PEARL_PROTECTION("{$e}Your ender pearl landed in an enemy guild!", 1),
    LANDS_GAMEMODE_PROTECTION("{$e}Your gamemode has been changed for entering a foreign land.", 1),
    LANDS_GAMEMODE_PROTECTION_ACTIVATED("{$e}You can't use creative mode in this land.", 1),
    LANDS_NATION_ZONE_PLACE("{$e}You cannot place blocks in nation zone of {$es}%nation%", 1, 3),
    LANDS_NATION_ZONE_BREAK("{$e}You cannot break blocks in nation zone of {$es}%nation%", 1, 3),
    LANDS_NATION_ZONE_INTERACT("{$e}You cannot interact with in nation zone of {$es}%nation%", 1, 3),
    @AdvancedMessage(actionbar="{$sep}&l-=( {$p}&lUnoccupied Land {$sep}&l)=-")
    LANDS_VISUALIZER_WILDERNESS(1, 2),
    @AdvancedMessage(title="%guilds_other_name%", subtitle="%guilds_other_lore%")
    LANDS_VISUALIZER_NO_GUILD(1, 2),
    @AdvancedMessage(title="&a%guilds_guild_name%", subtitle="%guilds_guild_color@hex%%guilds_lore%")
    LANDS_VISUALIZER_SELF(1, 2),
    @AdvancedMessage(title="%guilds_other_name%", subtitle="%guilds_other_guild_color@hex%%guilds_other_lore%")
    LANDS_VISUALIZER_NEUTRAL(1, 2),
    @AdvancedMessage(title="&9%guilds_other_name%", subtitle="%guilds_other_guild_color@hex%%guilds_other_lore%")
    LANDS_VISUALIZER_NATION(1, 2),
    @AdvancedMessage(actionbar="{$sep}&l-=( %guilds_nation_zone:info name=relation_color%Nation Zone{$sep}: %guilds_nation_zone:info name=nation_color@hex%%guilds_nation_zone:info name=nation_name% {$sep}&l)=-")
    LANDS_VISUALIZER_NATION_ZONE(1, 2),
    @AdvancedMessage(title="&6%guilds_other_name%", subtitle="%guilds_other_guild_color@hex%%guilds_other_lore%")
    LANDS_VISUALIZER_ALLY(1, 2),
    @AdvancedMessage(title="&e%guilds_other_name%", subtitle="%guilds_other_guild_color@hex%%guilds_other_lore%")
    LANDS_VISUALIZER_TRUCE(1, 2),
    @AdvancedMessage(title="&c%guilds_other_name%", subtitle="%guilds_other_guild_color@hex%%guilds_other_lore%")
    LANDS_VISUALIZER_ENEMY(1, 2),
    OTHER_GUILDS_PLACE("{$e}You can''t build in other guilds land.", 1, 2),
    OTHER_GUILDS_BREAK("{$e}You can''t build in other guilds land.", 1, 2),
    OTHER_GUILDS_KILL("{$e}You can''t kill {$es}%entity% {$e}in other guild land.", 1, 2),
    OTHER_GUILDS_INTERACT("{$e}You can''t interact in other guilds land.", 1, 2),
    OTHER_GUILDS_USE("{$e}You can''t use in other guilds land.", 1, 2),
    IN_CLAIM_ONLY_PLACING_GENERAL("{$e}You can only build in your claimed land.", 3, 4),
    IN_CLAIM_ONLY_PLACING_RADIUS("{$e}You can only build in your claimed land or witin {$s}%radius% {$e}chunks of your land.", 3, 4),
    IN_CLAIM_ONLY_PLACING_CHARGES("{$e}You need {$es}$%money% {$e}money and your guild needs {$es}%rp% {$e}resource points for placing blocks outside of claimed lands.", 3, 4),
    IN_CLAIM_ONLY_BREAKING_CHARGES("{$e}You need {$es}$%money% {$e}money and your guild needs {$es}%rp% {$e}resource points for breaking blocks outside of claimed lands.", 3, 4),
    IN_CLAIM_ONLY_BREAKING_RADIUS("{$e}You can only build in your claimed land or witin {$s}%radius% {$e}chunks of your land.", 3, 4),
    IN_CLAIM_ONLY_BREAKING_GENERAL("{$e}You can only build in your claimed land.", 3, 4),
    FLY_ENEMIES_NEARBY("{$e}There are enemies nearby. Your flight has been disabled.", 1),
    FLY_CHARGES_CANT_AFFORD("{$e}You need {$es}$%money% {$e}for every {$es}%interval% seconds {$e}you fly.", 1),
    FLY_CHARGES_CANT_AFFORD_GUILD("{$e}Your guild needs {$es}$%amount% {$e}for every {$es}%interval% seconds {$e}you fly.", 1, 2),
    FLY_OUT_OF_LAND("{$e}You can only use guilds fly in your own land. Your flight has been disabled.", 1, 2),
    FLY_DAMAGE("{$e}You took damage and your flight has been disabled.", 1),
    FLY_WARNINGS_CHARGES("{$e}Your flight will be disabled in {$es}%counter% {$e}seconds due to not being able to pay the charges.", 1, 2),
    FLY_WARNINGS_UNFRIENDLY_NEARBY_SAFE("{$p}You are now safe from unfriendly players.", 1, 2, 4),
    FLY_WARNINGS_UNFRIENDLY_NEARBY_WARN("{$e}Your flight is about to be disabled in {$es}%counter% {$e}seconds due to being close to an enemy! Go back immediately.", 1, 2, 4),
    FLY_WARNINGS_OUT_OF_LAND_SAFE("{$p}You are now safe in this land.", 1, 2, 5),
    FLY_WARNINGS_OUT_OF_LAND_WARN("{$e}Your flight is about to be disabled in {$es}%counter% {$e}seconds due to being in a land that you cannot fly in! Go back immediately", 1, 2, 5),
    RELATIONS_ANOTHER_REQUEST("{$es}%guild% {$e}has sent you a %relation% {$e}relation request before.", 1),
    RELATIONS_NO_PERMISSION("{$e}You don't have permission to manage any relationships.", 1),
    RELATIONS_NO_REQUESTS("{$e}You don't have any relationship requests sent to your group.", 1),
    RELATIONS_DISABLED_COMMANDS("{$e}You can't use that command in this territory.", 1),
    @Comment(
            forParent = true,
            value = {"", "Below names are also used for %guilds_relation_name%, but not for %guilds_land_relation%", "For the color of relations (%guilds_relation_color%), refer to relations.yml instead."}
    )
    RELATIONS_NEUTRAL_NAME("Neutral", 1, 2),
    RELATIONS_ENEMY_NAME("Enemy", 1, 2),
    RELATIONS_ALLY_NAME("Ally", 1, 2),
    RELATIONS_TRUCE_NAME("Truce", 1, 2),
    RELATIONS_NATION_NAME("Nation", 1, 2),
    RELATIONS_SELF_NAME("Self", 1, 2),
    RELATIONS_WILDERNESS_NAME("Wilderness", 1, 2),
    @Comment({"The color for this relation is special and is defined in language files instead of relations.yml"})
    RELATIONS_WILDERNESS_COLOR("&f", 1, 2),
    PROTECTED_SIGNS_CANT_MODIFY("{$e}You can't modify the settings of this protected block.", 2),
    PROTECTED_SIGNS_ALREADY_PROTECTED("{$s}This block is already protected.", 2),
    PROTECTED_SIGNS_BROKE("{$s}You broke a protected block.", 2),
    PROTECTED_SIGNS_UNPROTECTED("{$e}This block is no longer protected.", 2),
    PROTECTED_SIGNS_CANT_BREAK("{$e}Only the owner of the protected block can break it.", 2),
    PROTECTED_SIGNS_PROTECTED("{$e}This block is protected.", 2),
    PROTECTED_SIGNS_INVALID_SIGN("{$e}You can't protect using this sign.", 2),
    PROTECTED_SIGNS_INVALID_SIGN_BLOCK("{$e}You can't protect from this side of the block because there's a {$es}%block% {$e}placed here.", 2),
    PROTECTED_SIGNS_INVALID_BLOCK("{$e}You can't protect this block.", 2),
    PROTECTED_SIGNS_NOT_ATTACHED("{$e}This sign must be attached to a block to protect.", 2),
    PROTECTED_SIGNS_UNCLAIMED("{$e}You can't use protection signs in an unclaimed land.", 2),
    PROTECTED_SIGNS_GUILD_ITEMS("{$e}You can't use protection signs on guild items such as turrets and structures.", 2),
    PROTECTED_SIGNS_OTHER_GUILDS("{$e}You can't use protection signs in other guilds land.", 2),
    PROTECTED_SIGNS_CANT_PROTECT_DOUBLE_CHEST("{$e}This chest cannot be protected as the other side is in an unclaimed land.", 2),
    PROTECTED_SIGNS_PASSWORD_INVALID("{$e}Invalid password. Try again.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_CANT_MOVE("{$e}You're not allowed to move while entering the password.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_ERROR_429("{$e}Too many failed attempts.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_IN_COOLDOWN("{$e}This container requires a password to access, however you had too many failed attempts.\nYou can try again in{$sep}: {$es}%cooldown%", 2, 3),
    PROTECTED_SIGNS_PASSWORD_REQUIRED("{$e}This container requires a password to be opened. Enter the password or type hover:{{$es}cancel;{$es}Cancel;/cancel} {$e}to cancel the action.", 2, 3),
    PROTECTED_SIGNS_DOUBLE_CHEST_PROTECTED("{$p}Protecting the double chest.", 2),
    PROTECTED_SIGNS_PROTECTION_TYPE_ALREADY_USING("{$e}This block is already using this protection method.", 2, 4),
    PROTECTED_SIGNS_PROTECTION_TYPE_CHANGED("{$p}Successfully changed the block protection method to {$es}%protection%{$e}.", 2, 4),
    TOP_REWARDS_GUILD("{$p}Your guild has been rewarded {$s}$%money% {$p}money and {$s}%rp% {$p}resource points for being rank {$sep}: &5%rank%\n{$p}New Guild Balance{$sep}: {$s}$%guilds_fancy_bank%\n{$p}New Guild Resource Points{$sep}: {$s}%guilds_fancy_resource_points%", 2),
//    TOP_REWARDS_NATION("{$p}Your nation has been rewarded {$s}$%money% {$p}money and {$s}%rp% {$p}resource points for being rank {$sep}: &5%rank%\n{$p}New Nation Balance{$sep}: {$s}$%guilds_fancy_nation_bank%\n{$p}New Nation Resource Points{$sep}: {$s}%guilds_fancy_nation_resource_points%", 2),
    PROTECTED_SIGNS_PLAYERS_CLEARED("{$p}Removed all the members.", 2, 3),
    PROTECTED_SIGNS_PLAYERS_REMOVED("{$p}Removed {$s}%name% {$p}from the group.", 2, 3),
    PROTECTED_SIGNS_PLAYERS_LIMIT("{$e}You can't add more than {$es}%limit% {$e}member to the group.", 2, 3),
    PROTECTED_SIGNS_PLAYERS_INCLUDE_INCLUDED("{$p}Added {$s}%name% {$p}to the group.", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_INCLUDE_IS_ALREADY_IN_GROUP("&3%name% {$e}is already in the group.", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_INCLUDE_OWNER("{$e}You're the owner of the block...", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_INCLUDE_NAME("{$p}Enter the name of the player you wish to add.", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_EXCLUDE_EXCLUDED("{$p}Excluded {$s}%name% {$p}from the group.", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_EXCLUDE_IS_ALREADY_IN_GROUP("{$es}%name% {$e}is already excluded from the group.", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_EXCLUDE_OWNER("{$e}You're the owner of the block...", 2, 3, 4),
    PROTECTED_SIGNS_PLAYERS_EXCLUDE_NAME("{$p}Enter the name of the player you wish to exclude.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_CLEARED("{$p}Removed all the guilds.", 2, 3),
    PROTECTED_SIGNS_GUILDS_REMOVED("{$p}Removed {$s}%name% {$p}guild from the group.", 2, 3),
    PROTECTED_SIGNS_GUILDS_DISBANDED("{$e}Guild with UUID {$es}%id% {$e}has been automatically removed from this sign as they no longer exist.", 2, 3),
    PROTECTED_SIGNS_GUILDS_LIMIT("{$e}You can't add more than {$es}%limit% {$e}guilds to the group.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_ASK("{$s}Please enter the password or type hover:{{$e}Cancel;{$e}Cancel;/cancel} {$s}to cancel and hover:{{$e}remove;{$e}Remove;remove} {$s}to remove the password.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_CONTAINER_ONLY("{$e}You can only set password on containers such as {$es}chests, hoppers, furnaces and etc.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_LENGTH("{$e}Password length must be greater than {$es}%min% {$e}and less than {$es}%max% {$e}got{$sep}: {$es}%length%", 2, 3),
    PROTECTED_SIGNS_PASSWORD_REMOVED("{$p}Container's password has been removed.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_NOT_SET("{$e}No password is set for this protected block.", 2, 3),
    PROTECTED_SIGNS_PASSWORD_SET("{$p}Container password has been successfully changed.", 2, 3),
    PROTECTED_SIGNS_GUILDS_INCLUDE_INCLUDED("{$p}Added {$s}%name% {$p}guild to the group.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_INCLUDE_IS_ALREADY_IN_GROUP("&3%name% {$e}guild is already in the group.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_INCLUDE_OWNER("{$e}To include your guild, simply change the protection type.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_INCLUDE_NAME("{$p}Enter the name of the guild you wish to add.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_EXCLUDE_EXCLUDED("{$p}Excluded {$s}%name% {$p}guild from the group.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_EXCLUDE_IS_ALREADY_IN_GROUP("{$es}%name% {$e}guild is already excluded from the group.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_EXCLUDE_OWNER("{$e}To exlude your guild, simply change the protection type.", 2, 3, 4),
    PROTECTED_SIGNS_GUILDS_EXCLUDE_NAME("{$p}Enter the name of the guild you wish to exclude.", 2, 3, 4),
    DEATH_PENALTY("{$es}%name% {$e}has died and {$es}%penalty% {$e}resource points has been taken from your guild."),
    MISC_UPGRADES_ALERTS_NOTIFY_MEMBERS("{$es}%player% {$e}from enemy guild {$es}%guilds_guild_name% {$e}has entered your land at{$sep}: {$es}%world%&7, {$es}%x%&7, {$es}%y%&7, {$es}%z%",  1, 2, 3);
    private static final FileConfiguration config;
    private final LanguageEntry path;
    private final String defaultValue;

    Lang(int ... group) {
        this(null, group);
    }

    Lang(String defaultValue, int ... group) {
        this.path = DefinedMessenger.getEntry(null, this, group);
        this.defaultValue = defaultValue;
    }

//    private String getFromFile() {
//        String key = name().toLowerCase().replace('_', '-');
//        String value = config.getString(key);
//        if (value == null) return "";
//        return ChatColor.translateAlternateColorCodes('&', value);
//    }

//    public static String getPrefix() {
//        return PREFIX.getValue() + ChatColor.RESET;
//    }

//    private String getKey() {
//        return String.join(".", this.option.build(null, null));
//    }
//
//    public void sendError(Player player, Object ... vars) {
//        Config.errorSound(player);
//        this.sendMessage(player, vars);
//    }
//
//    public void sendMessage(CommandSender receiver, Object ... vars) {
//        String msg;
//        if (!this.context.isEmpty()) msg = this.build(Config.PREFIX.getBoolean());
//        else msg = config.getString(this.getKey());
//        for (int i = 0; i < vars.length; i += 2) {
//            String var = '%' + vars[i].toString() + '%';
//            msg = msg.replace(var, vars[i + 1].toString());
//        }
//        msg = ChatColor.translateAlternateColorCodes('&', msg);
//        if (receiver instanceof Player) {
////            Player.Spigot spigot = ((Player)receiver).spigot();
////                        for (BaseComponent[] components : this.message.build(builder).create()) {
////                spigot.sendMessage(components);
////            }
//            receiver.sendMessage(msg);
//        }
//        receiver.sendMessage(msg);
//    }
//
//    public Lang withContext(Player player) {
//        if (player == null) {
//            return this;
//        }
//        String displayName = SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(ServiceVault.Component.CHAT) ? ServiceVault.getDisplayName(player) : player.getDisplayName();
//
//        this.parse("displayname", displayName);
//
//        this.parse("pure-displayname", player.getDisplayName());
//        return this;
//    }
//
//    public Lang parse(String var, String replacement) {
//        this.context.add(var);
//        this.context.add(replacement);
//        return this;
//    }
//
//    public String getString() {
//        return config.getString(this.getKey());
//    }
//
//    public String build(boolean prefix) {
//        String prefixStr = "";
//        if (prefix) prefixStr = Lang.PREFIX.getString();
//        String msg = prefixStr + config.getString(this.getKey());
//        String[] vars = (String[]) this.context.toArray();
//        for (int i = 0; i < vars.length; i += 2) {
//            String var = '%' + vars[i] + '%';
//            if (msg != null) msg = msg.replace(var, vars[i + 1]);
//        }
//        return msg;
//    }

    @Deprecated
    public static String translateMaterial(XMaterial material) {
        return material.toString();
    }

    @Deprecated
    public void sendMessage(CommandSender receiver, OfflinePlayer placeholder, Object ... edits) {
        this.sendMessage(receiver, new MessageBuilder().placeholders(edits).withContext(placeholder));
    }
    public void sendConsoleMessage(Object ... edits) {
        this.sendMessage(Bukkit.getConsoleSender(), edits);
    }
    public void sendEveryoneMessage(Object ... edits) {
        this.sendConsoleMessage(edits);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.sendMessage(player, edits);
        }
    }

    static {
        InputStream in = CastelPlugin.getInstance().getResource("lang.yml");
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(in)));

        CLogger.info("Lang file read successfully");
    }

    @Override
    public @NotNull LanguageEntry getLanguageEntry() {
        return this.path;
    }

    @Override
    public @Nullable String getDefaultValue() {
        return this.defaultValue;
    }
}
