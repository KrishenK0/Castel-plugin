package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OpenProtectedBlockEvent extends CastelEvent implements Cancellable, PlayerOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Block block;
    private final ProtectionSign protectionSign;
    private final boolean canOpen;
    private boolean cancelled;

    public OpenProtectedBlockEvent(Player player, Block block, ProtectionSign protectionSign, boolean canOpen) {
        this.player = player;
        this.block = block;
        this.protectionSign = protectionSign;
        this.canOpen = canOpen;
    }

    public static HandlerList getHandlerList() { return handlers; }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public CastelPlayer getPlayer() {
        return CastelPlayer.getCastelPlayer(player);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean canOpen() {
        return canOpen;
    }

    public ProtectionSign getProtectionSign() {
        return protectionSign;
    }
}
