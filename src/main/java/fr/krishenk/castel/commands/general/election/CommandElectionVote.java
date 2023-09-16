package fr.krishenk.castel.commands.general.election;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;
import java.util.List;

public class CommandElectionVote extends CastelCommand {
    private static final Comparator<CastelPlayer> CANDIDATE_COMPARATOR = new Comparator<CastelPlayer>() {
        @Override
        public int compare(CastelPlayer first, CastelPlayer second) {
            int firstVotes = ElectionsManager.getCandidate(first.getUUID()).votes;
            int secondVotes = ElectionsManager.getCandidate(second.getUUID()).votes;
            int sComp = Integer.compare(firstVotes, secondVotes);
            return sComp != 0 ? sComp : first.compareTo(second);
        }
    };

    public CommandElectionVote(CommandElection parent) {
        super("vote", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!CommandElection.checkOngoingElection(context)) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                if (guild == null) context.sendMessage(Lang.NO_GUILD_DEFAULT);
                else Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> openCandidates(player, 0));
            }
        }
    }

    public static void openCandidates(Player player, int page) {
        /*
        InteractiveGUI gui = GUIAccessor.prepare(player, player, "election-candidates", new Object[]{"page", page + 1});
        ReusableOptionHandler holder = gui.getReusableOption("candidates");
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        Kingdom kingdom = kp.getKingdom();
        List<KingdomPlayer> members = kingdom.getKingdomPlayers();
        members.sort(CANDIDATE_COMPARATOR);
        int eachPage = holder.slotsCount();
        int maxPages = MathUtils.getPageNumbers(members.size(), eachPage);
        gui.getSettings().raw("pages", maxPages);
        gui.push("next-page", () -> {
            if (page + 1 >= maxPages) {
                KingdomsLang.GUIS_PAGES_NEXT_PAGE_NOT_AVAILABLE.sendError(player, new Object[]{"pages", maxPages, "page", page + 1});
            } else {
                openCandidates(player, page + 1);
            }
        }, new Object[0]);
        gui.push("previous-page", () -> {
            if (page + 1 >= maxPages) {
                KingdomsLang.GUIS_PAGES_PREVIOUS_PAGE_NOT_AVAILABLE.sendError(player, new Object[]{"pages", maxPages, "page", page + 1});
            } else {
                openCandidates(player, page - 1);
            }
        }, new Object[0]);
        KingdomPlayer[] var9 = (KingdomPlayer[])members.stream().skip((long)page * (long)eachPage).limit((long)eachPage).toArray((x$0) -> {
            return new KingdomPlayer[x$0];
        });
        int var10 = var9.length;

        for(int var11 = 0; var11 < var10; ++var11) {
            KingdomPlayer member = var9[var11];
            OfflinePlayer memberPlayer = member.getOfflinePlayer();
            ElectionsManager.CandidateDetails candidate = ElectionsManager.getCandidate(member.getId());
            holder.setEdits(new Object[]{"votes", candidate.votes, "candidates-description", candidate.statement != null ? candidate.statement : KingdomsLang.ELECTIONS_DEFAULT_STATEMENT.parse(memberPlayer, new Object[0])}).onNormalClicks(() -> {
                OfflinePlayer previous = ElectionsManager.vote(player, member.getId());
                if (previous == null) {
                    KingdomsLang.COMMAND_ELECTION_VOTE_VOTED.sendMessage(player, new Object[]{"candidate", memberPlayer.getName()});
                    player.closeInventory();
                    openCandidates(player, page);
                } else if (previous.getUniqueId().equals(member.getId())) {
                    KingdomsLang.COMMAND_ELECTION_VOTE_ALREADY_VOTED.sendMessage(player, new Object[]{"candidate", memberPlayer.getName()});
                } else {
                    KingdomsLang.COMMAND_ELECTION_VOTE_VOTED_AGAIN.sendMessage(player, new Object[]{"candidate", memberPlayer.getName(), "previous-candidate", previous.getName()});
                }

            }).pushHead(memberPlayer);
        }

        gui.open();
         */
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(1) ? context.getPlayers(0) : emptyTab();
    }
}
