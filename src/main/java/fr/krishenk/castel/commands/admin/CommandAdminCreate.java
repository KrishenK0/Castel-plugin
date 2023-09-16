package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.general.claims.CommandClaimSquare;
import fr.krishenk.castel.commands.general.home.CommandSetHome;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;

public class CommandAdminCreate extends CastelCommand {
    public CommandAdminCreate(CastelParentCommand parent) {
        super("create", parent);
    }

    public static void superCreate(Player player) {
        int name;
        name = 0;
        while (Guild.getGuild(String.valueOf(name)) != null) {
            ++name;
        }
        Guild guild = new Guild(player.getUniqueId(), String.valueOf(name));
        initSuperGuild(player, guild);
    }

    public static void initSuperGuild(Player player, Guild guild) {
        guild.addResourcePoints(MathUtils.randInt(5000, 10000));
        guild.addBank(MathUtils.randInt(5000, 10000));
        SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
        Pair<SimpleChunkLocation, SimpleChunkLocation> corners = CommandClaimSquare.getCenteredSquareCorners(chunk, 1);
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> result = CommandClaimSquare.claimSquare(corners.getKey(), corners.getValue(), cp, guild, false);
        result.forEach((k, v) -> guild.claim(k.inWorld(player.getWorld()), cp, ClaimLandEvent.Reason.ADMIN));
        CommandSetHome.setHome(guild, player.getLocation().add(0.0, 1.0, 0.0), cp);
        XSound.play(player, Config.CREATION_GUILDS_SOUND.getString());
    }

    @Override
    public void execute(CommandContext context) {
        if (context.argEquals(0, "^")) {
            if (!context.assertPlayer()) {
                if (!context.assertHasGuild()) {
                    context.sendError(Lang.COMMAND_CREATE_ALREADY_IN_GUILD);
                } else {
                    context.sendMessage(Lang.COMMAND_ADMIN_CREATE_SUPER_CREATING);
                    superCreate(context.senderAsPlayer());
                }
            }
        } else if (!context.assertArgs(2)) context.sendError(Lang.COMMAND_ADMIN_CREATE_USAGE);
        else {
            OfflinePlayer player;
            if (context.argEquals(0, "*")) {
                player = PlayerUtils.getFirstPlayerThat(pl -> !CastelPlayer.getCastelPlayer(pl).hasGuild());
                if (player == null) context.sendError(Lang.COMMAND_ADMIN_CREATE_ANY_NO_PLAYER_FOUND);
            } else {
                player = context.getOfflinePlayer(0);
            }

            if (player != null) {
                context.var("target", player.getName());
                String name = context.arg(1);
                Guild renameGuild = Guild.getGuild(name);
                context.var("guild", name);
                if (renameGuild != null) context.sendError(Lang.COMMAND_ADMIN_CREATE_ALREADY_EXISTS);
                else {
                    new Guild(player.getUniqueId(), name);
                    context.sendMessage(Lang.COMMAND_ADMIN_CREATE_CREATED);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return context.add(context.getPlayers(0), "^", "*");
        return context.isAtArg(1) && !context.arg(0).equals("^") ? tabComplete("&2<name>") : emptyTab();
    }
}
