package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.RelationUtil;
import org.bukkit.entity.Player;

public class CommandRejectRelation extends CastelCommand {
    public CommandRejectRelation() {
        super("rejectrelation", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            Guild guild = cp.getGuild();
            if (RelationUtil.hasAnyRelationManagementPermission(cp)) {
                if (guild.getRelationshipRequests().isEmpty()) {
                    context.sendError(Lang.RELATIONS_NO_REQUESTS);
                } else {
                    Guild rejectedGuild = context.getGuild(0);
                    if (rejectedGuild != null) {
                        context.getSettings().withContext(rejectedGuild);
                        GuildRelationshipRequest request = guild.getRelationshipRequests().remove(rejectedGuild.getId());
                        if (rejectedGuild == null) {
                            context.sendError(Lang.COMMAND_REJECTRELATION_NOT_REQUESTED);
                        } else {
                            GuildRelation relation = request.getRelation();
                            context.var("relation", relation.getColor() + relation.getName().buildPlain(context.getSettings()));
                            context.sendMessage(Lang.COMMAND_REJECTRELATION_REJECTED);
                            context.getSettings().withContext(player);
                            for (Player member : rejectedGuild.getOnlineMembers()) {
                                context.sendMessage(member, Lang.COMMAND_REJECTRELATION_NOTIFICATION);
                            }
                        }
                    }
                }
            }
        }
    }
}
