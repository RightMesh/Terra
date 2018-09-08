package io.left.rightmesh.libdtn;

import io.left.rightmesh.libdtn.bundleV6.EID;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This node configuration.
 *
 * @author Lucien Loiseau on 28/08/18.
 */
public class DTNConfiguration {

    // ---- SINGLETON ----
    private static final Object lock = new Object();
    private static DTNConfiguration instance;

    private static DTNConfiguration getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new DTNConfiguration();
            }
            return instance;
        }
    }

    // ---- ALL OPTIONS FOR CONFIGURATION ----
    public enum Entry {
        LOCAL_EID("localEID"),
        ALIASES("aliases"),
        MAX_LIFETIME("max_lifetime"),
        MAX_TIMESTAMP_FUTURE("max_timestamp_future"),
        ENABLE_API("api"),
        ENABLE_FORWARD("forward"),
        EID_SINGLETON_ONLY("eid_singleton_only"),
        ENABLE_LINKLOCAL_ROUTING("linklocal_routing"),
        ENABLE_STATIC_ROUTING("static_routing"),
        ENABLE_SMART_ROUTING("smart_routing"),
        STATIC_ROUTE_CONFIGURATION("static_routes_configuration"),
        STORAGE_TYPE("storage"),
        LIMIT_BLOCKSIZE("limit_blocksize");

        private final String key;

        Entry(final String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }


    /**
     * An observable configuration entry.
     *
     * @param <T> type of entry
     */
    public static class ConfigurationEntry<T> {
        protected BehaviorSubject<T> entry = BehaviorSubject.create();

        /**
         * Create a new ConfigurationEntry of type T.
         *
         * @param key       the key for this entry
         * @param initValue default value
         * @param <T>       type of entry
         * @return a new ConfigurationEntry
         */
        public static <T> ConfigurationEntry create(Entry key, T initValue) {
            ConfigurationEntry entry = new ConfigurationEntry(initValue);
            getInstance().entries.put(key.toString(), entry);
            return entry;
        }

        /**
         * Create a new ConfigurationEntrySet of type T initialized with empty set.
         *
         * @param key the key for this entry
         * @param <T> Set of type T
         * @return a new ConfigurationEntrySet
         */
        public static <T> ConfigurationEntrySet createSet(Entry key) {
            ConfigurationEntrySet entry = new<T> ConfigurationEntrySet();
            getInstance().entries.put(key.toString(), entry);
            return entry;
        }

        /**
         * Create a new ConfigurationEntryMap of type T.
         *
         * @param <T> type of Map key
         * @param <U> type of Map value
         * @param key the key for this entry
         * @return a new ConfigurationEntryMap
         */
        public static <T, U> ConfigurationEntryMap createMap(Entry key) {
            ConfigurationEntryMap entry = new<T, U> ConfigurationEntryMap();
            getInstance().entries.put(key.toString(), entry);
            return entry;
        }

        protected ConfigurationEntry(T value) {
            entry.onNext(value);
        }

        /**
         * Return the last configured value for this entry.
         *
         * @return value
         */
        public T value() {
            return entry.getValue();
        }

        /**
         * Observe the change of value for this entry.
         *
         * @return Observable of this entry
         */
        public Observable<T> observe() {
            return entry;
        }

        /**
         * Update the value of this entry.
         *
         * @param value to update
         */
        public void update(T value) {
            entry.onNext(value);
        }
    }

    /**
     * A observable configuration entry holding a Set.
     *
     * @param <T> Type of Set
     */
    public static class ConfigurationEntrySet<T> extends ConfigurationEntry {

        private Set<T> set;

        protected ConfigurationEntrySet() {
            super(new HashSet<T>());
            set = new HashSet<T>();
        }

        /**
         * Add a new entry to this set.
         *
         * @param value to add
         */
        public void add(T value) {
            set.add(value);
            update(set);
        }

        /**
         * delete an entry from this set.
         *
         * @param value to delete
         */
        public void del(T value) {
            if (set.remove(value)) {
                update(set);
            }
        }
    }

    /**
     * An observable configuration entry holding a Map.
     *
     * @param <T> type of Map key0
     * @param <U> type of Map value
     */
    public static class ConfigurationEntryMap<T, U> extends ConfigurationEntry {

        private Map<T, U> map;

        protected ConfigurationEntryMap() {
            super(new HashMap<T, U>());
            map = new HashMap<T, U>();
        }

        /**
         * Add a new entry to this map.
         *
         * @param key   of the entry
         * @param value of the entry
         */
        public void add(T key, U value) {
            map.put(key, value);
            update(map);
        }

        /**
         * Delete an entry from this map.
         *
         * @param key of the entry to delete
         */
        public void del(T key) {
            if (map.remove(key) != null) {
                update(map);
            }
        }
    }

    // storage
    enum StorageType {
        VOLATILE,
        PERSISTENT,
        BOTH
    }

    private HashMap<String, ConfigurationEntry> entries = new HashMap<>();

    private DTNConfiguration() {
        // default configuration
        ConfigurationEntry.create(Entry.LOCAL_EID, EID.generate());
        ConfigurationEntry.<EID>createSet(Entry.ALIASES);
        ConfigurationEntry.create(Entry.MAX_LIFETIME, (long) 0);
        ConfigurationEntry.create(Entry.MAX_TIMESTAMP_FUTURE, (long) 0);
        ConfigurationEntry.create(Entry.ENABLE_API, true);
        ConfigurationEntry.create(Entry.ENABLE_FORWARD, true);
        ConfigurationEntry.create(Entry.EID_SINGLETON_ONLY, false);
        ConfigurationEntry.create(Entry.ENABLE_LINKLOCAL_ROUTING, true);
        ConfigurationEntry.create(Entry.ENABLE_STATIC_ROUTING, true);
        ConfigurationEntry.create(Entry.ENABLE_SMART_ROUTING, false);
        ConfigurationEntry.<String, String>createMap(Entry.STATIC_ROUTE_CONFIGURATION);
        ConfigurationEntry.create(Entry.STORAGE_TYPE, StorageType.BOTH);
        ConfigurationEntry.create(Entry.LIMIT_BLOCKSIZE, (long) 1000000000);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigurationEntry<T> get(Entry key) {
        return getInstance().entries.get(key);
    }
}
