package fr.krishenk.castel.abstraction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class OfflinePlayer implements org.bukkit.OfflinePlayer {
    @NotNull
    private final UUID id;
    @NotNull
    private final String name;

    public OfflinePlayer(@NotNull UUID id, @NotNull String name) {
        this.id = id;
        this.name = name;
    }

    private final UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public UUID getUniqueId() {
        return this.id;
    }

    public boolean hasPlayedBefore() {
        return true;
    }

    public boolean isOnline() {
        return this.getPlayer() != null;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer((UUID) this.id);
    }

    public boolean isOp() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public boolean isBanned() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public boolean isWhitelisted() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void setWhitelisted(boolean value) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public long getFirstPlayed() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public long getLastPlayed() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public Location getBedSpawnLocation() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void setStatistic(@NotNull Statistic statistic, int newValue) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public int getStatistic(@NotNull Statistic statistic) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }

    @NotNull
    public Location getLastDeathLocation() {
        throw new UnsupportedOperationException("Cannot use method on fake offline player");
    }
}
