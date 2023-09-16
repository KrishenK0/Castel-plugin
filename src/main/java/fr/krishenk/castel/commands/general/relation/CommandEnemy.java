package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandEnemy extends CastelCommand {
    public CommandEnemy() {
        super("enemy", true);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        return RelationalCommandProcessor.execute(GuildRelation.ENEMY, context, Lang.COMMAND_ENEMY_ENEMIES);
    }

    static void notify(Guild guild, Guild ally, Guild enemiedAlly) {
        for (Player player : guild.getOnlineMembers()) {
            Lang.COMMAND_ENEMY_NOTIFY.sendError(player, (new MessageBuilder()).withContext(ally).other(enemiedAlly));
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return RelationalCommandProcessor.tabComplete(GuildRelation.ENEMY, context);
    }
}
