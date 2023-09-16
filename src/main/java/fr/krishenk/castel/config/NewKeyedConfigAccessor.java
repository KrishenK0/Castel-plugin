package fr.krishenk.castel.config;

import com.google.common.base.Enums;
import fr.krishenk.castel.config.implementation.NewKeyedYamlConfigAccessor;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface NewKeyedConfigAccessor {
    NewKeyedConfigAccessor withProperty(String var1);

    NewKeyedConfigAccessor applyProperties();

    NewKeyedYamlConfigAccessor withOption(String var1, String var2);

    default NewKeyedConfigAccessor forWorld(String world) {
        return this.withProperty(world).isSet() ? this : this.clearExtras().withProperty("default");
    }

    NewKeyedYamlConfigAccessor clearExtras();

    default NewKeyedConfigAccessor forWorld(Entity entity) {
        return this.forWorld(entity.getWorld().getName());
    }

    String getDynamicOption();

    boolean isSet();

    String getString();

    String getKey();

    MathCompiler.Expression getMathExpression();

    Object get();

    List<String> getStringList();

    Set<String> getSectionKeys();

    ConfigurationSection getSection();

    default ConditionalCompiler.LogicalOperand getCondition() {
        if (this.getKey() == null) {
            return null;
        }
        if (!(this.get() instanceof ConditionalCompiler.LogicalOperand)) {
                return ConditionalCompiler.compile(this.getString()).evaluate();
        }
        Objects.requireNonNull(this.get(), "null cannot be cast to non-null type ConditionalCompiler.LogicalOperand");
        return (ConditionalCompiler.LogicalOperand)this.get();
    }

    boolean getBoolean();

    List<Integer> getIntegerList();

    int getInt();

    double getDouble();

    long getLong();

    default Long getTimeMillis() {
        return this.getTime(PlaceholderContextProvider.EMPTY);
    }

    Long getTime(PlaceholderContextProvider ctx);

    @Deprecated
    default Long getTimeMillis(TimeUnit timeUnit) {
        String time = this.getString();
        return time == null ? null : TimeUtils.parseTime(time, timeUnit);
    }

    default <T extends Enum<T>> T getEnum(Class<T> enumClazz) {
        return Enums.getIfPresent(enumClazz, this.getString()).orNull();
    }
}


