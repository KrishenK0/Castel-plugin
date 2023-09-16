package fr.krishenk.castel.constants.group.model;

import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

public class InviteCode {
    private final String code;
    private final long createdAt;
    private long expiration;
    private final UUID createdBy;
    private final Set<UUID> usedBy;
    private int uses;

    public InviteCode(String code, long createdAt, long expiration, UUID createdBy, Set<UUID> usedBy, int uses) {
        this.code = code;
        this.createdAt = createdAt;
        this.redeemFor(expiration);
        this.createdBy = createdBy;
        this.usedBy = usedBy;
        this.setUses(uses);
    }

    public int getUses() {
        return this.uses;
    }

    public boolean isAllUsed() {
        return this.uses != 0 && this.usedBy.size() >= this.uses;
    }

    public OfflinePlayer getCreator() {
        return Bukkit.getOfflinePlayer(this.createdBy);
    }

    public UUID getCreatedBy() {
        return this.createdBy;
    }

    private void redeemFor(long expiresIn) {
        if (expiresIn < 0L)
            throw new IllegalArgumentException("Expiration of invite code must be greater than or equal to 0");
        if (expiresIn != 0L)
            TimeUtils.validateUnixTime(expiresIn);
        this.expiration = expiresIn;
    }

    public Set<UUID> getUsedBy() {
        return this.usedBy;
    }

    public void setUses(int uses) {
        if (uses < 0)
            throw new IllegalArgumentException("Invite code uses must be greater than or equal to 0");
        this.uses = uses;
    }

    public boolean hasExpired() {
        if (this.expiration == 0L) return false;
        return System.currentTimeMillis() >= this.expiration;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getCode() {
        return code;
    }
}
