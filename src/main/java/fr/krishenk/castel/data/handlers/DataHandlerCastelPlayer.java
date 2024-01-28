package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.lang.Config;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataHandlerCastelPlayer extends DataHandler<UUID, CastelPlayer> {

    public DataHandlerCastelPlayer() {
        super(StdIdDataType.UUID, new SQLDataHandlerProperties(new String[]{"power", "lastPowerCheckup"/*"chatChannel", "mapSize_height", "mapSize_width"*/}));
    }

    @Override
    public void save(SectionableDataSetter provider, CastelPlayer data) {
        provider.setUUID("guild", data.getGuildId());
        //provider.setString("lang", data.getLanguage().name());
        provider.setString("rank", data.getRankNode());
//        if (data.getChatChannelId() != null) {
//            provider.setString("chatChannel", data.getChatChannel().getDataId());
//        }
        provider.setLong("joinedAt", data.getJoinedAt());
        if (Config.Powers.POWER_ENABLED.getManager().getBoolean()) {
            provider.setDouble("power", data.getPower());
            provider.setLong("lastPowerCheckup", data.getLastPowerCheckup());
        }
        provider.setLong("lastDonationTime", data.getLastDonationTime());
        provider.setLong("lastDonationAmount", data.getLastDonationAmount());
        provider.setLong("totalDonations", data.getTotalDonations());
        provider.setBoolean("pvp", data.isPvp());
        provider.setBoolean("spy", data.isSpy());
        provider.setBoolean("admin", data.isAdmin());
        provider.setBoolean("sneakMode", data.isInSneakMode());
        provider.setBoolean("markers", data.isUsingMarkers());
        provider.setString("markersType", data.getMarkersType());
//        Pair<Integer, Integer> mapSize = data.getMapSize();
//        if (mapSize != null) {
//            SectionableDataSetter mapSizeProvider = provider.createSection("mapSize");
//            mapSizeProvider.setInt("height", mapSize.getKey());
//            mapSizeProvider.setInt("width", mapSize.getValue());
//        }
        provider.get("invites").setMap(data.getInvites(), (key, keyProvider, value) -> {
            keyProvider.setUUID(key);
            SectionableDataSetter valueProvider = keyProvider.getValueProvider().createSection();
            valueProvider.setUUID("sender", value.getSender());
            valueProvider.setLong("acceptTime", value.getAcceptTime());
            valueProvider.setLong("timestamp", value.getTimestamp());
        });
        provider.get("claims").setCollection(data.getClaims(), DataSetter::setSimpleChunkLocation);
        //provider.get("mutedChannels").setCollection(data.getMutedChannels(), DataSetter::setString);
        provider.get("readMails").setCollection(data.getReadMails(), DataSetter::setUUID);
        DataHandlerMetadata.serializeMetadata(provider, data);
    }

    @Override
    public CastelPlayer load(SectionableDataGetter provider, UUID uuid) throws SQLException {
        //String langName = provider.getString("lang");
        //SupportedLanguage language = langName != null ? SupportedLanguage.valueOf(langName) : LanguageManager.getDefaultLanguage();
        UUID guild = provider.get("guild").asUUID();
        String rank = provider.getString("rank");
        String markersType = provider.getString("markersType");
        //String channel = provider.get("chatChannel").asString((language) -> "GLOBAL");
        double power = provider.getDouble("power");
        long lastPowerCheckup = provider.getLong("lastPowerCheckup");
        long joinedAt = provider.getLong("joinedAt");
        long lastDonationTime = provider.getLong("lastDonationTime");
        long lastDonationAmount = provider.getLong("lastDonationAmount");
        long totalDonations = provider.getLong("totalDonations");
        boolean markers = provider.getBoolean("markers");
        boolean pvp = provider.getBoolean("pvp");
        boolean admin = provider.getBoolean("admin");
        boolean spy = provider.getBoolean("spy");
        boolean sneakMode = provider.getBoolean("sneakMode");
        Set<UUID> readMails = provider.get("readMails").asCollection(new HashSet<>(), (c, e) -> {
            try {
                c.add(e.asUUID());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
//        Set mutedChannels = provider.get("mutedChannels").asCollection(new HashSet(), (c, e) -> c.add(e.asString()));
        Set<SimpleChunkLocation> claims = provider.get("claims").asCollection(new HashSet<>(), (c, e) -> {
            try {
                c.add(e.asSimpleChunkLocation());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
//        SectionableDataGetter mapSizeSection = provider.get("mapSize").asSection();
//        int mapHeight = mapSizeSection.getInt("height");
//        int mapWidth = mapSizeSection.getInt("width");
//        Pair<Integer, Integer> mapSize = mapHeight == 0 || mapWidth == 0 ? null : Pair.of(mapHeight, mapWidth);
        HashMap<UUID, GuildInvite> invites = provider.get("invites").asMap(new HashMap<>(), (m, k, v) -> {
            try {
                m.put(k.asUUID(), new GuildInvite(v.get("sender").asUUID(), v.getLong("acceptTime"), v.getLong("timestamp")));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        CastelPlayer cp = new CastelPlayer(joinedAt, guild, uuid, admin, pvp, spy, markers, sneakMode, rank, markersType, lastDonationTime, lastDonationAmount, totalDonations, readMails, invites, claims);
        DataHandlerMetadata.deserializeMetadata(provider, cp);
        return cp;
    }
}
