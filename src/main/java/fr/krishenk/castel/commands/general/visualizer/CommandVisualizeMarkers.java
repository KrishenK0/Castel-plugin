package fr.krishenk.castel.commands.general.visualizer;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CommandVisualizeMarkers extends CastelCommand {
    public CommandVisualizeMarkers(CastelParentCommand parent) {
        super("markers", parent);
        registerMarkerPermissions();
    }

    private static Map<String, String> getTypes() {
        Map<String, String> types = new HashMap<>();
        types.put(Config.Claims.INDICATOR_DEFAULT_NAME.getManager().getString(), "");
        types.put(Config.Claims.INDICATOR_VISUALIZER_NAME.getManager().getString(), "BLOCKS");
        ConfigurationSection section = Config.Claims.INDICATOR_PARTICLES.getManager().getSection();
        if (section != null) {
            for (String key : section.getKeys(true)) {
                String name = section.getString(key + ".name");
                if (name != null) types.put(name, key);
            }
        }
        return types;
    }

    private static void registerMarkerPermissions() {
        for (String type : getTypes().values()) {
            String perm = type.toLowerCase(Locale.ENGLISH);
            if (perm.isEmpty()) perm = "default";
            String name = "castem.marker." + perm;
            PluginManager manager = Bukkit.getPluginManager();
            if (manager.getPermission(name) == null) {
                Permission permission = new Permission(name, "Castel land visualizer marker type", PermissionDefault.TRUE);
                manager.addPermission(permission);
            }
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            Player player = context.senderAsPlayer();
            String type = context.arg(0);
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            String defaultMethod = Config.Claims.INDICATOR_DEFAULT_METHOD.getManager().getString();
            Map<String, String> types = getTypes();
            String markers = cp.getMarkersType() == null ? defaultMethod : cp.getMarkersType();
            String realType = null;
            for (Map.Entry<String, String> tp : types.entrySet()) {
                if (tp.getKey().equalsIgnoreCase(type)) {
                    realType = tp.getValue();
                    break;
                }
            }

            if (realType == null) {
                Lang.COMMAND_VISUALIZE_MARKERS_INVALID.sendError(player, "markers", type);
            } else if (markers.equalsIgnoreCase(realType)) {
                Lang.COMMAND_VISUALIZE_MARKERS_ALREADY_USING.sendError(player, "markers", markers);
            } else {
                if (realType.isEmpty()) cp.setMarkersType(null);
                else cp.setMarkersType(realType);
                Lang.COMMAND_VISUALIZE_MARKERS_CHANGED.sendMessage(player, "markers", type);
                SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
                Land land = chunk.getLand();
                (new LandVisualizer()).forPlayer(player, cp).forLand(land, chunk.toChunk()).display(true);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.assertArgs(1) ? new ArrayList<>(getTypes().keySet()) : new ArrayList<>();
    }
}
