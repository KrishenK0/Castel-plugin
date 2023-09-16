package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.container.MessageContainer;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.function.Function;

public enum StandardCastelPlaceholder {
    MASSWAR_IS_RUNNING(false, null),
    MASSWAR_TIME(0, null),
    LANG("", data -> {
        CastelPlayer castelPlayer = data.getPlayer();
        return (castelPlayer != null ? castelPlayer.getLanguage().getNativeName() : null);
    }),
    PLAYER_CLAIMS(0, new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.getClaims().size() : null);
        }
    }),
    HAS_GUILD(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.hasGuild() : null);
        }
    }),
    JOINED(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.getJoinedAt() : null);
        }
    }),
    LAST_DONATION_TIME(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return (cp != null) ? cp.getLastDonationTime() : null;
        }
    }),
    LAST_DONATION_AMOUNT(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return cp != null ? cp.getLastDonationAmount() : null;
        }
    }),
    TOTAL_DONATIONS(0,new Function<CastelPlaceholderTranslationContext, Object>()

    {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return cp != null ? cp.getTotalDonations() : null;
        }
    }),
//
//    IS_INVADING(false,new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public final Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        Boolean bl;
//
//        CastelPlayer cp = data.getPlayer();
//        if (cp != null) {
//            CastelPlayer it = cp;
//            
//            CastelPlayer x = it;
//            
//            bl = x.isInvading();
//        } else {
//            bl = null;
//        }
//        return bl;
//    }
//    }),
    IS_SPY(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.isSpy() : null);
        }
    }),
    IS_ADMIN(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.isAdmin() : null);
        }
    }),
    IS_FLYING(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.isFlying() : null);
        }
    }),
    IS_PVP(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.isPvp() : null);
        }
    }),
    IN_SNEAK_MODE(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            CastelPlayer castelPlayer = data.getPlayer();
            return (castelPlayer != null ? castelPlayer.isInSneakMode() : null);
        }
    }),
    POWER(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return cp != null ? cp.getPower() : null;
        }
    }),
    TAX(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Double tax = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Guild guild = castelPlayer.getGuild();
                if (guild != null) {
                    tax = MathUtils.roundToDigits(guild.getTax(castelPlayer.getOfflinePlayer()), 2);
                }
            }
            return tax;
        }
    }),
//    CHAT_CHANNEL("",new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        String string;
//
//        CastelPlayer cp = data.getPlayer();
//        if (cp != null) {
//            CastelPlayer it = cp;
//
//            CastelPlayer x = it;
//
//            string = x.getChatChannel().getId();
//        } else {
//            string = null;
//        }
//        return string;
//    }
//    }),
//    CHAT_CHANNEL_NAME("",new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public final Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        MessageObjectBuilder messageObjectBuilder;
//
//        CastelPlayer cp = data.getPlayer();
//        if (cp != null) {
//            CastelPlayer it = cp;
//            
//            CastelPlayer x = it;
//            
//            messageObjectBuilder = x.getChatChannel().getName();
//        } else {
//            messageObjectBuilder = null;
//        }
//        return messageObjectBuilder;
//    }
//    }),
//    CHAT_CHANNEL_SHORT("",new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public final Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        MessageObjectBuilder messageObjectBuilder;
//
//        CastelPlayer cp = data.getPlayer();
//        if (cp != null) {
//            CastelPlayer it = cp;
//            
//            CastelPlayer x = it;
//            
//            messageObjectBuilder = x.getChatChannel().getShortName();
//        } else {
//            messageObjectBuilder = null;
//        }
//        return messageObjectBuilder;
//    }
//    }),
//    CHAT_CHANNEL_COLOR("",new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public final Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        MessageObject messageObject;
//
//        CastelPlayer cp = data.getPlayer();
//        if (cp != null) {
//            CastelPlayer it = cp;
//            
//            CastelPlayer x = it;
//            
//            messageObject = x.getChatChannel().getColor();
//        } else {
//            messageObject = null;
//        }
//        return messageObject;
//    }
//    }),
    RANK_NODE("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            String rankNode = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                rankNode = rank != null ? rank.getNode() : null;
            }
            return rankNode;
        }
    }),
    RANK_NAME("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            String rankName = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                rankName = rank != null ? rank.getName() : null;
            }
            return rankName;
        }
    }),
    RANK_COLOR("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            String color = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                color = rank != null ? rank.getColor() : null;
            }
            return color;
        }
    }),
    RANK_SYMBOL("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            String symbol = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                symbol = rank != null ? rank.getSymbol() : null;
            }
            return symbol;
        }
    }),
    RANK_PRIORITY(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Integer priority = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                priority = rank != null ? Integer.valueOf(rank.getPriority()) : null;
            }
            return priority;
        }
    }),
    RANK_MAX_CLAIMS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Integer maxClaims = null;

            CastelPlayer castelPlayer = data.getPlayer();
            if (castelPlayer != null) {
                Rank rank = castelPlayer.getRank();
                maxClaims = rank != null ? rank.getMaxClaims() : null;
            }
            return maxClaims;
        }
    }),
    MAP_HEIGHT(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return cp != null ? (cp.getMapSize() == null ? Integer.valueOf(Config.Map.HEIGHT.getManager().getInt()) : cp.getMapSize().getKey()) : null;
        }
    }),
    MAP_WIDTH(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            CastelPlayer cp = data.getPlayer();
            return cp != null ? cp.getMapSize() == null ? Integer.valueOf(Config.Map.WIDTH.getManager().getInt()) : cp.getMapSize().getValue() : null;
        }
    }),

    DISTANCE_FROM_CORE(0, null),

    LAND("", null),

    NAME("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getName() : null);
        }
    }),

    GUILD_NAME("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getName() : null);
        }
    }),

    LORE("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getLore() : null);
        }
    }),

    LEADER("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getLeader().getOfflinePlayer().getName() : null);
        }
    }),

    MEMBERS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMembers().size() : null);
        }
    }),

    ONLINE_MEMBERS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getOnlineMembers().size() : null);
        }
    }),

    OFFLINE_MEMBERS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMembers().size() - guild.getOnlineMembers().size() : null);
        }
    }),
    MEMBER("", null),
    RESOURCE_POINTS(0,new Function<CastelPlaceholderTranslationContext, Object>() {
            @Nullable
            public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            Guild guild = data.getGuild();
            return guild == null ? null : guild.getResourcePoints();
        }
    }),
    MIGHT(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMight() : null);
        }
    }),
    GUILD_HOME("",new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
        Object object = null;

        Guild guild = data.getGuild();
        if (guild != null) {
            if (guild.getHome() == null) {
                object = CastelLang.NONE;
            } else {
                Location location = guild.getHome();
                object = LocationUtils.locationMessenger(SimpleLocation.of(location));
            }
        }
        return object;
    }
    }),
//
//    NEXUS("",new Function<CastelPlaceholderTranslationContext, Object>()
//
//    {
//
//        @Nullable
//        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
//        LanguageEntryWithContext languageEntryWithContext;
//
//        Guild guild = data.getGuild();
//        if (guild != null) {
//            
//
//            
//
//            SimpleLocation simpleLocation = x.getNexus();
//            if (simpleLocation != null) {
//                SimpleLocation it2 = simpleLocation;
//                
//                SimpleLocation simpleLocation2 = x.getNexus();
//
//                languageEntryWithContext = LocationUtils.locationMessenger(simpleLocation2);
//            } else {
//                languageEntryWithContext = null;
//            }
//        } else {
//            languageEntryWithContext = null;
//        }
//        return languageEntryWithContext;
//    }
//    }),

    CLAIMS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getLandLocations().size(): null);
        }
    }),

    LANDS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getLandLocations().size() : null);
        }
    }),

    TAG("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getTag() : null);
        }
    }),

    GUILD_TAG("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getTag() : null);
        }
    }),

    MAX_CLAIMS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMaxClaims() : null);
        }
    }),

    MAX_LANDS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMaxClaims() : null);
        }
    }),

    MAX_MEMBERS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getMaxMembers() : null);
        }
    }),

    BANK(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getBank() : null);
        }
    }),

    RANKS(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getRanks().size() : null);
        }
    }),

    PACIFIST(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.isPacifist() : null);
        }
    }),
    GUILD_IS_PACIFIST(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.isPacifist() : null);
        }
    }),

    MAX_LANDS_MODIFIER(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            Guild guild = data.getGuild();
            return guild != null ? guild.getMaxLandsModifier() : null;
        }
    }),

    GUILD_POWER(0,new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            Guild guild = data.getGuild();
            return guild != null ? guild.getPower() : null;
        }
    }),

    MAILS_TOTAL(0,new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data){
            Guild guild = data.getGuild();
            return guild != null ? guild.getMails().size() : null;
        }
    }),

    AVG_LANDS_DISTANCE(0, null),

    GUILD_FLAG("", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getFlag() : null);
        }
    }),

    GUILD_COLOR("000000", new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            String string = null;
            Guild guild = data.getGuild();
            if (guild != null) {
                Color color = guild.getColor();
                if (color != null) {
                    Object[] objects = new Object[]{color.getRGB() & 0xFFFFFF};
                    string = String.format("%06x", Arrays.copyOf(objects, objects.length));
                }
            }
            return string;
        }
    }),

    GUILD_IS_PERMANENT(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.isPermanent() : null);
        }
    }),
    GUILD_IS_HIDDEN(false, new Function<CastelPlaceholderTranslationContext, Object>() {
        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? data.getGuild() : null);
        }
    }),

    GUILD_HOME_IS_PUBLIC(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.isHomePublic() : null);
        }
    }),

    GUILD_REQUIRES_INVITE(false, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return guild != null ? guild.requiresInvite() : null;
        }
    }),
    TOP_POSITION(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return guild != null ? CastelDataCenter.get().getGuildManager().getTopPosition(guild) : null;
        }
    }),
    SINCE(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? guild.getSince() : null);
        }
    }),

    SERVER_GUILD_TAX(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return guild != null ? MathUtils.roundToDigits(guild.calculateTax(), 2) : null ;
        }
    }),

    GUILD_TAX(0, new Function<CastelPlaceholderTranslationContext, Object>() {

        @Nullable
        public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
            Guild guild = data.getGuild();
            return (guild != null ? MathUtils.roundToDigits(guild.calculateTax(), 2) : null);
        }
    }),

    TOP("", null),

    GUILD_UPGRADE(0, null),

    RELATION_COLOR("", null),

    RELATION_NAME("", null);

    @NotNull
    public static final String IDENTIFIER = "castel";
    @NotNull
    public static final String OTHER_IDENTIFIER = "other_";
    @NotNull
    private static final Map<String, MessageContainer> GLOBAL_MACROS = new HashMap<>();

    StandardCastelPlaceholder(Object object, Function<? super CastelPlaceholderTranslationContext, ? extends Object> translator) {
        CastelPlaceholder.of(this.name().toLowerCase(Locale.ENGLISH), object, translator);
    }

    @NotNull
    public static Map<String, MessageContainer> getGlobalMacros() {
        return Collections.unmodifiableMap(GLOBAL_MACROS);
    }

    public static void init() {
        ConfigurationSection section = Config.PLACEHOLDERS_DEFAULTS.getSection();

        for (String key : section.getKeys(false)) {
            String string = key.toLowerCase(Locale.ROOT);
            CastelPlaceholder holder = CastelPlaceholder.getByName(string);
            if (holder == null) continue;
            holder.setConfiguredDefaultValue(section.get(key));
        }
        section = Config.PLACEHOLDERS_VARIABLES.getSection();
        for (String key : section.getKeys(false)) {
            MessageContainer varPlaceholder = MessageContainer.parse(Config.PLACEHOLDERS_VARIABLES.getManager().withProperty(key));
            GLOBAL_MACROS.put(key, varPlaceholder);
        }
    }

    public static MessageObject getMacro(@NotNull String id, @Nullable MessageBuilder settings) {
        MessageContainer messageContainer = GLOBAL_MACROS.get(id);
        if (messageContainer == null) {
            return null;
        }
        return messageContainer.get(settings);
    }


    @Nullable
    public static final MessageContainer getRawMacro(@NotNull String id) {
        return GLOBAL_MACROS.get(id);
    }
}

