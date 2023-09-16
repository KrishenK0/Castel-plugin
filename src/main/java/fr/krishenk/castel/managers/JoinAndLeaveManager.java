package fr.krishenk.castel.managers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import fr.krishenk.castel.managers.daily.TaxManager;
import fr.krishenk.castel.managers.land.CastelMap;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.XScoreboard;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JoinAndLeaveManager implements Listener {
    public static final List<Consumer<Player>> LEAVE_HANDLERS = new ArrayList<>(3);

    private static boolean showJoinLeaveMessages(Player player, boolean joining) {
        return Config.JOIN_LEAVE_MESSAGES.getBoolean() && !(joining ? CastelPluginPermission.SILENT_JOIN : CastelPluginPermission.SILENT_LEAVE).hasPermission(player) && !ServiceHandler.isVanished(player);
    }

    private static void sendMessage(Lang lang, Predicate<Player> filter, Player player, boolean console) {
        MessageBuilder settings = new MessageBuilder().withContext(player);
        Collection<? extends Player> players = filter == null ? Bukkit.getOnlinePlayers() : Bukkit.getOnlinePlayers().stream().filter(filter).collect(Collectors.toList());
        players.forEach(p -> lang.sendMessage(p, settings.other(p)));
        if (console) {
            lang.sendMessage(Bukkit.getConsoleSender(), settings);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int unreadMails;
        XScoreboard scoreboard;
        Player player = event.getPlayer();
        PlayerUtils.cachePlayer(player);

        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        Guild guild = cp.getGuild();
            cp.updatePower(false);
        if (!cp.getInvites().isEmpty()) {
            Lang.JOIN_INVITES.sendMessage(player, "invites", cp.getInvites().size());
        }
        CastelPlugin.taskScheduler().asyncLater(Duration.ofSeconds(5L), () -> {
            if (Config.FORCE_LANG.getBoolean()) {
//                    cp.setLanguage(LanguageManager.getDefaultLanguage());
            } else if (!player.hasPlayedBefore()) {
                String detectedLocale;
                SupportedLanguage chosen;
                if (Config.Powers.POWER_ENABLED.getManager().getBoolean()) {
                    double initial = Config.Powers.POWER_PLAYER_INITIAL.getManager().getDouble();
                        cp.setPower(initial, true);
                }
                if ((chosen = SupportedLanguage.fromName(detectedLocale = player.getLocale())) == null || !chosen.isInstalled()) {
                    Lang.JOIN_LANGUAGE_NOT_SUPPORTED.sendError(player, "detected-lang", detectedLocale);
//                        cp.setLanguage(LanguageManager.getDefaultLanguage());
                } else {
//                        cp.setLanguage(chosen);
                    Lang.JOIN_LANGUAGE_SUPPORTED.sendMessage(player, "detected-lang", detectedLocale);
                }
            }
        });
        if (JoinAndLeaveManager.showJoinLeaveMessages(player, true)) {
            event.setJoinMessage(null);
            Bukkit.getScheduler().runTaskLaterAsynchronously(CastelPlugin.getInstance(), () -> {
                if (guild == null) {
                    JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_JOIN_OTHERS, null, player, true);
                } else {
                    JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_JOIN_GUILD, guild::isMember, player, false);
                    JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_JOIN_OTHERS, players -> !guild.isMember(players), player, true);
                }
            }, 20L);
        }
            if ((scoreboard = CastelMap.SCOREBOARDS.get(player.getUniqueId())) != null) {
                scoreboard.setForPlayer(player);
            }
        if (guild == null) {
            if (Config.NO_GUILD_REMINDER.getBoolean() && cp.getJoinedAt() != 0L) {
                Lang.NO_GUILD_REMINDER.sendMessage(player);
            }
            return;
        }
//            if (cp.hasPermission(StandardGuildPermission.READ_MAILS) && (unreadMails = cp.countUnreadMails(guild.getMails())) > 0) {
//                CastelLang.JOIN_UNREAD_MAILS.sendMessage((CommandSender) player, "unread-mails", unreadMails);
//            }
            if (ElectionsManager.isAcceptingVotes() && !ElectionsManager.VOTES.containsKey(player.getUniqueId())) {
                Lang.ELECTIONS_JOIN_NOTIFY.sendMessage(player);
            }
        Location spawn = null;
        if (Config.HOME_ON_JOIN_GUILD_HOME.getBoolean() && cp.hasPermission(StandardGuildPermission.HOME) && (player.getBedSpawnLocation() == null || Config.HOME_RESPAWN_UNLESS_HAS_BED.getBoolean())) {
            spawn = guild.getHome();
        }
        if (spawn != null) {
            player.teleport(spawn);
        }
            if (Config.TAX_GUILDS_ENABLED.getBoolean() && Config.TAX_GUILDS_NOTIFICATIONS.getBoolean() && TaxManager.needsToPayTaxes(guild)) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(CastelPlugin.getInstance(), () -> {
                    double tax = guild.calculateTax();
                    if (!guild.hasMoney(tax)) {
                        Lang.TAX_NOTIFICATIONS.sendMessage(player, "tax", StringUtils.toFancyNumber(tax));
                    }
                }, 60L);
            }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasGuild()) {
            return;
        }
        Guild guild = cp.getGuild();
        Location spawn = null;
        if (Config.HOME_RESPAWN_GUILD_HOME.getBoolean() && cp.hasPermission(StandardGuildPermission.HOME)) {
            spawn = guild.getHome();
        }
        if (spawn != null && spawn.getWorld() != null) {
            event.setRespawnLocation(spawn);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        Guild guild = cp.getGuild();
        cp.updatePower(true);
        LEAVE_HANDLERS.forEach(x -> x.accept(player));
        if (JoinAndLeaveManager.showJoinLeaveMessages(player, false)) {
            event.setQuitMessage(null);
            if (guild == null) {
                JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_LEAVE_OTHERS, null, player, true);
            } else {
                JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_LEAVE_GUILD, guild::isMember, player, false);
                JoinAndLeaveManager.sendMessage(Lang.JOIN_LEAVE_MESSAGES_LEAVE_OTHERS, players -> !guild.isMember(players), player, true);
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), () -> {
                LandVisualizer.removeVisualizers(player, false);
//                if (!CastelConfig.Chat.RESET_CHANNEL_ON_LEAVE.getManager().getBoolean()) {
//                    cp.setChatChannel(KingdomsChatChannel.getGlobalChannel());
//                }
        });
        if (guild == null) {
            return;
        }
        cp.disableFlying(player);
        if (Config.KEEP_ADMIN_MODE.getBoolean()) {
            cp.setAdmin(false);
        }
    }
}

