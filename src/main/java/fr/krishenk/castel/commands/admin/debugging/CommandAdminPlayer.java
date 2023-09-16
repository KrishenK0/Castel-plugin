package fr.krishenk.castel.commands.admin.debugging;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandAdminPlayer extends CastelCommand {
    public CommandAdminPlayer(CastelParentCommand parent) {
        super("player", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.isPlayer() || !context.requireArgs(1)) {
            String[] args = context.args;
            CommandSender sender = context.getSender();
            OfflinePlayer player;
            if (args.length == 0) player = (OfflinePlayer) sender;
            else {
                player = getPlayer(sender, args[0]);
                if (player == null) return;
            }

            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            boolean hasGuild = guild != null;
            StringBuilder claims = new StringBuilder(cp.getClaims().size() * 30);

            SimpleChunkLocation claim;
            for (SimpleChunkLocation simpleChunkLocation : cp.getClaims()) {
                claim = simpleChunkLocation;
                if (claims.length() != 0) {
                    claims.append(" &8| ");
                }
                claims.append("&5").append(claim.getWorld()).append("&7, &6").append(claim.getX()).append("&7, &6").append(claim.getZ());
            }

            Pair<Integer, Integer> map = cp.getMapSize();
            Lang.COMMAND_ADMIN_PLAYER_INFO.sendMessage(sender, player, "id", player.getUniqueId(), "guild_id", hasGuild ? guild.getId() : "~", "visualizer", cp.isUsingMarkers() ? "&2Yes" : "&cNo", "spy", cp.isSpy() ? "&2Yes" : "&cNo", "invites", cp.getInvites().size(), "auto_claim", cp.getAutoClaim() ? "&2Yes" : "&cNo", "auto_map", cp.isAutoMap() ? "&2Yes" : "&cNo", "map_height", map != null ? map.getKey() : 126, "map_width", map != null ? map.getValue() : 126, "claims", claims.toString(), "compressed", cp.getCompressedData());
            if (args.length > 1 && (args[1].equalsIgnoreCase("fd") || args[1].equalsIgnoreCase("findDuplicates"))) {
                CompletableFuture.runAsync(() -> {
                    List<Guild> found = new ArrayList<>();
                    Collection<Guild> guilds = CastelDataCenter.get().getGuildManager().getGuilds();
                    for (Guild foundGuild : guilds) {
                        if (foundGuild.isMember(player.getUniqueId()) && !cp.getGuildId().equals(foundGuild.getId()))
                            found.add(guild);
                    }

                    if (found.isEmpty()) Lang.COMMAND_ADMIN_PLAYER_NO_DUPLICATES.sendMessage(sender);
                    else {
                        Lang.COMMAND_ADMIN_PLAYER_FOUND.sendMessage(sender, "name", player.getName(), "uuid", player.getUniqueId());
                        for (Guild foundGuild : found) {
                            Lang.COMMAND_ADMIN_PLAYER_GUILD.sendMessage(sender, (new MessageBuilder()).withContext(foundGuild).raw("uuid", foundGuild.getId()));
                        }
                    }
                }).exceptionally((ex) -> {
                    ex.printStackTrace();
                    return null;
                });
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? context.getPlayers(0) : context.isAtArg(1) ? tabComplete("findDuplicates") : emptyTab();
    }
}
