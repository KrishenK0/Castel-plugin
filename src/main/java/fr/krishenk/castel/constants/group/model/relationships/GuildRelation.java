package fr.krishenk.castel.constants.group.model.relationships;

import com.google.common.base.Enums;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.compiler.builders.LanguageEntryWithContext;
import fr.krishenk.castel.managers.PvPManager;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.internal.MapUtil;
import fr.krishenk.castel.utils.internal.enumeration.OrderedSet;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumMap;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public enum GuildRelation {
    SELF(null),
    NEUTRAL(null),
    TRUCE(StandardGuildPermission.TRUCE),
    ENEMY(StandardGuildPermission.ENEMY),
    ALLY(StandardGuildPermission.ALLIANCE);
    private static final Map<GuildRelation, Set<RelationAttribute>> DEFAULTS = new EnumMap<>(GuildRelation.class);
    private final StandardGuildPermission permission;
    private boolean customizable = true;
    private PvPManager.PvPType pvpType;

    GuildRelation(StandardGuildPermission permission) {
        this.permission = permission;
    }

    public static void init() {
        for (GuildRelation relation : GuildRelation.values()) {
            if (relation == SELF) continue;
            String relName = relation.name().toLowerCase(Locale.ENGLISH);
            ConfigurationSection section = Config.Relations.RELATIONS.getManager().getSection().getConfigurationSection(relName);
            Objects.requireNonNull(section, "Section is null for relationship: " + relName);
            if (section.isSet("customizable") && !section.getBoolean("customizable")) relation.customizable = false;

            if(PvPManager.isPvPType(PvPManager.PvPType.RELATIONAL)) {
                String pvp = section.getString("pvp");
                if (pvp == null) pvp = "";
                relation.pvpType = Enums.getIfPresent(PvPManager.PvPType.class, pvp.toUpperCase(Locale.ENGLISH)).or(PvPManager.PvPType.NORMAL);
            } else {
                relation.pvpType = PvPManager.getPvpType();
            }
            if (relation.pvpType == PvPManager.PvPType.RELATIONAL) {
                CLogger.info("PvP mode of individual relationships cannot be 'relationnal': " + relName);
            }
            OrderedSet<RelationAttribute> attriubes = new OrderedSet<>(5);
            for (RelationAttribute attriube : CastelPlugin.getInstance().getRelationAttributeRegistry().getRegistry().values()) {
                if (section.getBoolean(attriube.getNamespace().getConfigOptionName()))
                    attriubes.add(attriube);
            }
            DEFAULTS.put(relation, attriubes);
        }
        GuildRelation.SELF.customizable = false;
    }

    public boolean isCustomizable() {
        return customizable;
    }

    public PvPManager.PvPType getPvpType() {
        return pvpType;
    }

    public static Map<GuildRelation, Set<RelationAttribute>> deserialize(String name, JsonObject json, JsonDeserializationContext context) {
        JsonElement element = json.get("attributes");
        if (element == null) return GuildRelation.copyDefaults();
        json = element.getAsJsonObject();
        QuickEnumMap<GuildRelation, Set<RelationAttribute>> attributes = new QuickEnumMap<>(GuildRelation.values());
        for (GuildRelation rel : GuildRelation.values()) {
            JsonElement data = json.get(rel.name());
            if (data == null || !rel.customizable) {
                Set<RelationAttribute> defaults = DEFAULTS.get(rel);
                if (defaults == null) continue;
                attributes.put(rel, MapUtil.clone(defaults, new OrderedSet<>(defaults.size())));
                continue;
            }
            JsonArray array = data.getAsJsonArray();
            OrderedSet<RelationAttribute> attrs = new OrderedSet<>(array.size());
            for (JsonElement attrElement : array) {
                Namespace namespace = Namespace.fromString(attrElement.getAsString());
                RelationAttribute attr = CastelPlugin.getInstance().getRelationAttributeRegistry().getRegistered(namespace);
                if (attr == null)
                    CLogger.info("Unknown relationship attribute named '" + attrElement.getAsString() + "' (" + namespace + ") for group '" + name + "' removing it.");
                attrs.add(attr);
            }
            attributes.put(rel, attrs);
        }
        return attributes;
    }

    public static Map<GuildRelation, Set<RelationAttribute>> copyDefaults() {
        EnumMap<GuildRelation, Set<RelationAttribute>> relations = new EnumMap<>(GuildRelation.class);
        for (Map.Entry<GuildRelation, Set<RelationAttribute>> relation : DEFAULTS.entrySet()) {
            if (relation.getKey() == SELF) continue;
            Set<RelationAttribute> defaults = relation.getValue();
            relations.put(relation.getKey(), MapUtil.clone(defaults, new OrderedSet<>(defaults.size())));
        }
        return relations;
    }

    public String getConfigName() {
        return StringUtils.configOption(this.name());
    }

    public String getColor() {
        return Config.Relations.COLOR.getManager().withOption("relation", this.getConfigName()).getString();
    }

    public StandardGuildPermission getPermission() {
        return permission;
    }

    public LanguageEntryWithContext getName() {
        return new LanguageEntryWithContext("relations", this.getConfigName(), "name");
    }

    public MathCompiler.Expression getCost() {
        return Config.Relations.COST.getManager().withOption("relation", this.getConfigName()).getMathExpression();
    }

    public MathCompiler.Expression getLimit() {
        return Config.Relations.LIMIT.getManager().withOption("relation", this.getConfigName()).getMathExpression();
    }
}
