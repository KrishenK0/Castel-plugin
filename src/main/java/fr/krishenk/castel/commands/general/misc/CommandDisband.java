package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CommandDisband extends CastelCommand {
    private static final Cooldown<Integer> COOLDOWN = new Cooldown<>();
    public CommandDisband() { super("disband", true); }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                if (!cp.getUUID().equals(guild.getLeaderId())) {
                    Lang.COMMAND_DISBAND_LEADER_ONLY.sendMessage(player);
                } else {
                    if (Config.DISBAND_CONFIRM.getBoolean()) {
                        if (!COOLDOWN.add(player.getEntityId(), Config.DISBAND_CONFIRMATION_EXPIRATION.getInt(), TimeUnit.SECONDS)) {
                            Lang.COMMAND_DISBAND_CONFIRMATION.sendMessage(player);
                            return;
                        }
                    }
                    COOLDOWN.stop(player.getEntityId());
                    if (!guild.disband(GroupDisband.Reason.SELF).isCancelled()) {
                        Lang.COMMAND_DISBAND_SUCCESS.sendMessage(player);
                        if (Config.DISBAND_ANNOUNCE.getBoolean()) {
                            for (Player member : guild.getOnlineMembers()) {
                                Lang.COMMAND_DISBAND_ANNOUNCE.sendMessage(member);
                            }
                        }
                        guild.disband(null);
                    }
                }
            }
        }
    }
}
