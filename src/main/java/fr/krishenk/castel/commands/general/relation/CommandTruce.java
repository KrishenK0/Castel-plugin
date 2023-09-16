package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.lang.Lang;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandTruce extends CastelCommand {
    public CommandTruce() {
        super("truce", true);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        return RelationalCommandProcessor.execute(GuildRelation.TRUCE, context, Lang.COMMAND_TRUCE_TRUCES);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return RelationalCommandProcessor.tabComplete(GuildRelation.TRUCE, context);
    }
}
