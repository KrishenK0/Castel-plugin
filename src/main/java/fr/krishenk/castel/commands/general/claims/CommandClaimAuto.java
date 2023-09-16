package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.config.ConfigUtils;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandClaimAuto extends CastelCommand {
    protected static final HashMap<UUID, Integer> ACTIONBAR = new HashMap<>();

    public CommandClaimAuto(CastelParentCommand parent) {
        super("auto", parent);
    }

    protected static boolean cancelActionBar(Player target) {
        Integer task = ACTIONBAR.remove(target.getUniqueId());
        if (task != null) Bukkit.getScheduler().cancelTask(task);
        return task != null;
    }

    protected static void actionBar(Player player, Lang actionBar) {
        if (Config.Claims.ACTIONBAR_KEEP.getManager().getBoolean()) {
            int task = (new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) actionBar.sendMessage(player);
                    else {
                        CommandClaimAuto.ACTIONBAR.remove(player.getUniqueId());
                        this.cancel();
                    }
                }
            }).runTaskTimerAsynchronously(plugin, 0L, 40L).getTaskId();
            ACTIONBAR.put(player.getUniqueId(), task);
        } else actionBar.sendMessage(player);
    }

    protected static Player getTarget(CommandContext context, CastelPluginPermission othersPermission) {
        Player target;
        if (context.assertArgs(1)) {
            if (!context.hasPermission(othersPermission)) {
                context.sendError(Lang.COMMAND_CLAIM_AUTO_PERMISSION);
                return null;
            }

            target = context.getPlayer(0);
            if (target == null) return null;

            context.var("target", target.getName());
            CastelPlayer cp = CastelPlayer.getCastelPlayer(target);
            if (!cp.hasGuild()) {
                context.sendError(Lang.NOT_FOUND_PLAYER_NO_GUILD);
                return null;
            }
        } else {
            if (!context.isPlayer()) {
                context.wrongUsage();
                return null;
            }

            target = context.senderAsPlayer();
            if (context.assertHasGuild()) {
                return null;
            }
        }

        if (!Config.DISABLED_WORLDS.isInDisabledWorld(target, Lang.DISABLED_WORLD))
            return ConfigUtils.isInDisabledWorld(Config.Claims.DISABLED_WORLDS.getManager(), target, Lang.COMMAND_CLAIM_DISABLED_WORLD) ? null : target;
        return null;
    }

    @Override
    public void execute(CommandContext context) {
        Player target = getTarget(context, CastelPluginPermission.COMMAND_CLAIM_AUTO_OTHERS);
        if (target != null) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(target);
            if (!cp.hasPermission(StandardGuildPermission.CLAIM)) {
                StandardGuildPermission.CLAIM.sendDeniedMessage(target);
            } else {
                boolean wasAutoClaiming = cp.getAutoClaim();
                cp.setAutoClaim(!wasAutoClaiming);
                Lang activation = wasAutoClaiming ? Lang.COMMAND_CLAIM_AUTO_OFF : Lang.COMMAND_CLAIM_AUTO_ON;
                activation.sendError(target);
                if (wasAutoClaiming) {
                    if (cancelActionBar(target)) {
                        Lang.AUTO_CLAIM_ACTIONBAR_DISABLED.sendMessage(target);
                    }
                } else {
                    cancelActionBar(target);
                    if (Config.Claims.ACTIONBAR_AUTO_CLAIM.getManager().getBoolean()) {
                        actionBar(target, Lang.AUTO_CLAIM_ACTIONBAR_ENABLED);
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.assertArgs(1) && context.hasPermission(CastelPluginPermission.COMMAND_CLAIM_AUTO_OTHERS) ? context.getPlayers(0) : emptyTab();
    }
}
