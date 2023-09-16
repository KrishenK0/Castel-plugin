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

public class CommandSneak extends CastelCommand {
    public CommandSneak() {
        super("sneak", true);
    }

    @Override
    public void execute(CommandContext context) {
        Player player;
        if (context.assertArgs(1)) {
            if (!context.hasPermission(CastelPluginPermission.COMMAND_SNEAK_OTHERS, true)) {
                context.sendError(Lang.COMMAND_SNEAK_OTHERS_PERMISSION);
                return;
            }

            player = context.getPlayer(0);
            if (player == null) return;
        } else {
            if (context.assertPlayer()) return;

            player = context.senderAsPlayer();
        }

        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        boolean pvp = cp.isInSneakMode();
        cp.setSneakMode(!pvp);
        context.sendMessage(pvp ? Lang.COMMAND_SNEAK_DISABLED : Lang.COMMAND_SNEAK_ENABLED);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) && context.hasPermission(CastelPluginPermission.COMMAND_SNEAK_OTHERS, true) ? context.getPlayers(0) : emptyTab();
    }
}
