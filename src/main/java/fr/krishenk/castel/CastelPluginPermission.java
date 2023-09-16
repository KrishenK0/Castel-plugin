package fr.krishenk.castel;

import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Locale;

public enum CastelPluginPermission {
    DEBUG(),
    UPDATES(),
    STRUCTURES_WARPPAD_BYPASS_COUNTDOWN(),
    COMMAND_EXTRACTOR_OTHERS(),
    COMMAND_INVENTORY_OTHERS(),
    COMMAND_VAULT_OTHERS(),
    COMMAND_PVP_OTHERS(),
    COMMAND_SEAK_OTHERS(),
    COMMAND_HOME_OTHERS(PermissionDefault.TRUE),
    COMMAND_NATION_HOME_OTHERS(),
    COMMAND_FLY_OTHERS(),
    COMMAND_HOME_BYPASS_TIMER(),
    COMMAND_UNCLAIM_CHUNK(),
    COMMAND_SNEAK_OTHERS(),
    COMMAND_TELEPORT_BYPASS_TIMER(),
    COMMAND_NATION_HOME_BYPASS_TIMER(),
    COMMAND_UPDATES_FORCE(),
    COMMAND_UPDATES_DOWNLOAD(),
    COMMAND_CLAIM_AUTO_OTHERS(),
    COMMAND_UNCLAIM_AUTO_OTHERS(),
    PROTECTION$SIGNS_USE(PermissionDefault.TRUE),
    PROTECTION$SIGNS_USE_CREATIVE(),
    PROTECTION$SIGNS_OPEN(),
    PROTECTION$SIGNS_BREAK(),
    TELEPORT_TO_CLAIMS(),
    CREATIVE_PICKUP(),
    INVENTORY_BYPASS(),
    NEXUS_REMOVE(),
    CHAT_COLORS(),
    CHAT_TAG(),
    LAND_INTERACT(),
    LAND_BUILD_BREAK(),
    LAND_BUILD_PLACE(),
    LAND_BYPASS_CREATIVE(),
    COMMAND_VISUALIZE_DETAILS(),
    COMMAND_SHOW_ADMIN(),
    COMMAND_SHOW_OTHERS(),
    COMMAND_SHOW_SEE_VANISHED(),
    COMMAND_NATION_SHOW_OTHERS(),
    COMMAND_NATION_SHOW_ADMIN(),
    COMMAND_CLAIM_CHUNK(PermissionDefault.TRUE),
    CHAT_BYPASS_RANGED(PermissionDefault.TRUE),
    TURRETS_INTERACT(),
    TURRETS_BUILD_PLACE(),
    TURRETS_BUILD_BREAK(),
    STRUCTURES_INTERACT(),
    STRUCTURES_BUILD_PLACE(),
    STRUCTURES_BUILD_BREAK(),
    SHOW_HIDDEN_GROUPS(),
    FLIGHT_LANDS(),
    FLIGHT_DAMAGE(),
    FLIGHT_NEARBY$ENEMIES(),
    FLIGHT_BYPASS_CHARGES(),
    GUIS_BYPASS_CREATIVE(),
    SEE_OTHERS_HOLOGRAMS(),
    SILENT_JOIN(PermissionDefault.FALSE),
    SILENT_LEAVE(PermissionDefault.FALSE);
    private final Permission permission;

    private CastelPluginPermission() {
        this(PermissionDefault.OP);
    }

    private CastelPluginPermission(PermissionDefault permissionDefault) {
        String namespace = CastelPlugin.getInstance().getName().toLowerCase(Locale.ENGLISH);
        this.permission = new Permission(namespace + '.' + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.').replace('$', '-'), permissionDefault);
        Bukkit.getPluginManager().addPermission(this.permission);
    }

    public static void init() {
    }

    public boolean hasPermission(CommandSender sender, boolean checkAdminMode) {
        if (sender.hasPermission(this.permission)) {
            return true;
        }
        if (!checkAdminMode) {
            return false;
        }
        if (!(sender instanceof Player)) {
            return true;
        }
        return CastelPlayer.getCastelPlayer((OfflinePlayer)((Player)sender)).isAdmin();
    }

    public Permission getPermission() {
        return this.permission;
    }

    public boolean hasPermission(CommandSender sender) {
        return this.hasPermission(sender, false);
    }
}

