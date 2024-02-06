package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.abstraction.ImmutableLocation;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.InviteCode;
import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.group.model.relationships.RelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.group.upgradable.Upgrade;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.RankMap;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.utils.JsonItemStack;
import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;

public class DataHandlerGuild extends DataHandler<UUID, Guild> {
    private static SQLDataHandlerProperties createSQLDataHandlerProperties() {
        Object[] arrObj = new Object[]{SQLDataHandlerProperties.ofLocation("home"), "color", "flag"};
        String[] arrStr = ArrayUtils.mergeObjects(arrObj);
        return new SQLDataHandlerProperties(arrStr);
    }

    public DataHandlerGuild() {
        super(StdIdDataType.UUID, createSQLDataHandlerProperties());
    }

    @Override
    public void save(@NotNull SectionableDataSetter provider, @NotNull Guild data) {
        DataHandlerGroup.save(provider, data);
        provider.setUUID("leader", data.getLeaderId());
        provider.setString("lore", data.getLore());
        provider.setInt("maxLandsModifier", data.getMaxLandsModifier());
        //provider.setLong("lastInvasion", data.getLastInvasion());
        provider.setBoolean("pacifist", data.isPacifist());

        provider.get("lands").setCollection(data.getLandLocations(), DataSetter::setSimpleChunkLocation);


        provider.get("members").setCollection(data.getMembers(), DataHandlerGuild::saveMembers);

        SectionableDataSetter upgradeSection = provider.createSection("upgrades");
        Pair<String ,Map<Upgrade, Integer>>[] upgrades = new Pair[]{
                new Pair<>("misc", data.getMiscUpgrades()),
                new Pair<>("powerups", data.getPowerups()),
//                new Pair<>("champions", data.getChampionUpgrades())
        };

        for (Pair<String, Map<Upgrade, Integer>> upgrade : upgrades) {
            upgradeSection.get(upgrade.getKey()).setMap(upgrade.getValue(), (key, keyProvider, value) -> {
                keyProvider.setString(((Enum<?>)key).name());
                keyProvider.getValueProvider().setInt(value);
            });
        }

        SectionableDataSetter chestProvider = provider.get("chest");
        ItemStack[] itemStackArray = data.getChest().getContents();
        Map<Integer, ItemStack> itemStackMap = new HashMap<>();

        for (int i = 0; i < itemStackArray.length; i++) {
            itemStackMap.put(i, itemStackArray[i]);
        }
        chestProvider.setMap(itemStackMap, DataHandlerGuild::saveChest);

        //var10000 = provider.get("book");
        //Map var33 = data.getBook();
        //var10000.setMap(var33, DataHandlerGuild::save$lambda-8);
        //provider.get("mails").setCollection((Collection)data.getMails().keySet(), DataHandlerGuild::save$lambda-9);

        provider.get("challenges").setMap(data.getChallenges(), DataHandlerGuild::saveChallenge);
        provider.get("inviteCodes").setMap(data.getInviteCodes(), DataHandlerGuild::saveInvitationCodes);
    }


    @Override
    public Guild load(@NotNull SectionableDataGetter provider, @NotNull UUID id) throws SQLException {
        DataHandlerGroup.DataHolder parent = DataHandlerGroup.load(provider);

        UUID leader = provider.get("leader").asUUID();
        String name = parent.getName();
        String tag = parent.getTag();
        String lore = provider.getString("lore");
        String tax = parent.getTax();
        String flag = parent.getFlag();
        Color color = parent.getColor();

        //String championType = provider.get("championType").asString((Function0)NamelessClass_2.INSTANCE);
        int maxLandsModifier = provider.getInt("maxLandsModifier");
        double bank = parent.getBank();
        long resourcePoints = parent.getResourcePoints();
//        long lastInvasion = provider.getLong("lastInvasion");
        long since = parent.getSince();
        //long shieldSince = parent.getShieldSince();
        //long shieldTime = parent.getShieldTime();
        boolean pacifist = provider.getBoolean("pacifist");
        boolean publicHome = parent.getPublicHome();
        boolean permanent = parent.getPermanent();
        boolean requiresInvite = parent.getRequiresInvite();
        ImmutableLocation home = ImmutableLocation.of(parent.getHome());
        //SimpleLocation nexus = parent.getNexus();
        RankMap ranks = parent.getRanks();
        Map<GuildRelation, Set<RelationAttribute>> attributes = parent.getAttributes();
        Map<UUID, GuildRelationshipRequest> relationshipRequests = parent.getRelationshipRequests();
        Map<UUID, GuildRelation> relations = parent.getRelations();
        Set<UUID> members = provider.get("members").asCollection(new HashSet<>(), DataHandlerGuild::loadMembers);
        Set<SimpleChunkLocation> lands = provider.get("lands").asCollection(new HashSet<>(), DataHandlerGuild::loadLands);
        SectionableDataGetter upgrades = provider.get("upgrades").asSection();
        Map<MiscUpgrade, Integer> miscUpgrades = upgrades.get("misc").asMap(new QuickEnumMap<>(MiscUpgrade.values()), (map, key, value) -> {
            try {
                map.put(MiscUpgrade.valueOf(key.asString()), value.asInt());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Map<Powerup, Integer> powerups = upgrades.get("powerups").asMap(new QuickEnumMap<>(Powerup.values()), (map, key, value) -> {
            try {
                map.put(Powerup.valueOf(key.asString()), value.asInt());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        //Map championUpgrades = upgrades.get("champions").asMap((Map)(new QuickEnumMap((Enum[]) ChampionUpgrade.VALUES)), DataHandlerGuild::load$lambda-18);
        Map<Integer, ItemStack> chestItems = provider.get("chest").asMap(new HashMap<>(), DataHandlerGuild::loadChest);
//        Map<String, BookChapter> book = provider.get("book").asMap(new HashMap<>(), (map, key, value) -> {
//            map.put(key.asString(), new HashMap<>())
//        });
        Set<UUID> mails = provider.get("mails").asCollection(new HashSet<>(), (collectionn, key) -> {
            try {
                collectionn.add(key.asUUID());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Map<UUID, Long> challenges = provider.get("challenges").asMap(new HashMap<>(), DataHandlerGuild::loadChallenges);
        Map<String, InviteCode> inviteCodes = provider.get("inviteCodes").asMap(new HashMap<>(), DataHandlerGuild::loadInviteCodes);

        LinkedList<AuditLog> logs = parent.getLogs();

        Guild guild = new Guild(id,leader, name, tag, since, members, ranks,resourcePoints, home, publicHome, color, bank, tax, flag, relationshipRequests, relations, attributes, requiresInvite, mails, permanent,logs,lands,powerups, inviteCodes,challenges, chestItems, lore, pacifist, maxLandsModifier, miscUpgrades);
        DataHandlerMetadata.deserializeMetadata(provider, guild);
        return guild;
    }

    private static void loadMembers(HashSet set, SectionableDataGetter element) {
        try {
            set.add(element.asUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void loadLands(HashSet set, SectionableDataGetter element) {
        try {
            set.add(element.asSimpleChunkLocation());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void loadChest(HashMap map, DataGetter key, SectionableDataGetter value) {
        try {
            map.put(key.asInt(), JsonItemStack.deserialize(value.asString()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadChallenges(HashMap map, DataGetter key, SectionableDataGetter value) {
        try {
            map.put(key.asUUID(), value.asLong());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadInviteCodesUsedBy(HashSet list2, SectionableDataGetter element) {
        try {
            list2.add(element.asUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadInviteCodes(HashMap map, DataGetter key, SectionableDataGetter value) {
        String code = null;
        try {
            code = key.asString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long createdAt;
        int uses;
        UUID createdBy;
        long expiration;
        Set<UUID> usedBy;
        try {
            createdAt = value.getLong("createdAt");
            createdBy = value.get("createdBy").asUUID();
            expiration = value.getLong("expiration");
            uses = value.getInt("uses");
            usedBy = value.get("usedBy").asCollection(new HashSet(), DataHandlerGuild::loadInviteCodesUsedBy);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        map.put(code, new InviteCode(code, createdAt, expiration, createdBy, usedBy, uses));
    }

    private static void saveMembers(SectionCreatableDataSetter obj, UUID value) {
        obj.setUUID(value);
    }

    private static void saveChest(int key, MappedIdSetter keyProvider, ItemStack value) {
        keyProvider.setInt(key);
        keyProvider.getValueProvider().setString(JsonItemStack.serialize(value));
    }

    private static void saveChallenge(UUID key, MappedIdSetter keyProvider, Long value) {
        keyProvider.setUUID(key);
        SectionCreatableDataSetter sectionCreatableDataSetter = keyProvider.getValueProvider();
        sectionCreatableDataSetter.setLong(value);
    }

    private static void saveInvitationCodeUsedby(SectionCreatableDataSetter elementProvider, UUID element) {
        elementProvider.setUUID(element);
    }

    private static void saveInvitationCodes(String key, MappedIdSetter keyProvider, InviteCode value) {
        keyProvider.setString(key);
        SectionableDataSetter sectionableDataSetter = keyProvider.getValueProvider().createSection();
        sectionableDataSetter.setUUID("createdBy", value.getCreatedBy());
        sectionableDataSetter.setLong("createdAt", value.getCreatedAt());
        sectionableDataSetter.setLong("expiration", value.getExpiration());
        sectionableDataSetter.setInt("uses", value.getUses());
        sectionableDataSetter.get("usedBy").setCollection(value.getUsedBy(), DataHandlerGuild::saveInvitationCodeUsedby);
    }
}
