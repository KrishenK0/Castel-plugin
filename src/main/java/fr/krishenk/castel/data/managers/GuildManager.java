package fr.krishenk.castel.data.managers;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.StartupCache;
import fr.krishenk.castel.data.handlers.DataHandlerGuild;
import fr.krishenk.castel.utils.internal.IndexedHashMap;
import fr.krishenk.castel.utils.string.QuantumString;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.Predicate;

public class GuildManager extends DataManager<UUID, Guild> {
    private static final Map<QuantumString, UUID> NAMES = new HashMap<>();
    private static final IndexedHashMap<UUID, Integer> TOP = new IndexedHashMap<>(new UUID[0]);

    public void updateTop() {
        Collection<Guild> guilds = this.getGuilds();
        CLogger.info("Updating top guilds... " + guilds.size());
        UUID[] array = guilds.stream().sorted(Guild.getTopComparator().reversed()).map(Group::getId).toArray(UUID[]::new);
        TOP.set(array, x -> x);
    }

    public GuildManager(CastelDataCenter dataCenter) {
        super("guilds", dataCenter.constructDatabase("Guilds", "guilds", new DataHandlerGuild()));
        this.autoSave(CastelPlugin.getInstance());
        StartupCache.whenLoaded(_x -> {
            long updateInterval = TimeUtils.millisToTicks(1800000L); // 30mins
            Bukkit.getScheduler().runTaskTimerAsynchronously(CastelPlugin.getInstance(), this::updateTop, 0L, updateInterval);
        });
    }

    public static Map<QuantumString, UUID> getNames() {
        return Collections.unmodifiableMap(NAMES);
    }

    public static QuantumString toQuantumName(String name) {
        return new QuantumString(name, true);
    }

    public Collection<Guild> getGuilds() {
        return this.peekAllData();
    }

    public int getTopPosition(Guild guild) {
        return TOP.get(guild.getId(), TOP.size()) + 1;
    }

    public Guild getGuildAtPosition(int i) {
        Validate.isTrue(i > 0, "Guild top positions start at 1");
        UUID id = TOP.at(--i);
        if (id == null) return null;
        return Guild.getGuild(id);
    }

    public Guild[] getTopGuilds() {
        return Arrays.stream(TOP.asArray()).map(Guild::getGuild).toArray(Guild[]::new);
    }

    public List<Guild> getTopGuilds(int skip, int limit) {
        return this.getTopGuilds(skip, limit, null);
    }

    public List<Guild> getTopGuilds(int skip, int limit, Predicate<Guild> predicate) {
        return TOP.subList(skip, limit, id -> {
            Guild guild = Guild.getGuild(id);
            if (guild == null) return null;
            return predicate == null || predicate.test(guild) ? guild : null;
        });
    }

    public Guild getData(String key) {
        UUID id = NAMES.get(GuildManager.toQuantumName(key));
        return id == null ? null : this.getData(id);
    }

    public void renameGuild(Guild guild, String rename) {
        this.remove(guild);
        NAMES.put(GuildManager.toQuantumName(rename), guild.getId());
    }

    public void remove(Guild guild) {
        NAMES.remove(GuildManager.toQuantumName(guild.getName()));
    }

    @Override
    protected void onLoad(Guild value) {
        NAMES.put(GuildManager.toQuantumName(value.getName()), value.getId());
    }
}
