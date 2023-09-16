package fr.krishenk.castel.commands.general.home;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.GuildSetHomeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandSetHome extends CastelCommand {
    public CommandSetHome() {
        super("sethome", true);
    }

    public static GuildSetHomeEvent setHome(Guild guild, Location loc, CastelPlayer by) {
        loc = Config.HOME_SAFE.getBoolean() ? LocationUtils.getPreciseLocation(loc) : LocationUtils.roundLocationPrecision(loc);
        return guild.setHome(loc, by);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer()) return CommandResult.FAILED;
        Player player = context.senderAsPlayer();
        if (Config.DISABLED_WORLDS.isInDisabledWorld(player, Lang.DISABLED_WORLD)) return CommandResult.FAILED;
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasGuild()) {
            Lang.NO_GUILD_DEFAULT.sendMessage(player);
            return CommandResult.FAILED;
        } else {
            Guild guild = cp.getGuild();
            if (!cp.hasPermission(StandardGuildPermission.SET_HOME)) {
                StandardGuildPermission.SET_HOME.sendDeniedMessage(player);
                return CommandResult.FAILED;
            } else {
                String[] args = context.args;
                CommandSender sender = context.getSender();
                Land land = Land.getLand(player.getLocation());
                if (land != null && land.isClaimed()) {
                    if (!land.getGuildId().equals(guild.getId())) {
                        Lang.COMMAND_SETHOME_OTHERS_LAND.sendMessage(player);
                        return CommandResult.FAILED;
                    }
                } else if (Config.HOME_CLAIMED.getBoolean()) {
                    Lang.COMMAND_SETHOME_NOT_CLAIMED.sendMessage(sender);
                    return CommandResult.FAILED;
                }

                Location home = player.getLocation();
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "center":
                            home = LocationUtils.cleanLocation(home);
                            break;
                        case "centeraxis":
                            home = LocationUtils.centerAxis(home);
                            break;
                        case "centerview":
                            home = LocationUtils.centerView(home);
                    }
                }

                if (setHome(guild, home, cp).isCancelled()) return CommandResult.FAILED;
                for (Player member : guild.getOnlineMembers()) {
                    Lang.COMMAND_SETHOME_SET.sendMessage(member, "x", home.getBlockX(), "y", home.getBlockY(), "z", home.getBlockZ());
                }

                return CommandResult.SUCCESS;
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return !context.assertPlayer() && !context.assertArgs(1) ? Arrays.asList("center", "centerAxis", "centerView") : new ArrayList<>();
    }
}
