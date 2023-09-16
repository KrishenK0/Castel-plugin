package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandAdminOpenFile extends CastelCommand {
    @NotNull
    private static final String START_LINK = "github\\.com/CryptoMorin/KingdomsX/blob/\\w+";
    @NotNull
    private static final String LINE_INDICATOR_PATTERN = "(#L\\d+(-L\\d+)?)?";
    @NotNull
    private static final Pattern ENGLISH_URLS = Pattern.compile("github\\.com/CryptoMorin/KingdomsX/blob/\\w+/core/src/main/resources/([\\w/]+\\.yml)(#L\\d+(-L\\d+)?)?");
    @NotNull
    private static final Pattern LANGUAGES_URLS = Pattern.compile("github\\.com/CryptoMorin/KingdomsX/blob/\\w+/resources/languages/(\\w+)/guis/([\\w/]+\\.yml)(#L\\d+(-L\\d+)?)?");

    public CommandAdminOpenFile(CastelParentCommand parent) {
        super("openfile", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.requireArgs(1)) return CommandResult.FAILED;
        Player player;
        if (context.isPlayer()) {
            player = context.senderAsPlayer();
            if (!player.isOp()) {
                context.sendError(Lang.COMMAND_ADMIN_OPENFILE_PERMISSION);
                return CommandResult.FAILED;
            }
        }

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            String filePath;
            label: {
                filePath = context.arg(0);
                if (!filePath.startsWith("https://")) {
                    if (!filePath.startsWith("http://")) {
                        filePath = filePath.replace('*', ' ');
                        break label;
                    }
                }

                filePath = filePath.replace("%20", " ");
                context.var("link", filePath);
                boolean matched = false;
                Matcher matchResult = ENGLISH_URLS.matcher(filePath);
                if (matchResult.find()) {
                    matched = true;
                    String fileName = matchResult.group(1);
                    if (fileName.startsWith("guis/")) {
                        filePath = "guis/en/" + fileName.substring("guis/".length());
                    }
                }

                matchResult = LANGUAGES_URLS.matcher(filePath);
                if (matchResult.find()) {
                    matched = true;
                    String language = matchResult.group(1);
                    String fileName = matchResult.group(2);
                    filePath = "guis/" + language + '/' + fileName;
                }

                if (!matched) {
                    context.var("link", filePath);
                    context.sendError(Lang.COMMAND_ADMIN_OPENFILE_UNKNOWN_LINK);
                    return CommandResult.FAILED;
                }
            }

            File file = null;

            try {
                file = CastelPlugin.getPath(filePath).toFile();
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                context.var("error", e.getMessage());
                context.sendError(Lang.COMMAND_ADMIN_OPENFILE_ERROR);
                return CommandResult.FAILED;
            }

            if (filePath.startsWith("guis/")) {
                context.sendError(Lang.COMMAND_ADMIN_OPENFILE_GUI_WARNING);
            }

            context.var("file", file.toString());
            context.sendMessage(Lang.COMMAND_ADMIN_OPENFILE_OPENED);
            CLogger.info("File '" + file + "' has been opened on server's desktop");
            return CommandResult.SUCCESS;
        } else {
            context.sendError(Lang.COMMAND_ADMIN_OPENFILE_NOT_SUPPORTED);
            return CommandResult.FAILED;
        }
    }

    public static String sanitize(Path path) {
        return CastelPlugin.getFolder().relativize(path).toString().replace(' ', '*');
    }
}
