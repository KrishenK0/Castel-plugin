package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimClipboard {
    private static final Map<UUID, ClaimClipboard> CLIPBOARD = new HashMap<>();
    private final long started = System.currentTimeMillis();
    private final Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims;
    private final World world;

    public ClaimClipboard(Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims, World world) {
        this.claims = Objects.requireNonNull(claims, "Clipboard world cannot be null");
        this.world = Objects.requireNonNull(world, "Clipboard claims cannot be null");
    }

    public static void addClipboard(Player player, ClaimClipboard clipboard) {
        CLIPBOARD.put(player.getUniqueId(), clipboard);
    }

    public static Map<UUID, ClaimClipboard> getClipboard() {
        return CLIPBOARD;
    }

    public Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> getClaims() {
        return claims;
    }

    public long getStarted() {
        return started;
    }

    public Pair<Long, Double> getTotalCost() {
        long rp = 0L;
        double money = 0.0;
        for (AbstractClaimProcessor processor : this.claims.values()) {
            if (processor.isSuccessful()) {
                rp += processor.getResourcePoints();
                money += processor.getMoney();
            }
        }
        return Pair.of(rp, money);
    }

    public World getWorld() {
        return world;
    }

    public static class ClaimProcessor extends fr.krishenk.castel.managers.land.claiming.ClaimProcessor {
        public ClaimProcessor(SimpleChunkLocation chunk, CastelPlayer cp, Guild guild) {
            super(chunk, cp, guild);
        }

        protected Lang checkConstants() {
            return null;
        }
    }
}
