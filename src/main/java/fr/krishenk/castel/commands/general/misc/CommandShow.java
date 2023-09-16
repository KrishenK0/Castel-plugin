package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.builders.MessageObjectLinker;
import fr.krishenk.castel.locale.compiler.builders.MessageObjectWithContext;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.internal.Fn;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandShow extends CastelCommand {
    private static final int RELATION_LIMITE = 30;

    public CommandShow() {
        super("show", true);
    }

    private static void show(CommandSender sender, Guild guild, OfflinePlayer placeholder, boolean admin) {
        SupportedLanguage locale = LanguageManager.localeOf(sender);
        OfflinePlayer leader = Bukkit.getOfflinePlayer(guild.getLeaderId());
        Lang leaderStatus = leader.isOnline() ? Lang.COMMAND_SHOW_ONLINE_PREFIX : Lang.COMMAND_SHOW_OFFLINE_PREFIX;
        MessageObjectWithContext leaderBuilder = new MessageObjectWithContext(leaderStatus.getMessageObject(locale), (new MessageBuilder()).withContext(leader));
        MessageObjectLinker membersBuilder = new MessageObjectLinker();
        List<CastelPlayer> members = guild.getCastelPlayers();
        members.sort(CastelPlayer::compareTo);
        OfflinePlayer[] memberOffline = members.stream().map(CastelPlayer::getOfflinePlayer).toArray(OfflinePlayer[]::new);
        for (OfflinePlayer player : memberOffline) {
            Player online = player.getPlayer();
            boolean isOnline = online != null;
            if (isOnline && ServiceHandler.isVanished(online) && !CastelPluginPermission.COMMAND_SHOW_SEE_VANISHED.hasPermission(sender, true)) {
                isOnline = false;
            }

            Lang memberStatus = isOnline ? Lang.COMMAND_SHOW_ONLINE_PREFIX : Lang.COMMAND_SHOW_OFFLINE_PREFIX;
            membersBuilder.add(memberStatus.getMessageObject(locale), (new MessageBuilder().withContext(player)));
            membersBuilder.add(" ");
        }

        MessageObjectLinker allies = new MessageObjectLinker();
        MessageObjectLinker truces = new MessageObjectLinker();
        MessageObjectLinker enemies = new MessageObjectLinker();
        int ally = 0;
        int truce = 0;
        int enemy = 0;
        List<UUID> remove = new ArrayList<>();

        for (Map.Entry<UUID, GuildRelation> relation : guild.getRelations().entrySet()) {
            Guild otherGuild = Guild.getGuild(relation.getKey());
            if (otherGuild == null) {
                remove.add(relation.getKey());
                CLogger.error("Unknown guild while mapping relations of guild " + guild.getName() + ": " + relation.getKey());
            } else {
                GuildRelation rel = relation.getValue();
                MessageBuilder settings = (new MessageBuilder()).withContext(otherGuild).other(guild);
                MessageObject msg = Lang.COMMAND_SHOW_RELATION.getMessageObject(locale);
                switch (rel) {
                    case ALLY:
                        if (ally == 30) {
                            allies.add("...");
                        } else if (ally < 30) {
                            allies.add(msg, settings).add(" ");
                        }
                        ++ally;
                        break;
                    case TRUCE:
                        if (truce == 30) {
                            truces.add("...");
                        } else if (truce < 30) {
                            truces.add(msg, settings).add(" ");
                        }
                        ++truce;
                        break;
                    case ENEMY:
                        if (enemy == 30) {
                            enemies.add("...");
                        } else if (enemy < 30) {
                            enemies.add(msg, settings).add(" ");
                        }
                        ++enemy;
                }
            }
        }
        for (UUID id : remove) {
            guild.getRelations().remove(id);
        }

        MessageBuilder settings = (new MessageBuilder()).raws("leader_status", leaderBuilder, "members", membersBuilder, "truces", truces, "allies", allies, "enemies", enemies);
        settings.withContext(placeholder);
        if (sender instanceof Player) settings.other((Player) sender);

        Lang lang = admin ? Lang.COMMAND_SHOW_MESSAGE_ADMIN : Lang.COMMAND_SHOW_MESSAGE;
        lang.sendMessage(sender, settings);
        if (ally != 0) Lang.COMMAND_SHOW_ALLIES.sendMessage(sender, (new MessageBuilder()).raw("allies", allies));
        if (truce != 0) Lang.COMMAND_SHOW_TRUCES.sendMessage(sender, (new MessageBuilder()).raw("truces", truces));
        if (enemy != 0) Lang.COMMAND_SHOW_ENEMIES.sendMessage(sender, (new MessageBuilder()).raw("enemies", enemies));
    }

    @Override
    public void execute(CommandContext context) {
        if (context.isPlayer() || !context.requireArgs(1)) {
            boolean admin = context.hasPermission(CastelPluginPermission.COMMAND_SHOW_ADMIN, true);
            Guild guild;
            OfflinePlayer placeholder;
            if (context.argsLengthEquals(0)) {
                if (context.assertHasGuild()) return;

                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                guild = cp.getGuild();
                placeholder = player;
                admin = true;
            } else {
                guild = context.generalSelector(false);
                if (guild == null) return;

                placeholder = guild.getLeader().getOfflinePlayer();
            }

            if (!admin) admin = guild.isMember(context.senderAsPlayer());
            if (!admin && !context.hasPermission(CastelPluginPermission.COMMAND_SHOW_OTHERS))
                context.sendError(Lang.COMMAND_SHOW_OTHERS_PERMISSION);
            else
                show(context.getSender(), guild, placeholder, admin);
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.tabCompleteGeneralSelector(false, true, Fn.alwaysTrue());
    }
}
