package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.libs.xseries.XBiome;
import fr.krishenk.castel.utils.string.StringUtils;

public class BiomeClaimResult {
    private final XBiome biome;
    private final long costFactor;
    private final boolean isDisallowed;

    public BiomeClaimResult(XBiome biome, long costFactor, boolean isDisallowed) {
        this.biome = biome;
        this.costFactor = costFactor;
        this.isDisallowed = isDisallowed;
    }

    public XBiome getBiome() {
        return biome;
    }

    public long getCostFactor() {
        return costFactor;
    }

    public boolean isDisallowed() {
        return isDisallowed;
    }

    @Override
    public String toString() {
        return StringUtils.generatedToString(this);
    }
}
