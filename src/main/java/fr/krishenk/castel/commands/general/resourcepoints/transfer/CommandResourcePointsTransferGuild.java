package fr.krishenk.castel.commands.general.resourcepoints.transfer;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.group.Guild;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandResourcePointsTransferGuild extends CastelCommand {
    public CommandResourcePointsTransferGuild(CastelParentCommand parent) {
        super("guild", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        CommandResourcePointsTransfer.ResourcePointsTransfer transfer = new CommandResourcePointsTransfer.ResourcePointsTransfer();
        CommandResult result = transfer.handleBasics(context);
        if (result != CommandResult.SUCCESS) return result;
        Guild guild = context.getGuild();
        long amount = transfer.amount;
        // Implement to group
        return result;
    }
}
