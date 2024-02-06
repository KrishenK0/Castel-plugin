package fr.krishenk.castel.constants.group.model.logs.lands;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.data.dataproviders.DataSetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class LogGuildLand extends AuditLog {
    private UUID player;
    private Set<SimpleChunkLocation> lands;

    public LogGuildLand() {
    }

    public LogGuildLand(UUID player, Set<SimpleChunkLocation> lands) {
        this.player = player;
        this.lands = lands;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.player = json.get("player").asUUID();
        this.lands = json.get("lands").asCollection(new HashSet<>(), (c, e) -> {
            try {
                c.add(e.asSimpleChunkLocation());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("player", this.player);
        json.get("lands").setCollection(this.lands, DataSetter::setSimpleChunkLocation);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        if (this.player != null) builder.withContext(this.getPlayer());
        else builder.raw("player", Lang.UNKNOWN);
        builder.parse("lands", "{$s}" + this.lands.stream().map(x -> Lang.LOCATIONS_CHUNK.parse(LocationUtils.getChunkEdits(x))).collect(Collectors.joining("\n{$s}")));
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.player);
    }
}
