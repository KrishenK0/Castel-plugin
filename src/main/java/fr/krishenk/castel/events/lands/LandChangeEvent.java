package fr.krishenk.castel.events.lands;

import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class LandChangeEvent extends CastelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerMoveEvent moveEvent;
    private final CastelPlayer castelPlayer;
    private final SimpleChunkLocation fromChunk;
    private final SimpleChunkLocation toChunk;
    private final Land fromLand;
    private final Land toLand;
    private final PlayerTeleportEvent.TeleportCause cause;

    public LandChangeEvent(PlayerMoveEvent moveEvent, SimpleChunkLocation fromChunk, SimpleChunkLocation toChunk, PlayerTeleportEvent.TeleportCause cause) {
        this.moveEvent = moveEvent;
        this.castelPlayer = CastelPlayer.getCastelPlayer(moveEvent.getPlayer());
        this.fromChunk = fromChunk;
        this.toChunk = toChunk;
        this.fromLand = fromChunk.getLand();
        this.toLand = toChunk.getLand();
        this.cause = cause;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Land getFromLand() {
        return fromLand;
    }

    public Land getToLand() {
        return toLand;
    }

    public CastelPlayer getCastelPlayer() {
        return castelPlayer;
    }

    public Location getFrom() {
        return this.moveEvent.getFrom();
    }

    public void setFrom(Location location) {
        this.moveEvent.setFrom(location);
    }

    public Location getTo() {
        return this.moveEvent.getTo();
    }

    public void setTo(Location location) {
        this.moveEvent.setTo(location);
    }

    @Override
    public boolean isCancelled() {
        return this.moveEvent.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        this.moveEvent.setCancelled(b);
    }

    public SimpleChunkLocation getFromChunk() {
        return fromChunk;
    }

    public SimpleChunkLocation getToChunk() {
        return toChunk;
    }

    public PlayerTeleportEvent.TeleportCause getCause() {
        return cause;
    }

    public Player getPlayer() {
        return this.moveEvent.getPlayer();
    }
}
