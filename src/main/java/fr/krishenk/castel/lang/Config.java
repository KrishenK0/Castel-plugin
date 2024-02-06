package fr.krishenk.castel.lang;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.NewEnumConfig;
import fr.krishenk.castel.config.NewKeyedConfigAccessor;
import fr.krishenk.castel.config.implementation.NewKeyedYamlConfigAccessor;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.config.ConfigPath;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Set;

public enum Config implements NewKeyedConfigAccessor, NewEnumConfig {
    PREFIX,
    LANG,
    UPDATES_CONFIGS(1),
    UPDATES_AUTOMATIC_CONFIG_RELOADS(1),
    UPDATES_SYNCHRONIZE_GUIS_AUTOMATIC(1, 3),
    UPDATES_SYNCHRONIZE_GUIS_REFERENCE_LANGUAGE(1, 3),
    DATABASE_METHOD(1),
    DATABASE_USE_DATA_FOLDER(1),
    DATABASE_AUTO_SAVE_INTERVAL(1),
    DATABASE_SMART_SAVE(1),
    DATABASE_LOAD_ALL_DATA_ON_STARTUP(1),
    DATABASE_TABLE_PREFIX(1),
    DATABASE_TABLES_GUILDS(1, 2),
    DATABASE_TABLES_PLAYERS(1, 2),
    DATABASE_TABLES_LANDS(1, 2),
    DATABASE_TABLES_MAILS(1, 2),
    DATABASE_ADDRESS(1),
    DATABASE_DATABASE(1),
    DATABASE_USERNAME(1),
    DATABASE_PASSWORD(1),
    DATABASE_SSL_ENABLED(1, 2),
    DATABASE_SSL_VERIFY_SERVER_CERTIFICATE(1, 2),
    DATABASE_SSL_ALLOW_PUBLIC_KEY_RETRIEVAL(1, 2),
    DATABASE_POOL_SETTINGS_SIZE_MAX(1, 3, 4),
    DATABASE_POOL_SETTINGS_SIZE_MIN(1, 3, 4),
    DATABASE_POOL_SETTINGS_MAX_CONCURRENT_CONNECTIONS(1, 3),
    DATABASE_POOL_SETTINGS_MINIMUM_IDLE(1, 3),
    DATABASE_POOL_SETTINGS_MAXIMUM_LIFETIME(1, 3),
    DATABASE_POOL_SETTINGS_KEEPALIVE_TIME(1, 3),
    DATABASE_POOL_SETTINGS_CONNECTION_TIMEOUT(1, 3),
    DATABASE_POOL_SETTINGS_PROPERTIES(1, 3),
    DATABASE_URI(1, 2),
    DEBUG,
    DISABLED_WORLDS,
    PLACEHOLDERS_FORMATS(1),
    PLACEHOLDERS_VARIABLES(1),
    PLACEHOLDERS_DEFAULTS(1),
    FORCE_LANG,
    JOIN_LEAVE_MESSAGES,
    KEEP_ADMIN_MODE,
    HELP_DESCRIPTION(1),
    HELP_COMMANDS(1),
    HELP_FOOTER_PAGES(1),
    HELP_ORDER(1),
    NO_GUILD_REMINDER,
    TOP_GUILDS_MIGHT(2),
    TOP_GUILDS_AMOUNT(2),
    TOP_GUILDS_UPDATE_INTERVAL(2),
    TOP_GUILDS_SHOW_PACIFISTS(2),
    TOP_GUILDS_REWARDS_RESOURCE_POINTS(2, 3),
    TOP_GUILDS_REWARDS_BANK(2, 3),
    TOP_GUILDS_REWARDS_TOP(2, 3),
    GUILD_LORE_TITLE_FADE_IN(2, 3),
    GUILD_LORE_TITLE_STAY(2),
    GUILD_LORE_TITLE_FADE_OUT(2, 3),
    GUILD_LORE_ALLOW_COLORS(2),
    GUILD_LORE_IGNORE_COLORS(2),
    GUILD_LORE_MAX_LENGTH(2),
    GUILD_LORE_RENAMING_COOLDOWN(2),
    GUILD_LORE_ALLOW_NON_ENGLISH(2),
    GUILD_LORE_ALLOW_SYMBOLS(2),
    GUILD_LORE_REMOVE_KEYWORDS(2),
    GUILD_LORE_BLACKLISTED_NAMES(2),
    GUILD_FLY_ENABLED(2),
    GUILD_FLY_ALLOW_UNCLAIMED(2),
    GUILD_FLY_CHARGES_ENABLED(2, 3),
    GUILD_FLY_CHARGES_PLAYERS_ACTIVATION_COST(2, 3, 4),
    GUILD_FLY_CHARGES_PLAYERS_AMOUNT(2, 3, 4),
    GUILD_FLY_CHARGES_EVERY_SECONDS(2, 3),
    GUILD_FLY_CHARGES_PLAYERS_PAY_GUILD_ENABLED(2, 3, 4, 6),
    GUILD_FLY_CHARGES_PLAYERS_PAY_GUILD_RESOURCE_POINTS(2, 3, 4, 6),
    GUILD_FLY_CHARGES_GUILDS_AMOUNT(2, 3, 4),
    GUILD_FLY_CHARGES_GUILDS_ACTIVATION_COST(2, 3, 4),
    GUILD_FLY_CHARGES_GUILDS_RESOURCE_POINTS(2, 3, 4),
    GUILD_FLY_NEARBY_UNFRIENDLY_RANGE(2),
    GUILD_FLY_DISABLE_ON_DAMAGE(2),
    GUILD_FLY_WARNINGS_LAND(2, 3),
    GUILD_FLY_WARNINGS_UNFRIENDLY_NEARBY(2, 3),
    GUILD_FLY_WARNINGS_CHARGES(2, 3),
    CREATION_GUILDS_SOUND(1, 2),
    HOME_USE_NEXUS_IF_NOT_SET(1),
    HOME_ON_JOIN_GUILD_HOME(1, 3),
    HOME_ON_JOIN_NATION_SPAWN(1, 3),
    HOME_RESPAWN_GUILD_HOME(1, 2),
    HOME_RESPAWN_UNLESS_HAS_BED(1, 2),
    HOME_NATION_SPAWN_CAPITAL(1),
    HOME_RESPAWN_NATION_SPAWN(1, 2),
    HOME_SET_ON_CREATE(1),
    HOME_SET_ON_FIRST_CLAIM(1),
    HOME_SAFE(1),
    HOME_CLAIMED(1),
    HOME_TELEPORT_DELAY(1),
    HOME_NEXUS_LAND(1),
    HOME_USE_TIMER_MESSAGE(1),
    HOME_SHOULD_NOT_MOVE(1),
    HOME_SHOULD_NOT_BE_DAMAGED(1),
    HOME_UNSET_IF_UNCLAIMED(1, 3),
    HOME_UNSET_IF_INVADED(1, 3),
    DISBAND_CONFIRM(1),
    DISBAND_CONFIRMATION_EXPIRATION(1),
    DISBAND_ANNOUNCE(1),
    DISBAND_ANNOUNCE_INACTIVITY(1),
    DAILY_CHECKS_TIME(2),
    DAILY_CHECKS_INTERVAL(2),
    DAILY_CHECKS_ELECTIONS_ENABLED(2, 3),
    DAILY_CHECKS_ELECTIONS_INTERVAL(2, 3),
    DAILY_CHECKS_ELECTIONS_VOTE_PERCENTAGE(2, 3),
    DAILY_CHECKS_ELECTIONS_DURATION(2, 3),
    DAILY_CHECKS_ELECTIONS_DISALLOW_KICKS(2, 3, 4),
    DAILY_CHECKS_ELECTIONS_DISALLOW_JOINS(2, 3, 4),
    DAILY_CHECKS_TIMEZONE(2),
    DAILY_CHECKS_COUNTDOWNS(2),
    AUDIT_LOGS_EXPIRATION_DEFAULT(2, 3),
    AUDIT_LOGS_DISABLED(2),
    AUDIT_LOGS_EXPIRATION(2),
    TAX_GUILDS_ENABLED(1, 2),
    TAX_GUILDS_PACIFISM_FACTOR(1, 2),
    TAX_GUILDS_SCALING(1, 2),
    TAX_GUILDS_USE_RESOURCE_POINTS(1, 2),
    TAX_GUILDS_DISBAND_IF_CANT_PAY(1, 2),
    TAX_GUILDS_NOTIFICATIONS(1, 2),
    TAX_GUILDS_AGE(1, 2),
    TAX_GUILDS_MEMBERS_ENABLED(1, 2, 3),
    TAX_GUILDS_MEMBERS_KICK_IF_CANT_PAY(1, 2, 3),
    TAX_GUILDS_MEMBERS_AGE(1, 2, 3),
    TAX_GUILDS_MEMBERS_DEFAULT_EQUATION(1, 2, 3),
    TAX_NATIONS_ENABLED(1, 2),
    TAX_NATIONS_USE_RESOURCE_POINTS(1, 2),
    TAX_NATIONS_DISBAND_IF_CANT_PAY(1, 2),
    TAX_NATIONS_SCALING(1, 2),
    TAX_NATIONS_AGE(1, 2),
    TAX_NATIONS_MEMBERS_ENABLED(1, 2, 3),
    TAX_NATIONS_MEMBERS_USE_RESOURCE_POINTS(1, 2, 3),
    TAX_NATIONS_MEMBERS_KICK_IF_CANT_PAY(1, 2, 3),
    TAX_NATIONS_MEMBERS_DEFAULT_EQUATION(1, 2, 3),
    TELEPORT_TO_SPAWN_AFTER_KICK,
    INACTIVITY_GUILD_EXCLUDE_CONDITION(1, 2),
    INACTIVITY_GUILD_DISBAND(1, 2),
    INACTIVITY_GUILD_ANNOUNCE(1, 2),
    INACTIVITY_MEMBER_KICK(1, 2),
    INACTIVITY_MEMBER_EXCLUDE_CONDITION(1, 2),
    INACTIVITY_MEMBER_DISBAND_GUILD_IF_LEADER(1, 2),
    INACTIVITY_MEMBER_ANNOUNCE(1, 2),
    GUILD_NAME_RENAMING_COOLDOWN(2),
    GUILD_NAME_MAX_LENGTH(2),
    GUILD_NAME_MIN_LENGTH(2),
    GUILD_NAME_IGNORE_COLORS(2),
    GUILD_NAME_ALLOW_SPACES(2),
    GUILD_NAME_ALLOW_NON_ENGLISH(2),
    GUILD_NAME_ALLOW_SYMBOLS(2),
    GUILD_NAME_ALLOW_NUMBERS(2),
    GUILD_NAME_ALLOW_DUPLICATE_NAMES(2),
    GUILD_NAME_CASE_SENSITIVE(2),
    GUILD_NAME_BLACKLISTED_NAMES(2),
    ECONOMY_BANK_DEPOSIT_ENABLED(1, 2, 3),
    ECONOMY_BANK_DEPOSIT_MIN(1, 2, 3),
    ECONOMY_BANK_LIMIT_GUILDS(1, 2, 3),
    ECONOMY_BANK_LIMIT_NATIONS(1, 2, 3),
    ECONOMY_BANK_WITHDRAW_ENABLED(1, 2, 3),
    ECONOMY_BANK_WITHDRAW_MIN(1, 2, 3),
    ECONOMY_RESOURCE_POINTS_WORTH(1, 3),
    ECONOMY_RESOURCE_POINTS_MIN_WITHDRAW(1, 3),
    ECONOMY_COSTS_CREATE_GUILD(1, 2, 3),
    ECONOMY_CREATE_CONFIRMATION(1),
    ECONOMY_COSTS_CREATE_NATION(1, 2, 3),
    ECONOMY_COSTS_RENAME_GUILD(1, 2, 3),
    ECONOMY_COSTS_TAG_GUILD(1, 2, 3),
    ECONOMY_COSTS_RENAME_NATION(1, 2, 3),
    ANNOUNCEMENTS_LEADER(1),
    ANNOUNCEMENTS_RENAME(1),
    ANNOUNCEMENTS_CREATE_GUILD(1, 2),
    ANNOUNCEMENTS_CREATE_NATION(1, 2),
    TAGS_ATTEMPT_AUTOMATIC_SETTING(1),
    TAGS_RENAMING_COOLDOWN(1),
    TAGS_MAX_LENGTH(1),
    TAGS_MIN_LENGTH(1),
    TAGS_IGNORE_COLORS(1),
    TAGS_ALLOW_SPACES(1),
    TAGS_ALLOW_NON_ENGLISH(1),
    TAGS_ALLOW_SYMBOLS(1),
    TAGS_ALLOW_NUMBERS(1),
    TAGS_BLACKLISTED_NAMES(1),
    INVITATIONS_ALLOW_OFFLINE_INVITES(1),
    INVITATIONS_ALLOW_FROM_OTHER_GUILDS(1),
    INVITATIONS_ALLOW_MULTIPLE_INVITES(1),
    INVITATIONS_EXPIRATION_EXPIRE_ON_LEAVE(1, 2),
    INVITATIONS_EXPIRATION_DEFAULT_EXPIRE(1, 2),
    INVITATIONS_ANNOUNCE(1),
    INVITATIONS_CODES_GENERATOR_LENGTH_MIN(1, 2, 3, 4),
    INVITATIONS_CODES_PAPER_COST_RESOURCE_POINTS(1, 2, 3, 4),
    INVITATIONS_CODES_PAPER_ITEM(1, 2, 3),
    INVITATIONS_CODES_MAX(1, 2),
    INVITATIONS_CODES_USES_MIN(1, 2, 3),
    INVITATIONS_CODES_USES_MAX(1, 2, 3),
    INVITATIONS_CODES_EXPIRATION_MIN(1, 2, 3),
    INVITATIONS_CODES_EXPIRATION_MAX(1, 2, 3),
    INVITATIONS_CODES_GENERATOR_LENGTH_MAX(1, 2, 3, 4),
    INVITATIONS_CODES_GENERATOR_CHARACTERS(1, 2, 3),
    COLOR_RANGE_ENABLED(2),
    COLOR_RANGE_COLORS(2),
    COLOR_RANGE_BLACKLIST(2),
    EVENTS,
    ERROR_SOUND,
    TPA_DEFAULT_TIMER(1),
    TPA_ALLOW_FROM_OTHER_GUILDS(1),
    TPA_TELEPORT_PREPARE_SECONDS(1, 2),
    TPA_TELEPORT_TIMER_SECONDS(1, 2),
    MAILS_ENVELOPE_ITEM(1, 2),
    MAILS_ENVELOPE_REPLY_ITEM(1, 2),
    MAILS_ENVELOPE_MONEY(1, 2),
    MAILS_ENVELOPE_RESOURCE_POINTS(1, 2),
    MAILS_SUBJECT_LIMIT(1, 2),
    MAILS_SUBJECT_IGNORE_COLORS(1, 2),
    MAILS_SUMMARY_LIMIT(1, 2);
    public static final FileConfiguration MAIN;
    public static final FileConfiguration CLAIMS;
    public static final FileConfiguration POWERS;
    public static final FileConfiguration MAP;
    public static final FileConfiguration RESOURCE_POINTS;
    private static final FileConfiguration RELATIONS;
    public static FileConfiguration PROTECTION_SIGNS;
    public static final FileConfiguration MISC_UPGRADE;
    public static final FileConfiguration RANKS;
    private final ConfigPath option;

    Config() {
        this.option = new ConfigPath(StringUtils.configOption(this));
    }

    Config(String option) {
        this.option = new ConfigPath(option);
    }

    Config(int ... groups) {
        this.option = new ConfigPath(this.name(), groups);
    }


    public static void errorSound(Player player) {
        XSound.play(player, ERROR_SOUND.getString());
    }

    public static YamlConfiguration loadFile(String filename) {
        File file = new File(CastelPlugin.getInstance().getDataFolder(), filename);
        if (!file.exists()) CastelPlugin.getInstance().saveResource(filename, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    static {
       MAIN = Config.loadFile("config.yml");
       CLAIMS = Config.loadFile("claims.yml");
       POWERS = Config.loadFile("powers.yml");
       MAP = Config.loadFile("map.yml");
       RESOURCE_POINTS = Config.loadFile("resource-points.yml");
       RELATIONS = Config.loadFile("relations.yml");
       PROTECTION_SIGNS = Config.loadFile("protection-signs.yml");
       MISC_UPGRADE = Config.loadFile("misc-upgrades.yml");
       RANKS = Config.loadFile("ranks.yml");
    }

    @Override
    public NewKeyedConfigAccessor getManager() {
        return new NewKeyedYamlConfigAccessor(MAIN, this.option);
    }

    @Override
    public NewKeyedConfigAccessor withProperty(String var1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NewKeyedConfigAccessor applyProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NewKeyedYamlConfigAccessor withOption(String var1, String var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NewKeyedYamlConfigAccessor clearExtras() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDynamicOption() {
        return String.join(".", this.option.build(null, null));
    }

    @Override
    public boolean isSet() {
        return this.getManager().isSet();
    }

    String build() {
        return StringUtils.join(this.option.build(null, null), ".");
    }

    @Override
    public String getKey() {
        return MAIN.getString(this.build());
    }

    @Override
    public String getString() {
        return MAIN.getString(this.build());
    }

    @Override
    public MathCompiler.Expression getMathExpression() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get() {
        return MAIN.get(this.build());
    }

    @Override
    public List<String> getStringList() {
        return MAIN.getStringList(this.build());
    }

    @Override
    public Set<String> getSectionKeys() {
        return MAIN.getKeys(true);
    }

    @Override
    public ConfigurationSection getSection() {
        return MAIN.getConfigurationSection(this.build());
    }

    @Override
    public boolean getBoolean() {
        return MAIN.getBoolean(this.build());
    }

    @Override
    public List<Integer> getIntegerList() {
        return MAIN.getIntegerList(this.build());
    }

    @Override
    public int getInt() {
        return MAIN.getInt(this.build());
    }

    @Override
    public double getDouble() {
        return MAIN.getDouble(this.build());
    }

    @Override
    public long getLong() {
        return MAIN.getLong(this.build());
    }

    public boolean isInDisabledWorld(Entity player) {
        return this.isInDisabledWorld(player, null);
    }

    public boolean isInDisabledWorld(Entity player, Lang lang) {
        String world = player.getWorld().getName();
        if (this.getStringList().contains(world)) {
            if (lang != null) lang.sendMessage(player, "world", world);
            return true;
        }
        return false;
    }

    public boolean isInDisabledWorld(Block block) {
        return this.isInDisabledWorld(block.getWorld());
    }

    public boolean isInDisabledWorld(World world) {
        return this.getStringList().contains(world.getName());
    }

    @Override
    public Long getTime(PlaceholderContextProvider ctx) {
        Object value = MAIN.get(this.build());
        if (value == null) return null;
        Object cache;
        if (value instanceof String) {
            try {
                cache = MathCompiler.Companion.compile((String) value);
            } catch (Throwable ignored) {
                cache = TimeUtils.parseTime((String) value);
            }
            if (cache == null) return null;
            MAIN.set(this.build(), cache);
        } else cache = value;

        return cache instanceof Number ? Long.valueOf(((Number) cache).longValue()) : Long.valueOf((long) MathUtils.eval((MathCompiler.Expression) cache, ctx));
    }

    public enum Claims implements NewEnumConfig {
        DISABLE_PROTECTION_SYSTEM(),
        CONNECTION_RADIUS(),
        DISTANCE(),
        CLAIM_ON_CREATE(),
        DISABLED_WORLDS(),
        BIOMES(),
        CONFIRMATION(),
        RESOURCE_POINTS_CLAIMS(2),
        RESOURCE_POINTS_AUTO_CLAIMS(2),
        RESOURCE_POINTS_REFUND_UNCLAIM(2),
        RESOURCE_POINTS_REFUND_AUTO_UNCLAIM(2),
        MONEY_CLAIMS(1),
        MONEY_AUTO_CLAIMS(1),
        MONEY_REFUND_UNCLAIM(1),
        MONEY_REFUND_AUTO_UNCLAIM(1),
        BEACON_PROTECTED_EFFECTS(),
        POTION_PROTECTED_EFFECTS(),
        PROTECTED_REGION_RADIUS(),
        UNCLAIM_COOLDOWN(),
        STARTER_FREE(),
        HISTORY_ENABLED(1),
        HISTORY_LIMIT(1),
        ACTIONBAR_AUTO_CLAIM(1),
        ACTIONBAR_AUTO_UNCLAIM(1),
        ACTIONBAR_KEEP(1),
        MAX_CLAIMS(),
        FILL_MAX_ITERATIONS(1),
        FILL_MAX_CLAIMS(1),
        SQUARE_MAX_RADIUS(),
        LINE_MAX_DISTANCE(),
        COORDINATES_CLAIM_MAX_DISTANCE(),
        TEMPORARY_PERMISSIONS(),
        RESTORATION_ENABLED(1),
        RESTORATION_MAX_ACTIVE_RESTORING_CHUNKS(1),
        RESTORATION_BLOCK_RESTORATION_RATE(1),
        RESTORATION_IGNORED_BLOCKS(1),
        BUILD_IN_CLAIMED_ONLY_PLACE(4),
        BUILD_IN_CLAIMED_ONLY_BREAK(4),
        BUILD_IN_CLAIMED_ONLY_UNCLAIMED_BUILD_RADIUS(4),
        BUILD_IN_CLAIMED_ONLY_CHARGES_ENABLED(4, 5),
        BUILD_IN_CLAIMED_ONLY_CHARGES_IN_RANGE(4, 5),
        BUILD_IN_CLAIMED_ONLY_CHARGES_PLACING_RESOURCE_POINTS(4, 5, 6),
        BUILD_IN_CLAIMED_ONLY_CHARGES_PLACING_MONEY(4, 5, 6),
        BUILD_IN_CLAIMED_ONLY_CHARGES_BREAKING_RESOURCE_POINTS(4, 5, 6),
        BUILD_IN_CLAIMED_ONLY_CHARGES_BREAKING_MONEY(4, 5, 6),
        ALLOW_FLIGHT(),
        CORNER_MAX_CLAIMS(),
        UNCLAIM_CONFIRMATION_TURRETS(2),
        UNCLAIM_CONFIRMATION_STRUCTURES(2),
        UNCLAIM_CONFIRMATION_HOME(2),
        UNCLAIM_ALL_ENABLED(2),
        UNCLAIM_ALL_COST(2),
        UNCLAIM_ALL_ANNOUNCE(2),
        UNCLAIM_ALL_KEEP_NEXUS_LAND(2),
        UNCLAIM_ALL_CONFIRM_ENABLED(2, 3),
        UNCLAIM_ALL_CONFIRM_TIME(2, 3),
        UNCLAIM_ALL_CONFIRM_GUI(2, 3),
        INDICATOR_IGNORE_WORLDGUARD_REGIONS(1),
        INDICATOR_GUILDLESS_ENABLED(1),
        INDICATOR_VISUALIZER_ENABLED(1, 2),
        INDICATOR_VISUALIZER_CENTER(1, 2),
        INDICATOR_VISUALIZER_BORDER(1, 2),
        INDICATOR_VISUALIZER_FLOOR_CHECK_HEIGHT(1, 2),
        INDICATOR_VISUALIZER_ALLOW_DISABLE(1, 2),
        INDICATOR_VISUALIZER_ALLOW_TEMP_PERMANENT(1, 2),
        INDICATOR_VISUALIZER_STAY(1, 2),
        INDICATOR_VISUALIZER_ALL_STAY(1, 2),
        INDICATOR_CORNER_BLOCK("indicator.{relation}.corner-block"),
        INDICATOR_TWO_BLOCK("indicator.{relation}.two-block"),
        INDICATOR_SEND_MESSAGES_FOR_SAME_LAND_TYPE("indicator.{relation}.send-messages-for-same-land-type"),
        INDICATOR_PARTICLES(1),
        INDICATOR_VISUALIZER_NAME(1, 2),
        INDICATOR_DEFAULT_NAME(1, 2),
        INDICATOR_DEFAULT_METHOD(1, 2),
        INDICATOR_PARTICLE("indicator.{relation}.particles");
        private final ConfigPath option;

        Claims() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        Claims(String option) {
            this.option = new ConfigPath(option);
        }

        Claims(int ... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        @Override
        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(CLAIMS, this.option);
        }
    }

    public enum Powers implements NewEnumConfig {
        POWER_ENABLED(1),
        POWER_PLAYER_MAX(1, 2),
        POWER_PLAYER_MIN(1, 2),
        POWER_PLAYER_INITIAL(1, 2),
        POWER_PLAYER_REGENERATION_EVERY(1, 2, 3),
        POWER_PLAYER_REGENERATION_CHARGE(1, 2, 3),
        POWER_PLAYER_LOSS_DEATH(1, 2, 3),
        POWER_PLAYER_LOSS_OFFLINE_EVERY(1, 2, 3, 4),
        POWER_PLAYER_LOSS_OFFLINE_LOSE(1, 2, 3, 4),
        POWER_PLAYER_LOSS_OFFLINE_MIN(1, 2, 3, 4),
        POWER_FACTION_MAX(1, 2),
        POWERUPS_ENABLED("powerups.{upgrade}.enabled"),
        POWERUPS_COST("powerups.{upgrade}.cost"),
        POWERUPS_DEFAULT_LEVEL("powerups.{upgrade}.default-level"),
        POWERUPS_OWN_LAND_ONLY("powerups.{upgrade}.own-land-only"),
        POWERUPS_SCALING("powerups.{upgrade}.scaling"),
        POWERUPS_MAX_LEVEL("powerups.{upgrade}.max-level");

        private final ConfigPath option;

        Powers() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        Powers(String option) {
            this.option = new ConfigPath(option);
        }

        Powers(int... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(Config.POWERS, this.option);
        }
    }

    public enum Map {
        LIMIT_WIDTH(1),
        LIMIT_HEIGHT(1),
        WIDTH,
        HEIGHT,
        ELEMENTS,
        HEADER,
        FOOTER,
        BEGIN,
        DISTANCE,
        GUILD_PLAYER_ONLY,
        COMPASS;

        private final ConfigPath option;

        Map() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        Map(String option) {
            this.option = new ConfigPath(option);
        }

        Map(int... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(Config.MAP, this.option);
        }
    }

    public enum ResourcePoints implements NewEnumConfig {
        FOR_EACH,
        GIVE,
        LAST_DONATION_DURATION,
        GENERAL_FILTERS_LORE(2),
        GENERAL_FILTERS_ENCHANTED(2),
        GENERAL_FILTERS_MATERIAL_LIST(2, 3),
        GENERAL_FILTERS_MATERIAL_BLACKLIST(2, 3),
        CUSTOM,
        ADVANCED,
        CUSTOM_ITEMS,
        DEATH_PENALTY_AMOUNT(2),
        DEATH_PENALTY_DISABLED_WORLDS(2);

        private final ConfigPath option;

        ResourcePoints() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        ResourcePoints(String option) {
            this.option = new ConfigPath(option);
        }

        ResourcePoints(int ... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        @Override
        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(Config.RESOURCE_POINTS, this.option);
        }
    }

    public enum Relations implements NewEnumConfig {
        COLOR("relations.{relation}.color"),
        COST("relations.{relation}.cost"),
        LIMIT("relations.{relation}.limit"),
        REQUEST_EXPIRATION,
        PVP,
        AGREEMENTS,
        FORCE_SURVIVAL_MODE,
        PRIORITIZE_GUILD_RELATIONS,
        ALLOW_RELATIONS_BETWEEN_NATION_GUILDS,
        CUSTOMIZABLE("relations.{relation}.customizable"),
        RELATIONS("relations"),
        RELATIONS_EFFECTS("relations.{relation}.effects"),
        RELATIONS_DISABLED_COMMANDS("relations.{relation}.disabled-commands");

        private final ConfigPath option;

        Relations() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        Relations(String option) {
            this.option = new ConfigPath(option);
        }

        Relations(int... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(Config.RELATIONS, this.option);
        }
    }

    public enum ProtectionSigns implements NewEnumConfig {
        ENABLED(),
        PROTECT_UNCLAIMED(),
        PROTECTIONS_EXPLOSION(1),
        PROTECTIONS_PISTON(1),
        PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_ALL(1, 3),
        PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_CROSS_ORIGIN_CONTAINER_TRANSFERS(1, 3),
        CASE_SENSITIVE_CODES(),
        GUI(),
        QUICK_PROTECT_ENABLED(2),
        QUICK_PROTECT_SNEAK(2),
        SIGNS(),
        PASSWORDS_LENGTH_MIN(1, 2),
        PASSWORDS_LENGTH_MAX(1, 2),
        PASSWORDS_MAX_ATTEMPTS(1),
        PASSWORDS_MAX_ATTEMPTS_COOLDOWN(1),
        PASSWORDS_COOKIES(1),
        PASSWORDS_REMOVE_KEYWORD(1),
        LIMITS_PLAYERS(1),
        LIMITS_GUILDS(1),
        DENIED_SOUND(),
        BLOCKS(),
        CODES(),
        LINES(),
        EVERYONE_IN_GUILD_ENABLED(3),
        EVERYONE_IN_GUILD_CODES(3),
        EVERYONE_IN_GUILD_LINES(3),
        EVERYONE_ENABLED(1),
        EVERYONE_CODES(1),
        EVERYONE_LINES(1);

        private final ConfigPath option;

        ProtectionSigns(int... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(Config.PROTECTION_SIGNS, this.option);
        }
    }

    public enum MiscUpgrades implements NewEnumConfig {
        COST("{upgrade}.cost"),
        SCALING("{upgrade}.scaling"),
        ENABLED("{upgrade}.enabled"),
        MAX_LEVEL("{upgrade}.max-level"),
        DEFAULT_LEVEL("{upgrade}.default-level"),
        LEVELS("{upgrade}.levels"),
        ANTI_EXPLOSION_FANCY_EXPLOSIONS_ENABLED(2, 4),
        ANTI_EXPLOSION_FANCY_EXPLOSIONS_HEIGHT_MIN(2, 4, 5),
        ANTI_EXPLOSION_FANCY_EXPLOSIONS_HEIGHT_MAX(2, 4, 5),
        ANTI_EXPLOSION_FANCY_EXPLOSIONS_SPREAD_MIN(2, 4, 5),
        ANTI_EXPLOSION_FANCY_EXPLOSIONS_SPREAD_MAX(2, 4, 5),
        ANTI_EXPLOSION_DROP_DESTROYED_KINGDOM_ITEMS(2, 4, 5),
        ANTI_EXPLOSION_AUTO_REGENERATE_ENABLED(2, 4),
        ANTI_EXPLOSION_AUTO_REGENERATE_DELAY(2, 4),
        ANTI_EXPLOSION_AUTO_REGENERATE_INTERVAL(2, 4);
        private final ConfigPath option;

        MiscUpgrades(String option) {
            this.option = new ConfigPath(option);
        }

        MiscUpgrades(int ... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        @Override
        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(MISC_UPGRADE, this.option);
        }
    }

    public enum Ranks implements NewEnumConfig {
        CUSTOM_RANKS_ENABLED(2),
        CUSTOM_RANKS_LIMITS_RANKS(2, 3),
        CUSTOM_RANKS_LIMITS_MAX_CLAIMS(2, 3),
        CUSTOM_RANKS_LIMITS_LENGTH_NODE(2, 3, 4),
        CUSTOM_RANKS_LIMITS_LENGTH_NAME(2, 3, 4),
        CUSTOM_RANKS_LIMITS_LENGTH_COLOR(2, 3, 4),
        CUSTOM_RANKS_LIMITS_LENGTH_SYMBOL(2, 3, 4),
        CUSTOM_RANKS_LIMITS_MATERIAL_WHITELIST(2, 3, 4),
        CUSTOM_RANKS_LIMITS_MATERIAL_LIST(2, 3, 4),
        NEW_RANK_NODE(2),
        NEW_RANK_NAME(2),
        NEW_RANK_COLOR(2),
        NEW_RANK_SYMBOL(2),
        NEW_RANK_MATERIAL(2),
        NEW_RANK_MAX_CLAIMS(2),
        NEW_RANK_PERMISSIONS(2),
        INTERACT_BLOCKS(),
        COLOR("ranks.{rank}.color"),
        CHAT_COLOR("ranks.{rank}.chat-color"),
        SYMBOL("ranks.{rank}.symbol"),
        NAME("ranks.{rank}.name"),
        FORMAT("ranks.{rank}.format"),
        MATERIAL("ranks.{rank}.material"),
        MAX_CLAIMS("ranks.{rank}.max-claims"),
        PERMISSIONS("ranks.{rank}.permissions");
        private final ConfigPath option;

        Ranks() {
            this.option = new ConfigPath(StringUtils.configOption(this));
        }

        Ranks(String option) {
            this.option = new ConfigPath(option);
        }

        Ranks(int ... grouped) {
            this.option = new ConfigPath(this.name(), grouped);
        }

        public NewKeyedYamlConfigAccessor getManager() {
            return new NewKeyedYamlConfigAccessor(RANKS, this.option);
        }
    }
}
