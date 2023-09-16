package fr.krishenk.castel.events.lands;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;

import java.util.*;

public abstract class ClaimingEvent extends CastelEvent implements Cancellable, PlayerOperator, GuildOperator {
    private final CastelPlayer player;
    private Set<SimpleChunkLocation> lands;
    private final List<Land> cachedLands;
    private final Guild guild;
    private boolean cancelled;
    private final boolean claiming;

    public ClaimingEvent(CastelPlayer player, Guild guild, Set<SimpleChunkLocation> lands, boolean claiming) {
        this.guild = Objects.requireNonNull(guild);
        this.player = player;
        this.lands = Objects.requireNonNull(lands);
        this.claiming = claiming;
        this.cachedLands = new ArrayList<>(this.lands.size());
        this.cacheLands();
    }

    public Set<SimpleChunkLocation> getLandLocations() {
        return Collections.unmodifiableSet(this.lands);
    }

    private void cacheLands() {
        this.cachedLands.clear();
        for (SimpleChunkLocation chunk : this.lands) {
            if (chunk == null) throw new IllegalArgumentException("Land locations cannot contain a null land");
            Land land = chunk.getLand();
            if (this.claiming) {
                if (land == null) continue;
                if (land.isClaimed())
                    throw new IllegalArgumentException("Land '" + chunk + "'is already claimed by another guild: " + land.getGuildId());
                this.cachedLands.add(land);
                continue;
            }
            if (land != null && land.isClaimed()) continue;
            throw new IllegalArgumentException("Land '" + chunk + "' is not claimed by any guilds");
        }
    }

    public Set<SimpleChunkLocation> getLands() {
        return Collections.unmodifiableSet(this.lands);
    }

    public void setLands(Set<SimpleChunkLocation> lands) {
        this.lands = Objects.requireNonNull(lands);
        this.cacheLands();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }
}
