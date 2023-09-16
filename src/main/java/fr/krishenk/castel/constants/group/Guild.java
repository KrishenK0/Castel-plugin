package fr.krishenk.castel.constants.group;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.ImmutableLocation;
import fr.krishenk.castel.constants.group.model.InviteCode;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.group.model.relationships.RelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.GuildUpgrade;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.RankMap;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.data.managers.GuildManager;
import fr.krishenk.castel.events.general.*;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class Guild extends Group implements GuildOperator {
    private final Set<SimpleChunkLocation> lands;
    private Map<Powerup, Integer> powerups;
    private Map<MiscUpgrade, Integer> miscUpgrades;
    private Map<String, InviteCode> inviteCodes;
    private Map<UUID, Long> challenges;
    private Inventory chest;
    private String lore;
    private boolean pacifist;
    private int maxLandsModifier;

    public Guild(UUID id, UUID leader, String name, String tag, long since, Set<UUID> members, RankMap ranks, long resourcePoints, Location home, boolean publicHome, Color color, double bank, String tax, String flag, Map<UUID, GuildRelationshipRequest> relationshipRequests, Map<UUID, GuildRelation> relations, Map<GuildRelation, Set<RelationAttribute>> attributes, boolean requiresInvite, Set<UUID> mails, boolean permanent, Set<SimpleChunkLocation> lands, Map<Powerup, Integer> powerups, Map<String, InviteCode> inviteCodes, Map<UUID, Long> challenges, Map<Integer, ItemStack> chestItems, String lore, boolean pacifist, int maxLandsModifier, Map<MiscUpgrade, Integer> miscUpgrades) {
        super(id, leader, name, tag, since, members, ranks, resourcePoints, home, publicHome, color, bank, tax, flag, relationshipRequests, relations, attributes, requiresInvite, mails, permanent);
        this.miscUpgrades = miscUpgrades;
        this.inviteCodes = inviteCodes;
        this.challenges = challenges;
        this.lore = lore;
        this.pacifist = pacifist;
        this.maxLandsModifier = maxLandsModifier;
        this.lands = Objects.requireNonNull(lands, "Lands cannot be null");
        Inventory chest = Bukkit.createInventory(null, 9);
        for (Map.Entry<Integer, ItemStack> items : chestItems.entrySet()) {
            chest.setItem(items.getKey(), items.getValue());
        }
        this.chest = chest;
        this.powerups = powerups;
    }

    public InviteCode generateInviteCode(Duration expiration, UUID creator, int uses) {
        String code;
        int minLen = 5;
        int maxLen = 15;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        while (this.inviteCodes.containsKey(code = StringUtils.random(minLen, maxLen, chars)));
        return new InviteCode(code, System.currentTimeMillis(), expiration.toMillis() == 0L ? 0L : TimeUtils.afterNow(expiration), creator, new HashSet<>(), uses);
    }

    public Guild(UUID leader, String name) {
        super(leader, name);
        this.inviteCodes = new HashMap<>();
        this.challenges = new HashMap<>();
        this.pacifist = false;
        this.lands = new HashSet<>();
        this.chest = Bukkit.createInventory(null, 9);
        this.miscUpgrades = GuildUpgrade.getDefaults(MiscUpgrade.values());
        this.powerups = GuildUpgrade.getDefaults(Powerup.values());
        CastelPlayer cp = CastelPlayer.getCastelPlayer(leader);
        cp.joinGuild(this);
        cp.setRankInternal(this.ranks.getHightestRank().getNode());
        GuildCreateEvent event = new GuildCreateEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        CastelDataCenter.get().getGuildManager().load(this);
    }

    public static Guild getGuild(String name) {
        Validate.notEmpty(name, "Guild name cannot be null or empty");
        return CastelDataCenter.get().getGuildManager().getData(name);
    }

    public static Guild getGuild(UUID id) {
        Objects.requireNonNull(id, "Guild ID cannot be null");
        return CastelDataCenter.get().getGuildManager().getData(id);
    }

    public int getMaxMembers() {
        return 12;
    }

    public void updateChest() {
        HumanEntity[] viewers;
        for (HumanEntity player : viewers = this.chest.getViewers().toArray(new HumanEntity[0])) {
            player.closeInventory();
        }
        Inventory chest = Bukkit.createInventory(null, 9);
        for (int i = 0; i < this.chest.getSize(); i++) {
            ItemStack item = this.chest.getItem(i);
            chest.setItem(i, item);
        }
        this.chest = chest;
        for (HumanEntity player : viewers) {
            player.openInventory(chest);
        }
    }

    public boolean isMember(CastelPlayer cp) {
        return this.isMember(cp.getUUID());
    }

    public boolean isMember(OfflinePlayer player) {
        return this.isMember(player.getUniqueId());
    }

    public List<Guild> getGuildWithRelation(GuildRelation ... relations) {
        Objects.requireNonNull(relations, "Cannot get guilds with null relation");
        if (relations.length == 0) return new ArrayList<>();
        ArrayList<Guild> guilds = new ArrayList<>(this.relations.size());
        for (Map.Entry rel : this.relations.entrySet()) {
            if (!Arrays.stream(relations).anyMatch(x -> x == rel.getValue())) continue;
            Guild guild = Guild.getGuild((UUID) rel.getKey());
            if (guild == null)
                CLogger.info("&4Unknown relation with a Guild with a UUID &e" + rel.getKey() + " &4for Guild &e" + this.name + " &8(&e" + this.id + "&8)");
            guilds.add(guild);
        }
        return guilds;
    }

    public Map<String, InviteCode> getInviteCodes() {
        this.inviteCodes.values().removeIf(InviteCode::hasExpired);
        return inviteCodes;
    }

    public void setInviteCodes(Map<String, InviteCode> inviteCodes) {
        this.inviteCodes = inviteCodes;
    }

    public GroupRelationshipRequestEvent sendRelationshipRequest(CastelPlayer sender, Guild guild, GuildRelation relation, long acceptTime) {
        Objects.requireNonNull(guild, "Cannot send relationship to null guild");
        Objects.requireNonNull(guild, "Cannot have null relations");
        GroupRelationshipRequestEvent event = new GroupRelationshipRequestEvent(this, guild, relation, sender);
        if (this.getRelationWith(guild) == relation) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        GuildRelationshipRequest request = new GuildRelationshipRequest(relation, sender == null ? null : sender.getUUID(), acceptTime);
        guild.relationshipRequests.put(this.id, request);
        return event;
    }

    public GroupRelationshipRequestEvent sendRelationshipRequest(CastelPlayer sender, Guild guild, GuildRelation relation) {
        return this.sendRelationshipRequest(sender, guild, relation, 86400000L); // 1d
    }

    @Override
    public UUID getLeaderId() {
        return this.leader;
    }

    @Override
    public List<CastelPlayer> getCastelPlayers() {
        ArrayList<CastelPlayer> players = new ArrayList<>(this.members.size());
        for (UUID id : this.members) {
            players.add(CastelPlayer.getCastelPlayer(id));
        }
        return players;
    }

    public List<Land> getLands() {
        ArrayList<SimpleChunkLocation> corrupted = new ArrayList<>();
        ArrayList<Land> lands = new ArrayList<>(this.lands.size());
        for (SimpleChunkLocation loc : this.lands) {
            Land land = loc.getLand();
            if (land == null || !this.id.equals(land.getGuildId())) {
                MessageHandler.sendConsolePluginMessage("&cDetected unknown land at " + loc + " in guild " + this.name + " (" + this.id + ") Removing now...");
                corrupted.add(loc);
                continue;
            }
            lands.add(land);
        }
        if (!corrupted.isEmpty()) this.lands.removeAll(corrupted);
        return lands;
    }

    public ClaimLandEvent claim(SimpleChunkLocation loc, CastelPlayer claimer, ClaimLandEvent.Reason reason) {
        return this.claim(Collections.singleton(loc), claimer, reason, true);
    }

    public ClaimLandEvent claim(Set<SimpleChunkLocation> locations, CastelPlayer claimer, ClaimLandEvent.Reason reason, boolean history) {
        Objects.requireNonNull(locations, "Cannot claim null location");
        Objects.requireNonNull(reason, "Claiming reason cannot be null");
        if (locations.isEmpty()) throw new IllegalArgumentException("Claim lands list is empty");
        ClaimLandEvent event = new ClaimLandEvent(claimer, this, locations, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        locations = event.getLandLocations();
        for (SimpleChunkLocation loc : locations) {
            Land land = Land.getLand(loc);
            if (land != null)
                Validate.isTrue(!land.isClaimed(), "Land already claimed at: " + loc + " while attempting to claim for " + this.name + " (" + this.id +')');
            else land = new Land(loc);
            land.setGuild(this.id);
            if (claimer != null) land.setClaimedBy(claimer.getUUID());
            land.setSince(System.currentTimeMillis());
            this.lands.add(loc);
        }
        if (claimer != null) claimer.processClaims(locations, true, history);
        return event;
    }

    public UnclaimLandEvent unclaim(Set<SimpleChunkLocation> lands, CastelPlayer player, UnclaimLandEvent.Reason reason, boolean history) {
        if (lands.isEmpty()) throw new IllegalArgumentException("Unclaim lands list is empty");
        UnclaimLandEvent event = new UnclaimLandEvent(player, this, lands, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        lands = event.getLandLocations();
        if (player != null) player.processClaims(lands, false, history);
        for (SimpleChunkLocation location : lands) {
            Land land;
            if (!this.lands.remove(location))
                throw new IllegalArgumentException("This guild did not claim: " + location);
            if (this.home != null && SimpleChunkLocation.of(this.home).equals(location))
                this.setHome(null, player);
            if ((land = Objects.requireNonNull(location.getLand(), () -> "for land " + location + " guild " + this.name)).getClaimedBy() != null) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(land.getClaimedBy());
                cp.getClaims().remove(location);
            }
            land.silentUnclaim();
        }
        return event;
    }

    public UnclaimLandEvent unclaimIf(CastelPlayer player, UnclaimLandEvent.Reason reason, Predicate<Land> filter) {
        Objects.requireNonNull(reason, "Unclaim reason cannot be null");
        HashSet<SimpleChunkLocation> unclaimed = new HashSet<>();
        for (Land land : this.getLands()) {
            if (filter != null && !filter.test(land)) continue;
            unclaimed.add(land.getLocation());
        }
        return this.unclaim(unclaimed, player, reason, true);
    }

    public boolean isClaimed(SimpleChunkLocation location) {
        return this.lands.contains(location);
    }

    public Set<SimpleChunkLocation> getLandLocations() {
        return Collections.unmodifiableSet(this.lands);
    }

    public Set<SimpleChunkLocation> unsafeGetLandLocations() {
        return this.lands;
    }

    @Override
    public Set<UUID> unsafeGetMembers() {
        return this.members;
    }

    @Override
    public List<Player> getOnlineMembers() {
        ArrayList<Player> players = new ArrayList<>(this.members.size());
        for (UUID id : this.members) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) continue;
            players.add(player);
        }
        return players;
    }

    @Override
    public List<OfflinePlayer> getPlayerMembers() {
        ArrayList<OfflinePlayer> players = new ArrayList<>(this.members.size());
        for (UUID member : this.members) {
            players.add(Bukkit.getOfflinePlayer(member));
        }
        return players;
    }

    @Override
    public GroupRenameEvent rename(String name, CastelPlayer player) {
        Validate.notEmpty(name, "Cannot rename guild to null or empty name");
        GroupRenameEvent event = new GroupRenameEvent(name, player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        CastelDataCenter.get().getGuildManager().renameGuild(this, name);
        this.setName(name);
        return event;
    }

    public GuildRelation getRelationWith(Guild guild) {
        if (guild == null) return GuildRelation.NEUTRAL;
        if (this.id.equals(guild.id)) return GuildRelation.SELF;
        GuildRelation relation = this.relations.get(guild.id);
        return relation == null ? GuildRelation.NEUTRAL : relation;
    }

    @Override
    public GuildDisbandEvent triggerDisbandEvent(GroupDisband.Reason reason) {
        GuildDisbandEvent event = new GuildDisbandEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Override
    public GuildDisbandEvent disband(GroupDisband.Reason reason) {
        GuildDisbandEvent event = null;
        if (reason != null && (event = this.triggerDisbandEvent(reason)).isCancelled()) {
            return event;
        }
        for (UUID uuid : this.members) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(uuid);
            cp.silentlyLeaveGuild();
        }
        this.members.clear();
        for (Map.Entry<UUID, GuildRelation> entry : this.relations.entrySet()) {
            Guild rel = Guild.getGuild(entry.getKey());
            if (rel == null) continue;
            rel.relations.remove(this.id);
        }
        GuildManager manager = CastelDataCenter.get().getGuildManager();
        manager.remove(this);
        manager.delete(this.id);
        return event;
    }

    public boolean hasAttribute(Guild guild, RelationAttribute attribute) {
        return attribute.hasAttribute(this, guild);
    }

    public CastelPlayer getLeader() {
        return CastelPlayer.getCastelPlayer(this.leader);
    }

    public GuildLeaderChangeEvent setLeader(CastelPlayer newLeader, GuildLeaderChangeEvent.Reason reason) {
        Objects.requireNonNull(newLeader, "Cannot set a null player as a leader");
        Objects.requireNonNull(reason);
        GuildLeaderChangeEvent event = new GuildLeaderChangeEvent(this, newLeader, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        newLeader = event.getNewLeader();
        newLeader.setRank(null, this.ranks.getHightestRank());
        CastelPlayer oldLeader = CastelPlayer.getCastelPlayer(this.leader);
        oldLeader.demote(newLeader);
        this.leader = newLeader.getUUID();
        return event;
    }

    public void updateRankNode(String node, String newNode) {
        this.ranks.updateNode(node, newNode);
        for (CastelPlayer player : this.getCastelPlayers()) {
            if (!node.equals(player.getRankNode())) continue;
            player.setRankInternal(newNode);
        }
    }

    @Override
    public String getCompressedData() {
        return super.getCompressedData() +
                compressString(this.lore) +
//                compressString(this.championType) +
//                compressUUID(this.nation) +
                this.maxLandsModifier +
//                this.lastInvasion +
                compressBoolean(this.pacifist) +
                compressCollection(this.miscUpgrades.values(), Object::toString) +
//                compressCollection(this.championUpgrades.values(), Object::toString) +
                compressCollection(this.powerups.values(), Object::toString) +
                compressMap(this.challenges, 0, UUID::hashCode, Object::toString) +
                compressInventory(this.chest) +
//                compressMap(this.nationInvites, this.nationInvites.size(), KingdomsObject::compressUUID, KingdomRequest::hashCode) +
                compressCollection(this.lands, SimpleChunkLocation::getCompressedData) +
                compressCollection(this.inviteCodes.values(), (x) -> x.getCode() + x.getUses() + x.getUsedBy().hashCode() + x.getCreatedAt() + x.getExpiration() + x.getCreatedBy());
    }

    @Override
    public String toString() {
        return "Guild[ID:" + this.id + ", Name: "+ this.name +']';
    }

    public GuildSetHomeEvent setHome(Location home, CastelPlayer player) {
        GuildSetHomeEvent event = new GuildSetHomeEvent(home, this, player);
        if(Objects.equals(this.home, home)) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) this.home = ImmutableLocation.of(event.getNewLocation());
        return event;
    }

    public String getLore() {
        return lore;
    }

    public GuildLoreChangeEvent setLore(String lore, CastelPlayer player) {
        GuildLoreChangeEvent event = new GuildLoreChangeEvent(lore, this, player);
        if (Objects.equals(this.lore, lore)) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.lore = event.getNewLore();
        return event;
    }

    public int getMaxLandsModifier() {
        return maxLandsModifier;
    }

    public void setMaxLandsModifier(int maxLandsModifier) {
        this.maxLandsModifier = maxLandsModifier;
    }

    public boolean isPacifist() {
        return pacifist;
    }

    public void setPacifist(boolean pacifist) {
        this.pacifist = pacifist;
    }

    public Inventory getChest() {
        return chest;
    }

    public void setChest(Inventory chest) {
        this.chest = chest;
    }

    @Override
    public double getMight() {
        return 0; // Resource point + Members + ... ?
    }

    @Override
    public String getTaxOrDefault() {
        return this.tax == null ? "(money * 5) / 100" : this.tax;
    }

    @Override
    public double calculateTax() {
        String taxEquation = "5 * (guilds_members + 1) * (guilds_lands + 1) + pacifism_factor";
        // Calculate with Math + eval
        return 100;
    }

    public double getTax(OfflinePlayer player) {
        if (!ServiceHandler.bankServiceAvailable()) {
            return 0.0;
        }
        double money = ServiceVault.getMoney(player);
        try {
            // return MathUtils.eval(this.getTaxOrDefault(), player, "money", money, "money", money);
        } catch (Exception e) {
            this.tax = "(money * 5) / 100";
            try {
                // return MathUtils.eval(this.tax, player, "money", money, "money", money);
            } catch (Exception exFatal) {
                CLogger.info("&cCould not use the default tax equation to get the tax for &e"+player.getName() + " (" + player.getUniqueId()  + ") " + ' ' + "&cin guild &e" + this.name + " (" + this.id + "):");
                e.printStackTrace();
                exFatal.printStackTrace();
                return 0.0;
            }
        }
        return 0.0;
    }

    public Pair<Boolean, Double> payTaxes(OfflinePlayer player) {
        double amount = this.getTax(player);
        if (amount == 0.0) return Pair.of(true, 0.0);
        double money = ServiceVault.getMoney(player);
        if (money - amount < 0.0) {
            this.bank += money;
            ServiceVault.withdraw(player, money);
            return Pair.of(false, money);
        }
        double limit = 1000000D;
        if (this.bank + amount > limit) {
            amount = limit - this.bank;
        }
        this.bank += amount;
        ServiceVault.withdraw(player, amount);
        return Pair.of(true, amount);
    }

    public Map<UUID, Long> getChallenges() {
        if (this.challenges.isEmpty()) return this.challenges;
        long duration = 21600000L; // 6 hrs
        Iterator<Long> iter = this.challenges.values().iterator();
        while (iter.hasNext()) {
            long passed;
            long starts = iter.next();
            long now = System.currentTimeMillis();
            if (now < starts || (passed = now - starts) <= duration) continue;
            iter.remove();
        }
        return this.challenges;
    }

    public void setChallenges(Map<UUID, Long> challenges) {
        this.challenges = challenges;
    }

    @Override
    public Guild getGuild() {
        return this;
    }

    @Override
    public @Nullable Group getGroup() {
        return this;
    }

    public Integer getMaxClaims() {
        return this.getMaxClaims("default");
    }

    public Integer getMaxClaims(String world) {
        String maxClaimsEqn = Objects.requireNonNull(Config.Claims.MAX_CLAIMS.getManager().forWorld(world).getString(), "Max claims equation not found");
        double max = MathUtils.eval(maxClaimsEqn, this, "max_claims", 1);
        if (max < 0)
            throw new IllegalStateException("Max claims has evaluated to a negative number " + max + " for '" + this.name + "' guild.");
        return (int) max;
    }

    public boolean canBeOverclaimed() {
        return this.getPower() < this.getMaxClaims();
    }

    public double getPower() {
        double power = 0.0;
        UUID id;
        for (Iterator<UUID> it = this.members.iterator(); it.hasNext(); power += CastelPlayer.getCastelPlayer(id).getPower()) {
            id = it.next();
        }
        return power;
    }

    public int getUpgradeLevel(GuildUpgrade upgrade) {
        if (upgrade instanceof Powerup) {
            return this.powerups.getOrDefault(upgrade, 0);
        } else if (upgrade instanceof MiscUpgrade) {
            return this.miscUpgrades.getOrDefault(upgrade, 0);
        } else throw new IllegalArgumentException("Unknown upgrade type: " + upgrade);
    }

    public void setUpgradeLevel(GuildUpgrade upgrade, int newLevel) {
        if (upgrade instanceof Powerup) {
            this.powerups.put((Powerup) upgrade, newLevel);
        } else if (upgrade instanceof MiscUpgrade) {
            this.miscUpgrades.put((MiscUpgrade) upgrade, newLevel);
        } else throw new UnsupportedOperationException("Unknown guild upgrade: " + upgrade + " (" + upgrade.getClass().getName() + ')');
    }

    public boolean isStrongerThan(Guild other) {
        return ConditionProcessor.process(ConditionalCompiler.compile("(guilds_claims - guilds_other_claims > 50)").evaluate(), (new PlaceholderContextBuilder()).withContext(this).other(other));
    }

    public Map<Powerup, Integer> getPowerups() {
        return this.powerups;
    }

    public void setPowerups(Map<Powerup, Integer> powerups) {
        this.powerups = Objects.requireNonNull(powerups, "Powerups cannot be null");
    }

    public Map<MiscUpgrade, Integer> getMiscUpgrades() {
        return this.miscUpgrades;
    }

    public void setMiscUpgrades(Map<MiscUpgrade, Integer> miscUpgrades) {
        this.miscUpgrades = Objects.requireNonNull(miscUpgrades, "Misc upgrades cannot be null");
    }
}
