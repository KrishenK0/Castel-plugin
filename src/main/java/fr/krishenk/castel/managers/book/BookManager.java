package fr.krishenk.castel.managers.book;

import fr.krishenk.castel.events.members.GuildLeaveEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import fr.krishenk.castel.utils.nms.XBook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BookManager implements Listener {
    private static final IntHashMap<BookSession> BOOKS = new IntHashMap<>();

    public static void handle(Player player, BookSession session, List<String> pages, String title) {
        BOOKS.put(player.getEntityId(), session);
        ItemStack book = XBook.getBook(pages, player, title, true, true);
        player.getInventory().setItem(session.getSlot(), book);
    }

    public static BookSession getSession(Player player) {
        return BOOKS.get(player.getEntityId());
    }

    public static BookSession removeSession(Player player) {
        return BOOKS.remove(player.getEntityId());
    }

    public static void removeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BookSession slot = BOOKS.get(player.getEntityId());
            if (slot != null) player.getInventory().setItem(slot.getSlot(), null);
        }
    }

    public static BookSession removeBook(Player player) {
        BookSession slot = BOOKS.remove(player.getEntityId());
        if (slot == null) return null;
        player.getInventory().setItem(slot.getSlot(), null);
        return slot;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBookSign(PlayerEditBookEvent event) {
        if (event.isSigning()) {
            Player player = event.getPlayer();
            BookSession editingChapter = BOOKS.get(player.getEntityId());
            if (editingChapter != null) {
                if (editingChapter.getOnSign() != null) {
                    editingChapter.getOnSign().accept(event);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGuildLeave(GuildLeaveEvent event) {
        Player player = event.getPlayer().getPlayer();
        if (player != null) removeBook(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        BookSession slot = BOOKS.get(player.getEntityId());
        if (slot != null) {
            if (slot.getSlot() == player.getInventory().getHeldItemSlot()) {
                event.setCancelled(true);
                Lang.BOOKS_CANT_MOVE.sendError(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBookDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        BookSession slot = BOOKS.get(player.getEntityId());
        if (slot != null) {
            if (slot.getSlot() == player.getInventory().getHeldItemSlot()) {
                Lang.BOOKS_CANT_MOVE.sendError(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        removeBook(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        removeBook(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBookItemMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        BookSession slot = BOOKS.get(player.getEntityId());
        if (slot != null && event.getSlot() == slot.getSlot()) {
            event.setCancelled(true);
            Lang.BOOKS_CANT_MOVE.sendError(player);
        }
    }
}
