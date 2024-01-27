package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

import java.util.Map;

public class StandardGuildPermission extends GuildPermission {
    public static final StandardGuildPermission BROADCAST = StandardGuildPermission.register("BROADCAST");
    public static final StandardGuildPermission BUILD = StandardGuildPermission.register("BUILD");
    public static final StandardGuildPermission BUILD_OWNED = StandardGuildPermission.register("BUILD_OWNED");
    public static final StandardGuildPermission CLAIM = StandardGuildPermission.register("CLAIM");
    public static final StandardGuildPermission UNCLAIM = StandardGuildPermission.register("UNCLAIM");
    public static final StandardGuildPermission UNCLAIM_OWNED = StandardGuildPermission.register("UNCLAIM_OWNED");
    public static final StandardGuildPermission EDIT_RANKS = StandardGuildPermission.register("EDIT_RANKS");
    public static final StandardGuildPermission MANAGE_RANKS = StandardGuildPermission.register("MANAGE_RANKS");
    public static final StandardGuildPermission WITHDRAW = StandardGuildPermission.register("WITHDRAW");
    public static final StandardGuildPermission INTERACT = StandardGuildPermission.register("INTERACT");
    public static final StandardGuildPermission USE = StandardGuildPermission.register("USE");
    public static final StandardGuildPermission HOME = StandardGuildPermission.register("HOME");
    public static final StandardGuildPermission SET_HOME = StandardGuildPermission.register("SET_HOME");
    public static final StandardGuildPermission INVITE = StandardGuildPermission.register("INVITE");
    public static final StandardGuildPermission KICK = StandardGuildPermission.register("KICK");
    public static final StandardGuildPermission LORE = StandardGuildPermission.register("LORE");
    public static final StandardGuildPermission FLY = StandardGuildPermission.register("FLY");
    public static final StandardGuildPermission EXCLUDE_TAX = StandardGuildPermission.register("EXCLUDE_TAX");
    public static final StandardGuildPermission TRUCE = StandardGuildPermission.register("TRUCE");
    public static final StandardGuildPermission ENEMY = StandardGuildPermission.register("ENEMY");
    public static final StandardGuildPermission ALLIANCE = StandardGuildPermission.register("ALLIANCE");
    public static final StandardGuildPermission PROTECTION_SIGNS = StandardGuildPermission.register("PROTECTION_SIGNS");
    public static final StandardGuildPermission RELATION_ATTRIBUTES = StandardGuildPermission.register("RELATION_ATTRIBUTES");
    public static final StandardGuildPermission SETTINGS = StandardGuildPermission.register("SETTINGS");
    public static final StandardGuildPermission UPGRADE = StandardGuildPermission.register("UPGRADE");
    public static final StandardGuildPermission INSTANT_TELEPORT = StandardGuildPermission.register("INSTANT_TELEPORT");
    public static final StandardGuildPermission INVSEE = StandardGuildPermission.register("INVSEE");
    public static final StandardGuildPermission READ_MAILS = StandardGuildPermission.register("READ_MAILS");
    public static final StandardGuildPermission MANAGE_MAILS = StandardGuildPermission.register("MANAGE_MAILS");
    public static final StandardGuildPermission VIEW_LOGS = StandardGuildPermission.register("VIEW_LOGS");
    private final Lang deniedMessage = Lang.valueOf("PERMISSIONS_" + this.getNamespace().getKey());
    public StandardGuildPermission(String name) {
        super(Namespace.castel(name));
    }

    public static void init() {}

    static StandardGuildPermission register(String name) {
        StandardGuildPermission permission = new StandardGuildPermission(name);
        Map<Namespace, GuildPermission> registry = CastelPlugin.getInstance().getPermissionRegistry().getRawRegistry();
        permission.setHash(registry.size());
        registry.put(permission.getNamespace(), permission);
        return permission;
    }

    public void sendDeniedMessage(Player player) {
        this.deniedMessage.sendMessage(player);
        Config.errorSound(player);
    }

    public Lang getDeniedMessage() {
        return deniedMessage;
    }
}
