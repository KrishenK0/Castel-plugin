package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandRequireInvites extends CastelCommand {
    public CommandRequireInvites() {
        super("requireInvites", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() || !context.requireArgs(1)) {
            CastelPlayer cp = context.getCastelPlayer();
            if (!cp.hasGuild()) Lang.NO_GUILD_DEFAULT.sendMessage(cp.getPlayer());
            else {
                if (!cp.hasPermission(StandardGuildPermission.SETTINGS))
                    Lang.PERMISSIONS_SETTINGS.sendMessage(cp.getPlayer());
                else {
                    boolean value = context.parseBool(0);
                    cp.getGuild().setRequiresInvite(value);

                    for (Player member : cp.getGuild().getOnlineMembers()) {
                        context.sendMessage(member, value ? Lang.COMMAND_REQUIRE_INVITES_ON : Lang.COMMAND_REQUIRE_INVITES_OFF);
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            if (context.isAtArg(1)) return tabComplete("true", "false");
        }
        return new ArrayList<>();
    }
}
