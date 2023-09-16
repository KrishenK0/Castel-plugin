package fr.krishenk.castel.managers.land;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.XPotion;
import fr.krishenk.castel.managers.PvPManager;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumSet;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class LandEffectsManager implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPotionEffectThrow(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (potion.getShooter() instanceof Player) {
            List<String> disallowed = Config.Claims.POTION_PROTECTED_EFFECTS.getManager().getStringList();
            QuickEnumSet<XPotion> disallowedSet = new QuickEnumSet<>(XPotion.VALUES);

            for (String effect : disallowed) {
                Optional<XPotion> xPotion = XPotion.matchXPotion(effect);
                Objects.requireNonNull(disallowedSet);
                xPotion.ifPresent(disallowedSet::add);
            }

            boolean contains = false;
            for (PotionEffect effect : potion.getEffects()) {
                if (disallowedSet.contains(XPotion.matchXPotion(effect.getType()))) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                Player thrower = (Player) potion.getShooter();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(thrower);
                Guild guild = cp.getGuild();
                if (guild != null) {
                    for (LivingEntity entity : event.getAffectedEntities()) {
                        if (thrower != entity && entity instanceof Player && !PvPManager.canFight(thrower, (Player) entity))
                            event.setIntensity(entity, 0.0);
                    }
                }
            }
        }
    }

    static {
        Map<GuildRelation, List<XPotion.Effect>> effects = new EnumMap<>(GuildRelation.class);
        for (GuildRelation relation : GuildRelation.values()) {
            List<String> relEffects = Config.Relations.RELATIONS_EFFECTS.getManager().withOption("relation", StringUtils.configOption(relation)).getStringList();
            effects.put(relation, XPotion.parseEffects(relEffects));
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(CastelPlugin.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                if (!cp.isAdmin()) {
                    Land land = SimpleChunkLocation.of(player.getLocation()).getLand();
                    if (land != null && land.isClaimed()) {
                        GuildRelation relation = land.getGuild().getRelationWith(guild);
                        List<XPotion.Effect> relEffects = effects.get(relation);
                        Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> {
                            for (XPotion.Effect effect : relEffects) effect.apply(player);
                        });
                    }
                }
            }
        }, 10L, 200L);
    }

    public static class BeaconManager implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onPotionEffectsThrow(EntityPotionEffectEvent event) {
            if (event.getCause() == EntityPotionEffectEvent.Cause.BEACON) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    Land land = Land.getLand(player.getLocation());
                    if (land != null && land.isClaimed()) {
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (!land.getGuild().hasAttribute(cp.getGuild(), StandardRelationAttribute.CEASEFIRE))
                            event.setCancelled(true);
                    }
                }
            }
        }
    }
}
