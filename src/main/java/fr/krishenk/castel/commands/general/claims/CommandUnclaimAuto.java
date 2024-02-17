package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandUnclaimAuto extends CastelCommand {
    public CommandUnclaimAuto(CastelParentCommand parent) {
        super("auto", parent);
    }

    @Override
    public void execute(CommandContext context) {
        Player target = CommandClaimAuto.getTarget(context, CastelPluginPermission.COMMAND_UNCLAIM_AUTO_OTHERS);
        if (target != null) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(target);
            if (!cp.hasPermission(StandardGuildPermission.UNCLAIM)) {
                StandardGuildPermission.UNCLAIM.sendDeniedMessage(target);
            } else {
                boolean wasAutoClaiming = cp.getAutoClaim() == Boolean.FALSE;
                cp.setAutoClaim(wasAutoClaiming ? null : Boolean.FALSE);
                Lang activation = wasAutoClaiming ? Lang.COMMAND_UNCLAIM_AUTO_OFF : Lang.COMMAND_UNCLAIM_AUTO_ON;
                activation.sendMessage(target);
                if (wasAutoClaiming) {
                    if (CommandClaimAuto.cancelActionBar(target))
                        Lang.AUTO_UNCLAIM_ACTIONBAR_DISABLED.sendMessage(target);
                } else {
                    CommandClaimAuto.cancelActionBar(target);
                    if (Config.Claims.ACTIONBAR_AUTO_CLAIM.getManager().getBoolean())
                        CommandClaimAuto.actionBar(target, Lang.AUTO_UNCLAIM_ACTIONBAR_ENABLED);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.assertArgs(1) && context.hasPermission(CastelPluginPermission.COMMAND_UNCLAIM_AUTO_OTHERS) ? context.getPlayers(0) : emptyTab();
    }
}
