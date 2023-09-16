package fr.krishenk.castel.commands.general.text;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class CommandRename extends CastelCommand {
    private static final Cooldown<UUID> GUILD_COOLDOWN = new Cooldown<>();

    public CommandRename() {
        super("rename", true);
    }

    public static Cooldown<UUID> getGuildCooldown() {
        return GUILD_COOLDOWN;
    }

    public static boolean checkName(Player player, String name) {
        int max = Config.GUILD_NAME_MAX_LENGTH.getInt();
        int min = Config.GUILD_NAME_MIN_LENGTH.getInt();
        int len = Config.GUILD_NAME_IGNORE_COLORS.getBoolean() ? MessageHandler.stripColors(MessageHandler.colorize(name), false).length() : name.length();
        if (len >= min && len <= max) {
            if (!Config.GUILD_NAME_ALLOW_NUMBERS.getBoolean() && StringUtils.containsAnyLangNumber(name)) {
                Lang.COMMAND_CREATE_NAME_NUMBERS.sendMessage(player);
                return false;
            } else if (!Config.GUILD_NAME_ALLOW_NON_ENGLISH.getBoolean() && !StringUtils.isEnglish(name)) {
                Lang.COMMAND_CREATE_NAME_ENGLISH.sendMessage(player);
                return false;
            } else if (!Config.GUILD_NAME_ALLOW_SYMBOLS.getBoolean() && StringUtils.hasSymbol(name)) {
                Lang.COMMAND_CREATE_NAME_HAS_SYMBOLS.sendMessage(player);
                return false;
            } else {
                boolean match = false;
                for (String regex : Config.GUILD_NAME_BLACKLISTED_NAMES.getStringList()) {
                    if (Pattern.compile(regex).matcher(name).find()) {
                        match = true;
                        break;
                    }
                }

                if (!match) return true;
                Lang.GUILD_NAME_BLACKLISTED.sendMessage(player);
                return false;
            }
        }
        Lang.COMMAND_CREATE_NAME_LENGTH.sendMessage(player, "max", max, "min", min);
        return false;
    }

    public static boolean forbidden(Player player, String name) {
        if (!Config.GUILD_NAME_ALLOW_DUPLICATE_NAMES.getBoolean()) {
            Guild guild = Guild.getGuild(name);
            if (guild != null) {
                Lang.COMMAND_RENAME_NAME_ALREADY_IN_USE.sendMessage(player, "guild", guild.getName());
                return true;
            }
        }

        if (!name.toLowerCase(Locale.ENGLISH).contains("apothicas")) {
            return false;
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MessageHandler.sendPlayerPluginMessage(player, "&4You can't choose that name.");
                XSound.AMBIENT_CAVE.play(player);
                ParticleDisplay.of(Particle.MOB_APPEARANCE).spawn(player.getLocation(), player);
            }, 20L);
            player.setPlayerTime(18000L, false);
            Bukkit.getScheduler().runTaskLater(plugin, player::resetPlayerTime, 60L);
            return true;
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.hasPermission(StandardGuildPermission.LORE)) {
                StandardGuildPermission.LORE.sendDeniedMessage(player);
            } else {
                if (context.isAdmin()) {
                    long cd = GUILD_COOLDOWN.getTimeLeft(cp.getGuildId());
                    if (cd > 0L) {
                        context.sendError(Lang.COMMAND_RENAME_COOLDOWN, "cooldown", TimeFormatter.of(cd));
                        return;
                    }
                }

                String name;
                if (context.assertArgs(1)) name = StringUtils.buildArguments(context.args, Config.GUILD_NAME_ALLOW_SPACES.getBoolean() ? " " : "");
                else name = context.arg(0);

                if (name.equalsIgnoreCase("apothicas")) {
                    MessageHandler.sendPlayerPluginMessage(player, "&4You can't choose that name.");
                } else if (!forbidden(player, name)) {
                    Guild guild = cp.getGuild();
                    if (!cp.isAdmin()) {
                        if (!checkName(player, name)) return;

                        double cost = Config.ECONOMY_COSTS_RENAME_GUILD.getDouble();
                        if (!guild.hasMoney(cost)) {
                            Lang.COMMAND_RENAME_COST.sendMessage(player, "cost", cost);
                            return;
                        }

                        guild.addBank(-cost);
                    }

                    MessageBuilder msgSettings = (new MessageBuilder()).withContext(player).raw("name", name);
                    Lang msg = Lang.COMMAND_RENAME_SUCCESS;
                    Iterator<? extends Player> it;
                    if (Config.ANNOUNCEMENTS_RENAME.getBoolean()) {
                        it = Bukkit.getOnlinePlayers().iterator();
                        while (it.hasNext()) {
                            msg.sendMessage(it.next(), msgSettings);
                        }
                    } else {
                        it = guild.getOnlineMembers().iterator();
                        while (it.hasNext()) {
                            msg.sendMessage(it.next(), msgSettings);
                        }
                    }

                    if (guild.rename(name, cp).isCancelled()) {
                        GUILD_COOLDOWN.add(cp.getGuildId(), Config.GUILD_NAME_RENAMING_COOLDOWN.getTimeMillis());
                    }
                }
            }
        }
    }
}
