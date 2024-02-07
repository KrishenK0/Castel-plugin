package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildBankChangeEvent extends CastelEvent implements Cancellable, PlayerOperator, GroupOperator {
    private static final HandlerList HANDLDERS = new HandlerList();
    private boolean cancelled;
    private double amount;
    private Group group;
    private CastelPlayer player;

    public GuildBankChangeEvent(double amount, CastelPlayer player, Group group) {
        this.amount = amount;
        this.group = group;
        this.player = player;
    }

    public double getNewBank() {
        return this.group.getBank()+this.amount;
    }

    public double getOldBank() {
        return this.group.getBank();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLDERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLDERS;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
