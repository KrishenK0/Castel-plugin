package fr.krishenk.castel.constants.group.upgradable;

import fr.krishenk.castel.config.NewEnumConfig;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.string.StringUtils;

public interface StandardGuildUpgrade extends GuildUpgrade {
    NewEnumConfig getEnabledOption();

    NewEnumConfig getScalingdOption();

    NewEnumConfig getUpgradeOption();

    NewEnumConfig getMaxLevelOption();

    NewEnumConfig getDefaultLevelOption();

    @Override
    default double getScaling(PlaceholderContextProvider provider) {
        return this.eval(this.getScalingdOption(), provider);
    }

    default double eval(NewEnumConfig config, PlaceholderContextProvider provider) {
        MathCompiler.Expression expr = config.getManager().withOption("upgrade", StringUtils.configOption(this.getDataName())).getMathExpression();
        return MathUtils.eval(expr, provider);
    }

    @Override
    default boolean isEnabled(PlaceholderContextProvider provider) {
        return this.getEnabledOption().getManager().withOption("upgrade", StringUtils.configOption(this.getDataName())).getBoolean();
    }

    default boolean isEnabled() {
        return this.isEnabled(null);
    }

    @Override
    default double getUpgradeCost(PlaceholderContextProvider provider) {
        return this.eval(this.getUpgradeOption(), provider);
    }

    @Override
    default int getMaxLevel(PlaceholderContextProvider provider) {
        return (int) this.eval(this.getMaxLevelOption(), provider);
    }

    String getDataName();

    default int getDefaultLevel(PlaceholderContextProvider provider) {
        return this.getDefaultLevelOption() == null ? 0 : (int) this.eval(this.getDefaultLevelOption(), provider);
    }
}
