package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.ConfigUtils;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class AutoClaimManager implements Listener {

    public static void autoUnclaim(LandChangeEvent event, Guild guild, CastelPlayer cp) {
        if (cp.getAutoClaim() == Boolean.FALSE) {
            SimpleChunkLocation chunk = event.getToChunk();
            UnclaimProcessor processor = new UnclaimProcessor(chunk, cp, guild, true);
            processor.asAuto();
            processor.process();
            if (!processor.isSuccessful()) processor.sendIssue(event.getPlayer());
            else {
                processor.finalizeRequest();
                Land land = chunk.getLand();
                Player player = event.getPlayer();
                Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> {
                    if (!land.unclaim(cp, UnclaimLandEvent.Reason.AUTO_UNCLAIMED).isCancelled()) {
                        Lang.AUTO_UNCLAIM_SUCCESS.sendMessage(player, "world", chunk.getWorld(), "x", chunk.getX(), "z", chunk.getZ());
                    }
                });
            }
        }
    }

    public static void autoClaim(LandChangeEvent event, Guild guild, CastelPlayer cp) {
        if (cp.getAutoClaim() == Boolean.TRUE) {
            Player player = event.getPlayer();
            SimpleChunkLocation chunk = event.getToChunk();
            ClaimProcessor processor = new ClaimProcessor(chunk, cp, guild);
            processor.asAuto();
            processor.process();
            if (processor.isSuccessful()) {
                processor.finalizeRequest();
                Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> {
                    if (!guild.claim(chunk, cp, ClaimLandEvent.Reason.AUTO_CLAIMED).isCancelled()) {
                        Lang.AUTO_CLAIM_SUCCESS.sendMessage(player, "world", chunk.getWorld(), "x", chunk.getX(), "z", chunk.getZ());
                    }
                });
            } else processor.sendIssue(player);
        }
    }

    @EventHandler
    public void onDisabledWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.isAdmin() && cp.getAutoClaim() != null && ConfigUtils.isInDisabledWorld(Config.Claims.DISABLED_WORLDS.getManager(), player, Lang.AUTO_CLAIM_DISABLED_WORLD)) {
            cp.setAutoClaim(null);
        }
    }
}
