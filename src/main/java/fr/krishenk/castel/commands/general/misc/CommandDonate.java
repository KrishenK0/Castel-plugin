package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;

public class CommandDonate extends CastelCommand {
    public CommandDonate() {
        super("donate", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.requireArgs(2)) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                if (!cp.hasGuild()) {
                    Lang.NO_GUILD_DEFAULT.sendMessage(player);
                } else if (!cp.hasPermission(StandardGuildPermission.WITHDRAW)) {
                    StandardGuildPermission.WITHDRAW.sendDeniedMessage(player);
                } else {
                    Guild to = context.getGuild(0);
                    Integer donate = context.getInt(1);
                    if (donate != null) {
                        if (donate <= 0) {
                            Lang.COMMAND_DONATE_INVALID.sendMessage(player, "guild", to.getName(), "rp", donate);
                        } else {
                            Guild guild = cp.getGuild();
                            if (guild.getResourcePoints() < donate) {
                                Lang.COMMAND_DONATE_DONT_HAVE.sendMessage(player, "guild", guild.getName(), "rp", donate);
                            } else {
                                guild.addResourcePoints(-donate);
                                to.addResourcePoints(donate);
                                context.sendMessage(Lang.COMMAND_DONATE_DONE, "guild", to.getName(), "rp", donate);
                                for (Player member : to.getOnlineMembers()) {
                                    Lang.COMMAND_DONATE_DONATED.sendMessage(member, "guild", guild.getName(), "rp", donate);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return TabCompleteManager.getGuilds(context.arg(0));
        if (context.isAtArg(1)) return Collections.singletonList("<amount>");
        return emptyTab();
    }
}
