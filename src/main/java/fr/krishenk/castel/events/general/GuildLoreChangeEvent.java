package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildLoreChangeEvent extends CastelEvent implements Cancellable, PlayerOperator, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private String lore;
    private final Guild guild;
    private CastelPlayer player;

    public GuildLoreChangeEvent(String lore, Guild guild, CastelPlayer player) {
        this.lore = lore;
        this.guild = guild;
        this.player = player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getNewLore() {
        return lore;
    }

    public String getOldLore() {
        return this.guild.getLore();
    }

    public void setNewLore(String lore) {
        this.lore = lore;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
