package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandJoin extends CastelCommand {
    public CommandJoin() {
        super("join", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.hasGuild()) {
                Lang.COMMAND_JOIN_ALREADY_IN_GUILD.sendMessage(player);
            } else {
                Guild guild = context.getGuild(0);
                if (guild != null) {
                    if (guild.requiresInvite()) {
                        Lang.COMMAND_JOIN_REQUIRES_INVITE.sendMessage(player, "guild", guild.getName());
                    } else {
                        Lang requirement = CommandAccept.checkRequirementsToJoin(player, guild);
                        if (requirement != null) requirement.sendError(player, (new MessageBuilder()).withContext(player));

                        if (Config.DAILY_CHECKS_ELECTIONS_DISALLOW_JOINS.getBoolean() && ElectionsManager.isAcceptingVotes()) {
                            Lang.COMMAND_JOIN_ELECTIONS.sendMessage(player);
                        } else {
                            cp.joinGuild(guild);
                            for (Player member : guild.getOnlineMembers()) {
                                context.sendMessage(member, Lang.COMMAND_JOIN_JOINED);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer() && !context.assertArgs(1)) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(context.senderAsPlayer());
            return cp.hasGuild() ? emptyTab() : TabCompleteManager.getGuilds(context.arg(0));
        }
        return emptyTab();
    }
}
