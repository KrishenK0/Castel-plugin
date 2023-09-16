package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.GuildInviteEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandInvite extends CastelCommand implements Listener {
    public CommandInvite() {
        super("invite", true);
    }

    @Override
    public void execute(CommandContext context) {
        if(!context.assertPlayer() && !context.requireArgs(1)) {
            final Player inviter = context.senderAsPlayer();
            final OfflinePlayer invited = context.getOfflinePlayer(0);
            if (invited != null) {
                if (invited.getUniqueId().equals(inviter.getUniqueId())) {
                    Lang.COMMAND_INVITE_SELF.sendError(inviter);
                } else {
                    CastelPlayer inviterCp = CastelPlayer.getCastelPlayer(inviter);
                    if (!inviterCp.hasGuild()) {
                        Lang.NO_GUILD_DEFAULT.sendError(inviter);
                    } else if (!inviterCp.hasPermission(StandardGuildPermission.INVITE)) {
                        StandardGuildPermission.INVITE.sendDeniedMessage(inviter);
                    } else {
                        final CastelPlayer invitedCp = CastelPlayer.getCastelPlayer(invited);
                        if (invitedCp.hasGuild()) {
                            if (invitedCp.getGuildId().equals(inviterCp.getGuildId())) {
                                Lang.COMMAND_INVITE_ALREADY_JOINED.sendMessage(inviter, "invited", invited.getName());
                                return;
                            }

                            if (!Config.INVITATIONS_ALLOW_FROM_OTHER_GUILDS.getBoolean()) {
                                Lang.COMMAND_INVITE_ALREADY_IN_GUILD.sendMessage(inviter, "invited", invited.getName());
                                return;
                            }
                        }

                        GuildInvite previousInvite = invitedCp.getInvites().get(inviterCp.getGuildId());
                        if (previousInvite != null) {
                            Lang.COMMAND_INVITE_ALREADY_INVITED.sendMessage(inviter, "inviter", previousInvite.getCastelPlayer().getOfflinePlayer().getName());
                        } else {
                            final Guild guild = inviterCp.getGuild();
                            if (guild.isFull()) {
                                Lang.COMMAND_INVITE_MAX_MEMBERS.sendMessage(inviter);
                            } else {
                                Long time = null;
                                if (context.assertArgs(2)) {
                                    time = TimeUtils.parseTime(context.arg(1), TimeUnit.HOURS);
                                }

                                if (time == null) {
                                    time = TimeUtils.parseTime(Config.INVITATIONS_EXPIRATION_DEFAULT_EXPIRE.getString(), TimeUnit.HOURS);
                                }

                                GuildInviteEvent event = new GuildInviteEvent(guild, inviterCp, invitedCp, time);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    time = event.getAcceptTime();
                                    final GuildInvite invitation = new GuildInvite(inviterCp.getUUID(), time);
                                    invitedCp.getInvites().put(guild.getId(), invitation);
                                    final Object[] edits = new Object[]{"inviter", inviter.getName(), "invited", invited.getName(), "guild", guild.getName()};
                                    if (time < TimeUnit.MILLISECONDS.convert(1L, TimeUnit.HOURS)) {
                                        (new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                GuildInvite currentInvite = invitedCp.getInvites().get(guild.getId());
                                                if (currentInvite == invitation) {
                                                    invitedCp.getInvites().remove(guild.getId());
                                                    if (invited.isOnline()) Lang.COMMAND_INVITE_EXPIRED.sendMessage(invitedCp.getPlayer(), edits);

                                                    if (inviter.isOnline()) Lang.COMMAND_INVITE_EXPIRED_NOTIFY.sendMessage(inviterCp.getPlayer(), edits);
                                                }
                                            }
                                        }).runTaskLaterAsynchronously(plugin, TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS) * 20L);
                                    }

                                    if (Config.INVITATIONS_ANNOUNCE.getBoolean()) {
                                        for (Player member : guild.getOnlineMembers()) {
                                            Lang.COMMAND_INVITE_ANNOUNCE.sendMessage(member, edits);
                                        }
                                    }

                                    if (invited.isOnline())
                                        Lang.COMMAND_INVITE_INVITED.sendMessage(invited.getPlayer(), edits);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                Player player = (Player)sender;
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                if (guild == null) {
                    return Collections.singletonList(Lang.NO_GUILD_DEFAULT.parse(player));
                }

                return TabCompleteManager.getPlayers(args[0], (p) -> !guild.isMember(p));
            }

            if (args.length == 2) {
                return Collections.singletonList(ChatColor.GREEN + "[expiration]");
            }
        }

        return new ArrayList<>();
    }
}
