package fr.krishenk.castel.utils.scoreboards;

import com.google.common.collect.ImmutableList;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class XScoreboard {
    private static Object nms$Scoreboard;
    private static final Class<?> Scoreboard = ReflectionUtils.getNMSClass("world.scores", "Scoreboard");
    private static final Class<?> ScoreboardTeam = ReflectionUtils.getNMSClass("world.scores", "ScoreboardTeam");
    private static final Class<?> PacketPlayOutScoreboardTeam = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam");
    private static Class<?> PacketPlayOutScoreboardTeam$info;
    private static final List<String> SCOREBOARD_TEAM_ENTITY_LIST = new ArrayList<>();
    private static final Class<?> EnumChatFormat = ReflectionUtils.getNMSClass("EnumChatFormat");
    private static final MethodHandle CHAT_COMPONENT_TEXT;
    public static final int ALLOW_FRIENDLY_FIRE = 1;
    public static final int CAN_SEE_INVISIBLE_ENTITIES_ON_SAME_TEAM = 2;
    private Color color;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private Collection<Entity> members;
    private Object packetScoreboardTeam;

    public XScoreboard() {
        this.color = Color.WHITE;
        this.nameTagVisibility = NameTagVisibility.always;
        this.collisionRule = CollisionRule.always;
    }

    public void setScoreboard(Player player) {
        ReflectionUtils.sendPacketSync(player, this.packetScoreboardTeam);
    }

    public XScoreboard build() {
        try {
            Object nms$ScoreboardTeam = null;
            Object packetScoreboardTeam = null;
            if (ReflectionUtils.supports(17)) {
                if (nms$Scoreboard== null) nms$Scoreboard = Scoreboard.getConstructors()[0].newInstance();
                nms$ScoreboardTeam = ScoreboardTeam.getConstructors()[0].newInstance(nms$Scoreboard, this.color.getTeamName());
            } else {
                packetScoreboardTeam = PacketPlayOutScoreboardTeam.newInstance();
                getFreeAccessField(PacketPlayOutScoreboardTeam, "i").set(packetScoreboardTeam, TeamNode.CREATE.ordinal());
                getFreeAccessField(PacketPlayOutScoreboardTeam, "a").set(packetScoreboardTeam, this.color.getTeamName());
                getFreeAccessField(PacketPlayOutScoreboardTeam, "e").set(packetScoreboardTeam, this.nameTagVisibility.name());
                getFreeAccessField(PacketPlayOutScoreboardTeam, "f").set(packetScoreboardTeam, this.collisionRule.name());
            }

            Object prefix = CHAT_COMPONENT_TEXT.invoke("§" + this.color.colorCode);
            Object displayName = CHAT_COMPONENT_TEXT.invoke(this.color.getTeamName());
            Object suffix = CHAT_COMPONENT_TEXT.invoke("");
            if (ReflectionUtils.supports(17)) {
                if (PacketPlayOutScoreboardTeam$info == null) {
                    PacketPlayOutScoreboardTeam$info = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam$b");
                    if (PacketPlayOutScoreboardTeam$info == null) {
                        PacketPlayOutScoreboardTeam$info = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam$Parameters");
                    }
                }

                Class<?> IChatBaseComponent = ReflectionUtils.getNMSClass("network.chat", "IChatBaseComponent");
                ScoreboardTeam.getDeclaredMethod("setDisplayName", IChatBaseComponent).invoke(nms$ScoreboardTeam, displayName);
                ScoreboardTeam.getDeclaredMethod("setPrefix", IChatBaseComponent).invoke(nms$ScoreboardTeam, prefix);
                ScoreboardTeam.getDeclaredMethod("setSuffix", IChatBaseComponent).invoke(nms$ScoreboardTeam, suffix);
                ScoreboardTeam.getDeclaredMethod("setColor", EnumChatFormat).invoke(nms$ScoreboardTeam, this.color.packetValue);
                Object packetScoreboardTeamInfo = PacketPlayOutScoreboardTeam$info.getConstructors()[0].newInstance(nms$ScoreboardTeam);
                Constructor<?> ctor = PacketPlayOutScoreboardTeam.getDeclaredConstructor(String.class, Integer.TYPE, Optional.class, Collection.class);
                ctor.setAccessible(true);
                packetScoreboardTeam = ctor.newInstance(this.color.getTeamName(), TeamNode.CREATE.ordinal(), Optional.of(packetScoreboardTeamInfo), ImmutableList.of());
            } else {
                Object entitiesList;
                if (ReflectionUtils.supports(17)) {
                    SCOREBOARD_TEAM_ENTITY_LIST.clear();
                    entitiesList = SCOREBOARD_TEAM_ENTITY_LIST;
                } else {
                    Field field = PacketPlayOutScoreboardTeam.getDeclaredField("h");
                    field.setAccessible(true);
                    entitiesList = field.get(packetScoreboardTeam);
                }

                for (Entity entity : this.members) {
                    if (entity instanceof Player) ((Collection) entitiesList).add(entity.getName());
                    else ((Collection) entitiesList).add(entity.getUniqueId().toString());
                }

                getFreeAccessField(PacketPlayOutScoreboardTeam, "g").set(packetScoreboardTeam, this.color.packetValue);
                getFreeAccessField(PacketPlayOutScoreboardTeam, "c").set(packetScoreboardTeam, prefix);
                getFreeAccessField(PacketPlayOutScoreboardTeam, "b").set(packetScoreboardTeam, displayName);
                getFreeAccessField(PacketPlayOutScoreboardTeam, "d").set(packetScoreboardTeam, suffix);
                getFreeAccessField(PacketPlayOutScoreboardTeam, "j").set(packetScoreboardTeam, 0);
            }

            this.packetScoreboardTeam = packetScoreboardTeam;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public XScoreboard setCollisionRule(CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
        return this;
    }

    public XScoreboard setColor(Color color) {
        this.color = color;
        return this;
    }

    public XScoreboard setMembers(Collection<Entity> members) {
        this.members = members;
        return this;
    }

    public XScoreboard setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
        return this;
    }

    private static Field getFreeAccessField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle chatComponentTextCtor = null;
        Class<?> ChatComponentText = ReflectionUtils.getNMSClass("network.chat", "ChatComponentText");
        try {
            chatComponentTextCtor = lookup.findConstructor(ChatComponentText, MethodType.methodType(Void.TYPE, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        CHAT_COMPONENT_TEXT = chatComponentTextCtor;
    }

    public enum Color {
        BLACK(0, "0"),
        DARK_BLUE(1, "1"),
        DARK_GREEN(2, "2"),
        DARK_AQUA(3, "3"),
        DARK_RED(4, "4"),
        DARK_PURPLE(5, "5"),
        GOLD(6, "6"),
        GRAY(7, "7"),
        DARK_GRAY(8, "8"),
        BLUE(9, "9"),
        GREEN(10, "a"),
        AQUA(11, "b"),
        RED(12, "c"),
        PURPLE(13, "d"),
        YELLOW(14, "e"),
        WHITE(15, "f"),
        NONE(-1, "");
        private final Object packetValue;
        private final String colorCode;

        Color(int packetValue, String colorCode) {
            Object packet = null;
            try {
                packet = XScoreboard.EnumChatFormat.getMethod("a", Integer.TYPE).invoke(null, packetValue);
            } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            this.packetValue = packet;
            this.colorCode = colorCode;
        }

        String getTeamName() {
            String name = "GAPI#" + this.name();
            if (name.length() > 16) name = name.substring(0, 16);
            return name;
        }
    }

    public enum NameTagVisibility {
        always,
        hideForOtherTeams,
        hideForOwnTeam,
        never;
    }

    public enum CollisionRule {
        always,
        pushOtherTeams,
        pushOwnTeam,
        never;
    }

    private enum TeamNode {
        CREATE,
        REMOVE,
        UPDATE,
        ENTITY_ADD,
        ENTITY_REMOVE
    }
}
