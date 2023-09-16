package fr.krishenk.castel.constants.player;

import com.google.common.base.Strings;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.internal.enumeration.OrderedSet;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Rank implements Comparable<Rank>, Cloneable {
    private static final RankMap DEFAULT_RANKS = new RankMap();
    public static boolean CUSTOM_RANKS;
    private transient String node;
    private XMaterial material;
    private String name;
    private String color;
    private String symbol;
    private int priority;
    private int maxClaims;
    private Set<GuildPermission> permissions;

    public Rank(String node, String name, String color, String symbol, XMaterial material, int priority, int maxClaims, Set<GuildPermission> permissions) {
        Validate.isTrue(priority >= 0, "Rank priority must be greater than -1");
        this.node = Objects.requireNonNull(node, "Rank node cannot be null");
        this.name = name;
        this.color = color;
        this.symbol = symbol;
        this.material = Objects.requireNonNull(material, "Rank material cannot be null");
        this.priority = priority;
        this.maxClaims = maxClaims;
        this.permissions = Objects.requireNonNull(permissions, "Rank permission cannot be null");
    }

    public static void init() {
        DEFAULT_RANKS.clear();
        CUSTOM_RANKS = true;
        int priority = 0;

        for (String rank : Config.RANKS.getConfigurationSection("ranks").getKeys(false)) {
            String name = Config.Ranks.NAME.getManager().withOption("rank", rank).getString();
            String color = Config.Ranks.COLOR.getManager().withOption("rank", rank).getString();
            String symbol = Config.Ranks.SYMBOL.getManager().withOption("rank", rank).getString();
            String matStr = Config.Ranks.MATERIAL.getManager().withOption("rank", rank).getString();
            XMaterial material = Strings.isNullOrEmpty(matStr) ? XMaterial.DIRT : XMaterial.matchXMaterial(matStr).orElse(XMaterial.DIRT);
            int maxClaims = Config.Ranks.MAX_CLAIMS.getManager().withOption("rank", rank).getInt();
            Set<GuildPermission> permissions = new OrderedSet<>(0);
            for (String permission : Config.Ranks.PERMISSIONS.getManager().withOption("rank", rank).getStringList()) {
                GuildPermission perm = CastelPlugin.getInstance().getPermissionRegistry().getRegistered(Namespace.fromString(permission));
                if (perm == null)
                    MessageHandler.sendConsolePluginMessage("&cCould not find permission for default rank &e" + rank + " (" + name + ")&8: &e" + permission + " (" + perm + ')');
                else permissions.add(perm);
            }

            Objects.requireNonNull(color, "Default rank color is not found for " + rank);
            Objects.requireNonNull(symbol, "Default rank symbol is not found for " + rank);
            DEFAULT_RANKS.addOrReplace(new Rank(rank.toLowerCase(Locale.ENGLISH), name, color, symbol, material, priority, maxClaims, permissions));
            ++priority;
        }
    }

    public static RankMap copyDefaults() {
        return DEFAULT_RANKS.clone();
    }

    public static RankMap getDefaultRanks() {
        return DEFAULT_RANKS;
    }

    public static CastelPlayer determineNextLeader(ArrayList<CastelPlayer> members, Comparator<CastelPlayer> comparator) {
        Validate.notEmpty(members, "Members list cannot be null or empty");
        if (members.size() < 2) return null;
        if (comparator == null) members.sort(CastelPlayer::compareTo);
        else members.sort(comparator);
        return members.get(1);
    }

    public static Rank getHighestRank() {
        return DEFAULT_RANKS.getHightestRank();
    }

    public static Rank getLowestRank() {
        return DEFAULT_RANKS.getLowestRank();
    }

    public Set<GuildPermission> copyPermissions() {
        return new OrderedSet<>(this.permissions);
    }

    public boolean hasPermission(GuildPermission permision) {
        return this.isLeader() || this.permissions.contains(permision);
    }

    public Rank clone() {
        return new Rank(this.node, this.name, this.color, this.symbol, this.material, this.priority, this.maxClaims, this.copyPermissions());
    }

    @Override
    public int compareTo(@NotNull Rank rank) {
        return Integer.compare(this.priority, rank.priority);
    }

    public int hashCode() {
        return this.node.hashCode();
    }

    public String getCompressedData() {
        return this.node + this.name + this.symbol + (this.material == null ? "" : Integer.valueOf(this.material.ordinal())) + this.priority + this.maxClaims + CastelObject.compressCollection(this.permissions, GuildPermission::hashCode);
    }

    public String toString() {
        return "Rank:{node=" + this.node + ", name=" + this.name + ", priority=" + this.priority + ", color=" + this.color + ", symbol=" + this.symbol + ", maxClaims=" + this.maxClaims + ", material=" + this.material.name() + ", permissions=" + Arrays.toString(this.permissions.toArray()) + '}';
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rank)) return false;
        return this.node.equals(((Rank) obj).node);
    }

    public boolean isHigherThan(Rank rank) {
        Objects.requireNonNull(rank, "Cannot compare null rank");
        return this.priority < rank.priority;
    }

    public boolean isLeader() {
        return this.priority == 0;
    }

    public String getNode() {
        return node;
    }

    protected void setNode(String node) {
        this.node = Objects.requireNonNull(node, "Rank node cannot be null");
    }

    public boolean canBePromoted() {
        return this.priority > 1;
    }

    public XMaterial getMaterial() {
        return material;
    }

    public void setMaterial(XMaterial material) {
        this.material = material;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getMaxClaims() {
        return maxClaims;
    }

    public void setMaxClaims(int maxClaims) {
        this.maxClaims = maxClaims;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        Validate.isTrue(priority >= 0, "Rank priority must be greater than -1");
        this.priority = priority;
    }

    public Set<GuildPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<GuildPermission> permissions) {
        this.permissions = Objects.requireNonNull(permissions, "Rank permissions cannot be null");
    }
}
