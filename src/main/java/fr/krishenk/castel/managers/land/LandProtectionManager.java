package fr.krishenk.castel.managers.land;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.ranks.RankDeleteEvent;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XBlock;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.XTag;
import fr.krishenk.castel.managers.protectionsign.ProtectionSignManager;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.List;
import java.util.Optional;

public class LandProtectionManager implements Listener {
    public LandProtectionManager() {
        if (XMaterial.supports(16))
            Bukkit.getPluginManager().registerEvents(new BerryBushHandler(), CastelPlugin.getInstance());
    }

    private static boolean hasBuildPerms(Land land, CastelPlayer cp, Player player) {
        StandardGuildPermission permission = StandardGuildPermission.BUILD;
        if (cp.hasPermission(permission)) return true;
        else if (cp.hasPermission(StandardGuildPermission.BUILD_OWNED)) {
            if (land.getClaimedBy().equals(cp.getUUID())) return true;
            else {
                Lang.LANDS_BUILD_OWN_ONLY.sendError(player);
                return false;
            }
        } else {
            permission.sendDeniedMessage(player);
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankDelete(RankDeleteEvent event) {
        Group group = event.getGroup();
        if (group instanceof Guild) {
            Guild guild = (Guild) group;
            String node = event.getRank().getNode();
            for (CastelPlayer member : guild.getCastelPlayers()) {
                if (member.getRankNode().equals(node)) member.setRankInternal(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockChange(EntityChangeBlockEvent event) {
        switch (event.getEntityType()) {
            case WITHER:
            case FALLING_BLOCK:
            case VILLAGER:
                return;
            default:
                Land land = Land.getLand(event.getBlock());
                if (land != null) {
                    Guild landGuild = land.getGuild();
                    if (landGuild != null) {
                        if (event.getEntity() instanceof Player) {
                            Player player = (Player) event.getEntity();
                            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                            Guild playerGuild = cp.getGuild();
                            if (landGuild.hasAttribute(playerGuild, StandardRelationAttribute.BUILD)) return;
                        }

                        if (landGuild.getUpgradeLevel(MiscUpgrade.ANTI_TRAMPLE) > 1)
                            event.setCancelled(true);
                    }
                }
        }
    }

    public static boolean buildInClaimedOnly(Player player, CastelPlayer cp, SimpleChunkLocation chunk, boolean placing) {
        Config.Claims enabledOption = placing ? Config.Claims.BUILD_IN_CLAIMED_ONLY_PLACE : Config.Claims.BUILD_IN_CLAIMED_ONLY_BREAK;
        if (!enabledOption.getManager().getBoolean()) return false;
        boolean charges = Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_ENABLED.getManager().getBoolean();
        int unclaimedBuildRadius = Config.Claims.BUILD_IN_CLAIMED_ONLY_UNCLAIMED_BUILD_RADIUS.getManager().getInt();
        if (!charges && unclaimedBuildRadius <= 0) {
            (placing ? Lang.IN_CLAIM_ONLY_PLACING_GENERAL : Lang.IN_CLAIM_ONLY_BREAKING_GENERAL).sendError(player);
            return true;
        } else {
            Guild guild = cp.getGuild();
            boolean isInRange;
            if (unclaimedBuildRadius <= 0) isInRange = false;
            else {
                if (guild == null) {
                    (placing ? Lang.IN_CLAIM_ONLY_PLACING_GENERAL : Lang.IN_CLAIM_ONLY_BREAKING_GENERAL).sendError(player);
                    return true;
                }

                isInRange = chunk.findFromSurroundingChunks(unclaimedBuildRadius, false, (currentChunk) -> guild.isClaimed(currentChunk) ? true : null);
            }

            if (!charges) {
                if (!isInRange) {
                    (placing ? Lang.IN_CLAIM_ONLY_PLACING_RADIUS : Lang.IN_CLAIM_ONLY_BREAKING_RADIUS).sendError(player, "radius", unclaimedBuildRadius);
                    return true;
                }
                return false;
            } else {
                boolean inRange = Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_IN_RANGE.getManager().getBoolean();
                if (inRange == isInRange) return chargeBuilding(player, guild, placing);
                else if (isInRange) return false;
                else {
                    (placing ? Lang.IN_CLAIM_ONLY_PLACING_RADIUS : Lang.IN_CLAIM_ONLY_BREAKING_RADIUS).sendError(player, "radius", unclaimedBuildRadius);
                    return true;
                }
            }
        }
    }

    public static boolean chargeBuilding(Player player, Guild guild, boolean placing) {
        Config.Claims rpOpt = placing ? Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_PLACING_RESOURCE_POINTS : Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_BREAKING_RESOURCE_POINTS;
        Config.Claims moneyOpt = placing ? Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_PLACING_MONEY : Config.Claims.BUILD_IN_CLAIMED_ONLY_CHARGES_BREAKING_MONEY;
        long rp = rpOpt.getManager().getLong();
        double money = moneyOpt.getManager().getDouble();
        Runnable notifier = () -> (placing ? Lang.IN_CLAIM_ONLY_PLACING_CHARGES : Lang.IN_CLAIM_ONLY_BREAKING_CHARGES).sendError(player, "money", StringUtils.toFancyNumber(rp), "rp", StringUtils.toFancyNumber(rp));
        if (rp > 0L && guild == null) {
            notifier.run();
            return true;
        } else if (rp != 0L && guild != null && !guild.hasResourcePoints(rp)) {
            notifier.run();
            return true;
        } else if (money != 0L && ServiceHandler.bankServiceAvailable() && !ServiceVault.hasMoney(player, money)) {
            notifier.run();
            return true;
        } else {
            if (rp != 0L) guild.addResourcePoints(-rp);

            if (money != 0.0 && ServiceHandler.bankServiceAvailable())
                ServiceVault.withdraw(player, money);

            return false;
        }
    }

    private static boolean handleBuilding(Player player, CastelPlayer cp, SimpleChunkLocation chunk, boolean placing) {
        if (cp.isAdmin()) return false;
        else if ((placing ? CastelPluginPermission.LAND_BUILD_PLACE : CastelPluginPermission.LAND_BUILD_BREAK).hasPermission(player))
            return false;
        else {
            Land land = chunk.getLand();
            if (land != null && land.isClaimed()) {
                Guild guild = cp.getGuild();
                if (guild != null && !hasBuildPerms(land, cp, player)) return true;
                else if (!StandardRelationAttribute.BUILD.hasAttribute(guild, land.getGuild())) {
                    (placing ? Lang.OTHER_GUILDS_PLACE : Lang.OTHER_GUILDS_BREAK).sendError(player);
                    return true;
                } else return false;
            }
            return buildInClaimedOnly(player, cp, chunk, placing);
        }
    }

    public static boolean onEntityBreak(EntityDamageByEntityEvent event, Player player) {
        Entity entity = event.getEntity();
        boolean innocent = entity instanceof Animals || entity instanceof NPC || entity instanceof WaterMob;
        if (!innocent && !shouldCheckEntityInteraction(event.getEntityType())) return false;
        Land land = Land.getLand(entity.getLocation());
        if (land == null) return false;
        Guild guild = land.getGuild();
        if (guild == null) return false;
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (cp.isAdmin()) return false;
        if (!innocent) {
            if (handleBuilding(player, cp, SimpleChunkLocation.of(entity.getLocation()), false)) {
                event.setCancelled(true);
                return true;
            }
            return false;
        }
        if (guild.getMiscUpgrades().getOrDefault(MiscUpgrade.ANTI_TRAMPLE, 0) >= 3 && !MiscUpgrade.ANTI_TRAMPLE.getConfig().getStringList("blacklisted-animals").contains(entity.getType().getName()) && !StandardRelationAttribute.CEASEFIRE.hasAttribute(cp.getGuild(), guild)) {
            Lang.OTHER_GUILDS_KILL.sendError(player, "entity", entity.getName());
            if (event.getDamager() instanceof Projectile) event.getDamager().remove();

            event.setCancelled(true);
            return true;
        }
        return false;
    }

    public static boolean shouldCheckEntityInteraction(EntityType entityType) {
        switch (entityType) {
            case VILLAGER:
            case ARMOR_STAND:
            case PAINTING:
            case ITEM_FRAME:
            case ENDER_CRYSTAL:
            case BOAT: return true;
            default: return false;
        }
    }

    private static void handleEntityInteraction(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            Entity entity = event.getRightClicked();
            Player player = event.getPlayer();
            if (!CastelPluginPermission.LAND_INTERACT.hasPermission(player)) {
                Land land = Land.getLand(entity.getLocation());
                if (land != null) {
                    Guild guild = land.getGuild();
                    if (guild != null) {
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (!cp.isAdmin()) {
                            if (!StandardRelationAttribute.INTERACT.hasAttribute(cp.getGuild(), guild)) {
                                Lang.OTHER_GUILDS_INTERACT.sendError(player);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void bucketEmpty(PlayerBucketEmptyEvent event) { handleBucket(event); }

    @EventHandler(ignoreCancelled = true)
    public void bucketFill(PlayerBucketEmptyEvent event) { handleBucket(event); }

    private static void handleBucket(PlayerBucketEvent event) {
        Block block = event.getBlockClicked();
        if (!Config.DISABLED_WORLDS.isInDisabledWorld(block.getWorld())) {
            Land land = Land.getLand(block);
            if (land != null) {
                Guild guild = land.getGuild();
                if (guild != null) {
                    Player player = event.getPlayer();
                    CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                    if (!cp.isAdmin()) {
                        if (!cp.hasGuild()) {
                            event.setCancelled(true);
                            Lang.OTHER_GUILDS_PLACE.sendError(player);
                        } else {
                            Guild cpGuild = cp.getGuild();
                            if (!guild.hasAttribute(cpGuild, StandardRelationAttribute.BUILD)) {
                                Lang.OTHER_GUILDS_PLACE.sendError(player);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        SimpleLocation to = SimpleLocation.of(event.getToBlock());
        Land toLand = to.toSimpleChunkLocation().getLand();
        if (toLand != null && toLand.isClaimed()) {
            Land fromLand = Land.getLand(event.getBlock());
            Guild guild = fromLand == null ? null : fromLand.getGuild();
            if (!toLand.getGuild().hasAttribute(guild, StandardRelationAttribute.BUILD)) event.setCancelled(true);
        }
    }

    private static boolean handleEntityRemove(Entity entity, SimpleChunkLocation chunk) {
        if (Config.DISABLED_WORLDS.isInDisabledWorld(entity)) return false;
        Land land = chunk.getLand();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            return handleBuilding(player, cp, chunk, false);
        }
        return land != null && land.isClaimed();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public static void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!Config.Claims.DISABLE_PROTECTION_SYSTEM.getManager().getBoolean()) {
            if (!Config.DISABLED_WORLDS.isInDisabledWorld(player)) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                SimpleChunkLocation chunk = SimpleChunkLocation.of(event.getBlock());
                if (handleBuilding(player, cp, chunk, true)) event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public static void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Config.DISABLED_WORLDS.isInDisabledWorld(player)) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            SimpleChunkLocation chunk = SimpleChunkLocation.of(event.getBlock());
            if (!Config.Claims.DISABLE_PROTECTION_SYSTEM.getManager().getBoolean()) {
                if (handleBuilding(player, cp, chunk, false)) event.setCancelled(true);
            }
        }
    }

    public static boolean isItemEntity(ItemStack item) {
        Material type = item.getType();
        switch (type) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING:
            case END_CRYSTAL: return true;
            default:
                if (type.name().endsWith("BOAT")) return true;
                return item.getItemMeta() instanceof SpawnEggMeta || type.name().endsWith("MINECART");
        }
    }

    private static void cancelInteraction(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onRide(EntityMountEvent event) {
        if (!Config.Claims.DISABLE_PROTECTION_SYSTEM.getManager().getBoolean()) {
            Entity entity = event.getMount();
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (!CastelPluginPermission.LAND_INTERACT.hasPermission(player)) {
                    Land land = Land.getLand(entity.getLocation());
                    if (land != null) {
                        Guild guild = land.getGuild();
                        if (guild != null) {
                            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                            if (!cp.isAdmin()) {
                                if (!StandardRelationAttribute.INTERACT.hasAttribute(cp.getGuild(), guild)) {
                                    Lang.OTHER_GUILDS_INTERACT.sendError(player);
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() != null) {
            if (handleEntityRemove(event.getAttacker(), SimpleChunkLocation.of(event.getVehicle().getLocation())))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.isAdmin()) {
                Land land = Land.getLand(player.getLocation());
                if (land != null && land.isClaimed()) {
                    Guild guild = cp.getGuild();
                    Guild landGuild = land.getGuild();
                    if (preventGameMode(player, guild, landGuild)) {
                        Lang.LANDS_GAMEMODE_PROTECTION_ACTIVATED.sendError(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private static boolean preventGameMode(Player player, Guild guild, Guild landGuild) {
        if (CastelPluginPermission.LAND_BYPASS_CREATIVE.hasPermission(player)) return false;
        if (!Config.Relations.FORCE_SURVIVAL_MODE.getManager().getBoolean()) return false;
        if (StandardRelationAttribute.CEASEFIRE.hasAttribute(guild, landGuild)) return false;
        boolean wasAllowToFly = player.isFlying();
        player.setGameMode(GameMode.SURVIVAL);
        if (wasAllowToFly) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onTeleportProtection(LandChangeEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            Land land = event.getToLand();
            if (land != null && land.isClaimed()) {
                CastelPlayer cp = event.getCastelPlayer();
                if (!cp.isAdmin()) {
                    Guild guild = cp.getGuild();
                    Guild landGuild = land.getGuild();
                    boolean prevented = preventGameMode(player, guild, landGuild);
                    if (prevented) Lang.LANDS_GAMEMODE_PROTECTION.sendError(player);

                    if (prevented && event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && (!StandardRelationAttribute.CEASEFIRE.hasAttribute(guild, landGuild))) {
                        Lang.LANDS_PORTAL_PROTECTION.sendError(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        boolean handInteraction = event.getAction() == Action.RIGHT_CLICK_BLOCK || block != null && block.getType() == Material.DRAGON_EGG;
        if (handInteraction || event.getAction() == Action.PHYSICAL) {
            if (XBlock.isAir(block.getType())) {
                CLogger.warn(event.getPlayer().getName() + " might be using a hacked client. They clicked an AIR block (which is not possible): " + event.getAction() + " - " + block.getType() + " - " + LocationUtils.toReadableLocation(block.getLocation()));
            } else {
                Player player = event.getPlayer();
                Land land = Land.getLand(block);
                ItemStack item = event.getItem();
                boolean isInDisabledWorld = Config.DISABLED_WORLDS.isInDisabledWorld(player);
                CastelPlayer cp;
                if (handInteraction) {
                    SimpleLocation location = SimpleLocation.of(block);
                    boolean exceedsBuildLimit = LocationUtils.exceedsBuildLimit(event);
                    if (land == null) return;
                    if (isInDisabledWorld) return;
                    if (Config.Claims.DISABLE_PROTECTION_SYSTEM.getManager().getBoolean()) return;

                    cp = CastelPlayer.getCastelPlayer(player);
                    if (!exceedsBuildLimit && item != null && isItemEntity(item) && handleBuilding(player, cp, SimpleChunkLocation.of(block.getRelative(event.getBlockFace())), true)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (!isInDisabledWorld) {
                    if (land != null && (land.isClaimed() || Config.ProtectionSigns.PROTECT_UNCLAIMED.getManager().getBoolean())) {
                        boolean noPlate = event.getAction() != Action.PHYSICAL || !block.getType().name().endsWith("_PLATE");
                        if (noPlate) {
                            XMaterial mat = XMaterial.matchXMaterial(block.getType());
                            if (!XTag.FENCES.isTagged(mat) && !XTag.isInteractable(mat)) return;

                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                Optional<ProtectionSign> protectionOpt = ProtectionSign.getProtection(block);
                                if (protectionOpt.isPresent()) {
                                    ProtectionSignManager.handleProtectedBlock(event, protectionOpt.get());
                                }
                            }
                        }

                        if (land.isClaimed()) {
                            cp = CastelPlayer.getCastelPlayer(player);
                            if (!cp.isAdmin() && !CastelPluginPermission.LAND_INTERACT.hasPermission(player)) {
                                Guild guild = cp.getGuild();
                                boolean isConsumableInHand = item != null && item.getType().isEdible();
                                if (!isConsumableInHand && item != null) {
                                    XMaterial mat = XMaterial.matchXMaterial(item);
                                    isConsumableInHand = mat == XMaterial.SHIELD || mat == XMaterial.BOW || mat == XMaterial.CROSSBOW;
                                }

                                boolean showMessage = noPlate && !isConsumableInHand;
                                List<String> list = Config.Ranks.INTERACT_BLOCKS.getManager().getStringList();
                                XMaterial mat = XMaterial.matchXMaterial(block.getType());
                                if (mat.isOneOf(list)) {
                                    if (!StandardRelationAttribute.INTERACT.hasAttribute(guild, land.getGuild())) {
                                        event.setUseInteractedBlock(Event.Result.DENY);
                                        if (showMessage) Lang.OTHER_GUILDS_INTERACT.sendError(player);
                                    } else if (guild != null && !cp.hasPermission(StandardGuildPermission.INTERACT)) {
                                        if (showMessage) StandardGuildPermission.INTERACT.sendDeniedMessage(player);
                                        event.setUseInteractedBlock(Event.Result.DENY);
                                    }
                                } else if (!StandardRelationAttribute.USE.hasAttribute(guild, land.getGuild())) {
                                    event.setUseInteractedBlock(Event.Result.DENY);
                                    if (showMessage) Lang.OTHER_GUILDS_USE.sendError(player);
                                } else if (guild != null && !cp.hasPermission(StandardGuildPermission.USE)) {
                                    if (showMessage) StandardGuildPermission.USE.sendDeniedMessage(player);
                                    event.setUseInteractedBlock(Event.Result.DENY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!Config.Claims.DISABLE_PROTECTION_SYSTEM.getManager().getBoolean()) {
            if (handleEntityRemove(event.getRemover(), SimpleChunkLocation.of(event.getEntity().getLocation())))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void entityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Minecart || shouldCheckEntityInteraction(event.getRightClicked().getType()))
            handleEntityInteraction(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void entityInteractAt(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand)
            handleEntityInteraction(event);
    }

    public class BerryBushHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onSweetBerryHarvesting(@NotNull PlayerHarvestBlockEvent event) {
            if (event.getHarvestedBlock().getType() == Material.SWEET_BERRY_BUSH) {
                Player player = event.getPlayer();
                if (!Config.DISABLED_WORLDS.isInDisabledWorld(player)) {
                    CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                    if (!cp.isAdmin() && !CastelPluginPermission.LAND_INTERACT.hasPermission(player)) {
                        Block block = event.getHarvestedBlock();
                        SimpleChunkLocation chunk = SimpleChunkLocation.of(block);
                        Land land = chunk.getLand();
                        if (land != null) {
                            Guild guild = cp.getGuild();
                            if (!StandardRelationAttribute.USE.hasAttribute(guild, land.getGuild())) {
                                Lang.OTHER_GUILDS_USE.sendMessage(player);
                            } else {
                                if (guild == null || cp.hasPermission(StandardGuildPermission.USE)) return;

                                StandardGuildPermission.USE.sendDeniedMessage(player);
                            }

                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
