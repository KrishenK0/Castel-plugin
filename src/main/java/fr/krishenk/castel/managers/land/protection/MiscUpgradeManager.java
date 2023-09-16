package fr.krishenk.castel.managers.land.protection;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.ItemMatcher;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.cooldown.BiCooldown;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MiscUpgradeManager implements Listener {
    private static final Material SOIL = XMaterial.FARMLAND.parseMaterial();
    private static final Map<UUID, Cooldown<UUID>> GLORY_COOLDOWN = new HashMap<>();
    private static final BiCooldown<UUID, UUID> ALERTS = new BiCooldown<>();

    private static void addGloryCooldown(Player killer, Player victim) {
        Cooldown<UUID> killerCooldowns = GLORY_COOLDOWN.computeIfAbsent(killer.getUniqueId(), (k) -> new Cooldown<>());
        killerCooldowns.add(victim.getUniqueId(), Duration.ofMinutes(5L));
    }

    private static boolean isInGloryCooldown(Player killer, Player victim) {
        Cooldown<UUID> killerCooldowns = GLORY_COOLDOWN.get(killer.getUniqueId());
        return killerCooldowns != null && killerCooldowns.isInCooldown(victim.getUniqueId());
    }

    public static boolean canPlaceItemInGuildChest(ItemStack item) {
        ConfigurationSection config = MiscUpgrade.CHEST_SIZE.getConfig().getConfigurationSection("items");
        if (config == null) return true;
        else {
            boolean blacklist = config.getBoolean("blacklist");
            Iterator<String> it = config.getConfigurationSection("list").getKeys(true).iterator();
            ItemMatcher matcher;
            do {
                if (!it.hasNext()) return true;

                ConfigurationSection itemConfig = config.getConfigurationSection(it.next());
                matcher = new ItemMatcher(itemConfig);
            } while (matcher.matches(item) != blacklist);

            return false;
        }
    }

    public static void onEnemyEnterAlert(LandChangeEvent event) {
        if (!ServiceHandler.isVanished(event.getPlayer())) {
            CastelPlayer cp = event.getCastelPlayer();
            if (!cp.isAdmin()) {
                Guild guild = cp.getGuild();
                if (guild != null) {
                    Land to = event.getToLand();
                    if (to != null && to.isClaimed()) {
                        Guild toGuild = to.getGuild();
                        if (guild.getRelationWith(toGuild) == GuildRelation.ENEMY) {
                            if (!toGuild.isClaimed(event.getFromChunk())) {
                                if (!ALERTS.isInCooldown(toGuild.getId(), cp.getUUID())) {
                                    ALERTS.add(to.getGuildId(), cp.getUUID(), 1L, TimeUnit.MINUTES);
                                    Location loc = event.getPlayer().getLocation();
                                    MessageBuilder settings = (new MessageBuilder()).raws("world", loc.getWorld().getName(), "x", loc.getX(), "y", loc.getY(), "z", loc.getZ());
                                    for (Player member : toGuild.getOnlineMembers()) {
                                        Lang.MISC_UPGRADES_ALERTS_NOTIFY_MEMBERS.sendMessage(member, settings.withContext(event.getPlayer()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCropTrample(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block.getType() == SOIL) {
                if (MiscUpgrade.ANTI_TRAMPLE.isEnabled()) {
                    Player player = event.getPlayer();
                    if (!Config.DISABLED_WORLDS.isInDisabledWorld(player)) {
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (!cp.isAdmin()) {
                            Land land = Land.getLand(block);
                            if (land != null && land.isClaimed()) {
                                Guild guildLand = land.getGuild();
                                if (!guildLand.hasAttribute(cp.getGuild(), StandardRelationAttribute.BUILD)) {
                                    if (land.getGuild().getUpgradeLevel(MiscUpgrade.ANTI_TRAMPLE) > 0) {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGlory(EntityDeathEvent event) {
        if (MiscUpgrade.GLORY.isEnabled()) {
            LivingEntity victim = event.getEntity();
            Player killer = victim.getKiller();
            if (killer != null) {
                if (!Config.DISABLED_WORLDS.isInDisabledWorld(killer)) {
                    Land land = Land.getLand(victim.getLocation());
                    if (land != null) {
                        Guild guild = land.getGuild();
                        if (guild != null) {
                            int lvl = guild.getUpgradeLevel(MiscUpgrade.GLORY);
                            if (lvl > 0) {
                                int drop = event.getDroppedExp();
                                if (!(victim instanceof Player) || !isInGloryCooldown(killer, (Player) victim)) {
                                    ConfigurationSection xpSection = MiscUpgrade.GLORY.getConfig().getConfigurationSection("xp");
                                    MessageBuilder context = (new MessageBuilder()).withContext(killer).raw("xp", event.getDroppedExp()).raw("lvl", lvl);
                                    if (victim instanceof Player) {
                                        context.other((Player) victim);
                                    }

                                    for (String cond : xpSection.getKeys(true)) {
                                        context.raw("type", victim.getType().name());
                                        if (ConditionProcessor.process(ConditionalCompiler.compile(cond).evaluate(), context)) {
                                            drop = (int) MathUtils.eval(xpSection.getString(cond), context);
                                        }
                                    }

                                    if (victim instanceof Player) addGloryCooldown(killer, (Player) victim);
                                    event.setDroppedExp(drop);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Land toLand = Land.getLand(event.getTo());
            if (toLand != null && toLand.isClaimed()) {
                Guild toLandGuild = toLand.getGuild();
                if (toLandGuild.getUpgradeLevel(MiscUpgrade.ANTI_TRAMPLE) >= 3) {
                    Guild playerGuild = cp.getGuild();
                    if (!StandardRelationAttribute.CEASEFIRE.hasAttribute(toLandGuild, playerGuild)) {
                        event.setCancelled(true);
                        ParticleDisplay.of(Particle.CLOUD).withCount(10).spawn(event.getTo());
                        Lang.LANDS_ENDER_PEARL_PROTECTION.sendError(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onResourcePointsPenalty(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!Config.DISABLED_WORLDS.isInDisabledWorld(player)) {
            if (!Config.ResourcePoints.DEATH_PENALTY_DISABLED_WORLDS.getManager().isInDisabledWorld(player)) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                if (guild != null) {
                    long penalty = (long) MathUtils.eval(Config.ResourcePoints.DEATH_PENALTY_AMOUNT.getManager().getString(), player);
                    if (penalty != 0L) {
                        if (guild.hasResourcePoints(penalty)) {
                            guild.addResourcePoints(-penalty);
                            for (Player member : guild.getOnlineMembers()) {
                                Lang.DEATH_PENALTY.sendMessage(member, player, "penalty", StringUtils.toFancyNumber(penalty), "name", player.getName());
                            }
                        }
                    }
                }
            }
        }
    }
}
