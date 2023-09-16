package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandInvites extends CastelCommand {
    public CommandInvites() {
        super("invites", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            if (context.getCastelPlayer().getInvites().isEmpty()) {
                context.sendError(Lang.COMMAND_INVITES_NO_INVITES);
            } else {
                openInvitesGUI(player);
            }
        }
    }

    private void openInvitesGUI(Player player) {
        // TODO : Implement
        /*
                InteractiveGUI gui = GUIAccessor.prepare(player, KingdomsGUI.INVITES);
        if (gui == null) {
            return null;
        } else {
            Objects.requireNonNull(player);
            gui.push("back", player::closeInventory, new Object[0]);
            KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
            ReusableOptionHandler inviteOpt = gui.getReusableOption("invite");
            Iterator var4 = kp.getInvites().entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<UUID, KingdomInvite> inviteEntry = (Map.Entry)var4.next();
                KingdomInvite invite = (KingdomInvite)inviteEntry.getValue();
                OfflinePlayer sender = Bukkit.getOfflinePlayer(invite.getSender());
                boolean expired = !invite.canAccept();
                UUID kingdomId = (UUID)inviteEntry.getKey();
                inviteOpt.setEdits(new Object[]{"expired", expired, "timestamp", TimeUtils.getDateAndTime(invite.getTimestamp()), "time-passed", TimeFormatter.of(System.currentTimeMillis() - invite.getTimestamp()), "time-left-to-accpet", TimeFormatter.of(invite.getTimeLeftToAccept())}).on(ClickType.LEFT, (context) -> {
                    if (!expired) {
                        if (kp.hasKingdom()) {
                            context.sendError(KingdomsLang.COMMAND_ACCEPT_ALREADY_IN_KINGDOM, new Object[0]);
                            return;
                        }

                        CommandAccept.acceptInvite(player, Kingdom.getKingdom(kingdomId));
                    }

                    kp.getInvites().remove(kingdomId);
                    openInvitesGUI(player);
                }).on(ClickType.RIGHT, () -> {
                    CommandDecline.decline(player, Kingdom.getKingdom(kingdomId));
                    kp.getInvites().remove(kingdomId);
                    openInvitesGUI(player);
                }).pushHead(sender);
                if (!inviteOpt.hasNext()) {
                    break;
                }
            }

            gui.push("decline-all", () -> {
                CommandDecline.declineAll(player);
                openInvitesGUI(player);
            }, new Object[0]);
            gui.open();
            return gui;
        }
         */
    }
}
