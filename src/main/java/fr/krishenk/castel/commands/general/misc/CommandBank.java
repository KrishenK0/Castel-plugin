package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.services.ServiceVault;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CommandBank extends CastelCommand {
    public CommandBank() {
        super("bank", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!ServiceHandler.bankServiceNotAvailable(context.getSender()) && !context.assertPlayer() && !context.requireArgs(2) && !context.assertHasGuild()) {
            String[] args = context.args;
            CommandSender sender = context.getSender();
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                Lang.INVALID_NUMBER.sendError(player, "arg", args[1], "needed", "money");
                return;
            }

            if (amount == 0.0) {
                Lang.COMMAND_BANK_ZERO.sendError(player);
            } else if (amount < 0.0) {
                Lang.COMMAND_BANK_NEGATIVE.sendError(player);
            } else {
                String type = args[0].toLowerCase(Locale.ENGLISH);
                double minAmount;
                if (type.equalsIgnoreCase("withdraw")) {
                    if (!cp.hasPermission(StandardGuildPermission.WITHDRAW)) {
                        StandardGuildPermission.WITHDRAW.sendDeniedMessage(player);
                        return;
                    }

                    if (!Config.ECONOMY_BANK_WITHDRAW_ENABLED.getBoolean()) {
                        Lang.COMMAND_BANK_WITHDRAW_DISABLED.sendError(player);
                        return;
                    }

                    minAmount = Config.ECONOMY_BANK_WITHDRAW_MIN.getDouble();
                    if (amount < minAmount) {
                        Lang.COMMAND_BANK_WITHDRAW_MIN.sendError(player, "min", minAmount);
                        return;
                    }

                    if (guild.getBank() < amount) {
                        Lang.COMMAND_BANK_NOT_ENOUGH_GUILD_MONEY.sendError(player, "amount", amount);
                        return;
                    }

                    guild.addBank(-amount);
                    ServiceVault.deposit(player, amount);
                    Lang.COMMAND_BANK_WITHDRAW_SUCCESS.sendError(player, "amount", amount, "translated", amount, "balance", ServiceVault.getMoney(player));
                } else if (type.equalsIgnoreCase("deposit")) {
                    if (!Config.ECONOMY_BANK_DEPOSIT_ENABLED.getBoolean()) {
                        Lang.COMMAND_BANK_DEPOSIT_DISABLED.sendError(player);
                        return;
                    }

                    minAmount = Config.ECONOMY_BANK_DEPOSIT_MIN.getDouble();
                    if (amount < minAmount) {
                        Lang.COMMAND_BANK_DEPOSIT_MIN.sendError(player, "amount", amount);
                        return;
                    }

                    if (!ServiceVault.hasMoney(player, amount)) {
                        Lang.COMMAND_BANK_NOT_ENOUGH_MONEY.sendError(player, "amount", amount);
                        return;
                    }

                    double limit = Config.ECONOMY_BANK_LIMIT_GUILDS.getDouble();
                    if (guild.getBank() + amount > limit) {
                        Lang.COMMAND_BANK_DEPOSIT_LIMIT.sendMessage(player, "amount", amount, "limit", limit);
                    }

                    guild.addBank(amount);
                    ServiceVault.withdraw(player, amount);
                    Lang.COMMAND_BANK_DEPOSIT_SUCCESS.sendMessage(player, "amount", amount, "translated", amount, "balance", ServiceVault.getMoney(player));
                } else {
                    Lang.COMMAND_BANK_UNKNOWN_TRANSACTION.sendMessage(player, "transaction", args[0]);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer()) {
            if (context.argsLengthEquals(1)) return Arrays.asList("deposit", "withdraw");
            if (context.argsLengthEquals(2)) return Collections.singletonList("<amount>");
        }
        return new ArrayList<>();
    }
}
