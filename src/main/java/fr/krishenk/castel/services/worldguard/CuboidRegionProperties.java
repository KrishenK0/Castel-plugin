package fr.krishenk.castel.services.worldguard;

public class CuboidRegionProperties {
    public final int minX;
    public final int minZ;
    public final int maxX;
    public final int maxZ;

    public CuboidRegionProperties(int minX, int minZ, int maxX, int maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }
}
