package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.FlyManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandFly extends CastelCommand {
    public CommandFly() {
        super("fly", true);
    }

    @Override
    public void execute(CommandContext context) {
        boolean admin = false;
        Player player;
        if (context.assertArgs(1)) {
            if (!context.hasPermission(CastelPluginPermission.COMMAND_FLY_OTHERS, true)) {
                context.sendError(Lang.COMMAND_FLY_OTHERS_PERMISSION);
                return;
            }

            player = context.getPlayer(0);
            if (player == null) return;
            admin = true;
        } else {
            if (!context.isPlayer()) {
                context.sendError(Lang.COMMAND_FLY_USAGE);
                return;
            }

            player = context.senderAsPlayer();
        }

        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasGuild()) {
            context.sendError(admin ? Lang.NO_GUILD_TARGET : Lang.NO_GUILD_DEFAULT, "target", player.getName());
        } else {
            if (!cp.isAdmin() && !admin) {
                if (!cp.hasPermission(StandardGuildPermission.FLY)) {
                    StandardGuildPermission.FLY.sendDeniedMessage(player);
                    return;
                }

                Land land = Land.getLand(player.getLocation());
                if ((land == null || !land.isClaimed()) && !Config.GUILD_FLY_ALLOW_UNCLAIMED.getBoolean()) {
                    Lang.COMMAND_FLY_UNCLAIMED.sendMessage(player);
                    return;
                }

                Guild guild = cp.getGuild();
                Guild landGuild = land.getGuild();
                if (!guild.hasAttribute(landGuild, StandardRelationAttribute.FLY)) {
                    Lang.COMMAND_FLY_NOT_ALLOWED.sendMessage(player, "guild", landGuild.getName());
                    return;
                }

                int range = Config.GUILD_FLY_NEARBY_UNFRIENDLY_RANGE.getInt();
                if (range > 0) {
                    for (Entity nearby : player.getNearbyEntities(range, range, range)) {
                        if (nearby instanceof Player) {
                            Player enemy = (Player) nearby;
                            CastelPlayer enemyCp = CastelPlayer.getCastelPlayer(enemy);
                            if (!enemyCp.isInSneakMode() && !guild.hasAttribute(enemyCp.getGuild(), StandardRelationAttribute.CEASEFIRE)) {
                                Lang.COMMAND_FLY_OWN_ENEMY_NEARBY.sendError(player);
                                return;
                            }
                        }
                    }
                }
            }

            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                cp.setFlying(false);
                Lang.COMMAND_FLY_DISABLED.sendMessage(player);
            } else {
                if (Config.GUILD_FLY_CHARGES_ENABLED.getBoolean() && !cp.isAdmin() && !CastelPluginPermission.FLIGHT_BYPASS_CHARGES.hasPermission(player) && !FlyManager.handleCharges(player, cp, Pair.of(Config.GUILD_FLY_CHARGES_PLAYERS_ACTIVATION_COST.getManager(), Lang.COMMAND_FLY_CANT_AFFORD), Pair.of(Config.GUILD_FLY_CHARGES_GUILDS_ACTIVATION_COST.getManager(), Lang.COMMAND_FLY_CANT_AFFORD_GUILD))) {
                    return;
                }

                player.setAllowFlight(true);
                cp.setFlying(true);
                Lang.COMMAND_FLY_ENABLED.sendMessage(player);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) && context.hasPermission(CastelPluginPermission.COMMAND_FLY_OTHERS) ? context.getPlayers(0) :emptyTab();    }
}
