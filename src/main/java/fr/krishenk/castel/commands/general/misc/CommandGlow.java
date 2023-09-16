package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.internal.integer.IntHashSet;
import fr.krishenk.castel.utils.scoreboards.Glow;
import fr.krishenk.castel.utils.scoreboards.XScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CommandGlow extends CastelCommand implements Listener {
    public static final IntHashSet ACTIVATED = new IntHashSet();

    public CommandGlow() {
        super("glow");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ACTIVATED.remove(player.getEntityId())) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild != null) {
                for (Player entity : guild.getOnlineMembers()) {
                    Glow.removeGlow(player);
                }
            }
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (ReflectionUtils.supports(17)) {
            MessageHandler.sendMessage(context.getSender(), "&4This command is currently not supported in 1.17+");
        } else if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            Guild guild = context.getGuild();
            if (guild == null) {
                context.sendError(Lang.NO_GUILD_DEFAULT);
            } else {
                if (ACTIVATED.remove(player.getEntityId())) {
                    Glow.removeGlow(player);
                    context.sendMessage(Lang.COMMAND_GLOW_DISABLED);
                } else {
                    ACTIVATED.add(player.getEntityId());
                    for (Player member : guild.getOnlineMembers()) {
                        if (member != player) {
                            Glow.setGlowing(player, XScoreboard.Color.DARK_RED, member);
                        }
                    }

                    context.sendMessage(Lang.COMMAND_GLOW_ENABLED);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // TODO
    }
}
