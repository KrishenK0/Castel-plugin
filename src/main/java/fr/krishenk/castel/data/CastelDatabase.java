package fr.krishenk.castel.data;

import fr.krishenk.castel.constants.metadata.CastelObject;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CastelDatabase<K, T extends CastelObject<K>> {
    @Nullable T load(@NonNull K key);

    @NonNull Collection<T> load(@NonNull Collection<K> keys, @NonNull Collection<T> to, @NonNull DataManager<K, T> dataManager);

    @NonNull Collection<T> loadAllData();

    void save(@NonNull T data);

    void save(@NonNull Collection<T> datas);

    void delete(@NonNull K key);

    boolean hasData(@NonNull K key);

    @NonNull Collection<K> getDataKeys();

    void deleteAllData();

    void close();
}
