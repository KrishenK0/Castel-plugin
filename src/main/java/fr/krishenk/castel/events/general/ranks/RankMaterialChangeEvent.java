package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.libs.xseries.XMaterial;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankMaterialChangeEvent extends RankEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private XMaterial newMaterial;

    public RankMaterialChangeEvent(Rank rank, Group group, CastelPlayer player, XMaterial newColor) {
        super(rank, group, player);
        this.newMaterial = newColor;
    }

    public XMaterial getNewMaterial() {
        return newMaterial;
    }

    public void setNewMaterial(XMaterial newMaterial) {
        this.newMaterial = newMaterial;
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
