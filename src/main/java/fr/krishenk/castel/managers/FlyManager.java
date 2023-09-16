package fr.krishenk.castel.managers;

import com.google.common.base.Strings;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.config.NewKeyedConfigAccessor;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.events.members.GuildLeaveEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.services.SoftService;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import fr.krishenk.castel.utils.internal.integer.IntHashSet;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FlyManager implements Listener {
    private static final IntHashMap<Integer> WARNINGS_LAND = new IntHashMap<>();
    private static final IntHashMap<Integer> WARNINGS_UNFRIENDLY = new IntHashMap<>();
    private static final IntHashMap<Integer> WARNINGS_CHARGES = new IntHashMap<>();
    private static final IntHashSet PREVENT_FALL_DAMAGE = new IntHashSet();

    public static void  preventFallDamage(Entity entity) {
        PREVENT_FALL_DAMAGE.add(entity.getEntityId());
    }

    @EventHandler
    public void onFlightDisableDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (PREVENT_FALL_DAMAGE.remove(event.getEntity().getEntityId())) {
                event.setCancelled(true);
                CLogger.debug(CastelDebug.FALL$DAMAGE, "Prevented fall damage for " + event.getEntity());
            }
        }
    }

    public static boolean handleCharges(Player player, CastelPlayer cp, Pair<NewKeyedConfigAccessor, Lang> playerCosts, Pair<NewKeyedConfigAccessor, Lang> guildCosts) {
        Guild guild = cp.getGuild();
        Runnable finalizePlayerCosts = null;
        String guildEqn;
        double guildCharges;
        if (SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(ServiceVault.Component.ECO)) {
            guildEqn = playerCosts.getKey().forWorld(player).getString();
            if (!Strings.isNullOrEmpty(guildEqn)) {
                guildCharges = MathUtils.eval(guildEqn, player);
                if (!ServiceVault.hasMoney(player, guildCharges)) {
                    playerCosts.getValue().sendError(player, "money", StringUtils.toFancyNumber(guildCharges),"interval", Config.GUILD_FLY_CHARGES_EVERY_SECONDS.getInt());
                    return false;
                }

                double finalGuildCharges = guildCharges;
                finalizePlayerCosts = () -> {
                    ServiceVault.withdraw(player, finalGuildCharges);
                    if (Config.GUILD_FLY_CHARGES_PLAYERS_PAY_GUILD_ENABLED.getBoolean()) {
                        boolean useRp = Config.GUILD_FLY_CHARGES_PLAYERS_PAY_GUILD_RESOURCE_POINTS.getBoolean();
                        if (useRp) guild.addResourcePoints((long) finalGuildCharges);
                        else guild.addBank(finalGuildCharges);
                    }
                };
            }
        }

        guildEqn = guildCosts.getKey().forWorld(player).getString();
        if (Strings.isNullOrEmpty(guildEqn)) {
            return true;
        } else {
            label: {
                guildCharges = MathUtils.eval(guildEqn, player);
                boolean useRp = Config.GUILD_FLY_CHARGES_GUILDS_RESOURCE_POINTS.getBoolean();
                if (useRp) {
                    if (!guild.hasResourcePoints((long) guildCharges)) break label;
                } else if (!guild.hasMoney(guildCharges)) break label;
                
                if (useRp) guild.addResourcePoints((long) -guildCharges);
                else guild.hasMoney(-guildCharges);
                
                if (finalizePlayerCosts != null) finalizePlayerCosts.run();
                
                return true;
            }
            
            guildCosts.getValue().sendError(player, "amount", StringUtils.toFancyNumber(guildCharges), "interval", Config.GUILD_FLY_CHARGES_EVERY_SECONDS.getInt());
            return false;
        }
    }
    
    private static void handleCharges(Player player) {
        if (player.getAllowFlight()) {
            if (!CastelPluginPermission.FLIGHT_BYPASS_CHARGES.hasPermission(player)) {
                if (!PlayerUtils.invulnerableGameMode(player)) {
                    if (!WARNINGS_CHARGES.containsKey(player.getEntityId())) {
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (cp.isFlying() && !cp.isAdmin()) {
                            if (!handleCharges(player, cp, Pair.of(Config.GUILD_FLY_CHARGES_PLAYERS_AMOUNT.getManager(), Lang.FLY_CHARGES_CANT_AFFORD), Pair.of(Config.GUILD_FLY_CHARGES_GUILDS_AMOUNT.getManager(), Lang.FLY_CHARGES_CANT_AFFORD_GUILD))) {
                                handleCantPay(player, cp);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleCantPay(Player player, CastelPlayer cp) {
        int warning = Config.GUILD_FLY_WARNINGS_CHARGES.getInt();
        if (warning <= 0) {
            disableFly(player, cp);
        } else {
            int task = (new BukkitRunnable() {
                int counter = warning;

                @Override
                public void run() {
                    if (this.counter == 0) {
                        FlyManager.WARNINGS_CHARGES.remove(player.getEntityId());
                        FlyManager.disableFlight(cp, player);
                    } else {
                        Lang.FLY_WARNINGS_CHARGES.sendError(player, "counter", this.counter);
                        --this.counter;
                    }
                }
            }).runTaskTimerAsynchronously(CastelPlugin.getInstance(), 0L, 20L).getTaskId();
            WARNINGS_CHARGES.put(player.getEntityId(), task);
        }
    }

    private static boolean isNearbyUnfriendly(Player player, int range, boolean action) {
        if (!player.getAllowFlight()) return false;
        else if (CastelPluginPermission.FLIGHT_NEARBY$ENEMIES.hasPermission(player)) return false;
        else if (PlayerUtils.invulnerableGameMode(player)) return false;
        else {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.isFlying() && !cp.isAdmin()) {
                Guild guild = cp.getGuild();

                for (Entity nearby : player.getNearbyEntities(range, range, range)) {
                    if (nearby instanceof Player) {
                        Player enemy = (Player) nearby;
                        if (!PlayerUtils.invulnerableGameMode(enemy)) {
                            CastelPlayer enemyCp = CastelPlayer.getCastelPlayer(enemy);
                            if (!enemyCp.isInSneakMode()) {
                                Guild otherGuild = enemyCp.getGuild();
                                if (!guild.hasAttribute(otherGuild, StandardRelationAttribute.FLY)) {
                                    if (action && !WARNINGS_UNFRIENDLY.containsKey(player.getEntityId())) {
                                        int warning = Config.GUILD_FLY_WARNINGS_UNFRIENDLY_NEARBY.getInt();
                                        if (warning <= 0) {
                                            Lang.FLY_ENEMIES_NEARBY.sendError(player);
                                            disableFly(player, cp);
                                        } else {
                                            int task = (new BukkitRunnable() {
                                                int counter = warning;

                                                @Override
                                                public void run() {
                                                    if (this.counter == 0) {
                                                        Lang.FLY_ENEMIES_NEARBY.sendError(player);
                                                        FlyManager.WARNINGS_UNFRIENDLY.remove(player.getEntityId());
                                                        FlyManager.disableFlight(cp, player);
                                                        this.cancel();
                                                    } else {
                                                        if (FlyManager.isNearbyUnfriendly(player, range, false))
                                                            Lang.FLY_WARNINGS_UNFRIENDLY_NEARBY_WARN.sendError(player, "counter", this.counter--);
                                                        else {
                                                            Lang.FLY_WARNINGS_UNFRIENDLY_NEARBY_SAFE.sendMessage(player, "counter", this.counter);
                                                            FlyManager.WARNINGS_UNFRIENDLY.remove(player.getEntityId());
                                                            this.cancel();
                                                        }
                                                    }
                                                }
                                            }).runTaskTimer(CastelPlugin.getInstance(), 0L, 20L).getTaskId();
                                            WARNINGS_UNFRIENDLY.put(player.getEntityId(), task);
                                        }
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private static boolean canFlyInLand(Player player, CastelPlayer cp, Land to) {
        if (PlayerUtils.invulnerableGameMode(player)) return true;
        if (cp.isAdmin()) return true;
        Guild guild = to == null ? null : to.getGuild();
        if (guild == null) return Config.GUILD_FLY_ALLOW_UNCLAIMED.getBoolean();
        return cp.getGuild().hasAttribute(guild, StandardRelationAttribute.FLY);
    }

    private static void cancelTasks(Player player) {
        int id = player.getEntityId();
        Integer task = WARNINGS_UNFRIENDLY.remove(id);
        if (task != null) Bukkit.getScheduler().cancelTask(task);
        task = WARNINGS_LAND.remove(id);
        if (task != null) Bukkit.getScheduler().cancelTask(task);
    }

    private static void disableFlight(CastelPlayer cp, Player player) {
        Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> disableFly(player, cp));
    }

    public static void disableFly(Player player, CastelPlayer cp) {
        cp.disableFlying(player);
        noFallDamage(player);
    }

    public static void noFallDamage(Player player) {
        (new BukkitRunnable() {
            private int tries;

            @Override
            public void run() {
                ++this.tries;
                if (player.isOnGround()) {
                    FlyManager.PREVENT_FALL_DAMAGE.remove(player.getEntityId());
                    this.cancel();
                } else {
                    if (this.tries / 20 >= 2) this.cancel();
                }
            }
        }).runTaskTimerAsynchronously(CastelPlugin.getInstance(), 1L, 1L);
        PREVENT_FALL_DAMAGE.add(player.getEntityId());
    }

    public static void onFlyLandChange(LandChangeEvent event) {
        Player player = event.getPlayer();
        if (!CastelPluginPermission.FLIGHT_LANDS.hasPermission(player)) {
            if (player.getAllowFlight() && !PlayerUtils.invulnerableGameMode(player)) {
                if (!WARNINGS_LAND.containsKey(player.getEntityId())) {
                    CastelPlayer cp = event.getCastelPlayer();
                    if (!cp.isAdmin() && cp.isFlying()) {
                        Land to = event.getToLand();
                        if (!canFlyInLand(player, cp, to)) {
                            int warning = Config.GUILD_FLY_WARNINGS_LAND.getInt();
                            if (warning <= 0) {
                                Lang.FLY_OUT_OF_LAND.sendError(player);
                                disableFly(player, cp);
                            } else {
                                int task = (new BukkitRunnable() {
                                    int counter = warning;

                                    @Override
                                    public void run() {
                                        if (this.counter == 0) {
                                            Lang.FLY_OUT_OF_LAND.sendError(player);
                                            FlyManager.WARNINGS_LAND.remove(player.getEntityId());
                                            FlyManager.disableFlight(cp, player);
                                            this.cancel();
                                        } else {
                                            if (FlyManager.canFlyInLand(player, cp, Land.getLand(player.getLocation()))) {
                                                Lang.FLY_WARNINGS_OUT_OF_LAND_SAFE.sendMessage(player, "counter", this.counter);
                                                FlyManager.WARNINGS_LAND.remove(player.getEntityId());
                                                this.cancel();
                                            } else
                                                Lang.FLY_WARNINGS_OUT_OF_LAND_WARN.sendError(player, "counter", this.counter--);
                                        }
                                    }
                                }).runTaskTimerAsynchronously(CastelPlugin.getInstance(), 0L, 20L).getTaskId();
                                WARNINGS_LAND.put(player.getEntityId(), task);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlyDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (PvPManager.getDamager(event.getDamager()) != null) {
                Player player = (Player) event.getEntity();
                if (player.getAllowFlight()) {
                    if (!CastelPluginPermission.FLIGHT_DAMAGE.hasPermission(player)) {
                        if (Config.GUILD_FLY_DISABLE_ON_DAMAGE.getBoolean()) {
                            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                            if (!cp.isAdmin() && cp.isFlying()) {
                                disableFly(player, cp);
                                Lang.FLY_DAMAGE.sendError(player);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PREVENT_FALL_DAMAGE.remove(event.getPlayer().getEntityId());
        cancelTasks(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        PREVENT_FALL_DAMAGE.remove(event.getPlayer().getEntityId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUnclaim(UnclaimLandEvent event) {
        for (SimpleChunkLocation land : event.getLandLocations()) {
            Chunk chunk = land.toChunk();
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Player) {
                    CastelPlayer cp = CastelPlayer.getCastelPlayer((Player) entity);
                    flightInUnclaimed(cp);
                }
            }
        }
    }

    private void flightInUnclaimed(CastelPlayer cp) {
        if (cp.isFlying() && !cp.isAdmin()) {
            Player player = cp.getPlayer();
            if (player != null && !CastelPluginPermission.FLIGHT_LANDS.hasPermission(player)) {
                disableFly(player, cp);
                cancelTasks(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void duringFlyGuildLeaveEvent(GuildLeaveEvent event) {
        flightInUnclaimed(event.getPlayer());
    }

    static {
        int range = Config.GUILD_FLY_NEARBY_UNFRIENDLY_RANGE.getInt();
        Bukkit.getScheduler().runTaskTimer(CastelPlugin.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                if (cp.hasGuild() && cp.isFlying() && !cp.hasPermission(StandardGuildPermission.FLY)) {
                    StandardGuildPermission.FLY.sendDeniedMessage(player);
                    disableFly(player, cp);
                } else isNearbyUnfriendly(player, range, true);
            }
        }, 1200L, 100L);
        if (Config.GUILD_FLY_CHARGES_ENABLED.getBoolean()) {
            long chargesTicks = Config.GUILD_FLY_CHARGES_EVERY_SECONDS.getInt() * 20L;
            Bukkit.getScheduler().runTaskTimerAsynchronously(CastelPlugin.getInstance(), () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    handleCharges(player);
                }
            }, 1200L, chargesTicks);
        }
    }
}
