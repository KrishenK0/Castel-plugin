package fr.krishenk.castel.managers.daily;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.abstraction.ProlongedTask;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ElectionsManager extends ProlongedTask {
    private static final ElectionsManager INSTANCE = new ElectionsManager();
    private static BukkitTask concludeTask;
    private static final Map<UUID, CandidateDetails> CANDIDATE_DETAILS = new NonNullMap<>();
    public static final Map<UUID, UUID> VOTES = new NonNullMap<>();

    public static ElectionsManager getInstance() { return INSTANCE; }

    public static CandidateDetails getCandidate(UUID id) {
        return CANDIDATE_DETAILS.computeIfAbsent(id, (k) -> new CandidateDetails(null, 0));
    }

    public static OfflinePlayer vote(Player voter, UUID candidate) {
        UUID previouslyVotedCandidate = VOTES.put(voter.getUniqueId(), candidate);
        ++getCandidate(candidate).votes;
        if (previouslyVotedCandidate != null) {
            --getCandidate(previouslyVotedCandidate).votes;
        }
        return previouslyVotedCandidate == null ? null : Bukkit.getOfflinePlayer(previouslyVotedCandidate);
    }

    public static boolean isAcceptingVotes() {
        return concludeTask != null;
    }

    public ElectionsManager() {
        super(Duration.ofDays(Config.DAILY_CHECKS_ELECTIONS_INTERVAL.getInt()), TimeZoneHandler.DAILY_CHECKS, "daily checks", new String[]{"prolonged-tasks", "elections"}, null);
    }

    @Override
    public void remind(String str) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        Lang.ELECTIONS_BEGIN.sendEveryoneMessage();
        concludeTask = (new BukkitRunnable() {
            @Override
            public void run() {
                ElectionsManager.this.conclude();
                ElectionsManager.concludeTask = null;
            }
        }).runTaskLaterAsynchronously(CastelPlugin.getInstance(), TimeUtils.millisToTicks(Config.DAILY_CHECKS_ELECTIONS_DURATION.getTimeMillis(TimeUnit.HOURS)));
    }

    public void conclude() {
        Map<UUID, GuildElectionResult> topVotes = new HashMap<>();
        for (Map.Entry<UUID, CandidateDetails> candidate : CANDIDATE_DETAILS.entrySet()) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(candidate.getKey());
            if (cp.hasGuild()) {
                GuildElectionResult detail = topVotes.get(cp.getGuildId());
                if (detail == null) {
                    topVotes.put(cp.getGuildId(), new GuildElectionResult(candidate.getValue(), candidate.getKey()));
                } else {
                    if (candidate.getValue().votes > detail.details.votes) detail.change(candidate);
                    detail.plusVotes();
                }
            }
        }

        for (Map.Entry<UUID, GuildElectionResult> candidate : topVotes.entrySet()) {
            OfflinePlayer chosenCandidate = Bukkit.getOfflinePlayer(candidate.getValue().candidate);
            CandidateDetails candidateDetails = candidate.getValue().details;
            Guild guild = Guild.getGuild(candidate.getKey());
            double votePercentage = Config.DAILY_CHECKS_ELECTIONS_VOTE_PERCENTAGE.getDouble();
            double percent = MathUtils.getPercent(candidate.getValue().totalVotes, guild.getMembers().size());
            Lang lang;
            if (percent < votePercentage) lang = Lang.ELECTIONS_RESULTS;
            else lang = Lang.ELECTIONS_NOT_ENOUGH_DATA;

            if (guild.setLeader(CastelPlayer.getCastelPlayer(chosenCandidate), GuildLeaderChangeEvent.Reason.ELECTIONS).isCancelled()) return;

            for (Player member : guild.getOnlineMembers()) {
                lang.sendMessage(member, "candidate", chosenCandidate.getName(), "votes", candidateDetails.votes, "statement", candidateDetails.statement);
            }
        }
    }

    public static class CandidateDetails {
        public String statement;
        public int votes;

        public CandidateDetails(String statement, int votes) {
            this.statement = statement;
            this.votes = votes;
        }
    }

    private static class GuildElectionResult {
        public CandidateDetails details;
        public UUID candidate;
        public int totalVotes;

        private GuildElectionResult(CandidateDetails details, UUID candidate) {
            this.details = details;
            this.candidate = candidate;
        }

        public void plusVotes() {
            ++this.totalVotes;
        }

        public void change(Map.Entry<UUID, CandidateDetails> candidate) {
            this.candidate = candidate.getKey();
            this.details = candidate.getValue();
        }
    }
}
