package fr.krishenk.castel.commands.general.relation;

import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GroupRelationshipChangeEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.RelationUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RelationalCommandProcessor {
    public static CommandResult execute(GuildRelation relation, CommandContext context, Messenger requestLang) {
        if (context.assertPlayer() || context.requireArgs(1) || context.assertHasGuild()) return CommandResult.FAILED;

        context.var("relation", relation.getColor() + relation.getName().buildPlain(context.getSettings()));
        Player player = context.senderAsPlayer();
        CastelPlayer cp = context.getCastelPlayer();
        Guild guild = cp.getGuild();
        if (!cp.hasPermission(relation.getPermission()))  {

            relation.getPermission().sendDeniedMessage(player);
            return CommandResult.FAILED;
        }

        int relations = guild.countRelationships(relation);
        double maxRelations = MathUtils.eval(relation.getLimit(), context.getSettings());
        context.var("max", maxRelations);
        CommandResult result;
        if (maxRelations > 0 && relations >= maxRelations) {
            result = context.fail(context.lang("limit"));
            return result;
        } else {
            guild = context.getGuild(0);
            if (guild == null) return CommandResult.FAILED;
            Guild to = guild;
            context.getSettings().other(to);
            if (Objects.equals(guild.getId(), to.getId())) return context.fail(context.lang("self"));
            if (guild.getRelationWith(to) == relation) return context.fail(context.lang("already"));
            GuildRelationshipRequest first  = to.getRelationshipRequests().get(guild.getId());
            GuildRelationshipRequest other  = guild.getRelationshipRequests().get(to.getId());
            if (first != null && other != null) {
                guild.getRelationshipRequests().remove(to.getId());
                to.getRelationshipRequests().remove(guild.getId());
            } else {
                if ((first != null ? first.getRelation() : null) == relation)
                    return context.fail(context.lang("already-requested"));
            }

            if (relation != GuildRelation.ENEMY) {
                GuildRelationshipRequest request = guild.getRelationshipRequests().get(to.getId());
                if (request != null) {
                    if (request.getRelation() != relation) return context.fail(Lang.RELATIONS_ANOTHER_REQUEST);

                    if (RelationUtil.acceptRequest(player, guild, to, relation, requestLang).isCancelled()) {
                        return CommandResult.FAILED;
                    }

                    return CommandResult.SUCCESS;
                }

                if (guild.sendRelationshipRequest(cp, to, relation).isCancelled()) {
                    return CommandResult.FAILED;
                }

                context.sendMessage(context.lang("sender"));
                for (Player member : to.getOnlineMembers()) {
                    context.sendMessage(member, context.lang("receiver"));
                }
            } else {
                if (guild.isStrongerThan(to)) {
                    return context.fail(Lang.COMMAND_ENEMY_HIGHER_STRENGTH);
                }

                long cost = (long) MathUtils.eval(relation.getCost(), context.getSettings());
                context.var("cost", cost);
                if (cost != 0L && !guild.hasResourcePoints(cost)) {
                    return context.fail(Lang.COMMAND_ENEMY_COST);
                }

                GroupRelationshipChangeEvent event = new GroupRelationshipChangeEvent(cp, guild, to, relation);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return CommandResult.FAILED;

                if (cost != 0L) guild.addResourcePoints(-cost);
                guild.getRelations().put(to.getId(), relation);
                to.getRelations().put(guild.getId(), relation);

                for (Player member : guild.getOnlineMembers()) {
                    Lang.COMMAND_ENEMY_ENEMIES.sendMessage(member, "guild", to.getName());
                }

                for (Player member : to.getOnlineMembers()) {
                    Lang.COMMAND_ENEMY_ENEMIES.sendMessage(member, "guild", guild.getName());
                }

                Guild finalGuild = guild;
                guild.getGuildWithRelation(GuildRelation.ALLY).stream().filter(x -> x.getRelationWith(to) == GuildRelation.ALLY).forEach(x -> CommandEnemy.notify(x, finalGuild, to));
                to.getGuildWithRelation(GuildRelation.ALLY).stream().filter(x -> x.getRelationWith(finalGuild) == GuildRelation.ALLY).forEach(x -> CommandEnemy.notify(x, finalGuild, to));
            }
            return CommandResult.SUCCESS;
        }
    }

    public static final List<String> tabComplete(GuildRelation relation, CommandTabContext context) {
        if (context.isPlayer()) {
            CastelPlayer cp = context.getCastelPlayer();
            Guild guild = cp.getGuild();
            if (guild != null) {
                return context.getGuilds(0, (x) -> Objects.equals(guild, Guild.getGuild(x)) && guild.getRelationWith(Guild.getGuild(x)) != relation);
            }
        }
        return Collections.emptyList();
    }
}
