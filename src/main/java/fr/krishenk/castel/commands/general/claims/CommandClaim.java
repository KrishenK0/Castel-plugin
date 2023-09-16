package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.commands.general.home.CommandSetHome;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.ClaimProcessor;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;


import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandClaim extends CastelParentCommand {
    private static final Cooldown<Integer> CONFIRMATION = new Cooldown<>();

    public CommandClaim() {
        super("claim", true);
        if (!this.isDisabled()) {
            new CommandClaimAuto(this);
            new CommandClaimSquare(this);
            new CommandClaimFill(this);
            new CommandClaimLine(this);
            new CommandClaimConfirm(this);
            new CommandClaimClipboard(this);
            new CommandClaimCorner(this);
            new CommandClaimList(this);
        }
    }

    public static SimpleChunkLocation.WorldlessWrapper getChunkCoords(CommandSender sender, String xString, String zString) {
        if (StringUtils.isNumeric(xString) && StringUtils.isNumeric(zString)) {
            int x = Integer.parseInt(xString);
            int z = Integer.parseInt(zString);
            return new SimpleChunkLocation.WorldlessWrapper(x, z);
        }
        String invalid = StringUtils.isNumeric(xString) ? zString : xString;
        Lang.INVALID_NUMBER.sendError(sender, "arg", invalid);
        return null;
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            if (!Config.DISABLED_WORLDS.isInDisabledWorld(player, Lang.DISABLED_WORLD)) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                if (!cp.hasGuild()) {
                    context.sendError(Lang.NO_GUILD_DEFAULT);
                } else if (!cp.hasPermission(StandardGuildPermission.CLAIM)) {
                    StandardGuildPermission.CLAIM.sendDeniedMessage(player);
                } else {
                    Guild guild = cp.getGuild();
                    Location loc = player.getLocation();
                    SimpleChunkLocation chunk = SimpleChunkLocation.of(loc);
                    boolean home = true;
                    if (context.assertArgs(1)) {
                        if (!context.assertArgs(2)) {
                            context.sendError(Lang.COMMAND_CLAIM_CHUNK_USAGE);
                            super.execute(context);
                            return;
                        }

                        if (!context.hasPermission(CastelPluginPermission.COMMAND_CLAIM_CHUNK)) {
                            Lang.COMMAND_CLAIM_CHUNK_PERMISSION.sendError(player);
                            return;
                        }

                        SimpleChunkLocation.WorldlessWrapper coords = getChunkCoords(player, context.arg(0), context.arg(0));
                        if (coords == null) return;

                        chunk = coords.inWorld(player.getWorld());
                        int distance = (int) chunk.distance(SimpleChunkLocation.of(loc));
                        int maxDistance = Config.Claims.COORDINATES_CLAIM_MAX_DISTANCE.getManager().getInt();
                        if (distance > maxDistance) {
                            Lang.COMMAND_CLAIM_CHUNK_MAX_DISTANCE.sendError(player, "distance", distance, "max", maxDistance);
                            return;
                        }

                        loc = chunk.getCenterLocation();
                        home = false;
                    }

                    ClaimProcessor processor = (new ClaimProcessor(chunk, cp, guild)).process();
                    if (!processor.isSuccessful()) {
                        processor.sendIssue(context.getSender());
                    } else {
                        if (processor.hasCosts() && !cp.isAdmin()) {
                            Long confirmation = Config.Claims.CONFIRMATION.getManager().getTimeMillis(TimeUnit.SECONDS);
                            if (confirmation != null && confirmation > 0L && !CONFIRMATION.isInCooldown(player.getEntityId())) {
                                Lang.COMMAND_CLAIM_CONFIRMATION.sendMessage(player, processor.getContextHolder());
                                CONFIRMATION.add(player.getEntityId(), confirmation);
                                return;
                            }
                        }

                        processor.finalizeRequest();
                        if (processor.shouldOverclaim())
                            chunk.getLand().unclaim(cp, UnclaimLandEvent.Reason.OVERCLAIM, false);

                        if (!guild.claim(chunk, cp, ClaimLandEvent.Reason.CLAIMED).isCancelled()) {
                            CONFIRMATION.stop(player.getEntityId());
                            Lang.COMMAND_CLAIM_SUCCESS.sendMessage(player, "x", chunk.getX(), "z", chunk.getZ());
                            if (home && Config.HOME_SET_ON_FIRST_CLAIM.getBoolean() && guild.getLandLocations().size() == 1)
                                CommandSetHome.setHome(guild, loc, cp);

                            (new LandVisualizer()).forPlayer(player, cp).forLand(chunk.getLand(), chunk.toChunk()).display(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) {
            List<String> cmds = TabCompleteManager.getSubCommand(context.getSender(), this, context.getArgs());
            if (context.hasPermission(CastelPluginPermission.COMMAND_CLAIM_CHUNK) && cmds.isEmpty() && context.isNumber(0))
                cmds.addAll(tabComplete("&2<x>"));

            return cmds;
        }
        return context.isAtArg(1) ? tabComplete("&2<z>") : emptyTab();
    }
}
