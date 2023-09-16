package fr.krishenk.castel.commands.general.misc.mails;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.managers.mails.MailUserAgent;
import org.bukkit.entity.Player;

public class CommandMailOpen extends CastelCommand {
    public CommandMailOpen(CommandMail parent) {
        super("open", parent);
    }

    @Override
    public void execute(CommandContext context) {
        Mail mail = CommandMail.handleCommons(context);
        if (mail != null) {
            Player player = context.senderAsPlayer();
            Guild guild = context.getGuild();
            MailUserAgent.openMail(guild, player, mail);
        }
    }
}
