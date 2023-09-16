package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.cooldown.EntityCooldown;
import org.bukkit.entity.Player;

public class CommandUnclaimAll extends CastelCommand {
    public CommandUnclaimAll(CastelParentCommand parent) {
        super("all", parent);
    }

    public static int unclaimAll(CastelPlayer cp, UnclaimLandEvent.Reason reason, Guild guild) {
        UnclaimLandEvent event = guild.unclaimIf(cp, reason, null);
        return event.getLandLocations().size();
    }

    private static void unclaimAll(Player player, CastelPlayer cp, Guild guild) {
        int lands = unclaimAll(cp, UnclaimLandEvent.Reason.UNCLAIMED, guild);
        Lang.COMMAND_UNCLAIM_ALL_SUCCESS.sendMessage(player, "lands", lands);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (!cp.hasPermission(StandardGuildPermission.UNCLAIM)) {
                StandardGuildPermission.UNCLAIM.sendDeniedMessage(player);
            } else {
                int cost = Config.Claims.UNCLAIM_ALL_COST.getManager().getInt();
                if (cost != 0 && guild.getResourcePoints() < cost) {

                } else {
                    if (Config.Claims.UNCLAIM_ALL_CONFIRM_ENABLED.getManager().getBoolean()) {
//                        if (Config.Claims.UNCLAIM_ALL_CONFIRM_GUI.getManager().getBoolean()) {
//
//                        }

                        if (!EntityCooldown.add(player, "UNCLAIMALL", Config.Claims.UNCLAIM_ALL_CONFIRM_TIME.getManager().getInt())) {
                            Lang.COMMAND_UNCLAIM_ALL_CONFIRM.sendMessage(player, "lands", guild.getLandLocations().size());
                            return;
                        }
                    }

                    unclaimAll(player, cp, guild);
                }
            }
        }
    }
}
