package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.GuildPermission;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.utils.internal.enumeration.OrderedSet;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

public class DataHandlerRank {
    public static void serializeRank(Rank rank, SectionableDataSetter provider) {
        provider.setString("name", rank.getName());
        provider.setString("color", rank.getColor());
        provider.setString("symbol", rank.getSymbol());
        provider.setInt("priority", rank.getPriority());
        provider.setInt("maxClaims", rank.getMaxClaims());
        provider.setString("material", rank.getMaterial().name());
        provider.get("permissions").setCollection(rank.getPermissions(), (permProvider, value) -> {
            permProvider.setString(value.getNamespace().asNormalizedString());
        });
    }

    public static Rank deserializeRank(String node, SectionableDataGetter provider) throws SQLException {
        String name = provider.getString("name");
        String color = provider.getString("color");
        String symbol = provider.getString("symbol");
        int priority = provider.getInt("priority");
        int maxClaims = provider.getInt("maxClaims");
        XMaterial material = XMaterial.matchXMaterial(Objects.requireNonNull(provider.getString("material"))).orElse(XMaterial.DIRT);
        Set<GuildPermission> permissions = provider.get("permissions").asCollection(new OrderedSet<>(0), (perms, permProvider) -> {
            Namespace ns;
            try {
                ns = Namespace.fromString(permProvider.asString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            GuildPermission permission = CastelPlugin.getInstance().getPermissionRegistry().getRegistered(ns);
            if (permission == null) {
                CLogger.info("Unknown permission when parsing data for rank '" + node + "': " + node);
            } else
                perms.add(permission);
        });
        return new Rank(node, name, color, symbol, material, priority, maxClaims, permissions);
    }
}
