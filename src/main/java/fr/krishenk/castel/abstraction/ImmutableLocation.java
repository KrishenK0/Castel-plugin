package fr.krishenk.castel.abstraction;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ImmutableLocation extends Location {
    public ImmutableLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    private final Void modify() {
        throw new UnsupportedOperationException("Cannot modify immutable location");
    }

    @Override
    public void setWorld(@Nullable World world) {
        if (getWorld() != null) {
            World world2 = getWorld();
            World world3 = world;
            if (!Objects.equals(world2 != null ? world2.getUID() : null, world3 != null ? world3.getUID() : null)) {
                modify();
                throw new IllegalArgumentException("Invalid world UID");
            }
        }
        super.setWorld(world);
    }

    @Override
    public void setX(double x) {
        this.modify();
    }

    @Override
    public void setY(double y) {
        this.modify();
    }

    @Override
    public void setZ(double z) {
        this.modify();
    }

    @Override
    public void setYaw(float yaw) {
        this.modify();
    }

    @Override
    public void setPitch(float pitch) {
        this.modify();
    }

    @NotNull
    @Override
    public Location setDirection(@NotNull Vector vector) {
        Location location = this.clone().setDirection(vector);
        return location;
    }

    @NotNull
    @Override
    public Location add(@NotNull Vector vec) {
        Location location = this.clone().add(vec);
        return location;
    }

    @NotNull
    @Override
    public Location add(@NotNull Location vec) {
        Location location = this.clone().add(vec);
        return location;
    }

    @NotNull
    @Override
    public Location add(double x, double y, double z) {
        Location location = this.clone().add(x, y, z);
        return location;
    }

    @NotNull
    @Override
    public Location subtract(@NotNull Vector vec) {
        Location location = this.clone().subtract(vec);
        return location;
    }

    @NotNull
    @Override
    public Location subtract(@NotNull Location vec) {
        Location location = this.clone().subtract(vec);
        return location;
    }

    @NotNull
    @Override
    public Location subtract(double x, double y, double z) {
        Location location = this.clone().subtract(x, y, z);
        return location;
    }

    @NotNull
    @Override
    public Location multiply(double m) {
        Location location = this.clone().multiply(m);
        return location;
    }

    @Nullable
    public static final ImmutableLocation of(Location location) {
        ImmutableLocation immutableLocation = null;
        if (location != null) {
            immutableLocation = location instanceof ImmutableLocation ? (ImmutableLocation) location : null;
            if (immutableLocation == null)
                immutableLocation = new ImmutableLocation(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        }
        return immutableLocation;
    }
}
