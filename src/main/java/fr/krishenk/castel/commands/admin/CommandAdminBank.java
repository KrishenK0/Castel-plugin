package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminBank extends CastelCommand {
    public CommandAdminBank(CastelParentCommand parent) {
        super("bank", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(2)) {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                String[] args = context.args;
                CommandSender sender = context.getSender();
                String number;
                String action;
                if (args.length > 2) {
                    action = args[1];
                    number = args[2];
                } else {
                    action = "add";
                    number = args[1];
                }

                double old = guild.getBank();
                SetterHandler.SetterResult result = SetterHandler.eval(action, old, number);
                if (result == SetterHandler.SetterResult.NOT_NUMBER)
                    Lang.INVALID_NUMBER.sendMessage(sender, "arg", number);
                else if (result == SetterHandler.SetterResult.UNKNOWN)
                    Lang.COMMAND_ADMIN_BANK_INVALID_ACTION.sendMessage(sender, "action", action);
                else {
                    guild.setBank(result.getValue());
                    String fancyBal = StringUtils.toFancyNumber(old);
                    MessageBuilder settings = new MessageBuilder().raws("bank", fancyBal, "guild", guild.getName()).withContext(guild);
                    Lang.COMMAND_ADMIN_BANK_DONE.sendMessage(sender, settings);
                    for (Player member : guild.getOnlineMembers()) {
                        Lang.COMMAND_ADMIN_BANK_ADDED.sendMessage(member, settings);
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return context.getGuilds(0);
        if (context.isAtArg(1)) return SetterHandler.tabComplete(context.arg(1));
        return context.isAtArg(2) ? tabComplete("<amount>") : emptyTab();
    }
}
