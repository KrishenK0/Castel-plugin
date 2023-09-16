package fr.krishenk.castel.commands.general.misc.mails;

import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.mails.MailUserAgent;
import fr.krishenk.castel.utils.internal.FastUUID;

import java.util.UUID;

public class CommandMail extends CastelParentCommand {
    public CommandMail() {
        super("mail", true);
        new CommandMailReply(this);
        new CommandMailOpen(this);
    }

    protected static Mail handleCommons(CommandContext context) {
        if (context.assertPlayer() || context.assertHasGuild()) return null;
        if (!context.argsLengthEquals(1)) {
            context.wrongUsage();
            context.sendError(Lang.COMMAND_NOT_INTENDED_FOR_DIRECT_USE);
            return null;
        }
        CastelPlayer cp = context.getCastelPlayer();
        if (!cp.hasPermission(StandardGuildPermission.MANAGE_MAILS)) {
            StandardGuildPermission.MANAGE_MAILS.sendDeniedMessage(context.senderAsPlayer());
            return null;
        }
        String id = context.arg(0);
        context.var("id", id);
        UUID uuid;
        try {
            uuid = FastUUID.fromString(id);
        } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
            context.sendError(Lang.COMMAND_MAIL_INVALID_ID);
            context.sendError(Lang.COMMAND_NOT_INTENDED_FOR_DIRECT_USE);
            return null;
        }
        Mail mail = Mail.getMail(uuid);
        if (mail == null) {
            context.sendError(Lang.COMMAND_MAIL_NOT_FOUND);
            context.sendError(Lang.COMMAND_NOT_INTENDED_FOR_DIRECT_USE);
            return null;
        }
        context.getSettings().addAll(MailUserAgent.getEditsForMail(mail).getPlaceholders());
        Guild guild = cp.getGuild();
        if (!mail.getRecipients().containsKey(guild.getId())) {
            context.sendError(Lang.COMMAND_MAIL_NOT_YOURS);
            context.sendError(Lang.COMMAND_NOT_INTENDED_FOR_DIRECT_USE);
            return null;
        }
        return mail;
    }
}
