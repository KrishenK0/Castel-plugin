package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.CastelBukkitExtensions;
import fr.krishenk.castel.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import scala.Function1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CommandAdminFSCK extends CastelCommand {
    public CommandAdminFSCK(CastelParentCommand parent) {
        super("fsck", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        context.sendMessage(Lang.COMMAND_ADMIN_FSCK_SCANNING);
        int corrupted = 0;
        List<Pair<DefinedMessenger, Function1<MessageBuilder, MessageBuilder>>> corruptions = new ArrayList<>();
        long startMillis = System.currentTimeMillis();

        try {
            CastelDataCenter dataCenter = plugin.getDataCenter();
            for (DataManager<?, ?> dataManager : dataCenter.getAllDataManagers()) {
                dataManager.loadAllData();
            }

            for (Guild guild : dataCenter.getGuildManager().getLoadedData()) {
                for (SimpleChunkLocation chunk : guild.getLandLocations()) {
                    Land land = chunk.getLand();
                    if (land == null || !land.isClaimed()) {
                        ++corrupted;
                        guild.unsafeGetLandLocations().remove(chunk);
                    }
                }

                for (UUID memberUUID : guild.getMembers()) {
                    CastelPlayer cp = CastelBukkitExtensions.INSTANCE.asCastelPlayer(memberUUID);
                    if (!Objects.equals(guild.getId(), cp.getGuildId())) {
                        ++corrupted;
                        guild.unsafeGetMembers().remove(memberUUID);
                    }
                }

                guild.getRelationshipRequests().keySet().removeIf(x -> CastelBukkitExtensions.INSTANCE.asGuild(x) == null);
                guild.getRelations().keySet().removeIf(x -> CastelBukkitExtensions.INSTANCE.asGuild(x) == null);
                if (guild.getMembers().isEmpty()) guild.disband(GroupDisband.Reason.ADMIN);
                else {
                    if (!Objects.equals(CastelBukkitExtensions.INSTANCE.asCastelPlayer(guild.getLeaderId()).getGuildId(), guild.getId())) {
                        guild.setLeader(CastelBukkitExtensions.INSTANCE.asCastelPlayer(guild.getMembers().toArray(new UUID[0])[0]), GuildLeaderChangeEvent.Reason.ADMIN);
                    }
                }
            }

            for (Land land : dataCenter.getLandManager().getLoadedData()) {
                if (land.isClaimed() && land.getGuild() == null) land.silentUnclaim();
                if (Bukkit.getWorld(land.getLocation().getWorld()) == null) {
                    corruptions.add(Pair.of(Lang.COMMAND_ADMIN_FSCK_CORRUPTION_LAND_UNKNOWN_WORLD, (c) -> LocationUtils.getChunkEdits(c, land.getLocation(), "")));
                }
            }

            for (CastelPlayer player : dataCenter.getCastelPlayerManager().getLoadedData()) {
                if (player.getGuildId() == null) {
                    Guild guild = CastelBukkitExtensions.INSTANCE.asGuild(player.getGuildId());
                    if (guild == null || !guild.isMember(player.getUUID())) {
                        ++corrupted;
                        player.silentlyLeaveGuild();;
                    }
                }
            }
        } catch (Throwable e) {
            return context.fail(Lang.COMMAND_ADMIN_FSCK_ERROR);
        }

        long endMillis = System.currentTimeMillis();
        context.var("corrupted", corrupted);
        context.getSettings().raw("corruptions", corruptions);
        context.var("time", endMillis - startMillis);
        if (corrupted == 0) {
            context.sendMessage(Lang.COMMAND_ADMIN_FSCK_DONE_NO_CORRUPTION);
        } else {
            context.sendMessage(Lang.COMMAND_ADMIN_FSCK_DONE_CORRUPTED);
            for (Pair<DefinedMessenger, Function1<MessageBuilder, MessageBuilder>> corruption : corruptions) {
                DefinedMessenger msg = corruption.getKey();
                Function1<MessageBuilder, MessageBuilder> vars = corruption.getValue();
                vars.apply(context.getSettings());
                context.sendMessage(msg);
            }
        }

        return CommandResult.SUCCESS;
    }
}
