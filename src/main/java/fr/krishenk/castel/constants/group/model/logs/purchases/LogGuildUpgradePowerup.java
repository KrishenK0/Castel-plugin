package fr.krishenk.castel.constants.group.model.logs.purchases;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.group.upgradable.Upgrade;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.UUID;

public class LogGuildUpgradePowerup extends LogGuildGeneralUpgrade {
    private static final Namespace NS = Namespace.castel("GUILD_UPGRADE_POWERUP");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildUpgradePowerup();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    protected LogGuildUpgradePowerup() {
    }

    @Override
    public AuditLogProvider getProvider() {
        return PROVIDER;
    }

    public LogGuildUpgradePowerup(UUID player, long resourcePoints, int oldLevel, int newLevel, Upgrade upgrade) {
        super(player, resourcePoints, oldLevel, newLevel, upgrade);
    }

    @Override
    protected Upgrade getUpgradeFromString(String upgradeName) {
        return Powerup.valueOf(upgradeName);
    }
}
