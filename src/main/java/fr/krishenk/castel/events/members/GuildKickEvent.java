package fr.krishenk.castel.events.members;

import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class GuildKickEvent extends GuildLeaveEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer kicker;

    public GuildKickEvent(CastelPlayer player, boolean admin, OfflinePlayer kicker) {
        super(player, admin ? LeaveReason.ADMIN : LeaveReason.KICKED);
        this.kicker = Objects.requireNonNull(kicker, "Kicker cannot be null");
    }

    public OfflinePlayer getKicker() {
        return kicker;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
