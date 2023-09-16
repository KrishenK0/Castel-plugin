package fr.krishenk.castel.config;

import com.google.common.base.Enums;
import fr.krishenk.castel.config.implementation.ConfigAccessorConfigurationSection;
import fr.krishenk.castel.utils.config.ConfigSection;
import fr.krishenk.castel.utils.time.TimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface ConfigAccessor {
    ConfigAccessor noDefault();

    boolean isSet(String ... var1);

    String getString(String ... var1);

    List<String> getStringList(String ... var1);

    Set<String> getKeys();

    ConfigAccessor gotoSection(String ... var1);

    String getCurrentPath();

    boolean getBoolean(String ... var1);

    int getInt(String ... var1);

    Object get(String ... var1);

    double getDouble(String ... var1);

    long getLong(String ... var1);

    ConfigSection getSection();

    List<Integer> getIntegerList(String ... var1);

    default Long getTimeMillis(String option) {
        return this.getTimeMillis(option, TimeUnit.SECONDS);
    }

    Map<String, Object> getEntries();

    default Long getTimeMillis(String option, TimeUnit timeUnit) {
        String time = this.getString(option);
        return time == null ? null : TimeUtils.parseTime(time, timeUnit);
    }

    default <T extends Enum<T>> T getEnum(Class<T> enumClazz, String option) {
        return Enums.getIfPresent(enumClazz, this.getString(option)).orNull();
    }

    default ConfigAccessorConfigurationSection toBukkitConfigurationSection() {
        return new ConfigAccessorConfigurationSection(this);
    }
}


