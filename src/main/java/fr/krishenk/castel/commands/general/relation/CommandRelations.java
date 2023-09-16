package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CommandRelations extends CastelCommand {
    public CommandRelations() {
        super("relations", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            Guild guild = context.getGuild();
            Map<UUID, GuildRelationshipRequest> requests = guild.getRelationshipRequests();
            if (requests.isEmpty()) Lang.RELATIONS_NO_REQUESTS.sendError(player);
            else {
                openRelationsGUI(player, guild);
            }
        }
    }

    private void openRelationsGUI(Player player, Guild guild) {
        /*
        InteractiveGUI gui = GUIAccessor.prepare(player, KingdomsGUI.KINGDOM$RELATION$REQUESTS);
        if (gui == null) {
            return null;
        } else {
            Objects.requireNonNull(player);
            gui.push("back", player::closeInventory, new Object[0]);
            ReusableOptionHandler inviteOpt = gui.getReusableOption("request");
            Iterator var4 = kingdom.getRelationshipRequests().entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<UUID, KingdomRelationshipRequest> requestEntry = (Map.Entry)var4.next();
                KingdomRelationshipRequest request = (KingdomRelationshipRequest)requestEntry.getValue();
                OfflinePlayer sender = Bukkit.getOfflinePlayer(request.getSender());
                boolean expired = !request.canAccept();
                UUID kingdomId = (UUID)requestEntry.getKey();
                Kingdom requestedKingdom = Kingdom.getKingdom((UUID)requestEntry.getKey());
                boolean invalid = requestedKingdom == null;
                KingdomRelation rel = request.getRelation();
                ReusableOptionHandler opt = inviteOpt.setEdits(new Object[]{"expired", expired, "invalid", invalid, "sender", sender.getName(), "timestamp", TimeUtils.getDateAndTime(request.getTimestamp())});
                opt.getSettings().raw("time-passed", TimeFormatter.of(System.currentTimeMillis() - request.getTimestamp())).raw("time-left-to-accpet", TimeFormatter.of(request.getTimeLeftToAccept())).raw("relation-name", rel.getName()).raw("relation-color", rel.getColor()).withContext(requestedKingdom);
                opt.on(ClickType.LEFT, (context) -> {
                    if (!expired && !invalid) {
                        RelationUtil.acceptRequest(player, kingdom, requestedKingdom, request.getRelation(), RelationUtil.getAcceptMsgOf(request.getRelation()));
                    }

                    kingdom.getRelationshipRequests().remove(kingdomId);
                    openRelationsGUI(player, kingdom);
                }).on(ClickType.RIGHT, (ctx) -> {
                    if (!invalid) {
                        ctx.sendError(KingdomsLang.COMMAND_REJECTRELATION_REJECTED, new Object[0]);
                        Iterator var7 = requestedKingdom.getOnlineMembers().iterator();

                        while(var7.hasNext()) {
                            Player member = (Player)var7.next();
                            KingdomsLang.COMMAND_REJECTRELATION_NOTIFICATION.sendError(member, (new MessageBuilder()).withContext(player).parse("relation", rel.getColor() + rel.getName().buildPlain(ctx.getSettings())));
                        }
                    }

                    kingdom.getRelationshipRequests().remove(kingdomId);
                    openRelationsGUI(player, kingdom);
                }).pushHead(sender);
                if (!inviteOpt.hasNext()) {
                    break;
                }
            }

            gui.push("decline-all", () -> {
                kingdom.getRelationshipRequests().clear();
                openRelationsGUI(player, kingdom);
            }, new Object[0]);
            gui.open();
            return gui;
        }
         */
    }
}
