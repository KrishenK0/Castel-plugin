package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.managers.land.claiming.ClaimProcessor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CommandClaimConfirm extends CastelCommand {
    public CommandClaimConfirm(CastelParentCommand parent) {
        super("confirm", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            ClaimClipboard session = ClaimClipboard.getClipboard().remove(player.getUniqueId());
            if (session != null) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = Objects.requireNonNull(cp.getGuild(), "Player without a guild has a clipboard: " + player.getName());
                if (!cp.hasPermission(StandardGuildPermission.CLAIM)) {
                    StandardGuildPermission.CLAIM.sendDeniedMessage(player);
                } else {
                    Set<SimpleChunkLocation> finalClaimables = new HashSet<>();
                    for (AbstractClaimProcessor result : session.getClaims().values()) {
                        SimpleChunkLocation chunk = result.getChunk();
                        if (result.isSuccessful()) {
                            ClaimProcessor newResult = (ClaimProcessor) result.recompile();
                            if (!newResult.isSuccessful()) {
                                newResult.getContextHolder().raw("x", chunk.getX()).raw("z", chunk.getZ()).raw("error", newResult.processIssue());
                                Lang.COMMAND_CLAIM_CONFIRM_FAIL.sendError(context.getSender(), newResult.getContextHolder());
                            } else {
                                newResult.finalizeRequest();
                                finalClaimables.add(chunk);
                            }
                        }
                    }

                    if (!finalClaimables.isEmpty()) {
                        Lang.COMMAND_CLAIM_CONFIRM_SUCCESS.sendMessage(player);
                        guild.claim(finalClaimables, cp, ClaimLandEvent.Reason.CLAIMED, true);
                    }
                }
            }
            context.sendError(Lang.COMMAND_CLAIM_CONFIRM_EMPTY);
        }
    }
}
