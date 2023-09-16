package fr.krishenk.castel.commands.general.election;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import org.bukkit.entity.Player;

public class CommandElectionStatement extends CastelCommand {
    public CommandElectionStatement(CommandElection commandElection) {
        super("statement", commandElection);
    }

    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            if (!CommandElection.checkOngoingElection(context) && !context.assertPlayer()) {
                Player player = context.senderAsPlayer();
                ElectionsManager.CandidateDetails candidate = ElectionsManager.getCandidate(player.getUniqueId());
                candidate.statement = context.joinArgs();
                context.sendMessage(Lang.COMMAND_ELECTION_STATEMENT_SET, "statement", candidate.statement);
            }
        }
    }
}
