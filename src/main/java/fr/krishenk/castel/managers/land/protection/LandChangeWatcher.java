package fr.krishenk.castel.managers.land.protection;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.managers.FlyManager;
import fr.krishenk.castel.managers.land.CastelMap;
import fr.krishenk.castel.managers.land.claiming.AutoClaimManager;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Objects;

public class LandChangeWatcher implements Listener {

    private static void preparedLandIndicator(Player player, CastelPlayer cp, Guild playersGuild, LandChangeEvent event) {
        if (playersGuild != null || Config.Claims.INDICATOR_GUILDLESS_ENABLED.getManager().getBoolean()) {
            if (!Config.Claims.INDICATOR_IGNORE_WORLDGUARD_REGIONS.getManager().getBoolean() || !ServiceHandler.isInRegion(event.getToChunk())) {
                Guild fromLandGuild = event.getFromLand() == null ? null : event.getFromLand().getGuild();
                Guild toLandGuild = event.getToLand() == null ? null : event.getToLand().getGuild();
                String fromLandRelation = LandVisualizer.getRelationOf(event.getFromChunk(), fromLandGuild, playersGuild);
                String toLandRelation = LandVisualizer.getRelationOf(event.getToChunk(), toLandGuild, playersGuild);
                LandVisualizer visualizer = (new LandVisualizer()).forPlayer(player, cp).forLand(event.getToLand(), event.getFromChunk().toChunk());
                if (cp.isUsingMarkers()) {
                    visualizer.displayIndicators();
                }

                if (!Objects.equals(fromLandGuild, toLandGuild) || Config.Claims.INDICATOR_SEND_MESSAGES_FOR_SAME_LAND_TYPE.getManager().withOption("relation", toLandRelation).getBoolean() || !fromLandRelation.equals(toLandRelation)) {
                    CLogger.debug(CastelDebug.LAND$VISUALIZERS, () -> fromLandRelation + " -> " + toLandRelation);
                    visualizer.displayMessages();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLandChange(LandChangeEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), () -> {
           Player player = event.getPlayer();
           if (!Config.DISABLED_WORLDS.isInDisabledWorld(player, null)) {
               CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
               Guild guild = cp.getGuild();
               MiscUpgradeManager.onEnemyEnterAlert(event);
               AutoClaimManager.autoUnclaim(event, guild, cp);
               AutoClaimManager.autoClaim(event, guild, cp);
               if (cp.isAutoMap()) cp.buildMap().display();

               if (CastelMap.isUsingScoreboard(player)) {
                   CastelMap map = cp.buildMap();
                   Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), map::displayAsScoreboard);
               }

               preparedLandIndicator(player, cp, guild, event);
               if (Config.GUILD_FLY_ENABLED.getBoolean()) FlyManager.onFlyLandChange(event);
           }
        });
    }
}
