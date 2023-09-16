package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.general.GroupResourcePointConvertEvent;
import fr.krishenk.castel.events.general.ranks.PlayerRankChangeEvent;
import fr.krishenk.castel.events.members.GuildJoinEvent;
import fr.krishenk.castel.events.members.GuildLeaveEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.managers.land.CastelMap;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CastelPlayer extends CastelObject<UUID> implements Comparable<CastelPlayer>, GuildOperator {
    private final transient LinkedList<ClaimingHistory> landHistory = new LinkedList<>();
    private long joinedAt;
    private UUID guild;
    private final transient UUID uuid;
    //private transient boolean autoMap;
    private transient boolean flying;
    //private transient Boolean autoClaim;
    private boolean admin;
    private boolean pvp;
    private boolean spy;
    private boolean markers;
    private boolean sneakMode;
    //private Set<UUID> readMails;
    private String rank;
    private String markersType;
    //private String chatChannel;
    //private Set<String> mutedChannels;
    private Map<UUID, GuildInvite> invites;
    private Set<SimpleChunkLocation> claims;
    private Set<SimpleLocation> protectedBlocks;
    private transient int landHistoryPosition = -1;
    //private Pair<Integer, Integer> mapSize;
    private SimpleLocation jailCell;
    private double power;
    private long lastPowerCheckup;
    private Pair<Integer, Integer> mapSize;
    private Boolean autoClaim;
    private long totalDonations;
    private long lastDonationTime;
    private long lastDonationAmount;
    private Set<UUID> readMails;
    private transient boolean autoMap = false;

    public CastelPlayer(UUID uuid, UUID guild, long joinedAt) {
        this.uuid = uuid;
        this.guild = guild;
        this.joinedAt = joinedAt;
        this.invites = new NonNullMap<>();
        this.readMails = new HashSet<>();
        this.claims = new HashSet<>();
        this.markers = false;
        //this.chatChannel = "";
        //this.mutedChannels = new HashSet<>();
        if (uuid != null) CastelDataCenter.get().getCastelPlayerManager().load(this);
    }

    public CastelPlayer(long joinedAt, UUID guild, UUID uuid, boolean admin, boolean pvp, boolean spy, boolean markers, boolean sneakMode, String rank, String markersType, long lastDonationTime, long lastDonationAmount, long totalDonations, Set<UUID> readMails, Map<UUID, GuildInvite> invites, Set<SimpleChunkLocation> claims) {
        this.uuid = uuid;
        this.guild = guild;
        this.rank = rank;
        //this.chatChannel = Objects.requireNonNull(chatChannel, "Player chat channel cannot be null");
        this.markersType = markersType;
        this.joinedAt = joinedAt;
        //this.mutedChannels = Objects.requireNonNull(mutedChannels, "Player muted chat channel cannot be null");
        this.lastDonationTime = lastDonationTime;
        this.lastDonationAmount = lastDonationAmount;
        this.totalDonations = totalDonations;
        this.readMails = readMails;
        this.invites = invites;
        this.claims = Objects.requireNonNull(claims, "Player claims cannot be null");
        this.pvp = pvp;
        this.spy = spy;
        this.admin = admin;
        this.markers = markers;
        this.sneakMode = sneakMode;
        //this.mapSize = mapSize;
    }

    public static CastelPlayer getCastelPlayer(OfflinePlayer player) {
        return CastelPlayer.getCastelPlayer(player.getUniqueId());
    }

    public static CastelPlayer getCastelPlayer(UUID uuid) {
        CastelPlayer cp = CastelDataCenter.get().getCastelPlayerManager().getData(uuid);
        return cp == null ? new CastelPlayer(uuid, null, System.currentTimeMillis()) : cp;
    }

    public boolean hasGuild() {
        return this.guild != null;
    }

    /*
    public int countUnreadMails(Map<UUID, Mail> mails) {
        return mails.keySet().stream().filter(m -> !this.readMails.contains(m)).count();
    }*/

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public boolean isInSameGuildAs(CastelPlayer cp) {
        return this.guild != null && cp.guild != null && FastUUID.equals(this.guild, cp.guild);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.uuid);
    }

    @Override
    public Guild getGuild() {
        if (this.guild == null) return null;
        Guild guildInstance = Guild.getGuild(this.guild);
        if (guildInstance == null) {
            CLogger.info("Invaild guild for &e" + this.getOfflinePlayer().getName() + " (" + this.uuid + ") &4 removing player data...");
            this.silentlyLeaveGuild();
            return null;
        }
        return guildInstance;
    }

    public SimpleLocation getJailCell() {
        return this.jailCell;
    }

    public void setJailCell(SimpleLocation location) {
        this.jailCell = location;
    }

    public Rank getRank() {
        Guild guild = this.getGuild();
        if (guild == null) return null;
        if (this.rank == null) return guild.getRanks().getLowestRank();
        Rank rank = guild.getRanks().get(this.rank);
        if (rank != null) return rank;
        rank = guild.getLeaderId().equals(this.uuid) ? guild.getRanks().getHightestRank() : guild.getRanks().getLowestRank();
        this.rank = rank.getNode();
        return rank;
    }

    public void setRankInternal(String rank) {
        this.rank = rank;
    }

    public PlayerRankChangeEvent setRank(CastelPlayer byPlayer, Rank rank) {
        Objects.requireNonNull(rank, "Guild member rank cannot be null");
        Guild guild = this.getGuild();
        if (!guild.getRanks().has(rank.getNode()))
            throw new IllegalArgumentException("The specified rank '" + rank.getNode() + "' is not in guild: " + guild.getName());
        Rank oldRank = this.rank == null ? guild.getRanks().getLowestRank() : guild.getRanks().get(this.rank);
        PlayerRankChangeEvent event = new PlayerRankChangeEvent(oldRank, rank, guild, this, byPlayer);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.rank = rank.getNode();
        return event;
    }

    public String getRankNode() {
        return this.rank;
    }

    public GuildLeaveEvent leaveGuild(LeaveReason reason) {
        Objects.requireNonNull(reason, "Guild leave reason cannot be null");
        Objects.requireNonNull(this.guild, "Player is not a member of a guild to leave");
        GuildLeaveEvent event = new GuildLeaveEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.getGuild().getMembers().remove(this.uuid);
        this.silentlyLeaveGuild();
        return event;
    }

    public void silentlyLeaveGuild() {
        /*if (this.jailCell != null) {
            ...
        }*/

        this.guild = null;
        this.rank = null;
        this.flying = false;
        //this.autoClaim = null;
        //this.chatChannel = CastelChatChannel;
        this.joinedAt = System.currentTimeMillis();
        this.landHistoryPosition = -1;
        this.landHistory.clear();
        this.claims.clear();
        Player player = this.getPlayer();
        if (player != null) this.disableFlying(player);
    }

    public GuildJoinEvent joinGuild(Guild guild) {
        return this.joinGuild(guild, null);
    }

    public GuildJoinEvent joinGuild(Guild guild, Consumer<GuildJoinEvent> modifier) {
        Objects.requireNonNull(guild, "Cannot join a null guild");
        Validate.isTrue(!guild.getId().equals(this.guild), "Player is alreadt in this guild");
        GuildJoinEvent event = new GuildJoinEvent(guild, this);
        if (modifier != null) modifier.accept(event);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.joinedAt = System.currentTimeMillis();
        this.guild = guild.getId();
        guild.getMembers().add(this.uuid);
        return event;
    }

    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CastelPlayer)) return false;
        CastelPlayer cp = (CastelPlayer) obj;
        return FastUUID.equals(this.uuid, cp.uuid);
    }
    public UUID getGuildId() {
        return this.guild;
    }

    public UUID getDataKey() {
        return this.uuid;
    }
/*
    public CastelChatChannel getChatChannel() {
        CastelChatChannel channel;
    }*/

    public boolean hasPermission(GuildPermission permission) {
        return this.hasAnyPermission(permission);
    }

    private boolean hasAnyPermission(GuildPermission ... permissions) {
        if (this.admin) return true;
        Objects.requireNonNull(permissions, "Cannot check null permission");
        if (!this.hasGuild())
            throw new NullPointerException("Cannot check permission of a player that is not in a guild: " + this.uuid + " | " + this.getOfflinePlayer().getName());
        Rank rank = this.getRank();
        for (GuildPermission permission : permissions) {
            if (permission == null || !rank.hasPermission(permission)) continue;
            return true;
        }
        return false;
    }

    public void processClaims(Set<SimpleChunkLocation> lands, boolean claimed, boolean history) {
        if (history) {
            int limit = 10;
            this.landHistoryPosition = this.landHistory.size();
            if (this.landHistory.size() >= limit)
                this.landHistory.removeFirst();
        }
        if (claimed) this.claims.addAll(lands);
        else this.claims.removeAll(lands);
    }

    public ClaimingHistory claimingHistory(boolean undo) {
        if (this.landHistory.isEmpty()) return null;
        if (this.landHistoryPosition <= -1 || this.landHistoryPosition >= this.landHistory.size()) {
            if (undo) {
                if (this.landHistoryPosition - 1 <= -1) return null;
                --this.landHistoryPosition;
            } else {
                if (this.landHistoryPosition + 1 >= this.landHistory.size()) return null;
                ++this.landHistoryPosition;
            }
            return this.landHistory.get(this.landHistoryPosition);
        }
        ClaimingHistory land = this.landHistory.get(this.landHistoryPosition);
        this.landHistoryPosition = undo ? --landHistoryPosition : ++landHistoryPosition;
        return land;
    }

    public LinkedList<ClaimingHistory> getLandHistory() {
        return landHistory;
    }

    public PlayerRankChangeEvent promote(CastelPlayer byPlayer) {
        return this.jumpFromRank(byPlayer, this.getRank(), -1);
    }

    public PlayerRankChangeEvent demote(CastelPlayer byPlayer) {
        return this.jumpFromRank(byPlayer, this.getRank(), 1);
    }

    private PlayerRankChangeEvent jumpFromRank(CastelPlayer byPlayer, Rank rank, int jump) {
        Objects.requireNonNull(rank, "Cannot change rank of a player with no rank");
        int priority = rank.getPriority() + jump;
        Guild guild = this.getGuild();
        Rank newRank = guild.getRanks().getSortedRanks().get(priority);
        if (newRank == null)
            throw new IllegalArgumentException("Cannot jump from rank '" + rank + "' in group '" + guild.getName() + "' with maximum of " + guild.getRanks().size() + ' ' + "ranks (" + guild.getRanks().ranks.values().stream().map(x -> x.getName() + ':' + x.getPriority()).collect(Collectors.toList()));
        if (newRank.isLeader())
            throw new IllegalStateException("Jump from rank " + rank.getNode() + " (" + rank.getPriority() + ") to leader rank" + newRank.getNode() + " (" + newRank.getPriority() + ") (hit: use Guild#setLeader() instead)");
        if (!guild.getRanks().has(rank.getNode()))
            throw new IllegalArgumentException("The old rank '" + rank.getNode() + "' is not in guild: " + guild.getName());
        PlayerRankChangeEvent event = new PlayerRankChangeEvent(rank, newRank, guild, this, byPlayer);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.rank = newRank.getNode();
        return event;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getCompressedData() {
        return CastelPlayer.compressUUID(this.guild) + (this.joinedAt == 0L ? "" : Long.valueOf(this.joinedAt)) + (this.rank == null ? "" : this.rank) + /*(this.mapSize == null ? "" : Integer.valueOf(this.mapSize.getKey() + this.mapSize.getValue())) +*/ CastelPlayer.compressCollection(this.invites.keySet(), CastelObject::compressUUID) + (this.markers ? Character.valueOf('1') : "") + (this.pvp ? Character.valueOf('1') : "") + (this.admin ? Character.valueOf('1') : "") + (this.spy ? Character.valueOf('1') : "") + (this.sneakMode ? Character.valueOf('1') : "") + this.compressMetadata();
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public Map<UUID, GuildInvite> getInvites() {
        return this.invites;
    }

    public void setInvites(Map<UUID, GuildInvite> invites) {
        this.invites = invites;
    }

    public boolean isHigher(CastelPlayer cp) {
        return this.getRank().isHigherThan(cp.getRank());
    }

    public boolean isUsingMarkers() {
        return this.markers;
    }

    public void setUsingMarkers(boolean markers) {
        this.markers = markers;
    }

    public Pair<Integer, Integer> getMapSize() {
        return this.mapSize;
    }

    public void setMapSize(Pair<Integer, Integer> mapSize) {
        this.mapSize = mapSize;
        if (CastelMap.SCOREBOARDS.containsKey(this.uuid) && this.getPlayer() != null)
            this.buildMap().displayAsScoreboard();
    }

    public long getLastPowerCheckup() {
        return lastPowerCheckup;
    }

    public int getLandHistoryPosition() {
        return landHistoryPosition;
    }

    public void setLandHistoryPosition(int landHistoryPosition) {
        Validate.isTrue(landHistoryPosition < this.landHistory.size(), "Land history position cannot be bigger than the land history size");
        this.landHistoryPosition = landHistoryPosition;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public boolean toggleFlight() {
        this.flying = !this.flying;
        return this.flying;
    }

    public void disableFlying(Player player) {
        this.flying = false;
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    public boolean isSpy() {
        return spy;
    }

    public void setSpy(boolean spy) {
        this.spy = spy;
    }

    public String getMarkersType() {
        return markersType;
    }

    public void setMarkersType(String markersType) {
        this.markersType = markersType;
    }

    public Set<SimpleLocation> getProtectedBlocks() {
        return protectedBlocks;
    }

    public void setProtectedBlocks(Set<SimpleLocation> protectedBlocks) {
        this.protectedBlocks = Objects.requireNonNull(protectedBlocks, "Player's protected blocks cannot be null");
    }

    public boolean isInSneakMode() {
        return sneakMode;
    }

    public void setSneakMode(boolean sneakMode) {
        this.sneakMode = sneakMode;
    }

    public void updatePower(Boolean online) {
        if (Config.Powers.POWER_ENABLED.getManager().getBoolean()) {
            OfflinePlayer player = this.getOfflinePlayer();
            long now = System.currentTimeMillis();
            long passed = now - this.lastPowerCheckup;
            this.lastPowerCheckup = now;
            if (online == null) online = player.isOnline();

            double lose;
            double div;
            if (online) {
                long every = Config.Powers.POWER_PLAYER_REGENERATION_EVERY.getManager().getTimeMillis(TimeUnit.MINUTES);
                lose = passed / every;
                if (lose > 0.0) {
                    double charge = MathUtils.eval(Config.Powers.POWER_PLAYER_REGENERATION_CHARGE.getManager().getString(), player);
                    div = MathUtils.eval(Config.Powers.POWER_PLAYER_MAX.getManager().getString(), player);
                    this.power += charge * lose;
                    this.power = Math.min(div, this.power);
                }
            } else {
                double limit = MathUtils.eval(Config.Powers.POWER_PLAYER_LOSS_OFFLINE_MIN.getManager().getString(), player);
                if (this.power > limit) {
                    lose = MathUtils.eval(Config.Powers.POWER_PLAYER_LOSS_OFFLINE_LOSE.getManager().getString(), player);
                    if (lose != 0.0) {
                        long every = Config.Powers.POWER_PLAYER_LOSS_OFFLINE_EVERY.getManager().getTimeMillis(TimeUnit.DAYS);
                        div = passed / every;
                        if (div > 0.0) {
                            this.power += lose * div;
                            this.power = Math.max(limit, this.power);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int compareTo(CastelPlayer other) {
        if (!this.hasGuild()) return other.hasGuild() ? -1 : 0;
        if (!other.hasGuild()) return 1;
        Rank rank = this.getRank();
        Rank otherRank = other.getRank();
        if (otherRank == null) return rank == null ? 0 : 1;
        int comparedRank = rank.compareTo(otherRank);
        if (comparedRank != 0) return comparedRank;
        return Long.compare(this.joinedAt, other.joinedAt);
    }

    public Set<SimpleChunkLocation> getClaims() {
        return this.claims;
    }

    public SupportedLanguage getLanguage() {
        return SupportedLanguage.EN;
    }

    public double getPower() {
        this.updatePower(null);
        return this.power;
    }

    public double addPower(double power) {
        return this.setPower(this.power + power);
    }

    public double setPower(double power) {
        return this.setPower(power, false);
    }

    public double setPower(double power, boolean ignoreLimits) {
        this.lastPowerCheckup = System.currentTimeMillis();
        if (!ignoreLimits) {
            OfflinePlayer player = this.getOfflinePlayer();
            double min = MathUtils.eval(Config.Powers.POWER_PLAYER_MIN.getManager().getString(), player);
            double max = MathUtils.eval(Config.Powers.POWER_PLAYER_MAX.getManager().getString(), player);
            return this.power = Math.max(min, Math.min(max, power));
        }
        return this.power = power;
    }


    public CastelMap buildMap() {
        Player player = Objects.requireNonNull(this.getPlayer(), "Cannot show map to offline player");
        int height;
        int width;
        if (this.mapSize == null) {
            height = Config.Map.HEIGHT.getManager().getInt();
            width = Config.Map.WIDTH.getManager().getInt();
        } else {
            height = this.mapSize.getKey();
            width = this.mapSize.getValue();
        }
        return (new CastelMap()).forPlayer(player).setSize(height, width);
    }

    public Boolean getAutoClaim() {
        return this.autoClaim;
    }

    public void setAutoClaim(Boolean autoClaim) {
        this.autoClaim = autoClaim;
    }

    public boolean hasAnyPermision(GuildPermission ... permissions) {
        if (this.admin) return true;
        Objects.requireNonNull(permissions, "Cannnot checks null permissions");
        if (!this.hasGuild()) throw new NullPointerException("Cannot check permission of a player that is not in a guild: " + this.uuid + " | " + this.getOfflinePlayer().getName());
        Rank rank = this.getRank();
        for (GuildPermission permission : permissions) {
            if (permission != null && rank.hasPermission(permission)) return true;
        }
        return true;
    }

    public GroupResourcePointConvertEvent donate(long amount) {
        return this.donate(amount, null, null);
    }

    public GroupResourcePointConvertEvent donate(Guild guild, long amount) {
        return this.donate(guild, amount, null, null);
    }

    public GroupResourcePointConvertEvent donate(long amount, List<ItemStack> items, List<ItemStack> result) {
        return this.donate(this.getGuild(), amount, items, result);
    }

    public GroupResourcePointConvertEvent donate(Group group, long amount, List<ItemStack> items, List<ItemStack> result) {
        GroupResourcePointConvertEvent donateEvent = new GroupResourcePointConvertEvent(this, items, group, result, amount);
        Bukkit.getPluginManager().callEvent(donateEvent);
        if (donateEvent.isCancelled()) return donateEvent;
        else {
            amount = donateEvent.getAmount();
            this.totalDonations += amount;
            long time = Config.ResourcePoints.LAST_DONATION_DURATION.getManager().getTimeMillis();
            long current = System.currentTimeMillis();
            long diff = current - this.lastDonationTime;
            if (diff >= time) {
                this.lastDonationTime = current;
                this.lastDonationAmount = amount;
            } else this.lastDonationAmount += amount;

            return donateEvent;
        }
    }

    public void readMail(Mail mail) {
        Objects.requireNonNull(mail, "Cannot read null mail");
        this.readMails.add(mail.getId());
    }

    public Set<UUID> getReadMails() {
        this.readMails.removeIf((read) -> !CastelDataCenter.get().getMTG().exists(read));
        return this.readMails;
    }

    public void setReadMails(Set<UUID> readMails) {
        this.readMails = readMails;
    }

    public long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(long totalDonations) {
        this.totalDonations = totalDonations;
    }

    public long getLastDonationAmount() {
        return lastDonationAmount;
    }

    public void setLastDonationAmount(long lastDonationAmount) {
        this.lastDonationAmount = lastDonationAmount;
    }

    public long getLastDonationTime() {
        return lastDonationTime;
    }

    public void setLastDonationTime(long lastDonationTime) {
        this.lastDonationTime = lastDonationTime;
    }

    public void toggleSpy() {
        this.spy = !this.spy;
    }

    public boolean isAutoMap() {
        return this.autoMap;
    }
}
