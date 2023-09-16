package fr.krishenk.castel.constants.land;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.LandUtil;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Land extends CastelObject<SimpleChunkLocation> {
//    private final transient @NonNull Map<UUID, Invasion> invasions = new ConcurrentHashMap<UUID, Invasion>();
//    private @NonNull Map<SimpleLocation, Turret> turrets = new HashMap<SimpleLocation, Turret>();
    private @NonNull Map<SimpleLocation, ProtectionSign> protectedBlocks = new HashMap<SimpleLocation, ProtectionSign>();
//    private @NonNull Map<SimpleLocation, Structure> structures = new HashMap<SimpleLocation, Structure>();
    private final transient SimpleChunkLocation location;
    private @Nullable UUID guild;
    private @Nullable UUID claimedBy;
    private long since;

    public Land(@NonNull SimpleChunkLocation location, @Nullable UUID guild, @Nullable UUID claimedBy, /*@NonNull Map<SimpleLocation, Structure> structures, @NonNull Map<SimpleLocation, Turret> turrets,*/ @NonNull Map<SimpleLocation, ProtectionSign> protectedBlocks, long since) {
        this.location = location;
//        this.turrets = Objects.requireNonNull(turrets, "Turrets cannot be null");
        this.protectedBlocks = Objects.requireNonNull(protectedBlocks, "Protected blocks cannot be null");
//        this.structures = Objects.requireNonNull(structures, "Structures cannot be null");
        this.guild = guild;
        this.claimedBy = claimedBy;
        this.since = since;
    }

    public Land(@NonNull Guild guild, @NonNull SimpleChunkLocation chunk) {
        this(guild.getId(), chunk);
    }

    public Land(@Nullable UUID guild, @NonNull SimpleChunkLocation chunk) {
        if (Land.getLand(chunk) != null) {
            throw new NullPointerException("Cannot construct a new land object for " + chunk + " for guild " + guild + " becuase it already exists.");
        }
        this.location = chunk;
        this.guild = guild;
        this.since = System.currentTimeMillis();
        CastelDataCenter.get().getLandManager().load(this);
    }

    public Land(@NonNull SimpleChunkLocation chunk) {
        this((UUID)null, chunk);
    }

    public static @Nullable Land getLand(@NonNull Location location) {
        return Land.getLand(SimpleChunkLocation.of(location));
    }

    public static @Nullable Land getLand(@NonNull Chunk chunk) {
        return Land.getLand(SimpleChunkLocation.of(chunk));
    }

    public static @Nullable Land getLand(@NonNull Block block) {
        return SimpleChunkLocation.of(block).getLand();
    }

    public static @Nullable Land getLand(@NonNull SimpleChunkLocation loc) {
        return (Land)CastelDataCenter.get().getLandManager().getData(loc);
    }

    public static @Nullable Land getLand(@NonNull SimpleLocation loc) {
        return Land.getLand(loc.toSimpleChunkLocation());
    }

//    public static @Nullable NationZone getNationZone(SimpleChunkLocation chunk) {
//        int radius = CastelConfig.Invasions.NATIONS_NATION_ZONE_RADIUS.getManager().getInt();
//        if (radius < 1) {
//            return null;
//        }
//        Land mainLand = chunk.getLand();
//        if (mainLand != null && mainLand.isClaimed()) {
//            return null;
//        }
//        return chunk.findFromSurroundingChunks(radius, c -> {
//            Land land = c.getLand();
//            if (land == null) {
//                return null;
//            }
//            Guild guild = land.getGuild();
//            if (guild == null) {
//                return null;
//            }
//            Nation nation = guild.getNation();
//            if (nation != null && nation.getCapitalId().equals(guild.getId())) {
//                return new NationZone(nation, guild, land);
//            }
//            return null;
//        });
//    }

    public static Land validateDistance(SimpleChunkLocation chunk, UUID guild) {
        int distance = Config.Claims.DISTANCE.getManager().getInt();
        if (distance <= 0) {
            return null;
        }
        return chunk.findFromSurroundingChunks(distance, c -> {
            Land land = c.getLand();
            if (land != null && land.isClaimed() && !FastUUID.equals(land.guild, guild)) {
                return land;
            }
            return null;
        });
    }

    public static boolean isConnected(SimpleChunkLocation chunk, Guild guild) {
        if (guild.getLandLocations().isEmpty()) {
            return true;
        }
        int radius = Config.Claims.CONNECTION_RADIUS.getManager().getInt();
        if (radius <= 0) {
            return true;
        }
        return Land.isConnected(chunk, guild, radius);
    }

    public static boolean disconnectsLandsAfterUnclaim(SimpleChunkLocation unclaimed, Guild guild) {
        int radius = Config.Claims.CONNECTION_RADIUS.getManager().getInt();
        if (radius <= 0) {
            return false;
        }
        Set<SimpleChunkLocation> landsInWorld = guild.getLandLocations().stream().filter(x -> x.getWorld().equals(unclaimed.getWorld()) && !x.equalsIgnoreWorld(unclaimed)).collect(Collectors.toSet());
        if (landsInWorld.size() == 1) {
            return false;
        }
        return LandUtil.getConnectedClusters(radius, landsInWorld).size() > 1;
    }

    public static boolean isConnected(SimpleChunkLocation chunk, Guild guild, int radius) {
        String world = chunk.getWorld();
        if (chunk.anySurroundingChunks(radius, guild::isClaimed)) {
            return true;
        }
        return guild.getLandLocations().stream().noneMatch(x -> x.getWorld().equals(world));
    }

    public boolean isHomeLand() {
        Guild guild = this.getGuild();
        if (guild == null) {
            return false;
        }
        Location home = guild.getHome();
        if (home == null) {
            return false;
        }
        return SimpleChunkLocation.of(home).equals(this.location);
    }

    public boolean isClaimed() {
        return this.guild != null;
    }

//    public boolean isNexusLand() {
//        return this.getStructure((Structure structure) -> ((StructureType)((StructureStyle)structure.getStyle()).getType()).isNexus()) != null;
//    }

//    public Structure getStructure(@NonNull Predicate<Structure> predicate) {
//        for (Structure value : this.structures.values()) {
//            if (!predicate.test(value)) continue;
//            return value;
//        }
//        return null;
//    }
//
//    public <T extends Structure> T getStructure(@NonNull Class<T> type) {
//        return (T)this.getStructure(type::isInstance);
//    }

    public int hashCode() {
        int prime = 31;
        int result = 19;
        result = prime * result + this.location.hashCode();
        if (this.guild != null) {
            result = prime * result + this.guild.hashCode();
        }
        if (this.claimedBy != null) {
            result = prime * result + this.claimedBy.hashCode();
        }
//        result = prime * result + this.structures.hashCode();
//        result = prime * result + this.turrets.hashCode();
        result = prime * result + this.protectedBlocks.hashCode();
        result = prime * result + Long.hashCode(this.since);
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Land)) {
            return false;
        }
        Land land = (Land)obj;
        return this.location.equals(land.location) && Objects.equals(this.guild, land.guild) && this.since == land.since && Objects.equals(this.claimedBy, land.claimedBy) /*&& this.structures.equals(land.structures) && this.turrets.equals(land.turrets)*/ && this.protectedBlocks.equals(land.protectedBlocks);
    }

    public boolean simpleEquals(@Nullable Land land) {
        return land != null && (this == land || this.location.equals(land.location));
    }

    public UnclaimLandEvent unclaim(@Nullable CastelPlayer by, UnclaimLandEvent.Reason reason) {
        return this.unclaim(by, reason, true);
    }

    public UnclaimLandEvent unclaim(@Nullable CastelPlayer by, UnclaimLandEvent.Reason reason, boolean history) {
        Guild guild = Objects.requireNonNull(this.getGuild(), () -> "Cannot unclaim " + this.location + " it wasn't claimed by a guild");
        return guild.unclaim(Collections.singleton(this.location), by, reason, history);
    }

    public void silentUnclaim() {
//        for (Structure structure : this.structures.values().toArray(new Structure[0])) {
//            if (!CastelConfig.Structures.REMOVE_UNCLAIMED.getManager().getBoolean() && !((StructureType)((StructureStyle)structure.getStyle()).getType()).isNexus() || this.guild == null) continue;
//            structure.remove(new GuildItemRemoveContext());
//        }
//        if (CastelConfig.Claims.RESTORATION_ENABLED.getManager().getBoolean()) {
//            ChunkSnapshotManager.queueRestoration(this.location);
//        }
        this.guild = null;
        this.since = System.currentTimeMillis();
        this.claimedBy = null;
        if (/*this.structures.isEmpty() && this.turrets.isEmpty() &&*/ this.protectedBlocks.isEmpty() && this.metadata.isEmpty()) {
            CastelPlugin.getInstance().getDataCenter().getLandManager().delete(this.location);
        }
    }

    public @NonNull SimpleChunkLocation getLocation() {
        return this.location;
    }

    @Override
    public @NonNull SimpleChunkLocation getDataKey() {
        return this.location;
    }

    public String toString() {
        return "Land:{location=" + this.location + ", guild=" + this.guild + '}';
    }

    @Override
    public @NonNull String getCompressedData() {
        return Land.compressUUID(this.guild) + Land.compressUUID(this.claimedBy) + this.since /*+ Land.compressCollection(this.structures.values(), GuildItem::hashCode) + Land.compressCollection(this.turrets.values(), GuildItem::hashCode)*/ + Land.compressCollection(this.protectedBlocks.values(), ProtectionSign::getCompressedData) + this.compressMetadata();
    }

    public @Nullable Guild getGuild() {
        if (this.guild == null) {
            return null;
        }
        Guild guildInstance = Guild.getGuild(this.guild);
        if (guildInstance == null) {
            MessageHandler.sendConsolePluginMessage("&4Invalid guild data for land &e" + this.location + " &4removing its data...");
            this.guild = null;
            this.silentUnclaim();
        }
        return guildInstance;
    }

    public void setGuild(@Nullable UUID guild) {
        this.guild = guild;
    }

//    public boolean isBeingInvaded() {
//        return !this.invasions.isEmpty();
//    }

    public @Nullable UUID getGuildId() {
        return this.guild;
    }

//    public @NonNull Map<SimpleLocation, Structure> getStructures() {
//        return this.structures;
//    }

    public @Nullable UUID getClaimedBy() {
        return this.claimedBy;
    }

    public void setClaimedBy(@Nullable UUID claimedBy) {
        this.claimedBy = claimedBy;
    }

    public @Nullable CastelPlayer getClaimer() {
        return this.claimedBy == null ? null : CastelPlayer.getCastelPlayer(this.claimedBy);
    }

//    public @Nullable Map<UUID, Invasion> getInvasions() {
//        return Collections.unmodifiableMap(this.invasions);
//    }
//
//    public void addInvasion(@NonNull Invasion invasion) {
//        UUID id = invasion.getInvader().getGuildId();
//        if (this.invasions.containsKey(id)) {
//            throw new IllegalArgumentException("The guild with ID " + id + " is already invading land at " + this.location + " conflicting between: " + invasion.getInvaderPlayer().getName() + " and " + this.invasions.get(id).getInvaderPlayer().getName());
//        }
//        if (!invasion.getAffectedLands().contains(this.location)) {
//            throw new IllegalArgumentException("The provided invasion isn't affecting this land: " + invasion.getAffectedLands());
//        }
//        this.invasions.put(id, invasion);
//    }
//
//    public void endInvasions(Invasion.Result result) {
//        this.invasions.values().forEach(i -> i.end(result));
//    }
//
//    public Invasion removeInvasion(@NonNull Guild guild) {
//        return this.removeInvasion(guild.getId());
//    }
//
//    public Invasion removeInvasion(@NonNull UUID guild) {
//        return this.invasions.remove(guild);
//    }

    public @NonNull Map<SimpleLocation, ProtectionSign> getProtectedBlocks() {
        return this.protectedBlocks;
    }

    public void setProtectedBlocks(@NonNull Map<SimpleLocation, ProtectionSign> protectedBlocks) {
        this.protectedBlocks = protectedBlocks;
    }

//    public @NonNull Map<SimpleLocation, Turret> getTurrets() {
//        return this.turrets;
//    }
//
//    public void setTurrets(@NonNull Map<SimpleLocation, Turret> turrets) {
//        this.turrets = turrets;
//    }

    public long getSince() {
        return this.since;
    }

    public void setSince(long since) {
        this.since = since;
    }
}


