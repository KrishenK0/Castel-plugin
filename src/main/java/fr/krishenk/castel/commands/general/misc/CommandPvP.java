package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandPvP extends CastelCommand {
    public CommandPvP() {
        super("pvp", true);
    }

    @Override
    public void execute(CommandContext context) {
        Player player;
        if (context.assertArgs(1)) {
            if (!context.hasPermission(CastelPluginPermission.COMMAND_PVP_OTHERS, true)) {
                context.sendError(Lang.COMMAND_PVP_OTHERS_PERMISSION);
                return;
            }

            player = context.getPlayer(0);
            if (player == null) return;
        } else {
            if (context.assertPlayer()) return;

            player = context.senderAsPlayer();
        }

        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        boolean state;
        if (context.assertArgs(2)) state = context.parseBool(1);
        else state = !cp.isPvp();

        context.sendMessage(state ? Lang.COMMAND_PVP_ON : Lang.COMMAND_PVP_OFF);
        cp.setPvp(state);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.hasPermission(CastelPluginPermission.COMMAND_FLY_OTHERS)) {
            if (context.isAtArg(1)) return context.getPlayers(0);
            if (context.isAtArg(2)) return tabComplete("on", "off");
        }
        return emptyTab();
    }
}
