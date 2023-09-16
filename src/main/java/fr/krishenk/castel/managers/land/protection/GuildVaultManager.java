package fr.krishenk.castel.managers.land.protection;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.internal.integer.IntHashSet;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GuildVaultManager implements Listener {
    public static final IntHashSet VIEWERS = new IntHashSet();

    public static void asViewer(Player player) {
        VIEWERS.add(player.getEntityId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player.getUniqueId());
        if (cp.hasGuild()) {
            Guild guild = cp.getGuild();
            if (guild.getChest() == event.getPlayer().getOpenInventory().getTopInventory()) {
                VIEWERS.remove(event.getPlayer().getEntityId());
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (VIEWERS.contains(player.getEntityId())) {
                if (event.getClickedInventory() != player.getOpenInventory().getTopInventory()) {
                    ItemStack clicked = event.getCurrentItem();
                    if (clicked != null) {
                        if (!MiscUpgradeManager.canPlaceItemInGuildChest(clicked)) {
                            Lang.VAULT_BLACKLISTED_ITEM.sendError(player);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
