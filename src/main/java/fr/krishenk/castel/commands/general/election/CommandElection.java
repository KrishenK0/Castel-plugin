package fr.krishenk.castel.commands.general.election;

import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import fr.krishenk.castel.utils.time.TimeFormatter;

import java.time.temporal.ChronoUnit;

public class CommandElection extends CastelParentCommand {
    public CommandElection() {
        super("election", true);
        if (!this.isDisabled()) {
            new CommandElectionVote(this);
            new CommandElectionStatement(this);
        }
    }

    public static boolean checkOngoingElection(CommandContext context) {
        if (ElectionsManager.isAcceptingVotes()) return false;
        context.sendError(Lang.COMMAND_ELECTION_NO_ONGOING_ELECTION, "next-election", TimeFormatter.of(ElectionsManager.getInstance().untilNextChecks(ChronoUnit.MILLIS)));
        return true;
    }
}
