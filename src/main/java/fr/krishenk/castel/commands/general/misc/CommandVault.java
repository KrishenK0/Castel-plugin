package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandVault extends CastelCommand {
    public CommandVault() {
        super("vault", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Guild guild;
            if (context.assertArgs(1)) {
                if (!context.hasPermission(CastelPluginPermission.COMMAND_VAULT_OTHERS)) {
                    context.sendError(Lang.COMMAND_VAULT_OTHERS_PERMISSION);
                    return;
                }

                guild = context.getGuild();
                if (guild == null) return;
            } else {
                CastelPlayer cp = context.getCastelPlayer();
                guild = cp.getGuild();
                if (guild == null) {
                    context.sendError(Lang.NO_GUILD_DEFAULT);
                    return;
                }
            }

            Player player = context.senderAsPlayer();
            player.openInventory(guild.getChest());
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) && context.hasPermission(CastelPluginPermission.COMMAND_VAULT_OTHERS, true) ? context.getGuilds(0) : emptyTab();
    }
}
