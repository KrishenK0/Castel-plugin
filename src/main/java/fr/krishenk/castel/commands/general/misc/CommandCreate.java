package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.general.home.CommandSetHome;
import fr.krishenk.castel.commands.general.text.CommandRename;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.managers.land.claiming.ClaimProcessor;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCreate extends CastelCommand {
    private static final Cooldown<Integer> CONFIRMATION_COMMAND = new Cooldown<>();

    public CommandCreate() {
        super("create", true);
    }

    @Override
    public void execute(@NonNull CommandContext context) {
        if (context.assertPlayer()) return;
        if (context.requireArgs(1)) return;
        Player player = context.senderAsPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);

        if (cp.hasGuild()) context.sendError(Lang.COMMAND_CREATE_ALREADY_IN_GUILD);
        String name = StringUtils.buildArguments(context.args, Config.GUILD_NAME_ALLOW_SPACES.getBoolean() ? " " : "");
        if (CommandRename.forbidden(player, name)) return;
        if (!cp.isAdmin()) {
            if (!CommandRename.checkName(player, name)) return;
            if (ServiceHandler.bankServiceAvailable()) {
                double cost = Config.ECONOMY_COSTS_CREATE_GUILD.getDouble();
                if (cost > 0.0) {
                    if (!ServiceVault.hasMoney(player, cost)) {
                        Lang.COMMAND_CREATE_COST.sendMessage(player, "cost", cost);
                        return;
                    }
                    if (!CONFIRMATION_COMMAND.add(player.getEntityId(), 100000L)) {
                        Lang.COMMAND_CREATE_CONFIRMATION.sendMessage(player, "cost", cost);
                        return;
                    }
                    ServiceVault.withdraw(player, cost);
                }
            }
        }
        Guild guild = new Guild(player.getUniqueId(), name);
        if (Config.ANNOUNCEMENTS_CREATE_GUILD.getBoolean()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                Lang.COMMAND_CREATE_ANNOUNCE.sendMessage(online, player, "guild", name);
            }
        } else Lang.COMMAND_CREATE_SUCCESS.sendMessage(player, "guild", name);

        if (Config.TAGS_ATTEMPT_AUTOMATIC_SETTING.getBoolean()) {
            String tagName = StringUtils.findCapitalized(name);
            if (tagName.isEmpty()) name.substring(0, Math.min(5, name.length()));
            guild.setTag(tagName);
        }

        SimpleChunkLocation chunkLocation = SimpleChunkLocation.of(player.getLocation());
        ClaimProcessor claimProcessor = (new ClaimProcessor(chunkLocation, cp, guild)).process();
        boolean canClaim = claimProcessor.isSuccessful();
        int claims = Config.Claims.CLAIM_ON_CREATE.getManager().getInt();
        boolean claimed = claims >= 0 && canClaim;
        if (claimed)
            claimed = !guild.claim(Collections.singleton(chunkLocation), cp, ClaimLandEvent.Reason.INITIAL_GUILD_CLAIMS, false).isCancelled();

        if (claimed) claimProcessor.finalizeRequest();

        if (claimed && claims > 0) {
            for (SimpleChunkLocation chunk : chunkLocation.getChunksAround(claims)) {
                if (!chunk.equalsIgnoreWorld(chunkLocation))
                    guild.claim(Collections.singleton(chunk), cp, ClaimLandEvent.Reason.INITIAL_GUILD_CLAIMS, false);
            }
        }

        if (Config.HOME_SET_ON_CREATE.getBoolean()) {
            if (Config.HOME_CLAIMED.getBoolean()) {
                if (claimed) CommandSetHome.setHome(guild, player.getLocation().add(0.0, 1.0 ,0.0), cp);
            } else CommandSetHome.setHome(guild, player.getLocation().add(0.0, 1.0 ,0.0), cp);
        }

        XSound.play(player, Config.CREATION_GUILDS_SOUND.toString());
        CONFIRMATION_COMMAND.stop(player.getEntityId());
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        return args.length == 1 ? Collections.singletonList("<name>") : new ArrayList<>();
    }
}
