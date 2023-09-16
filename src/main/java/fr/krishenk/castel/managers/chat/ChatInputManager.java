package fr.krishenk.castel.managers.chat;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.abstraction.MoveSensitiveAction;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ChatInputManager implements Listener {
    private static final IntHashMap<ChatInputHandler<?>> CONVERSATIONS = new IntHashMap<>();

    public static boolean startConversation(Player player, ChatInputHandler<?> handler) {
        Objects.requireNonNull(player, "Player conversation cannot be null");
        Objects.requireNonNull(handler, "Conversation handler cannot be null");
        CONVERSATIONS.put(player.getEntityId(), handler);
        return true;
    }

    public static void endAllConversations() {
        CONVERSATIONS.clear();
    }

    public static boolean isConversing(Player player) {
        return CONVERSATIONS.containsKey(player.getEntityId());
    }

    public static boolean endConversation(Player player) {
         return CONVERSATIONS.remove(player.getEntityId()) != null;
    }

    public static <V> CompletableFuture<V> awaitInput(Player player, Function<String, V> supplier) {
        ChatInputHandler<?> handler = new ChatInputHandler<>();
        CompletableFuture<V> completableFuture = new CompletableFuture<>();
        handler.onInput(event -> {
            String msg = event.getMessage();
            V result = supplier.apply(msg);
            if (result == null) return false;
            else {
                completableFuture.complete(result);
                return true;
            }
        });
        handler.onCancel(() -> completableFuture.complete(null));
        startConversation(player, handler);
        player.closeInventory();
        return completableFuture;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputHandler<?> handler = CONVERSATIONS.get(player.getEntityId());
        if (handler != null) {
            event.setCancelled(true);
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            String translatedCancel = Lang.CHAT_INPUT_CANCEL.parse(player);
            Locale locale = cp.getLanguage().getLocale();
            if (event.getMessage().toLowerCase(locale).equals(translatedCancel.toLowerCase(locale))) {
                endConversation(player);
                handler.onCancel.run();
            } else {
                if (handler.sync && !Bukkit.isPrimaryThread()) {
                    Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> {
                        if (handler.onInput.apply(event)) endConversation(player);
                    });
                } else if (handler.onInput.apply(event)) {
                    endConversation(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCancel(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage().substring(1);
        if (cmd.equalsIgnoreCase("cancel")) {
            ChatInputHandler<?> handler = CONVERSATIONS.get(player.getEntityId());
            if (handler != null) {
                event.setCancelled(true);
                endConversation(player);
                handler.onCancel.run();
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        endConversation(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            int id = event.getEntity().getEntityId();
            MoveSensitiveAction task = CONVERSATIONS.get(id);
            if (task != null && task.onDamage != null && task.onDamage.apply(event)) {
                CONVERSATIONS.remove(id);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), () -> {
            if (LocationUtils.hasMoved(event.getFrom(), Objects.requireNonNull(event.getTo()))) {
                int id = event.getPlayer().getEntityId();
                MoveSensitiveAction task = CONVERSATIONS.get(id);
                if (task != null && task.onMove != null && task.onMove.apply(event))
                    CONVERSATIONS.remove(id);
            }
        });
    }
}
