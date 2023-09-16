package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.LocationLocale;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.PaperUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class CommandAdminLand extends CastelCommand {
    public CommandAdminLand(CastelParentCommand parent) {
        super("land", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(2)) {
            String[] args = context.args;
            CommandSender sender = context.getSender();
            Player player = context.senderAsPlayer();
            int coorIndex = 0;
            World world;
            if (args.length > 2) {
                world = Bukkit.getWorld(args[0]);
                if (world == null) {
                    Lang.COMMAND_ADMIN_LAND_INVALID_WORLD.sendMessage(player, "world", args[0]);
                    return;
                }

                coorIndex = 1;
            } else world = player.getWorld();

            int x;
            int z;
            try {
                x = Integer.parseInt(args[coorIndex]);
                z = Integer.parseInt(args[coorIndex+1]);
            } catch (NumberFormatException e) {
                Lang.COMMAND_ADMIN_LAND_INVALID_COORDINATES.sendMessage(player, "world", world, "x", args[coorIndex], "z", args[coorIndex+1]);
                return;
            }

            context.sendMessage(Lang.COMMAND_ADMIN_LAND_PREPARING);
            SimpleChunkLocation chunk = new SimpleChunkLocation(world.getName(), x, z);
            PaperUtils.prepareChunks(chunk).thenRun(() -> {
                Location playerDir = player.getLocation();
                Location location = chunk.getCenterLocation();
                location.setYaw(playerDir.getYaw());
                location.setPitch(playerDir.getPitch());
                CastelPlugin.taskScheduler().sync().execute(() -> player.teleport(location));
                LocationLocale.of(location).withBuilder(context.getSettings()).build();
                LocationLocale.of(chunk).withBuilder(context.getSettings()).withPrefix("chunk_").build();
                context.sendMessage(Lang.COMMAND_ADMIN_LAND_TELEPORTED);
            });
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) {
            if (StringUtils.isNumeric(context.arg(0))) return tabComplete("<x>");
            else {
                List<String> worlds = Bukkit.getWorlds().stream().map(World::getName).filter(w -> w.startsWith(context.arg(0))).collect(Collectors.toList());
                worlds.add(MessageHandler.colorize("&5<x>"));
                return worlds;
            }
        }
        if (context.isAtArg(1))
            return StringUtils.isNumeric(context.arg(0)) ? tabComplete(MessageHandler.colorize("&5<z>")) : tabComplete(MessageHandler.colorize("&5<x>"));
        return context.isAtArg(2) && !StringUtils.isNumeric(context.arg(0)) ? tabComplete(MessageHandler.colorize("&5<z>")) : emptyTab();
    }
}
