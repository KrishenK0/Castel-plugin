package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildPermission;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RankPermissionChangeEvent extends RankEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private Set<GuildPermission> newPermission;

    public RankPermissionChangeEvent(Rank rank, Group group, CastelPlayer player, Set<GuildPermission> newPermission) {
        super(rank, group, player);
        this.newPermission = newPermission;
    }

    public Set<GuildPermission> getNewPermission() {
        return newPermission;
    }

    public void setNewPermission(Set<GuildPermission> newPermission) {
        this.newPermission = newPermission;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
