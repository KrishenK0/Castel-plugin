package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAdminToggles extends CastelCommand {
    public CommandAdminToggles(CastelParentCommand parent) {
        super("toggles", parent);
    }

    @Override
    public void execute(CommandContext context) {
        context.sendMessage(Lang.COMMAND_ADMIN_TOGGLES_HEADER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.isAdmin()) {
                MessageBuilder settings = new MessageBuilder();
                if (cp.getOfflinePlayer().isOnline()) settings.withContext(cp.getOfflinePlayer().getPlayer());
                else settings.withContext(cp.getOfflinePlayer());

                Lang.COMMAND_ADMIN_TOGGLES_ENTRY.sendMessage(context.getSender(), settings);
            }
        }
    }
}
