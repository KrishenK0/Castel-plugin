package fr.krishenk.castel.constants.group.upgradable;

import fr.krishenk.castel.config.NewEnumConfig;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.string.StringUtils;

public enum Powerup implements StandardGuildUpgrade {
    DAMAGE_BOOST,
    DAMAGE_REDUCTION,
    REGENERATION_BOOST,
    ARROW_BOOST;
    private boolean ownLandOnly;

    public static void init() {
        for (Powerup powerup : values()) {
            powerup.ownLandOnly = Config.Powers.POWERUPS_OWN_LAND_ONLY.getManager().withOption("upgrade", StringUtils.configOption(powerup)).getBoolean();
        }
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(this.name());
    }

    @Override
    public String getDataName() {
        return this.name();
    }

    public boolean isOwnLandOnly() {
        return ownLandOnly;
    }

    @Override
    public NewEnumConfig getEnabledOption() {
        return Config.Powers.POWERUPS_ENABLED;
    }

    @Override
    public NewEnumConfig getScalingdOption() {
        return Config.Powers.POWERUPS_SCALING;
    }

    @Override
    public NewEnumConfig getUpgradeOption() {
        return Config.Powers.POWERUPS_COST;
    }

    @Override
    public NewEnumConfig getMaxLevelOption() {
        return Config.Powers.POWERUPS_MAX_LEVEL;
    }

    @Override
    public NewEnumConfig getDefaultLevelOption() {
        return Config.Powers.POWERUPS_DEFAULT_LEVEL;
    }

    @Override
    public Messenger getDisplayName() {
        return new LanguageEntryMessenger("upgrades", "powerup", StringUtils.configOption(this.name()), "name");
    }
}
