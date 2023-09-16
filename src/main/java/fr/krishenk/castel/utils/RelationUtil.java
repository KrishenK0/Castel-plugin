package fr.krishenk.castel.utils;

import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.GroupRelationshipChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RelationUtil {
    public static List<String> getRelationableGuilds(CommandTabContext context, Guild guild, GuildRelation relation) {
        return context.getGuilds(0, (x) -> !guild.getName().equalsIgnoreCase(x) && guild.getRelationWith(Guild.getGuild(x)) != relation);
    }

    public static int maxRelationExceed(Group group, GuildRelation relation) {
        int relations = group.countRelationships(relation);
        int maxRelations = Config.Relations.RELATIONS.getManager().withProperty(StringUtils.configOption(relation)).withProperty("limit").getInt();
        return maxRelations > 0 && relations >= maxRelations ? maxRelations : 0;
    }

    public static List<String> handleRelationTab(CommandTabContext context, GuildRelation relation) {
        if (context.isPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.hasGuild()) {
                Guild guild = cp.getGuild();
                return getRelationableGuilds(context, guild, relation);
            }
        }
        return new ArrayList<>();
    }

    public static boolean event(CastelPlayer player, Group first, Group second, GuildRelation relation) {
        GroupRelationshipChangeEvent event = new GroupRelationshipChangeEvent(player, first, second, relation);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public static GroupRelationshipChangeEvent acceptRequest(Player player, Group group, Group to, GuildRelation relation, Messenger notifier) {
        return acceptRequest(player, group, to, relation, notifier, notifier);
    }

    public static GroupRelationshipChangeEvent acceptRequest(Player player, Group group, Group to, GuildRelation relation, Messenger senderNotifier, Messenger receiverNotifier) {
        Objects.requireNonNull(relation);
        GroupRelationshipChangeEvent event = new GroupRelationshipChangeEvent(CastelPlayer.getCastelPlayer(player), group, to, relation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        MessageBuilder settings = (new MessageBuilder()).withContext(player).parse("guild", to.getName());
        for (Player member : group.getOnlineMembers()) {
            senderNotifier.sendMessage(member, settings);
        }

        settings.parse("guild", group.getName());
        for (Player member : to.getOnlineMembers()) {
            receiverNotifier.sendMessage(member, settings);
        }

        if (relation == GuildRelation.NEUTRAL) {
            group.getRelations().remove(to.getId());
            to.getRelations().remove(group.getId());
        } else {
            group.getRelations().put(to.getId(), relation);
            to.getRelations().put(group.getId(), relation);
        }
        group.getRelationshipRequests().remove(to.getId());
        to.getRelationshipRequests().remove(group.getId());
        return event;
    }

    public static boolean hasAnyRelationManagementPermission(CastelPlayer cp) {
        if (!cp.hasAnyPermision(StandardGuildPermission.ALLIANCE, StandardGuildPermission.TRUCE, StandardGuildPermission.ENEMY)) {
            Lang.RELATIONS_NO_PERMISSION.sendError(cp.getPlayer());
            return false;
        }
        return true;
    }

    public static Lang getAcceptMsgOf(GuildRelation relation) {
        switch (relation) {
            case ALLY: return Lang.COMMAND_ALLY_ALLIES;
            case TRUCE: return Lang.COMMAND_TRUCE_TRUCES;
            case NEUTRAL: return Lang.COMMAND_REVOKE_NEUTRALS;
            default: return null;
        }
    }
}
