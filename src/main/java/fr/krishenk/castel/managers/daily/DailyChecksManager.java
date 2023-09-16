package fr.krishenk.castel.managers.daily;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.events.general.GroupServerTaxPayEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.abstraction.ProlongedTask;
import fr.krishenk.castel.managers.logger.CastelLogger;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DailyChecksManager extends ProlongedTask {
    private static final DailyChecksManager INSTANCE = new DailyChecksManager();

    public static DailyChecksManager getInstance() { return INSTANCE; }

    private DailyChecksManager() {
        super(Duration.ofDays(Config.DAILY_CHECKS_INTERVAL.getInt()), TimeZoneHandler.DAILY_CHECKS, "daily checks", new String[]{"prolonged-tasks", "daily-checks"}, Config.DAILY_CHECKS_COUNTDOWNS.getStringList());
    }

    public void remind(String formattedTime) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Lang.TAX_REMINDER.sendMessage(player, "time", formattedTime);
        }

        Lang.TAX_REMINDER.sendMessage(Bukkit.getConsoleSender(), "time", formattedTime);
    }

    @Override
    public void run() {
        CastelPlugin plugin = CastelPlugin.getInstance();
        MessageHandler.sendConsolePluginMessage("&2Performing daily checks...");
        CastelLogger.getMain().log("Performing daily checks...");
        boolean guildTaxes = Config.TAX_GUILDS_ENABLED.getBoolean();
        String taxEquation = Config.TAX_GUILDS_SCALING.getString();
        long guildsTaxAge = Config.TAX_GUILDS_AGE.getTimeMillis();
        Long playerInactivity = Config.INACTIVITY_MEMBER_KICK.getTimeMillis();
        Long guildInactivity = Config.INACTIVITY_GUILD_DISBAND.getTimeMillis();
        boolean checkPlayerInactivity = playerInactivity != null && playerInactivity > 0L;
        Long membersTaxAge = Config.TAX_GUILDS_MEMBERS_AGE.getTimeMillis();
        final boolean memberTaxEnabled = membersTaxAge != null && Config.TAX_GUILDS_MEMBERS_ENABLED.getBoolean();

        boolean useRpGuild = Config.TAX_GUILDS_USE_RESOURCE_POINTS.getBoolean();
        long now = System.currentTimeMillis();

        for (Guild guild : plugin.getDataCenter().getGuildManager().getGuilds()) {
            boolean needsToPayTaxes = !guild.isPermanent() && guildTaxes;

            if (!needsToPayTaxes) {
                continue;
            }

            if (now - guild.getSince() < guildsTaxAge) {
                guild.getOnlineMembers().forEach(Lang.TAX_GUILD_AGE::sendMessage);
                continue;
            }

            AtomicReference<Double> total = new AtomicReference<>(0.0);
            AtomicBoolean isInactive = new AtomicBoolean(!guild.isPermanent() && guildInactivity != null && guildInactivity > 0L);

            guild.getPlayerMembers().forEach(member -> {
                long lastPlayed = member.getLastPlayed();
                long passedSinceLastPlay = now - lastPlayed;

                if (isInactive.get() && passedSinceLastPlay < guildInactivity) {
                    isInactive.set(false);
                }

                if (checkPlayerInactivity && passedSinceLastPlay > playerInactivity) {
                    InactivityManager.handleInactiveMember(guild, member);
                } else if (memberTaxEnabled) {
                    CastelPlayer cp = CastelPlayer.getCastelPlayer(member);

                    if (!cp.hasGuild()) {
                        MessageHandler.sendConsolePluginMessage("&4Unknown kingdom for player &e" + member.getName() + " &4while collecting member taxes for &e" + guild.getName() + " &4kingdom. Removing them...");
                        CastelLogger.getMain().log("Unknown kingdom for player &e" + member.getName() + " while collecting member taxes for " + guild.getName() + " kingdom. Removing them...");
                        guild.getMembers().remove(member.getUniqueId());
                    } else {
                        Player player;

                        if (now - cp.getJoinedAt() <= membersTaxAge) {
                            player = cp.getPlayer();

                            if (player != null) {
                                Lang.TAX_EXCLUDED_NEW.sendMessage(player);
                                CastelLogger.getMain().log(member.getName() + " from " + guild.getName() + " was excluded from paying taxes because they're a new member.");
                            }
                        } else if (cp.hasPermission(StandardGuildPermission.EXCLUDE_TAX)) {
                            player = cp.getPlayer();

                            if (player != null) {
                                Lang.TAX_EXCLUDED_PERMISSION.sendMessage(player);
                            }
                        } else {
                            Pair<Boolean, Double> success = guild.payTaxes(member);
                            double paid = success.getValue();
                            String fancyTaxNumber = StringUtils.toFancyNumber(paid);
                            String balance = StringUtils.toFancyNumber(ServiceVault.getMoney(cp.getOfflinePlayer()));

                            if (success.getKey()) {
                                if (member.isOnline() && paid != 0.0) {
                                    Lang.TAX_PAID.sendMessage(member.getPlayer(), "tax", fancyTaxNumber, "bal", balance);
                                }

                                total.updateAndGet(v -> v + paid);
                            } else if (Config.TAX_GUILDS_MEMBERS_KICK_IF_CANT_PAY.getBoolean()) {
                                guild.getOnlineMembers().forEach(playerOnline -> {
                                    if (playerOnline.getUniqueId().equals(member.getUniqueId())) {
                                        Lang.TAX_KICK.sendMessage(playerOnline, member, "tax", fancyTaxNumber, "bal", balance);
                                    } else {
                                        Lang.TAX_KICK_ANNOUNCE.sendMessage(playerOnline, member, "tax", fancyTaxNumber, "bal", balance);
                                    }
                                });

                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    cp.leaveGuild(LeaveReason.TAX);
                                });

                                CastelLogger.getMain().log("Kicked " + member.getName() + " from " + guild.getName() + " kingdom due to not being able to pay taxes: " + balance + " < " + fancyTaxNumber);
                            }
                        }
                    }
                }
            });

            if (guildTaxes) {
                String fancyTaxNumber = StringUtils.toFancyNumber(total.get());
                guild.getOnlineMembers().forEach(player -> {
                    Lang.TAX_TOTAL.sendMessage(player, "tax", fancyTaxNumber);
                });
            }

            if (isInactive.get() && InactivityManager.disband(guild)) {
                continue;
            }

            double pacifismFactor = guild.isPacifist() ? MathUtils.eval(Config.TAX_GUILDS_PACIFISM_FACTOR.getString(), guild) : 0.0;
            double tax = MathUtils.eval(taxEquation, guild, "pacifism_factor", pacifismFactor, "pacifism_factor", pacifismFactor);
            String fancyTaxNumber = StringUtils.toFancyNumber(tax);
            GroupServerTaxPayEvent event = new GroupServerTaxPayEvent(guild, tax);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            if (useRpGuild) {
                // Implement resource points handling
            } else if (guild.hasMoney(tax)) {
                guild.addBank(-tax);

                guild.getOnlineMembers().forEach(player -> {
                    Lang.TAX_GUILD_PAID.sendMessage(player, "tax", fancyTaxNumber);
                });
            } else if (!Config.TAX_GUILDS_DISBAND_IF_CANT_PAY.getBoolean()) {
                if (useRpGuild) {
                    // Set resource points to 0
                } else {
                    guild.setBank(0.0);
                }
            } else {
                CastelLogger.getMain().log(guild.getName() + " was disbanded due to not being able to pay taxes: " + /*StringUtils.toFancyNumber(useRpGuild ? (double) guild.getResourcePoints() : guild.getBank())*/ guild.getBank() + " < " + fancyTaxNumber);

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (guild.isMember(player)) {
                        Lang.TAX_GUILD_DISBANDED.sendError(player, "tax", fancyTaxNumber);
                    } else {
                        Lang.TAX_GUILD_DISBANDED_ANNOUNCE.sendMessage(player, "guild", guild.getName(), "tax", fancyTaxNumber);
                    }
                });

                Guild finalGuild = guild;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!finalGuild.triggerDisbandEvent(GroupDisband.Reason.TAXES).isCancelled()) {
                        Lang.TAX_GUILD_DISBANDED_ANNOUNCE.sendMessage(Bukkit.getConsoleSender(), new MessageBuilder().withContext(finalGuild).raws("guild", finalGuild.getName(), "tax", fancyTaxNumber));
                        finalGuild.disband(null);
                    }
                });
            }
        }
    }

    static {
        CastelLogger.getMain().newLine().newLine().log("Starting daily checks services...").log("Timezone: " + TimeZoneHandler.SERVER_TIME_ZONE).log("Cycle time: " + TimeZoneHandler.DAILY_CHECKS);
    }
}
