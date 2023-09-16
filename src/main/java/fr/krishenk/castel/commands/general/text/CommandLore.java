package fr.krishenk.castel.commands.general.text;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CommandLore extends CastelCommand {
    public CommandLore() { super("lore", true); }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.requireArgs(1)) {
                String[] args = context.args;
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                if (!cp.hasPermission(StandardGuildPermission.LORE)) {
                    StandardGuildPermission.LORE.sendDeniedMessage(player);
                } else if (args.length == 1 && Config.GUILD_LORE_REMOVE_KEYWORDS.getStringList().contains(args[0])) {
                    Guild guild = cp.getGuild();
                    if (!guild.setLore(null, cp).isCancelled())
                        Lang.COMMAND_LORE_REMOVED.sendMessage(player);
                } else {
                    String lore = joinArgs(args);
                    if (!cp.isAdmin()) {
                        int limit = Config.GUILD_LORE_MAX_LENGTH.getInt();
                        int len = Config.GUILD_LORE_IGNORE_COLORS.getBoolean() ? MessageHandler.stripColors(MessageHandler.colorize(lore), false).length() : lore.length();
                        if (len > limit) {
                            Lang.COMMAND_LORE_NAME_LENGTH.sendMessage(player);
                            return;
                        }
                        if (!Config.GUILD_LORE_ALLOW_NON_ENGLISH.getBoolean() && !StringUtils.isEnglish(lore)) {
                            Lang.COMMAND_LORE_NAME_ENGLISH.sendMessage(player);
                            return;
                        }

                        if (!Config.GUILD_LORE_ALLOW_SYMBOLS.getBoolean() && StringUtils.hasSymbol(lore)) {
                            Lang.COMMAND_LORE_NAME_HAS_SYMBOLS.sendMessage(player);
                            return;
                        }

                        boolean match = false;
                        if (!match) {
                            for (String regex : Config.GUILD_LORE_BLACKLISTED_NAMES.getStringList()) {
                                if (Pattern.compile(regex).matcher(lore).find()) {
                                    match = true; break;
                                }
                            }

                            if (match) {
                                Lang.COMMAND_LORE_BLACKLISTED.sendMessage(player);
                                return;
                            }
                        }

                        if (Config.GUILD_LORE_ALLOW_COLORS.getBoolean()) {
                            lore = MessageCompiler.compile(lore, MessageCompilerSettings.none().colorize()).buildPlain(new MessageBuilder());
                        }

                        Guild guild = cp.getGuild();
                        if (!guild.setLore(lore, cp).isCancelled()) {
                            (new LandVisualizer()).forLand(SimpleChunkLocation.of(player.getLocation())).forPlayer(player, cp).forGuild(guild).displayMessages();
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length == 1) {
            List<String> keywords = Config.GUILD_LORE_REMOVE_KEYWORDS.getStringList();
            if (!keywords.isEmpty()) {
                String keyword = keywords.get(0);
                if (args[0].isEmpty() || keyword.toLowerCase(Locale.ENGLISH).startsWith(args[0].toLowerCase(Locale.ENGLISH))) {
                    return Collections.singletonList(keywords.get(0));
                }
            }
        }
        return new ArrayList<>();
    }
}
