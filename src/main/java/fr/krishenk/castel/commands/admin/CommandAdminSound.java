package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.utils.string.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandAdminSound extends CastelCommand {
    public CommandAdminSound(CastelParentCommand parent) {
        super("sound", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.requireArgs(1)) {
                XSound.Record record;
                try {
                    record = XSound.parse(String.join(",", context.args));
                } catch (Throwable var4) {
                    context.sendError(Lang.COMMAND_ADMIN_SOUND_ERROR, "error", var4.getMessage());
                    return;
                }

                if (record == null) {
                    context.sendError(Lang.COMMAND_ADMIN_SOUND_ERROR, "error", "No Sound");
                }

                context.sendMessage(Lang.COMMAND_ADMIN_SOUND_PLAYING);
                record.forPlayer(context.senderAsPlayer()).play();
            }
        }
    }

    @Override
    public @NotNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) {
            String sound = StringUtils.replace(context.arg(0).toUpperCase(Locale.ENGLISH), '-', '_').toString();
            Stream<String> stream = Arrays.stream(XSound.VALUES).map(Enum::name);
            return sound.isEmpty() ? stream.collect(Collectors.toList()) : stream.filter((x) -> x.contains(sound)).collect(Collectors.toList());
        }
        if (context.isAtArg(1)) {
            return tabComplete("&9[volume]");
        }
        return context.isAtArg(2) ? tabComplete("&6[pitch]") : emptyTab();
    }
}