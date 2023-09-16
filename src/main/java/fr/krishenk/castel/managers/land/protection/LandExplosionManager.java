package fr.krishenk.castel.managers.land.protection;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.events.items.CastelItemRemoveContext;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.internal.integer.IntHashSet;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class LandExplosionManager implements Listener {
    public static final boolean REGENERATE = ReflectionUtils.supports(13) && Config.MiscUpgrades.ANTI_EXPLOSION_AUTO_REGENERATE_ENABLED.getManager().getBoolean();
    private static final double HEIGHT_MIN = Config.MiscUpgrades.ANTI_EXPLOSION_FANCY_EXPLOSIONS_HEIGHT_MIN.getManager().getDouble();
    private static final double HEIGHT_MAX = Config.MiscUpgrades.ANTI_EXPLOSION_FANCY_EXPLOSIONS_HEIGHT_MAX.getManager().getDouble();
    private static final double SPREAD_MIN = Config.MiscUpgrades.ANTI_EXPLOSION_FANCY_EXPLOSIONS_SPREAD_MIN.getManager().getDouble();
    private static final double SPREAD_MAX = Config.MiscUpgrades.ANTI_EXPLOSION_FANCY_EXPLOSIONS_SPREAD_MAX.getManager().getDouble();
    private static final IntHashSet EXPLOSION_BLOCKS = new IntHashSet();
    private static final Map<Integer, List<Entity>> EXPLODED_ENTITIES = new HashMap<>();
    private static final Set<ExplosionHandler> ONGOING_REGENERATIONS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<SimpleLocation, Hanging> BLOCKS_HOLDING_HANGING = new HashMap<>();

    public static void forceOngoingRegenerations() {
        for (ExplosionHandler regen : ONGOING_REGENERATIONS) {
            regen.forceFinishRegeneration();
        }
    }

    private static void fancyExplode(Block block) {
        if (!XMaterial.supports(13) || !block.isPassable()) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector vector = new Vector(random.nextDouble(SPREAD_MIN, SPREAD_MAX), random.nextDouble(HEIGHT_MIN, HEIGHT_MAX), random.nextDouble(SPREAD_MIN, SPREAD_MAX));
            FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), block.getType().createBlockData());
            EXPLOSION_BLOCKS.add(falling.getEntityId());
            falling.setDropItem(false);
            falling.setVelocity(vector);
        }
    }

    private static ItemStack[] getContainerContent(BlockState state) {
        ItemStack[] items = null;
        if (state instanceof InventoryHolder) {
            InventoryHolder inv = (InventoryHolder) state;
            if (inv instanceof Chest) {
                Chest chest = (Chest) inv;
                items = chest.getBlockInventory().getContents();
                chest.getBlockInventory().clear();
            } else {
                items = inv.getInventory().getStorageContents();
                inv.getInventory().clear();
            }
        }
        return items;
    }

    private static boolean isAttachableOrHangingBlock(BlockState state) {
        if (state instanceof Banner) return true;
        else if (state instanceof Sign) return true;
        else {
            XMaterial material = XMaterial.matchXMaterial(state.getType());
            switch (material) {
                case TWISTING_VINES_PLANT:
                case VINE:
                case TWISTING_VINES:
                case WEEPING_VINES_PLANT:
                case WEEPING_VINES:
                case LANTERN:
                case SOUL_LANTERN:
                case LADDER:
                    return true;
                default:
                    String name = material.name();
                    if (name.endsWith("TORCH")) return true;
                    else if (name.endsWith("BUTTON")) return true;
                    else return name.contains("CORAL");
            }
        }
    }

    private static boolean handleEntities(Entity target) {
        if (Config.DISABLED_WORLDS.isInDisabledWorld(target)) return false;
        else if (!MiscUpgrade.ANTI_EXPLOSION.isEnabled()) {
            return false;
        } else {
            SimpleChunkLocation targetChunk = SimpleChunkLocation.of(target.getLocation());
            Land targetLand = targetChunk.getLand();
            if (targetLand == null) return false;
            else {
                Guild guild = targetLand.getGuild();
                if (guild == null) return false;
                else return guild.getMiscUpgrades().getOrDefault(MiscUpgrade.ANTI_EXPLOSION, 0) >= 3;
            }
        }
    }

    private static void breakPaintings(Painting painting, boolean add) {
        int height = painting.getArt().getBlockHeight();
        int width = painting.getArt().getBlockWidth();
        BlockFace facing = painting.getAttachedFace();
        BlockFace leftFace = getFacingOnLeft(facing);
        Block center = painting.getLocation().getBlock().getRelative(facing, 1);
        Block adjustedCenter = (facing == BlockFace.EAST || facing == BlockFace.NORTH) && width > 1 ? center.getRelative(leftFace) : center;
        Block topLeftCorner = height < 3 && width < 3 ? adjustedCenter : adjustedCenter.getRelative(BlockFace.UP, height > 2 ? 1 : 0 ).getRelative(leftFace);

        for (int y = 0; y < height; y++) {
            Block currentY = topLeftCorner.getRelative(BlockFace.DOWN, y);
            for (int x = 0; x < width; x++) {
                Block current = currentY.getRelative(leftFace.getOppositeFace(), x);
                SimpleLocation loc = SimpleLocation.of(current);
                if (add) BLOCKS_HOLDING_HANGING.put(loc, painting);
                else BLOCKS_HOLDING_HANGING.remove(loc);
            }
        }
    }

    private static BlockFace getFacingOnLeft(BlockFace face) {
        switch (face) {
            case EAST: return BlockFace.NORTH;
            case NORTH: return BlockFace.WEST;
            case WEST: return BlockFace.SOUTH;
            case SOUTH: return BlockFace.EAST;
            default:
                throw new AssertionError("Unknown hanging attached face: " + face);
        }
    }

    private static SimpleLocation getSupportingBlock(Hanging hanging) {
        return SimpleLocation.of(hanging.getLocation().add(hanging.getAttachedFace().getDirection()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onFancyExplosionFall(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FALLING_BLOCK) {
            if (EXPLOSION_BLOCKS.remove(entity.getEntityId())) event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void hangingBreak(HangingBreakEvent event) {
        Hanging hanging = event.getEntity();
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (hanging instanceof ItemFrame) {
                BLOCKS_HOLDING_HANGING.remove(getSupportingBlock(hanging));
            } else if (hanging instanceof Painting) {
                breakPaintings((Painting) hanging, false);
            }
        } else {
            if (hanging instanceof ItemFrame) {
                BLOCKS_HOLDING_HANGING.put(getSupportingBlock(hanging), hanging);
            } else if (hanging instanceof Painting) {
               breakPaintings((Painting) hanging, true);
            }

            if (handleEntities(hanging)) event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return;
        }

        if (event.getEntity().getType() != EntityType.ARMOR_STAND)
            protectEntityFromExplosion(event, event.getDamager(), event.getEntity());
    }

    public static void protectEntityFromExplosion(Cancellable event, Entity damager, Entity entity) {
        if (handleEntities(entity)) {
            if (entity != null) event.setCancelled(true);

            if (REGENERATE && entity.hasGravity() && !(entity instanceof Player)) {
                entity.setGravity(false);
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setAI(false);
                    if (XMaterial.supports(17))
                        ((LivingEntity) entity).setInvisible(true);
                }

                EXPLODED_ENTITIES.computeIfAbsent(damager.getEntityId(), (k) -> new ArrayList<>()).add(entity);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onUnknownExplosion(BlockExplodeEvent event) {
        if (!event.blockList().isEmpty()) {
            if (!Config.DISABLED_WORLDS.isInDisabledWorld(event.getBlock()))
                new ExplosionHandler(event, event.blockList(), null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityAntiExplosion(EntityExplodeEvent event) {
        if (event.getEntity() != null) {
            if (!Config.DISABLED_WORLDS.isInDisabledWorld(event.getEntity())) {
                new ExplosionHandler(event, event.blockList(), event.getEntity());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWitherSkullExplosion(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.WITHER ) {
            Land land = Land.getLand(event.getBlock());
            if (land != null) {
                Guild guild = land.getGuild();
                if (guild != null && guild.getUpgradeLevel(MiscUpgrade.ANTI_EXPLOSION) > 1)
                    event.setCancelled(true);
            }
        }
    }

    private static class ExplosionHandler {
        final BlockState[] states;
        final ArrayList<BlockState> requireBaseStates;
        final Map<SimpleLocation, ItemStack[]> containers;
        final boolean antiExplosionEnabled;
        final boolean regenerate;
        final boolean fancyExplosion;
        final boolean protectionSigns;
        final Iterator<Block> iterator;
        final Entity source;
        int stateIndex;
        Block block;
        SimpleLocation location;
        Land land;
        Guild guild;
        BukkitTask regenerationTask;
        boolean baseBlocksBuilt;
        private final CLogger logger;
        private int regulatorCaused;
        private int guildCaused;
        private final Event cause;

        ExplosionHandler(Event cause, List<Block> blocks, Entity source) {
            this.antiExplosionEnabled = MiscUpgrade.ANTI_EXPLOSION.isEnabled();
            this.regenerate = LandExplosionManager.REGENERATE && this.antiExplosionEnabled;
            this.fancyExplosion = XMaterial.supports(13) && Config.MiscUpgrades.ANTI_EXPLOSION_FANCY_EXPLOSIONS_ENABLED.getManager().getBoolean();
            this.protectionSigns = Config.ProtectionSigns.PROTECTIONS_EXPLOSION.getManager().getBoolean();
            this.stateIndex = 0;
            this.logger = new CLogger(CastelDebug.EXPLOSIONS);
            Map<Land, Guild> trusted = new IdentityHashMap<>(9);
            if (this.regenerate) LandExplosionManager.ONGOING_REGENERATIONS.add(this);

            this.cause = cause;
            this.source = source;
            this.iterator = blocks.iterator();
            this.states = this.regenerate ? new BlockState[blocks.size()] : null;
            this.requireBaseStates = this.regenerate ? new ArrayList<>() : null;
            this.containers = this.regenerate ? new HashMap<>() : null;
            int requireLevel = source != null && source.getType() == EntityType.CREEPER ? 0 : 1;
            if (CLogger.isDebugging()) {
                this.logger.property("Source", source);
                this.logger.property("Blocks", blocks.size());
                this.logger.property("Fancy", this.fancyExplosion);
                this.logger.property("Regenerate", this.regenerate);
                this.logger.property("Anti-Explosion", this.antiExplosionEnabled);
            }

            while (this.iterator.hasNext()) {
                this.block = this.iterator.next();
                this.location = SimpleLocation.of(this.block);
                this.land = Land.getLand(this.location);
                this.guild = null;
                if (this.land == null) this.animateBlock();
                else {
                    this.guild = trusted.get(this.land);
                    if (this.guild != null) this.handleTrustedLand();
                    else {
                        this.guild = this.land.getGuild();
                        if (this.guild == null) {
                        } else if (this.protectionSigns && ProtectionSign.isProtected(this.block)) {
                            if (!this.canBreak(BlocType.PROTECTED_BLOCK, () -> "everyone?")) {
                                this.iterator.remove();
                            }
                        }
                        /*else if (this.guild.isPacifist() || this.antiExplosionEnabled && this.guild.getMiscUpgrades().getOrDefault(fr.krishenk.castel.constants.group.upgradable.MiscUpgrade.ANTI_EXPLOSION, 0) > requireLevel) {
                            Regulator
                        }*/
                        else {
                            ++this.guildCaused;
                            this.animateBlock();
                        }
                    }
                }
            }

            if (this.guildCaused > 0) this.logger.property("Blocks broken due to normal causes", this.guildCaused);
            if (this.regulatorCaused > 0 ) this.logger.property("Regulatored", this.regulatorCaused);
            this.logger.property("Left Blocks", blocks.size());
            if (this.regenerate) this.regenerateBlocks();
            else this.logger.end();
        }

        private void animateBlock() {
            if (this.fancyExplosion)
                LandExplosionManager.fancyExplode(this.block);
        }

        void handleTrustedLand() {
            if (this.guild.isPacifist() || !this.canBreak(BlocType.OTHER, () -> XMaterial.matchXMaterial(this.block.getType()).name())) {
                if (this.regenerate && !this.block.getType().name().contains("CHEST")) {
                    if (LandExplosionManager.BLOCKS_HOLDING_HANGING.containsKey(this.location)) {
                        this.iterator.remove();
                        return;
                    }

                    this.animateBlock();
                    this.handleGeneration(this.block);
                } else this.iterator.remove();
            }
        }

        private CastelItemRemoveContext getBreakContext() {
            CastelItemRemoveContext ctx = new CastelItemRemoveContext();
            ctx.setCause(this.cause);
            return ctx;
        }

        boolean canBreak(BlocType blocType, Supplier<String> type) {
            return false;
        }

        void handleGeneration(Block block) {
            BlockState state = block.getState();
            ItemStack[] content = LandExplosionManager.getContainerContent(state);
            if (content != null) this.containers.put(SimpleLocation.of(block), content);

            if (LandExplosionManager.isAttachableOrHangingBlock(state)) this.requireBaseStates.add(state);
            else this.states[this.stateIndex++] = state;

            block.setType(Material.AIR, false);
        }

        void onRegenerationEnd() {
            if (this.regenerate) LandExplosionManager.ONGOING_REGENERATIONS.remove(this);

            this.regenerateEntities();
            this.logger.end();
        }

        void regenerateEntities() {
            if (this.source != null) {
                List<Entity> entities = LandExplosionManager.EXPLODED_ENTITIES.remove(this.source.getEntityId());
                if (entities != null) {
                    for (Entity entity : entities) {
                        entity.setGravity(true);
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).setAI(true);
                            if (XMaterial.supports(17)) {
                                ((LivingEntity) entity).setInvisible(false);
                                ParticleDisplay.of(Particle.CLOUD).withCount(50).offset(1.0).spawn(entity.getLocation().add(0, .5, 0));
                            }
                        }
                    }

                    this.logger.property("Regenerated entities", entities.size());
                }
            }
        }

        public void forceFinishRegeneration() {
            if (this.regenerate && this.regenerationTask != null) {
                this.regenerationTask.cancel();
            }

            if (!this.baseBlocksBuilt) {
                while (this.stateIndex >= 0) {
                    BlockState state = this.states[this.stateIndex];
                    state.update(true);
                    this.handleChest(state);
                    if (--this.stateIndex < 0) {
                        this.baseBlocksBuilt = true;
                        ++this.stateIndex;
                        break;
                    }
                }
            }

            while (this.stateIndex < this.requireBaseStates.size()) {
                this.requireBaseStates.get(this.stateIndex++).update(true);
            }

            this.regenerateEntities();
        }

        void handleChest(BlockState state) {
            if (state instanceof InventoryHolder) {
                InventoryHolder inv = (InventoryHolder) state;
                ItemStack[] content = this.containers.get(SimpleLocation.of(state.getLocation()));
                if (inv instanceof Chest) {
                    Chest chest = (Chest) inv;
                    chest.getBlockInventory().setContents(content);
                } else inv.getInventory().setContents(content);
            }
        }

        void regenerateBlocks() {
            this.baseBlocksBuilt = this.stateIndex == 0;
            if (this.stateIndex == 0 && this.requireBaseStates.isEmpty()) {
                this.onRegenerationEnd();
            } else {
                if (this.stateIndex > 0) {
                    --this.stateIndex;
                }

                Arrays.sort(this.states, (s1, s2) -> {
                    if (s1 == null && s2 == null) return 0;
                    else if (s1 == null) return 1;
                    else return s2 == null ? -1 : Integer.compare(s2.getY(), s1.getY());
                });
                this.logger.log("Starting the regeneration of " + this.states.length + " blocks with " + this.requireBaseStates.size() + " bases.");
                this.regenerationTask = (new BukkitRunnable() {
                    @Override
                    public void run() {
                        BlockState state;
                        if (ExplosionHandler.this.baseBlocksBuilt) {
                            try {
                                state = ExplosionHandler.this.requireBaseStates.get(ExplosionHandler.this.stateIndex++);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }

                            if (!state.update(true)) {
                                MessageHandler.sendConsolePluginMessage("&4Failed to uptadfe state for derived block regeneration: " + state.getBlock());
                            }

                            if (ExplosionHandler.this.stateIndex >= ExplosionHandler.this.requireBaseStates.size()) {
                                this.cancel();
                            }
                        } else {
                            try {
                                state = ExplosionHandler.this.states[ExplosionHandler.this.stateIndex];
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }

                            if (state == null) {
                                if (ExplosionHandler.this.requireBaseStates.isEmpty()) {
                                    this.cancel();
                                } else {
                                    ExplosionHandler.this.baseBlocksBuilt = true;
                                    ExplosionHandler.this.stateIndex = 0;
                                }
                            } else {
                                if (!state.update(true)) {
                                    MessageHandler.sendConsolePluginMessage("&4Failed to update state for block regeneration: " + state.getBlock());
                                }

                                ExplosionHandler.this.handleChest(state);
                                if (--ExplosionHandler.this.stateIndex < 0) {
                                    if (ExplosionHandler.this.requireBaseStates.isEmpty()) this.cancel();
                                    else {
                                        ExplosionHandler.this.baseBlocksBuilt = true;
                                        ++ExplosionHandler.this.stateIndex;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public synchronized void cancel() throws IllegalStateException {
                        super.cancel();
                        ExplosionHandler.this.onRegenerationEnd();
                    }
                }).runTaskTimer(CastelPlugin.getInstance(), Config.MiscUpgrades.ANTI_EXPLOSION_AUTO_REGENERATE_DELAY.getManager().getInt() * 20L, Config.MiscUpgrades.ANTI_EXPLOSION_AUTO_REGENERATE_INTERVAL.getManager().getLong());
            }
        }
    }

    public enum BlocType {
        OTHER,
        PROTECTED_BLOCK;
    }
}
