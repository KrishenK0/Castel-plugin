package fr.krishenk.castel.commands.general.misc.mails;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.managers.mails.MailUserAgent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandMailReply extends CastelCommand {
    public CommandMailReply(CommandMail parent) {
        super("reply", parent);
    }

    @Override
    public void execute(CommandContext context) {
        Mail mail = CommandMail.handleCommons(context);
        if (mail != null) {
            Player player = context.senderAsPlayer();
            Guild guild = context.getGuild();
            ItemStack item = MailUserAgent.generateEnvelopeInReplyTo(player, guild, mail, Guild.class);
            XItemStack.addItems(player.getInventory(), false, item);
        }
    }
}
