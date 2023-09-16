package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;

import java.util.Set;

public class ClaimingHistory {
    private final Set<SimpleChunkLocation> claims;
    private final boolean wasClaimed;

    public ClaimingHistory(Set<SimpleChunkLocation> claims, boolean wasClaimed) {
        this.claims = claims;
        this.wasClaimed = wasClaimed;
    }

    public Set<SimpleChunkLocation> getClaims() {
        return claims;
    }

    public boolean wasClaimed() {
        return wasClaimed;
    }
}
