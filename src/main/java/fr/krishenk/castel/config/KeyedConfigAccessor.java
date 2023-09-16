package fr.krishenk.castel.config;

import com.google.common.base.Enums;
import fr.krishenk.castel.config.implementation.KeyedYamlConfigAccessor;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.config.NodeInterpreter;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface KeyedConfigAccessor {
    KeyedConfigAccessor withProperty(String var1);

    KeyedConfigAccessor applyProperties();

    KeyedYamlConfigAccessor withOption(String var1, String var2);

    default KeyedConfigAccessor forWorld(String world) {
        return this.withProperty(world).isSet() ? this : this.clearExtras().withProperty("default");
    }

    KeyedYamlConfigAccessor clearExtras();

    default KeyedConfigAccessor forWorld(Entity entity) {
        return this.forWorld(entity.getWorld().getName());
    }

    String getDynamicOption();

    boolean isSet();

    String getString();

    Node getNode();

    MathCompiler.Expression getMathExpression();

    <T> T get(NodeInterpreter<T> var1);

    List<String> getStringList();

    Set<String> getSectionKeys();

    ConfigAccessor getSection();

    boolean getBoolean();

    List<Integer> getIntegerList();

    int getInt();

    double getDouble();

    long getLong();

    default Long getTimeMillis() {
        return this.getTime(PlaceholderContextProvider.EMPTY);
    }

    default Long getTime(PlaceholderContextProvider ctx) {
        return NodeInterpreter.getTime(this.getNode(), ctx);
    }

    @Deprecated
    default Long getTimeMillis(TimeUnit timeUnit) {
        String time = this.getString();
        return time == null ? null : TimeUtils.parseTime(time, timeUnit);
    }

    default <T extends Enum<T>> T getEnum(Class<T> enumClazz) {
        return Enums.getIfPresent(enumClazz, this.getString()).orNull();
    }
}


