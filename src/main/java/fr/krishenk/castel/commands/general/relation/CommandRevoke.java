package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.RelationUtil;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandRevoke extends CastelCommand {
    public CommandRevoke() {
        super("revoke", true);
    }

    static StandardGuildPermission getPermissionForRelation(GuildRelation relation) {
        switch (relation) {
            case ALLY: return StandardGuildPermission.ALLIANCE;
            case TRUCE: return StandardGuildPermission.TRUCE;
            case ENEMY: return StandardGuildPermission.ENEMY;
            default: return null;
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            Guild guild = cp.getGuild();
            Guild to = context.getGuild(0);
            if (to != null) {
                context.getSettings().other(to);
                context.var("guild", to.getName());
                if (RelationUtil.hasAnyRelationManagementPermission(cp)) {
                    if (guild.getId().equals(to.getId())) {
                        context.sendError(Lang.COMMAND_REVOKE_SELF);
                    } else {
                        GuildRelation relation = guild.getRelationWith(to);
                        context.var("relation", relation.getColor() + relation.getName().buildPlain(context.getSettings()));
                        if (relation == GuildRelation.NEUTRAL) {
                            context.sendError(Lang.COMMAND_REVOKE_ALREADY_NEUTRAL);
                        } else if (!cp.hasPermission(getPermissionForRelation(relation))) {
                            context.sendError(Lang.COMMAND_REVOKE_SPECIFIC_PERMISSION);
                        } else {
                            if (relation == GuildRelation.ENEMY) {
                                GuildRelationshipRequest request = guild.getRelationshipRequests().remove(to.getId());
                                if (request != null) {
                                    GuildRelation requestRelation = request.getRelation();
                                    if (requestRelation != GuildRelation.NEUTRAL) {
                                        Lang.RELATIONS_ANOTHER_REQUEST.sendError(player, "guild", to.getName(), "relation", requestRelation.getColor() + requestRelation.getName().buildPlain(context.getSettings()));
                                        return;
                                    }

                                    RelationUtil.acceptRequest(player, guild, to, GuildRelation.NEUTRAL, Lang.COMMAND_REVOKE_NEUTRALS);
                                    return;
                                }

                                if (guild.sendRelationshipRequest(cp, to, GuildRelation.NEUTRAL).isCancelled()) return;

                                context.sendMessage(Lang.COMMAND_REVOKE_REQUEST_SENDER);
                                for (Player member : to.getOnlineMembers()) {
                                    Lang.COMMAND_REVOKE_REQUEST_RECEIVER.sendMessage(member, "guild", guild.getName(), "relation", GuildRelation.NEUTRAL.getColor() + GuildRelation.NEUTRAL.getName().buildPlain(context.getSettings()));
                                }
                            } else {
                                RelationUtil.acceptRequest(player, guild, to, GuildRelation.NEUTRAL, Lang.COMMAND_REVOKE_SENDER, Lang.COMMAND_REVOKE_RECEIVER);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isPlayer() && context.isAtArg(0)) {
            Guild guild = context.getGuild();
            return guild == null ? emptyTab() : context.suggest(0, guild.getRelations().keySet().stream().map(Guild::getGuild).map(Group::getName).toArray(String[]::new));
        }
        return emptyTab();
    }
}
