package fr.krishenk.castel.managers.abstraction;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.Function;

public class MoveSensitiveAction {
    public Function<PlayerMoveEvent, Boolean> onMove;
    public Function<EntityDamageEvent, Boolean> onDamage;

    public MoveSensitiveAction() {}

    public void onMove(Function<PlayerMoveEvent, Boolean> handler) {
        this.onMove = handler;
    }

    public void onDamage(Function<EntityDamageEvent, Boolean> handler) {
        this.onDamage = handler;
    }

    public void onAnyMove(Function<Player, Boolean> handler) {
        this.onDamage = (event) -> handler.apply((Player) event.getEntity());
        this.onMove = (event) -> handler.apply(event.getPlayer());
    }
}
