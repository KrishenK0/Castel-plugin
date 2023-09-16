package fr.krishenk.castel.data;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.utils.cache.CacheHandler;
import fr.krishenk.castel.utils.cache.CaffeineWrapper;
import fr.krishenk.castel.utils.cache.JavaMapWrapper;
import fr.krishenk.castel.utils.cache.PeekableMap;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class DataManager<K, T extends CastelObject<K>> {
    public static final boolean SMART = true;
    protected static final long INTERVAL = 86400000; // 24hr
    private final String displayName;
    protected final PeekableMap<K, T> cache;
    protected final Set<K> doesntExist;
    protected CastelDatabase<K, T> database;
    private boolean savingState = true;
    private final boolean timedCache;

    public DataManager(String displayName, CastelDatabase<K, T> database) {
        this(displayName, database, false);
    }

    public DataManager(String displayName, CastelDatabase<K,T> database, boolean caheExistence) {
        this(displayName, database, caheExistence, true);
    }

    public DataManager(String displayName, CastelDatabase<K,T> database, boolean caheExistence, boolean timedCache) {
        this.displayName = displayName;
        this.database = database;
        this.doesntExist = caheExistence ? new HashSet<>() : null;
        CacheLoader<K, T> loader = caheExistence ? key -> {
            if (DataManager.this.doesntExist.contains(key)) return null;
            T data = database.load(key);
            if (data == null) DataManager.this.doesntExist.add(key);
            DataManager.this.onLoad(data);
            return data;
        } : key -> {
            T data = database.load(key);
            if (data != null) DataManager.this.onLoad(data);
            return data;
        };
        if(!timedCache) { // load all data on startup
            this.cache = new JavaMapWrapper<>(new ConcurrentHashMap<>(), loader);
            this.timedCache = false;
        } else {
            Caffeine<Object, Object> builder = CacheHandler.newBuilder();
            builder.expireAfterAccess(INTERVAL * 2L, TimeUnit.MILLISECONDS);
            this.cache = new CaffeineWrapper<K, T>(builder.build(loader));
            this.timedCache = true;
        }
    }

    public void delete(K identifier) {
        this.unload(identifier);
        this.database.delete(identifier);
        if (this.doesntExist != null) this.doesntExist.add(identifier);
    }

    public String getDisplayName() {return this.displayName;}

    public void clear() {
        this.cache.clear();
        if (this.doesntExist != null) this.doesntExist.clear();
    }

    public void deleteAllData() {
        this.clear();
        this.database.deleteAllData();
    }

    public void setSavingState(boolean savingState) {
        this.savingState = savingState;
    }
    
    public void copyCacheTo(DataManager<K, T> dataManager) {
        dataManager.cache.putAll(this.cache);
    }
    
    public void emptyCache() {
        this.cache.clear();
    }
    
    public int loadAllData() {
        Collection<T> datas = this.database.loadAllData();
        if (datas == null) return 0;
        for (CastelObject data : datas) {
            this.load((T) data);
        }
        return datas.size();
    }

    public Collection<T> load(Collection<K> keys) {
        Collection<K> chosenKeys = keys;
        ArrayList<T> loaded = new ArrayList<>(keys.size());
        for (K key : keys) {
            T data = this.cache.getIfPresent(key);
            if (data == null) continue;
            loaded.add(data);
            if (chosenKeys == keys) chosenKeys = new HashSet<>(keys);
            chosenKeys.remove(key);
        }
        this.database.load(chosenKeys, loaded, this);
        return loaded;
    }

    protected void autoSave(CastelPlugin plugin) {
        long ticks = INTERVAL / 1000L * 20L;
        new BukkitRunnable(){
            public void run() {
                DataManager.this.saveAll(true);
            }
        }.runTaskTimerAsynchronously(plugin, ticks, ticks);
    }

    public void saveAll(boolean smart) {
        if (smart) {
            if (!this.savingState) {
                CLogger.info("Saving state was turned off for " + this.displayName + ", skipping saving data...");
                return;
            }
            ArrayList<T> datas = new ArrayList<>(this.cache.size());
            for (T data : this.cache.values()) {
                if (SMART && !data.shouldSave()) continue;
                datas.add(data);
            }
            this.database.save(datas);
        } else {
            this.database.save(this.cache.values());
        }
    }

    public void loadAndSave(K key, T data) {
        this.load(data);
        this.database.save(data);
    }

    public T getDataIfLoaded(K key) {
        return this.cache.getIfPresent(key);
    }

    public boolean isLoaded(K key) {
        return this.cache.containsKey(key);
    }

    protected void onLoad(T value) {}

    protected void unload(K key) {
        this.cache.remove(key);
    }

    public void load(T data) {
        K key = data.getDataKey();
        this.cache.put(key, data);
        this.onLoad(data);
        if (this.doesntExist != null) this.doesntExist.remove(key);
    }

    public T peek(K key) {
        CastelObject<Object> data = (CastelObject) this.cache.peek(key);
        if (data == null) data = (CastelObject) this.database.load(key);
        return (T) data;
    }

    public boolean exists(K key) {
        if (this.doesntExist != null && this.doesntExist.contains(key)) return false;
        if (this.cache.containsKey(key)) return true;
        return this.database.hasData(key);
    }

    public @Nullable T getData(K key) {
        return this.cache.get(key);
    }

    public int size() {
        return this.cache.size();
    }

    public Collection<T> getLoadedData() {
        return this.cache.values();
    }

    public Collection<T> peekAllData() {
        if (this.timedCache) return this.getLoadedData();
        Collection<K> keys = this.database.getDataKeys();
        if (keys == null) return new ArrayList<>();
        Collection<T> values = new ArrayList<>(keys.size());
        for (K key : keys) {
            if (this.cache.get(key) != null) continue;
            T data = this.database.load(key);
            if (data == null) throw new NullPointerException("Peeked data cannot be null with a valid key: " + key);
            values.add(data);
        }
        return values;
    }
}
