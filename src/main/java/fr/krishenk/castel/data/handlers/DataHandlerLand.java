package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.data.dataproviders.SQLDataHandlerProperties;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.data.dataproviders.StdIdDataType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class DataHandlerLand extends DataHandler<SimpleChunkLocation, Land> {
    public DataHandlerLand() {
        super(StdIdDataType.SIMPLE_CHUNK_LOCATION, new SQLDataHandlerProperties(new String[0]));
    }

    @Override
    public void save(SectionableDataSetter provider, Land data) {
        provider.setUUID("guild", data.getGuildId());
        provider.setUUID("claimedBy", data.getClaimedBy());
        provider.setLong("since", data.getSince());
        provider.get("protectedBlocks").setMap(data.getProtectedBlocks(), (key, keyProvider, value) -> {
            keyProvider.setSimpleLocation(key);
            SectionableDataSetter valueProvider = keyProvider.getValueProvider().createSection();
            valueProvider.setString("password", value.getPassword());
            valueProvider.get("sign").setSimpleLocation(value.getSign());
            valueProvider.setUUID("owner", value.getOwner());
            valueProvider.setLong("since", value.getSince());
            valueProvider.setString("protectionType", value.getProtectionType().name());
            valueProvider.get("players").setMap(value.getPlayers(), (pKey, pKeyProvider, pValue) -> {
                pKeyProvider.setUUID(pKey);
                pKeyProvider.getValueProvider().setBoolean(pValue);
            });
            valueProvider.get("guilds").setMap(value.getGuilds(), (pKey, pKeyProvider, pValue) -> {
                pKeyProvider.setUUID(pKey);
                pKeyProvider.getValueProvider().setBoolean(pValue);
            });
        });
    }

    @Override
    public Land load(SectionableDataGetter provider, SimpleChunkLocation location) throws SQLException {
        UUID guild = provider.get("guild").asUUID();
        UUID claimedBy = provider.get("claimedBy").asUUID();
        long since = provider.getLong("since");
        HashMap<SimpleLocation, ProtectionSign> protectedBlocks = provider.get("protectedBlocks").asMap(new HashMap<>(), (map, key, value) -> {
            try {
                SimpleLocation loc = key.asSimpleLocation();
                SimpleLocation sign = value.get("sign").asSimpleLocation();
                String password = value.getString("password");
                UUID owner = value.get("owner").asUUID();
                long sinceProtectionSign = value.getLong("since");
                String protectionTypeStr = value.getString("protectionType");
                ProtectionSign.ProtectionType protectionType = protectionTypeStr == null ? ProtectionSign.ProtectionType.PROTECTED : ProtectionSign.ProtectionType.valueOf(protectionTypeStr);
                HashMap<UUID, Boolean> players = value.get("players").asMap(new HashMap<>(), (proMap, proKey, proValue) -> {
                    try {
                        proMap.put(proKey.asUUID(), proValue.asBoolean());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                HashMap<UUID, Boolean> guilds = value.get("guilds").asMap(new HashMap<>(), (proMap, proKey, proValue) -> {
                    try {
                        proMap.put(proKey.asUUID(), proValue.asBoolean());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                map.put(loc, new ProtectionSign(loc, sign, owner, protectionType, password, since, players, guilds));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return new Land(location, guild, claimedBy, protectedBlocks, since);
    }
}
