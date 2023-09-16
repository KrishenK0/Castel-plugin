package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import org.bukkit.Bukkit;

import java.util.List;

public class CommandTop extends CastelCommand {
    public CommandTop() {
        super("top", true);
    }

    @Override
    public void execute(CommandContext context) {
        context.sendMessage(Lang.COMMAND_TOP_LOADING);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int page = 1;
            if (context.assertArgs(1)) {
                Integer tempPage = context.getInt(0);
                if (tempPage == null) return;
                page = tempPage;
            }

            int nextPage = page + 1;
            int previousPage = page - 1;
            Object[] edits = new Object[]{"page", page, "next_page", nextPage, "previous_page", previousPage};
            if (page - 1 < 0) {
                context.sendMessage(Lang.COMMAND_TOP_NEGATIVE, edits);
            } else {
                boolean showPacifists = Config.TOP_GUILDS_SHOW_PACIFISTS.getBoolean();
                int limit = Config.TOP_GUILDS_AMOUNT.getInt();
                int skip = (page - 1 ) * limit;
                List<Guild> guilds = CastelDataCenter.get().getGuildManager().getTopGuilds(skip, limit, showPacifists ? null : (x) -> !x.isPacifist());
                if (guilds.isEmpty()) {
                    context.sendError(Lang.COMMAND_TOP_NO_MORE_PAGES);
                } else {
                    CLogger.debug(CastelDebug.COMMAND_TOP, "Displaying top guilds with limit="+limit + ", skip=" + skip + ", showPacifists=" + showPacifists + ", page=" + page + ", filtered=" + guilds.size());
                    context.sendMessage(Lang.COMMAND_TOP_HEADER);
                    int ranking = skip;
                    for (Guild guild : guilds) {
                        ++ranking;
                        Lang.COMMAND_TOP_ENTRY.sendMessage(context.getSender(), (new MessageBuilder()).withContext(guild).raw("rank", String.valueOf(ranking)).raws(edits));
                    }
                    context.sendMessage(Lang.COMMAND_TOP_FOOTER, edits);
                }
            }
        });
    }
}
