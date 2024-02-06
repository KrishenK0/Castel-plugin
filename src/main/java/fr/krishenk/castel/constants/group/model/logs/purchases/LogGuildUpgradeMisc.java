package fr.krishenk.castel.constants.group.model.logs.purchases;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.group.upgradable.Upgrade;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.UUID;

public class LogGuildUpgradeMisc extends LogGuildGeneralUpgrade {
    private static final Namespace NS = Namespace.castel("GUILD_UPGRADE_MISC");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildUpgradeMisc();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    @Override
    public AuditLogProvider getProvider() {
        return null;
    }

    protected LogGuildUpgradeMisc() {
    }

    public LogGuildUpgradeMisc(UUID player, long resourcePoints, int oldLevel, int newLevel, Upgrade upgrade) {
        super(player, resourcePoints, oldLevel, newLevel, upgrade);
    }

    @Override
    protected Upgrade getUpgradeFromString(String upgradeName) {
        return MiscUpgrade.valueOf(upgradeName);
    }
}
