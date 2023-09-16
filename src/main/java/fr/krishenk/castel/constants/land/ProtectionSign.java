package fr.krishenk.castel.constants.land;

import com.google.common.base.Strings;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.libs.xseries.XBlock;
import fr.krishenk.castel.utils.BCrypt;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.internal.ExpirableSet;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.versionsupport.VersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProtectionSign extends CastelObject<SimpleLocation> {
    private static final long COOKIES = 21600000L; // 6 hours
    private final transient @NonNull SimpleLocation location;
    private final @NonNull SimpleLocation sign;
    private final long since;
    private @Nullable String password;
    private @NonNull UUID owner;
    private @NonNull Map<UUID, Boolean> players;
    private @NonNull Map<UUID, Boolean> guilds;
    private transient @NonNull ExpirableSet<UUID> temporarilyTrusted = new ExpirableSet(COOKIES, TimeUnit.MILLISECONDS, true);
    private ProtectionType protectionType;

    public ProtectionSign(@NonNull SimpleLocation location, @NonNull SimpleLocation sign, @NonNull UUID owner, @NonNull ProtectionType protectionType, @Nullable String password, long since, @NonNull Map<UUID, Boolean> players, @NonNull Map<UUID, Boolean> guilds) {
        this.location = Objects.requireNonNull(location, "Protection Sign location cannot be null");
        this.sign = Objects.requireNonNull(sign, "Protection Sign's sign location cannot be null");
        this.owner = Objects.requireNonNull(owner, "Protection Sign owner cannot be null");
        this.protectionType = Objects.requireNonNull(protectionType, "Protection type cannot be null");
        this.password = password;
        this.since = since;
        this.players = Objects.requireNonNull(players, "Protection Sign players cannot be null");
        this.guilds = Objects.requireNonNull(guilds, "Protection Sign guilds cannot be null");
    }

    public static boolean isProtected(@NonNull Block block) {
        return ProtectionSign.getProtection(block).isPresent();
    }

    public static boolean canBlockBeProtected(@NonNull Block block) {
        return XBlock.isOneOf(block, Arrays.asList(new String[]{"CHEST", "TRAPPED_CHEST", "FURNACE", "BLAST_FURNACE", "ENCHANTING_TABLE", "BARREL", "BEACON", "BREWING_STAND", "CHEST", "COMPOSTER", "GRINDSTONE", "LECTERN", "LOOM", "SMOKER", "STONECUTTER", "HOPPER", "DROPPER", "DISPENSER", "CONTAINS:SHULKER_BOX", "CONTAINS:DOOR"}));
    }

    public static @NonNull ProtectionSign placeProtection(@Nullable Land land, @NonNull Block block, @NonNull Block sign, @NonNull Player player, ProtectionType protectionType) {
        SimpleLocation location = SimpleLocation.of(block);
        ProtectionSign protection = new ProtectionSign(location, SimpleLocation.of(sign), player.getUniqueId(), protectionType, null, System.currentTimeMillis(), new HashMap<UUID, Boolean>(), new HashMap<UUID, Boolean>());
        if (land == null) {
            land = new Land(location.toSimpleChunkLocation());
        }
        land.getProtectedBlocks().put(location, protection);
        return protection;
    }

    public boolean shouldRemoveProtectionAfterBreak(Block block) {
        return !(block.getState() instanceof Chest) || SimpleLocation.of(block).equalsIgnoreWorld(this.location);
    }

    public static @NonNull Optional<ProtectionSign> getProtection(@NonNull Block block) {
        Objects.requireNonNull(block, "Cannot get protection of a null block");
        if (ProtectionSign.isSign(block)) {
            if ((block = VersionSupport.getAttachedBlock(block)) == null) {
                return Optional.empty();
            }
            SimpleLocation location = SimpleLocation.of(block);
            Land land = location.toSimpleChunkLocation().getLand();
            if (land == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(land.getProtectedBlocks().get(location));
        }
        SimpleLocation location = SimpleLocation.of(block);
        Land land = location.toSimpleChunkLocation().getLand();
        if (land == null) {
            return Optional.empty();
        }
        ProtectionSign protection = land.getProtectedBlocks().get(location);
        if (protection == null) {
            Inventory inventory;
            BlockState state = block.getState();
            Block otherHalf = VersionSupport.getOtherHalfIfDoor(state);
            if (otherHalf != null) {
                protection = land.getProtectedBlocks().get(SimpleLocation.of(otherHalf));
            } else if (state instanceof Chest && (inventory = ((Chest)state).getInventory()) instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest)inventory.getHolder();
                Chest leftChest = (Chest)doubleChest.getLeftSide();
                Chest rightChest = (Chest)doubleChest.getRightSide();
                SimpleChunkLocation mainChunk = SimpleChunkLocation.of(block);
                Block rightBlock = rightChest.getBlock();
                SimpleChunkLocation otherChunk = SimpleChunkLocation.of(block = LocationUtils.equalsIgnoreWorld(block, rightBlock) ? leftChest.getBlock() : rightBlock);
                if (!otherChunk.equalsIgnoreWorld(mainChunk) && (land = otherChunk.getLand()) == null) {
                    return Optional.empty();
                }
                location = SimpleLocation.of(block);
                protection = land.getProtectedBlocks().get(location);
            }
        }
        return Optional.ofNullable(protection);
    }

    public static boolean isSign(@NonNull Block block) {
        return block.getType().name().endsWith("SIGN");
    }

    public boolean isContainer() {
        BlockState state = this.getBlock().getState();
        return state instanceof Container;
    }

    public long getSince() {
        return this.since;
    }

    public int hashCode() {
        int prime = 31;
        int result = 14;
        result = prime * result + this.location.hashCode();
        result = prime * result + this.owner.hashCode();
        result = prime * result + this.players.hashCode();
        result = prime * result + this.guilds.hashCode();
        result = prime * result + this.protectionType.ordinal();
        return result;
    }

//    public void updateSign() {
//        List<String> lines = ProtectionSignManager.getProtectionTypeSignLines(this.protectionType);
//        Block signBlock = this.sign.getBlock();
//        Sign state = (Sign)signBlock.getState();
//        MessageBuilder settings = new MessageBuilder().withContext(this.getOwnerPlayer());
//        for (int i = 0; i < lines.size(); ++i) {
//            String line = MessageCompiler.compile(lines.get(i)).buildPlain(settings);
//            state.setLine(i, line);
//        }
//        state.update();
//    }

    @Override
    public @NonNull SimpleLocation getDataKey() {
        return this.location;
    }

    @Override
    public @NonNull String getCompressedData() {
        return FastUUID.toString(this.owner) + this.protectionType.ordinal() + (this.password == null ? "" : this.password) + CastelObject.compressMap(this.players, this.players.size() * 16, FastUUID::toString, v -> v != null ? Character.valueOf('1') : "") + CastelObject.compressMap(this.guilds, this.guilds.size() * 16, FastUUID::toString, v -> v != null ? Character.valueOf('1') : "");
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProtectionSign)) {
            return false;
        }
        ProtectionSign sign = (ProtectionSign)obj;
        return this.protectionType == sign.protectionType && this.owner.equals(sign.owner) && this.location.equals(sign.location);
    }

    public boolean isOwner(@NonNull OfflinePlayer player) {
        return this.owner.equals(player.getUniqueId());
    }

    public @NonNull ProtectionSign removeProtection() {
        Land land = this.location.toSimpleChunkLocation().getLand();
        return land.getProtectedBlocks().remove(this.location);
    }

    public @NonNull UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@NonNull UUID owner) {
        this.owner = Objects.requireNonNull(owner, "Protection Sign owner cannot be null");
    }

    public @NonNull OfflinePlayer getOwnerPlayer() {
        return Bukkit.getOfflinePlayer((UUID)this.owner);
    }

    public @NonNull Map<UUID, Boolean> getPlayers() {
        return this.players;
    }

    public void setPlayers(@NonNull Map<UUID, Boolean> players) {
        this.players = Objects.requireNonNull(players, "Protection Sign players cannot be null");
    }

    public @NonNull Map<UUID, Boolean> getGuilds() {
        return this.guilds;
    }

    public void setGuilds(@NonNull Map<UUID, Boolean> guilds) {
        this.guilds = Objects.requireNonNull(guilds, "Protection Sign guilds cannot be null");
    }

    public @NonNull SimpleLocation getSign() {
        return this.sign;
    }

    public AccessResult canAccess(@NonNull OfflinePlayer player) {
        return this.canAccess(player.getUniqueId());
    }

    public AccessResult canAccess(@NonNull UUID player) {
        Boolean guildAllowed;
        Objects.requireNonNull(player, "Cannot check protected block access for a null player");
        if (this.owner.equals(player)) {
            return AccessResult.ACCEPTED;
        }
        Boolean allowed = this.players.get(player);
        if (allowed != null) {
            return allowed ? AccessResult.ACCEPTED : AccessResult.DENIED;
        }
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (cp.hasGuild() && (guildAllowed = this.guilds.get(cp.getGuildId())) != null) {
            return guildAllowed ? AccessResult.ACCEPTED : AccessResult.DENIED;
        }
        if (this.temporarilyTrusted.contains(player)) {
            return AccessResult.ACCEPTED;
        }
        switch (this.protectionType) {
            case PROTECTED: {
                return this.hasPassword() ? AccessResult.PASSWORD : AccessResult.DENIED;
            }
            case EVERYONE: {
                return AccessResult.ACCEPTED;
            }
            case EVERYONE_IN_GUILD: {
                CastelPlayer ownerCp = CastelPlayer.getCastelPlayer(this.owner);
                return Objects.equals(ownerCp.getGuildId(), cp.getGuildId()) ? AccessResult.ACCEPTED : (this.hasPassword() ? AccessResult.PASSWORD : AccessResult.DENIED);
            }
        }
        throw new AssertionError("Unhandled protection type: " + this.protectionType.name());
    }

    public @NonNull ProtectionType getProtectionType() {
        return this.protectionType;
    }

    public void setProtectionType(@NonNull ProtectionType protectionType) {
        this.protectionType = Objects.requireNonNull(protectionType, "Protection type cannot be null");
    }

    public @NonNull Land getLand() {
        return Objects.requireNonNull(this.location.toSimpleChunkLocation().getLand(), "Unexpected null land data for a protected block");
    }

    public @NonNull SimpleLocation getLocation() {
        return this.location;
    }

    public @NonNull Block getBlock() {
        return this.location.toBukkitLocation().getBlock();
    }

    public @NonNull ExpirableSet<UUID> getTemporarilyTrusted() {
        return this.temporarilyTrusted;
    }

    public void setTemporarilyTrusted(@NonNull ExpirableSet<UUID> temporarilyTrusted) {
        this.temporarilyTrusted = Objects.requireNonNull(temporarilyTrusted, "Temporarily trusted protection sign cannot be null");
    }

    public @Nullable String getPassword() {
        return this.password;
    }

    public void setHashedPassword(@Nullable String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be empty");
        }
        this.password = password;
    }

    public void changePassword(@Nullable String password) {
        this.temporarilyTrusted.clear();
        this.password = password == null ? null : this.hashPassword(password);
    }

    public boolean verifyPassword(@NonNull String password) {
        if (Strings.isNullOrEmpty((String)password)) {
            throw new IllegalArgumentException("Cannot check null or empty password: " + password);
        }
        Objects.requireNonNull(this.password, "Cannot check password for a chest with no password");
        return BCrypt.checkpw(password, this.password);
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean hasPassword() {
        return this.password != null;
    }

    public enum ProtectionType {
         PROTECTED,
        EVERYONE_IN_GUILD,
         EVERYONE;

        public String getDisplayname() {
            //ConfigAccessor config = KingdomsConfig.PROTECTION_SIGNS.accessor();
            switch (this) {
                case PROTECTED: {
                    return "Protected";
                }
                case EVERYONE_IN_GUILD: {
                    return "Everyone in Guild";
                }
                case EVERYONE: {
                    return "Everyone";
                }
            }
            throw new AssertionError("Unhandled protection type displayname: " + this.name());
        }
    }

    public enum AccessResult {
         ACCEPTED,
         DENIED,
         PASSWORD
    }
}

