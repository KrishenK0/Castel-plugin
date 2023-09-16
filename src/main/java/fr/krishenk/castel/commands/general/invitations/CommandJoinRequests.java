package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandJoinRequests extends CastelCommand {
    public CommandJoinRequests() {
        super("joinRequests", true);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer() || context.assertHasGuild()) return CommandResult.FAILED;
        CastelPlayer cp = context.getCastelPlayer();
        if (!cp.hasPermission(StandardGuildPermission.INVITE)) {
            return context.fail(StandardGuildPermission.INVITE.getDeniedMessage());
        }
        openGUI(cp, 0);
        return CommandResult.SUCCESS;
    }

    public static void openGUI(CastelPlayer viewer, int page) {
        /*
            Objects.requireNonNull(viewer, "viewer must not be null");
            InteractiveGUI gui = GUIAccessor.prepare(viewer.getPlayer(), KingdomsGUI.KINGDOM_JOIN_REQUESTS);
            Kingdom kingdom = viewer.getKingdom();
            Objects.requireNonNull(kingdom, "kingdom must not be null");

            Pair<ReusableOptionHandler, Collection<Map.Entry<UUID, Long>>> paginationResult = GUIPagination.paginate(
                    gui,
                    JoinRequests.Companion.getJoinRequests(kingdom).entrySet(),
                    "requests",
                    page,
                    (viewer, newPage) -> openGUI(viewer, newPage)
            );
            ReusableOptionHandler option = paginationResult.getFirst();
            Collection<Map.Entry<UUID, Long>> requests = paginationResult.getSecond();

            for (Map.Entry<UUID, Long> joinRequest : requests) {
                KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(joinRequest.getKey());
                Objects.requireNonNull(kp, "kp must not be null");
                Player player = kp.getPlayer();
                OfflinePlayer offlinePlayer = kp.getOfflinePlayer();
                Objects.requireNonNull(offlinePlayer, "offlinePlayer must not be null");

                Object[] edits = new Object[] { "sent_date", TimeFormatter.dateOf(joinRequest.getValue().longValue()) };
                option.setEdits(edits)
                        .on(ClickType.LEFT, (kingdom, kp, viewer, player, offlinePlayer, page, ctx) -> {
                            JoinRequests.Companion.processJoinRequest(kingdom, kp, true, kp);
                            ctx.getSettings().withContext(viewer.getOfflinePlayer());
                            if (player != null) {
                                ctx.sendMessage((CommandSender) player, KingdomsLang.COMMAND_JOINREQUESTS_ACCEPTED_PLAYER, new Object[0]);
                            }
                            ctx.getSettings().withContext(offlinePlayer);
                            ctx.sendMessage(KingdomsLang.COMMAND_JOINREQUESTS_ACCEPTED_SELF, new Object[0]);
                            openGUI(viewer, page);
                        })
                        .on(ClickType.RIGHT, (kingdom, kp, viewer, player, offlinePlayer, page, ctx) -> {
                            JoinRequests.Companion.processJoinRequest(kingdom, kp, false, kp);
                            ctx.getSettings().withContext(viewer.getOfflinePlayer());
                            if (player != null) {
                                ctx.sendMessage((CommandSender) player, KingdomsLang.COMMAND_JOINREQUESTS_DENIED_PLAYER, new Object[0]);
                            }
                            ctx.getSettings().withContext(offlinePlayer);
                            ctx.sendMessage(KingdomsLang.COMMAND_JOINREQUESTS_DENIED_SELF, new Object[0]);
                            openGUI(viewer, page);
                        })
                        .pushHead(offlinePlayer);
            }

            Objects.requireNonNull(gui, "gui must not be null");
            InteractiveGUI.open(gui, false, false, 3);
         */
    }
}
