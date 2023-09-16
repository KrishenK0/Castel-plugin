package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.group.model.relationships.RelationAttribute;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.constants.player.RankMap;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.data.json.NamedJsonDataProvider;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumMap;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;

public class DataHandlerGroup {
    public static final DataHandlerGroup INSTANCE = new DataHandlerGroup();
    
    private DataHandlerGroup() {}

    public static void save(@NotNull SectionableDataSetter provider, @NotNull Group data) {
        provider.setString("name", data.getName());
        provider.setString("tag", data.getTag());
        provider.setString("tax", data.getTax());
        provider.setDouble("publicHomeCost", data.getPublicHomeCost());
        if (data.getFlag() != null) {
            provider.setString("flag", data.getFlag());
        }
        if (data.getColor() != null) {
            provider.setInt("color", data.getColor().getRGB());
        }
        provider.setDouble("bank", data.getBank());
        provider.setLong("resourcePoints", data.getResourcePoints());
        provider.setLong("since", data.getSince());
        //provider.setLong("shieldSince", data.getShieldSince());
        //provider.setLong("shieldTime", data.getShieldTime());
        provider.setBoolean("hidden", data.isHidden());
        provider.setBoolean("publicHome", data.isHomePublic());
        provider.setBoolean("permanent", data.isPermanent());
        provider.setBoolean("requiresInvite", data.requiresInvite());

        if (data.getHome() != null) {
            SectionableDataSetter sectionableDataSetter2 = provider.get("home");
            Location location = data.getHome();
            Objects.requireNonNull(location);
            sectionableDataSetter2.setLocation(location);
        }

        provider.get("ranks").setMap(data.getRanks().getRanks(), (key, keyProvider, value) -> {
            keyProvider.setString(key);
            NamedJsonDataProvider section = (NamedJsonDataProvider) keyProvider.getValueProvider().createSection();
            DataHandlerRank.serializeRank(value, section);
        });

        provider.get("attributes").setMap(data.getAttributes(), DataHandlerGroup::saveRelations);
        provider.get("relationshipRequests").setMap(data.getRelationshipRequests(), DataHandlerGroup::saveRelationhipRequests);
        provider.get("relations").setMap(data.getRelations(), DataHandlerGroup::saveRelation);

        //SectionableDataSetter sectionableDataSetter8 = provider.get("logs");
        //LinkedList<AuditLog> linkedList = data.getLogs();
        //Objects.requireNonNull(linkedList, "data.logs");
        //sectionableDataSetter8.setCollection(linkedList, DataHandlerGroup::saveAuditLogs);
        DataHandlerMetadata.serializeMetadata(provider, data);
    }

    @NotNull
    public static DataHolder load(@NotNull SectionableDataGetter data) throws SQLException {
        DataHolder dataHolder = new DataHolder();
        String string = data.getString("name");
        Objects.requireNonNull(string);
        dataHolder.setName(string);
        dataHolder.setTag(data.getString("tag"));
        dataHolder.setTax(data.getString("tax"));
        dataHolder.setFlag(data.getString("flag"));
        dataHolder.setPublicHomeCost(data.getDouble("publicHomeCost"));
        int color = data.getInt("color");
        dataHolder.setColor(color != 0 ? new Color(color) : null);
        dataHolder.setBank(data.getDouble("bank"));
        dataHolder.setResourcePoints(data.getLong("resourcePoints"));
        dataHolder.setSince(data.getLong("since"));
        //dataHolder.setShieldSince(data.getLong("shieldSince"));
        //dataHolder.setShieldTime(data.getLong("shieldTime"));
        dataHolder.setPublicHome(data.getBoolean("publicHome"));
        dataHolder.setPermanent(data.getBoolean("permanent"));
        dataHolder.setRequiresInvite(data.getBoolean("requiresInvite"));
        dataHolder.setHome(data.get("home").asLocation());
        //dataHolder.setNexus(data.get("nexus").asSimpleLocation());
        HashMap<String, Rank> rankMapping = data.get("ranks").asMap(new HashMap<>(), DataHandlerGroup::loadRank);
        RankMap rankMap;
        if (rankMapping.isEmpty()) {
            rankMap = Rank.copyDefaults();
        } else {
            TreeMap<Integer, Rank> sorted = new TreeMap<>();
            for (Map.Entry<String, Rank> entry : rankMapping.entrySet()) {
                Rank rank = entry.getValue();
                sorted.put(rank.getPriority(), rank);
            }
            rankMap = new RankMap(rankMapping, sorted);
        }

        dataHolder.setRanks(rankMap);
        dataHolder.setAttributes(data.get("attributes").asMap(new QuickEnumMap<>(GuildRelation.values()), DataHandlerGroup::loadAttributes));
        dataHolder.setRelationshipRequests(data.get("relationshipRequests").asMap(new HashMap<>(), DataHandlerGroup::loadRelationshipRequests));
        dataHolder.setRelations(data.get("relations").asMap(new HashMap<>(), DataHandlerGroup::loadRelations));
        //dataHolder.setLogs(data.get("logs").asCollection(new LinkedList<>(), (arg0, arg1) -> DataHandlerGroup.load$lambda-18$lambda-17(dataHolder, arg0, arg1)))
        return dataHolder;
    }

    private static void saveRank(String key, MappedIdSetter keyProvider, Rank value) {
        keyProvider.setString(key);
        DataHandlerRank.serializeRank(value, keyProvider.getValueProvider().createSection());
    }

    private static void saveRelationAttributes(SectionCreatableDataSetter elementProvider, RelationAttribute element) {
        elementProvider.setString(element.getNamespace().asNormalizedString());
    }

    private static void saveRelations(GuildRelation key, MappedIdSetter keyProvider, Set value) {
        keyProvider.setString(key.name());
        SectionCreatableDataSetter sectionCreatableDataSetter = keyProvider.getValueProvider();
        sectionCreatableDataSetter.setCollection(value, DataHandlerGroup::saveRelationAttributes);
    }

    private static void saveRelationhipRequests(UUID key, MappedIdSetter keyProvider, GuildRelationshipRequest value) {
        SectionableDataSetter sectionableDataSetter = keyProvider.getValueProvider().createSection();
        sectionableDataSetter.setString("relation", value.getRelation().name());
        sectionableDataSetter.setUUID("sender", value.getSender());
        sectionableDataSetter.setLong("acceptTime", value.getAcceptTime());
        sectionableDataSetter.setLong("timestamp", value.getTimestamp());
    }

    private static void saveRelation(UUID key, MappedIdSetter keyProvider, GuildRelation value) {
        keyProvider.setUUID(key);
        keyProvider.getValueProvider().setString(value.name());
    }

    private static void loadRank(HashMap<String, Rank> map, DataGetter key, SectionableDataGetter value) {
        try {
            Rank rank = DataHandlerRank.deserializeRank(key.asString(), value);
            map.put(key.asString(), rank);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRelationsAttributes(HashSet set, SectionableDataGetter element) {
        Namespace namespace;
        try {
            namespace = Namespace.fromString(element.asString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        set.add(CastelPlugin.getInstance().getRelationAttributeRegistry().getRegistered(namespace));
    }

    private static void loadAttributes(Map map, DataGetter key, SectionableDataGetter value) {
        try {
            map.put(GuildRelation.valueOf(key.asString()), value.asCollection(new HashSet<>(), DataHandlerGroup::loadRelationsAttributes));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRelationshipRequests(HashMap map, DataGetter key, SectionableDataGetter value) {
        try {
            GuildRelation relation = GuildRelation.valueOf(value.getString("relation"));
            UUID sender = value.get("sender").asUUID();
            long acceptTime = value.getLong("acceptTime");
            long timestamp = value.getLong("timestamp");

            map.put(key.asUUID(), new GuildRelationshipRequest(relation, sender, acceptTime, timestamp));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRelations(HashMap map, DataGetter key, SectionableDataGetter value) {
        try {
            map.put(key.asUUID(), GuildRelation.valueOf(value.asString()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private static void saveAuditLogs(SectionCreatableDataSetter elementProvider, AuditLog element) {
        SectionableDataSetter sectionCreatableDataSetter = elementProvider.createSection();
        sectionCreatableDataSetter.setString("namespace", element.getProvider().getNamespace().asNormalizedString());
        element.serialize(new SerializationContext<DataSetter>(elementProvider));
    }*/

    public static class DataHolder {
        private UUID leader;
        private String name;
        private String tag;
        private long since;
        private Set<UUID> members;
        private RankMap ranks;
        private Location home;
        private double publicHomeCost;
        private boolean publicHome;
        private Color color;
        private double bank;
        private long resourcePoints;
        private String tax;
        private String flag;
        private Map<UUID, GuildRelationshipRequest> relationshipRequests;
        private Map<UUID, GuildRelation> relations;
        private Map<GuildRelation, Set<RelationAttribute>> attributes;
        private boolean requiresInvite;
        private boolean permanent;
        private boolean hidden;

        public String getName() {
            if (this.name != null) return name;
            return null;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UUID getLeader() {
            return leader;
        }

        public void setLeader(UUID leader) {
            this.leader = leader;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public long getSince() {
            return since;
        }

        public void setSince(long since) {
            this.since = since;
        }

        public Set<UUID> getMembers() {
            return members;
        }

        public void setMembers(Set<UUID> members) {
            this.members = members;
        }

        public RankMap getRanks() {
            return ranks;
        }

        public void setRanks(RankMap ranks) {
            this.ranks = ranks;
        }

        public Location getHome() {
            return home;
        }

        public void setHome(Location home) {
            this.home = home;
        }

        public double getPublicHomeCost() {
            return publicHomeCost;
        }

        public void setPublicHomeCost(double publicHomeCost) {
            this.publicHomeCost = publicHomeCost;
        }

        public boolean getPublicHome() {
            return publicHome;
        }

        public void setPublicHome(boolean publicHome) {
            this.publicHome = publicHome;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public double getBank() {
            return bank;
        }

        public void setBank(double bank) {
            this.bank = bank;
        }

        public String getTax() {
            return tax;
        }

        public void setTax(String tax) {
            this.tax = tax;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public Map<UUID, GuildRelationshipRequest> getRelationshipRequests() {
            return relationshipRequests;
        }

        public void setRelationshipRequests(Map<UUID, GuildRelationshipRequest> relationshipRequests) {
            this.relationshipRequests = relationshipRequests;
        }

        public Map<UUID, GuildRelation> getRelations() {
            return relations;
        }

        public void setRelations(Map<UUID, GuildRelation> relations) {
            this.relations = relations;
        }

        public Map<GuildRelation, Set<RelationAttribute>> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<GuildRelation, Set<RelationAttribute>> attributes) {
            this.attributes = attributes;
        }

        public boolean getRequiresInvite() {
            return requiresInvite;
        }

        public void setRequiresInvite(boolean requiresInvite) {
            this.requiresInvite = requiresInvite;
        }

        public boolean getPermanent() {
            return permanent;
        }

        public void setPermanent(boolean permanent) {
            this.permanent = permanent;
        }

        public long getResourcePoints() {
            return resourcePoints;
        }

        public void setResourcePoints(long resourcePoints) {
            this.resourcePoints = resourcePoints;
        }

        public boolean getHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }
    }
}
