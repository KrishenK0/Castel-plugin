package fr.krishenk.castel.locale;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public abstract class LocationLocale {
    private MessageBuilder messageBuilder = new MessageBuilder();
    private String prefix = "";

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    public void setMessageBuilder(MessageBuilder messageBuilder) {
        this.messageBuilder = Objects.requireNonNull(messageBuilder);
    }

    public LocationLocale withPrefix(String prefix) {
        this.prefix = Objects.requireNonNull(prefix);
        return this;
    }

    public LocationLocale withBuilder(MessageBuilder builder) {
        this.messageBuilder = Objects.requireNonNull(builder);
        return this;
    }

    public abstract void parsedEdit();

    public MessageBuilder build() {
        this.parsedEdit();
        return this.messageBuilder;
    }

    public static LocationLocale of(SimpleLocation location) {
        return new SimpleLocationLocale(location);
    }

    public static LocationLocale of(Location location) {
        return new PreciseLocationLocale(location);
    }

    public static LocationLocale of(SimpleChunkLocation location) {
        return new SimpleChunkLocationLocale(location);
    }

    public String getPrefix() {
        return prefix;
    }

    public static class SimpleLocationLocale extends LocationLocale {
        private final SimpleLocation location;
        public SimpleLocationLocale(SimpleLocation location) {
            this.location = location;
        }

        @Override
        public void parsedEdit() {
            this.getMessageBuilder().raw(this.getPrefix() + "translated-world", LocationUtils.translateWorld(this.location.getWorld())).raw(this.getPrefix() + "world", this.location.getWorld()).raw(this.getPrefix() + 'x', this.location.getX()).raw(this.getPrefix() + 'y', this.location.getY()).raw(this.getPrefix() + 'z', this.location.getZ());
        }
    }

    public static class PreciseLocationLocale extends LocationLocale {
        private final Location location;

        public PreciseLocationLocale(Location location) {
            this.location = location;
        }

        @Override
        public void parsedEdit() {
            World world = this.location.getWorld();
            String worldName = world != null ? world.getName() : "";
            this.getMessageBuilder().raw(this.getPrefix() + "translated-world", LocationUtils.translateWorld(worldName)).raw(this.getPrefix() + "world", worldName).raw(this.getPrefix() + 'x', this.location.getX()).raw(this.getPrefix() + 'y', this.location.getY()).raw(this.getPrefix() + 'z', this.location.getZ()).raw(this.getPrefix() + "yaw", this.location.getYaw()).raw(this.getPrefix() + "pitch", this.location.getPitch());
        }
    }

    public static class SimpleChunkLocationLocale extends LocationLocale {
        private final SimpleChunkLocation location;

        public SimpleChunkLocationLocale(SimpleChunkLocation location) {
            this.location = location;
        }

        @Override
        public void parsedEdit() {
            this.getMessageBuilder().raw(this.getPrefix() + "translated-world", LocationUtils.translateWorld(this.location.getWorld())).raw(this.getPrefix() + "world", this.location.getWorld()).raw(this.getPrefix() + 'x', this.location.getX()).raw(this.getPrefix() + 'z', this.location.getZ());
        }
    }
}
