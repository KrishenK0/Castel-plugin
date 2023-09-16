package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroupServerTaxPayEvent extends CastelEvent implements Cancellable, GroupOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Group group;
    private double amount;
    private boolean cancelled;

    public GroupServerTaxPayEvent(Group group, double amount) {
        super(true);
        this.group = Objects.requireNonNull(group);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
