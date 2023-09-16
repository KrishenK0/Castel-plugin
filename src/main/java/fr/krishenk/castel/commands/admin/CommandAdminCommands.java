package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.permissions.PermissionDefault;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CommandAdminCommands extends CastelCommand {
    private static final String OUTPUT_FILE = "command-permissions.txt";

    public CommandAdminCommands(CastelParentCommand parent) {
        super("commands", parent);
    }

    @Override
    public void execute(CommandContext context) {
        boolean defaultsOnly = context.assertArgs(1) && context.arg(0).equalsIgnoreCase("defaults");

        Path path = CastelPlugin.getInstance().getDataFolder().toPath().resolve(OUTPUT_FILE);
        try {
            BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            try {
                for (CastelCommand cmd : CastelCommandHandler.getCommands(SupportedLanguage.EN).values()) {
                    if (!defaultsOnly || cmd.getPermission().getDefault() == PermissionDefault.TRUE) {
                        writer.write(cmd.getName() + " (" + StringUtils.join(cmd.getAliases().get(SupportedLanguage.EN).toArray(), ", ") + ") " + cmd.getPermission().getName());
                        writer.newLine();
                    }
                }
            } catch (Throwable e) {
                try {
                    writer.close();
                } catch (Throwable ex) {
                    e.addSuppressed(ex);
                }
                throw e;
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete("defaults") : emptyTab();
    }
}
