package fr.krishenk.castel.constants.group.upgradable;

import fr.krishenk.castel.config.NewEnumConfig;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

public enum MiscUpgrade implements StandardGuildUpgrade {
    ANTI_EXPLOSION,
    ANTI_TRAMPLE,
    GUARDS,
    INSANITY,
    GLORY,
    CHEST_SIZE(false),
    MAX_MEMBER(false),
    MAX_CLAIMS(false),
    INVASION_TELEPORTATION,
    INVASION(false);
    public final boolean canBeDisabled;

    MiscUpgrade() {
        this(true);
    }

    MiscUpgrade(boolean canBeDisabled) {
        this.canBeDisabled = canBeDisabled;
    }

    public boolean canBeDisabled() {
        return canBeDisabled;
    }

    public String getDataName() {
        return this.name();
    }

    public ConfigurationSection getConfig() {
        return Config.MISC_UPGRADE.getDefaults().getConfigurationSection(StringUtils.configOption(this));
    }


    @Override
    public String toString() {
        return StringUtils.capitalize(this.name());
    }

    public NewEnumConfig getEnabledOption() { return Config.MiscUpgrades.ENABLED; }

    @Override
    public NewEnumConfig getScalingdOption() {
        return Config.MiscUpgrades.SCALING;
    }

    @Override
    public NewEnumConfig getUpgradeOption() {
        return Config.MiscUpgrades.COST;
    }

    public NewEnumConfig getMaxLevelOption() { return Config.MiscUpgrades.MAX_LEVEL; }

    public NewEnumConfig getDefaultLevelOption() { return Config.MiscUpgrades.DEFAULT_LEVEL; }

    public Messenger getDisplayName() {
        return new LanguageEntryMessenger("upgrades", "misc", StringUtils.configOption(this.name()), "name");
    }
}
