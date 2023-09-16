package fr.krishenk.castel.utils.debugging;

import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DebugSettings {
    private boolean isWhitelist;
    @NotNull
    private final Set<DebugNS> list;
    @NotNull
    public static final Map<UUID, DebugSettings> SETTINGS = new NonNullMap<UUID, DebugSettings>();

    public DebugSettings(boolean isWhitelist, @NotNull Set<DebugNS> list) {
        this.isWhitelist = isWhitelist;
        this.list = list;
    }

    public final boolean isWhitelist() {
        return this.isWhitelist;
    }

    public final void setWhitelist(boolean whitelist) {
        this.isWhitelist = whitelist;
    }

    @NotNull
    public final Set<DebugNS> getList() {
        return this.list;
    }

    public DebugSettings() {
        this(false, new HashSet<>());
    }


    @NotNull
    public static DebugSettings getSettings(@NotNull CommandContext context) {
        final CommandSender sender = context.getSender();
        return getSettings(sender);
    }


    @NotNull
    public static DebugSettings getSettings(@NotNull CommandSender sender) {
        final UUID id = (sender instanceof Player) ? ((Player)sender).getUniqueId() : FastUUID.ZERO;
        return DebugSettings.SETTINGS.computeIfAbsent(id, (e) -> new DebugSettings());
    }
}

