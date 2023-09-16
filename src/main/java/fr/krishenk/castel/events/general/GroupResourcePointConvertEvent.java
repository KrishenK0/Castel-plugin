package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupResourcePointConvertEvent extends CastelEvent implements Cancellable, GroupOperator {
    private static final HandlerList handlers = new HandlerList();
    private final CastelPlayer castelPlayer;
    private final List<ItemStack> items;
    private final Group group;
    private List<ItemStack> leftOvers;
    private long amount;
    private boolean cancelled;

    public GroupResourcePointConvertEvent(CastelPlayer castelPlayer, List<ItemStack> items, Group group, List<ItemStack> leftOvers, long amount) {
        this.castelPlayer = castelPlayer;
        this.items = items;
        this.group = group;
        this.leftOvers = leftOvers;
        this.amount = amount;
    }

    public static HandlerList getHandlerList() { return handlers; }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CastelPlayer getCastelPlayer() {
        return castelPlayer;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<ItemStack> getLeftOvers() {
        return leftOvers;
    }

    public void setLeftOvers(List<ItemStack> leftOvers) {
        this.leftOvers = leftOvers;
    }

    @Override
    public Group getGroup() {
        return group;
    }
}
