package fr.krishenk.castel.constants.group.model.relationships;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.namespace.Namespace;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class StandardRelationAttribute extends RelationAttribute {
    public static final StandardRelationAttribute BUILD = StandardRelationAttribute.register("BUILD");
    public static final StandardRelationAttribute CEASEFIRE = StandardRelationAttribute.register("CEASEFIRE");
    public static final StandardRelationAttribute AUTO_REVOKE = StandardRelationAttribute.register("AUTO_REVOKE");
    public static final StandardRelationAttribute INTERACT = StandardRelationAttribute.register("INTERACT");
    public static final StandardRelationAttribute HOME = StandardRelationAttribute.register("HOME");
    public static final StandardRelationAttribute USE = StandardRelationAttribute.register("USE");
    public static final StandardRelationAttribute FLY = StandardRelationAttribute.register("FLY");
    public static final StandardRelationAttribute TELEPORT = StandardRelationAttribute.register("TELEPORT");
    public static final StandardRelationAttribute INVADE = StandardRelationAttribute.register("INVADE");
    public static final StandardRelationAttribute SHOW_HOLOGRAMS = StandardRelationAttribute.register("SHOW_HOLOGRAMS");
    public static final StandardRelationAttribute TURRET_CEASEFIRE = StandardRelationAttribute.register("TURRET_CEASEFIRE");
    public static final StandardRelationAttribute MANAGE_TURRETS = StandardRelationAttribute.register("MANAGE_TURRETS");
    public static final StandardRelationAttribute MANAGE_STRUCTURES = StandardRelationAttribute.register("MANAGE_STRUCTURES");

    public StandardRelationAttribute(@NotNull String name) {
        super(Namespace.castel(name));
    }

    public static void init() {
    }

    static StandardRelationAttribute register(String name) {
        StandardRelationAttribute attr = new StandardRelationAttribute(name);
        Map<Namespace, RelationAttribute> registry = CastelPlugin.getInstance().getRelationAttributeRegistry().getRawRegistry();
        attr.setHash(registry.size());
        registry.put(attr.getNamespace(), attr);
        return attr;
    }

    @Override
    public boolean hasAttribute(@Nullable Group group, @Nullable Group other) {
        return StandardRelationAttribute.hasAttribute(this, group, other);
    }

    public static boolean hasAttribute(@NonNull RelationAttribute attr, @Nullable Group group, @Nullable Group other) {
        if (group instanceof Guild && other instanceof Guild) {
            return StandardRelationAttribute.hasAttribute(attr, (Guild)group, (Guild)other);
        }
        return false;
    }

    public boolean hasAttribute(@Nullable Guild group, @Nullable Guild other) {
        return StandardRelationAttribute.hasAttribute(this, group, other);
    }

    public static boolean hasAttribute(@NonNull RelationAttribute attr, @Nullable Guild Guild, @Nullable Guild other) {
        GuildRelation relation;
        if (other == null) {
            return true;
        }
        GuildRelation GuildRelation = relation = Guild == null ? fr.krishenk.castel.constants.group.model.relationships.GuildRelation.NEUTRAL : Guild.getRelationWith(other);
        if (relation == GuildRelation.SELF) {
            return attr != INVADE;
        }
        Set<RelationAttribute> attributes = other.getAttributes().get(relation);
        if (Container.AGREEMENTS) {
            if (Guild == null) {
                return attributes != null && attributes.contains(attr);
            }
            Set<RelationAttribute> selfAgreement = Guild.getAttributes().get(relation);
            return selfAgreement != null && attributes != null && selfAgreement.contains(attr) && attributes.contains(attr);
        }
        return attributes != null && attributes.contains(attr);
    }

    private static final class Container {
        private static final boolean AGREEMENTS = true;

        private Container() {
        }
    }
}


