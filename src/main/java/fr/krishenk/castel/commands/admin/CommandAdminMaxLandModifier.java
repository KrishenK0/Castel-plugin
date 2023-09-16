package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminMaxLandModifier extends CastelCommand {
    public CommandAdminMaxLandModifier(CastelParentCommand parent) {
        super("maxLandModifier", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(2)) {
            String[] args = context.args;
            CommandSender sender = context.getSender();
            Guild guild = context.getGuild(0);
            if (guild != null) {
                String number;
                String action;
                if (args.length > 2) {
                    action = args[1];
                    number = args[2];
                } else {
                    action = "add";
                    number = args[1];
                }

                SetterHandler.SetterResult result = SetterHandler.eval(action, guild.getMaxLandsModifier(), number);
                if (result == SetterHandler.SetterResult.NOT_NUMBER)
                    Lang.INVALID_NUMBER.sendMessage(sender, "arg", number);
                else if (result == SetterHandler.SetterResult.UNKNOWN)
                    Lang.COMMAND_ADMIN_MAXLANDMODIFIER_INVALID_ACTION.sendMessage(sender, "action", action);
                else {
                    guild.setMaxLandsModifier((int) result.getValue());
                    Lang.COMMAND_ADMIN_MAXLANDMODIFIER_SUCCESS.sendMessage(sender, "guild", guild.getName(), "amount", guild.getMaxLandsModifier());
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
