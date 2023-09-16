package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.string.tree.StringPathBuilder;
import fr.krishenk.castel.utils.string.tree.StringTree;
import fr.krishenk.castel.utils.string.tree.TreeStyle;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandAdminFiles extends CastelCommand {
    public CommandAdminFiles(CastelParentCommand parent) {
        super("files", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        Path ouput = CastelPlugin.getFolder().resolve("files.txt");
        context.var("output", ouput);
        context.var("sanitized_output", CommandAdminOpenFile.sanitize(ouput));
        ArrayList<String> files = new ArrayList<>();
        try {
            Files.walk(CastelPlugin.getFolder()).forEach((it) -> {
                try {
                    files.add(CastelPlugin.getFolder().relativize(it).toString().replace('\\', '/') + " ("+ StringUtils.toFancyNumber(Files.size(it)) +')');
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TreeStyle style = new TreeStyle(StringTree.getUTF_CHARACTER_SET(), new HashMap<>());
        style.setFlatten(true);
        style.setIndentation(2);
        StringTree pathBuilder = new StringPathBuilder(files).toStringTree(style).print();
        List<String> lines = new ArrayList<>(pathBuilder.getLines().size());
        for (StringBuilder line : pathBuilder.getLines()) {
            lines.add(line.toString());
        }

        try {
            Files.write(ouput, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.sendMessage(Lang.COMMAND_ADMIN_FILES_DONE);
        return CommandResult.SUCCESS;
    }
}
