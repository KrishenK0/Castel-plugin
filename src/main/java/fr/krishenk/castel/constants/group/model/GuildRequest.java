package fr.krishenk.castel.constants.group.model;

import fr.krishenk.castel.constants.player.CastelPlayer;

import java.util.UUID;

public abstract class GuildRequest {
    private final UUID sender;
    private final long acceptTime;
    private final long timestamp;

    public GuildRequest(UUID sender, long acceptTime, long timestamp) {
        this.sender = sender;
        this.acceptTime = acceptTime;
        this.timestamp = timestamp;
    }

    public GuildRequest(UUID sender, long acceptTime) {
        this(sender, acceptTime, System.currentTimeMillis());
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getAcceptTime() {
        return this.acceptTime;
    }

    public long getTimeLeftToAccept() {
        long passed = System.currentTimeMillis() - this.timestamp;
        return Math.max(0L, this.acceptTime - passed);
    }

    public boolean canAccept() {
        return this.acceptTime > 0L;
    }

    public UUID getSender() {
        return this.sender;
    }

    public CastelPlayer getCastelPlayer() {
        return CastelPlayer.getCastelPlayer(this.sender);
    }

    public int hashCode() {
        int prime = 31;
        int result = 14;
        result = prime * result + this.sender.hashCode();
        result = prime * result + Long.hashCode(this.acceptTime);
        result = prime * result + Long.hashCode(this.timestamp);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GuildRequest)) return false;
        GuildRequest request = (GuildRequest) obj;
        return this.acceptTime == request.acceptTime && this.timestamp == request.timestamp && this.sender.equals(request.sender);
    }
}
