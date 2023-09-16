package fr.krishenk.castel.commands.general.misc;

import com.github.benmanes.caffeine.cache.Cache;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.metadata.StandardGuildMetadata;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.members.GuildKickEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import fr.krishenk.castel.utils.cache.CacheHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandKick extends CastelCommand {
    private static final Cache<UUID, Byte> EASTER_CACHE = CacheHandler.newBuilder().expireAfterWrite(1L, TimeUnit.SECONDS).build();

    public CommandKick() { super("kick", true); }

    @Override
    public void execute(CommandContext context) {
        if (context.assertPlayer()) {
            MessageHandler.sendConsolePluginMessage("&2You can use &6/c admin kick <player> &2instead.");
        } else if (!context.requireArgs(1) && !context.assertHasGuild()) {
            OfflinePlayer kick = context.getOfflinePlayer(0);
            if (kick != null) {
                kick(context.getGuild(), context.senderAsPlayer(), kick, false);
            }
        }
    }

    public static void kick(Guild guild, Player kicker, OfflinePlayer kick, boolean orHashPermission) {
        CastelPlayer kickerCp = CastelPlayer.getCastelPlayer(kicker);
        CastelPlayer kickedCp = CastelPlayer.getCastelPlayer(kick);
        if (!orHashPermission && !kickerCp.hasPermission(StandardGuildPermission.KICK)) {
            StandardGuildPermission.KICK.sendDeniedMessage(kicker);
        } else if (kick.getUniqueId().equals(kicker.getUniqueId())) {
            if (EASTER_CACHE.getIfPresent(kicker.getUniqueId()) == null) {
                Lang.COMMAND_KICK_SELF.sendError(kicker);
                ParticleDisplay.of(Particle.CLOUD).withCount(100).offset(0.5).spawn(kicker.getLocation());
                Vector velocity = kicker.getLocation().getDirection().normalize().multiply(1.1);
                velocity.setY(-0.3);
                kicker.setVelocity(velocity);
                double hp = kicker.getHealth();
                if (hp >= 0.001) {
                    kicker.damage(1.0E-6);
                    kicker.setHealth(hp);
                }

                EASTER_CACHE.put(kicker.getUniqueId(), (byte) 0);
            } else Lang.COMMAND_KICK_SELF_SECONDARY.sendMessage(kicker);
        } else if (!guild.isMember(kick)) {
            Lang.COMMAND_KICK_NOT_IN_GUILD.sendError(kicker, "kicked", kick.getName());
        } else if (Config.DAILY_CHECKS_ELECTIONS_DISALLOW_KICKS.getBoolean() && ElectionsManager.isAcceptingVotes()) {
            Lang.COMMAND_KICK_ELECTIONS.sendMessage(kicker);
        } else {
            GuildKickEvent event = new GuildKickEvent(kickedCp, false, kicker);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (kickedCp.hasGuild()) {
                    if (!kickerCp.getRank().isHigherThan(kickedCp.getRank())) {
                        Lang.COMMAND_KICK_CANT_KICK.sendError(kicker, "kicked", kick.getName());
                        return;
                    }

                    if (kick.isOnline())
                        Lang.COMMAND_KICK_PERSON.sendMessage(kick.getPlayer(), "guild", guild.getName(), "kicker", kicker.getName());

                    kickedCp.leaveGuild(LeaveReason.KICKED);
                } else guild.getMembers().remove(kickedCp.getUUID());

                for (Player member : guild.getOnlineMembers()) {
                    Lang.COMMAND_KICK_ANNOUNCE.sendMessage(member, "kicker", kicker.getName(), "kicked", kick.getName());
                }

                if (Config.TELEPORT_TO_SPAWN_AFTER_KICK.getManager().getBoolean()) {
                    Player onlineKicked = kick.getPlayer();
                    if (onlineKicked != null)
                        onlineKicked.teleport(onlineKicked.getWorld().getSpawnLocation());
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.isPlayer()) return emptyTab();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(context.senderAsPlayer());
        Guild guild = cp.getGuild();
        return guild != null && context.isAtArg(0) ? context.getGuildPlayers(guild, context.arg(0), (player) -> !player.getUniqueId().equals(cp.getGuildId())) : emptyTab();
    }
}
