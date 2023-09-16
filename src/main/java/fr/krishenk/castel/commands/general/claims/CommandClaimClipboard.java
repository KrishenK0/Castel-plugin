package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.managers.land.claiming.ClaimProcessor;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommandClaimClipboard extends CastelCommand {
    public CommandClaimClipboard(CastelParentCommand parent) {
        super("clipboard", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            ClaimClipboard session = ClaimClipboard.getClipboard().get(player.getUniqueId());
            if (session == null) {
                context.sendError(Lang.COMMAND_CLAIM_CLIPBOARD_EMPTY);
            } else if (!session.getWorld().getUID().equals(player.getWorld().getUID())) {
                context.sendError(Lang.COMMAND_CLAIM_CLIPBOARD_DIFFERENT_WORLD, "world", player.getWorld().getName());
            } else {
                Action action;
                if (context.assertArgs(1)) {
                    try {
                        action = CommandClaimClipboard.Action.valueOf(context.arg(0).toUpperCase(Locale.ENGLISH));
                    } catch (IllegalArgumentException e) {
                        context.sendError(Lang.COMMAND_CLAIM_CLIPBOARD_UNKNOWN_ACTION, "action", context.arg(0));
                        return;
                    }
                } else action = null;

                if (action != null && action != Action.CLEAR && !context.assertArgs(3)) {
                    context.sendError(Lang.COMMAND_CLAIM_CLIPBOARD_USAGE);
                } else {
                    CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                    Guild guild = Objects.requireNonNull(cp.getGuild(), () -> "Player without a guild has a clipboard: " + player.getName());
                    if (!cp.hasPermission(StandardGuildPermission.CLAIM)) {
                        StandardGuildPermission.CLAIM.sendDeniedMessage(player);
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                            cp.buildMap().clipboardMode().display();
                            Pair<Long, Double> costs = session.getTotalCost();
                            context.sendMessage(Lang.COMMAND_CLAIM_CLIPBOARD_COST, "rp", StringUtils.toFancyNumber(costs.getKey()), "money", StringUtils.toFancyNumber(costs.getValue()));
                        });
                        if (action != null) {
                            if (action == Action.CLEAR) {
                                ClaimClipboard.getClipboard().remove(player.getUniqueId());
                                Lang.COMMAND_CLAIM_CLIPBOARD_CLEARED.sendMessage(player);
                            } else {
                                SimpleChunkLocation.WorldlessWrapper coords = CommandClaim.getChunkCoords(player, context.arg(1), context.arg(2));
                                if (coords != null) {
                                    SimpleChunkLocation chunk = coords.inWorld(session.getWorld());
                                    context.var("x", coords.getX()).var("z", coords.getZ());
                                    switch (action) {
                                        case ADD:
                                            if (session.getClaims().containsKey(chunk.worldlessWrapper())) {
                                                context.sendError(Lang.COMMAND_CLAIM_CLIPBOARD_ALREADY_ADDED);
                                            } else {
                                                ClaimProcessor result = (new ClaimProcessor(chunk, cp, guild)).process();
                                                if (result.isSuccessful()) context.sendMessage(Lang.COMMAND_CLAIM_CLIPBOARD_ADDED);
                                                else result.getIssue().sendError(player, result.getContextHolder());
                                            }
                                            break;
                                        case REMOVE:
                                            if (session.getClaims().remove(coords) != null) context.sendMessage(Lang.COMMAND_CLAIM_CLIPBOARD_REMOVED);
                                            else context.sendMessage(Lang.COMMAND_CLAIM_CLIPBOARD_NOT_FOUMD);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length == 1) {
            return tabComplete("add", "remove", "clear");
        } else {
            if (args.length > 1 && !args[0].equalsIgnoreCase("clear"))
                return tabComplete("&2<x> <z>");

            if (args.length == 3 && !args[0].equalsIgnoreCase("clear"))
                return tabComplete("&2<z>");

            return emptyTab();
        }
    }

    private enum Action {
        ADD,
        REMOVE,
        CLEAR
    }
}
