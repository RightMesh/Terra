package io.left.rightmesh.libdtn;

import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.utils.Log;
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

    private HashMap<String, ConfigurationEntry> entries = new HashMap<>();

    // ---- CONFIGURATION ENTRIES ----
    public enum Entry {
        LOCAL_EID("localEID"),
        ALIASES("aliases"),
        MAX_LIFETIME("max_lifetime"),
        MAX_TIMESTAMP_FUTURE("max_timestamp_future"),
        ALLOW_RECEIVE_ANONYMOUS_BUNDLE("allow_receive_anonymous_bundle"),
        ENABLE_STATUS_REPORTING("enable_status_reporting"),
        ENABLE_FORWARDING("dtn_enable_forwarding"),
        COMPONENT_ENABLE_REGISTRATION("component_enable_registration"),
        COMPONENT_ENABLE_STATIC_API("component_enable_static_api"),
        COMPONENT_ENABLE_DAEMON_API("component_enable_daemon_api"),
        EID_SINGLETON_ONLY("eid_singleton_only"),
        COMPONENT_ENABLE_CLA_STCP("component_enable_cla_stcp"),
        CLA_STCP_LISTENING_PORT("cla_tcp_port"),
        COMPONENT_ENABLE_LINKLOCAL_ROUTING("component_enable_linklocal_routing"),
        COMPONENT_ENABLE_STATIC_ROUTING("component_enable_static_routing"),
        COMPONENT_ENABLE_SMART_ROUTING("component_enable_smart_routing"),
        STATIC_ROUTE_CONFIGURATION("static_routes_configuration"),
        COMPONENT_ENABLE_VOLATILE_STORAGE("component_enable_volatile_storage"),
        COMPONENT_ENABLE_SIMPLE_STORAGE("component_enable_simple_storage"),
        SIMPLE_STORAGE_PATH("simple_storage_paths"),
        LIMIT_BLOCKSIZE("limit_blocksize"),
        COMPONENT_ENABLE_LOGGING("component_enable_logging"),
        LOG_LEVEL("log_level");

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
        BehaviorSubject<T> entry = BehaviorSubject.create();


        ConfigurationEntry(T value) {
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

        ConfigurationEntrySet() {
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

        ConfigurationEntryMap() {
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

    /**
     * Create a new ConfigurationEntry of type T.
     *
     * @param key       the key for this entry
     * @param initValue default value
     * @param <T>       type of entry
     * @return a new ConfigurationEntry
     */
    <T> ConfigurationEntry createEntry(Entry key, T initValue) {
        ConfigurationEntry entry = new ConfigurationEntry(initValue);
        entries.put(key.toString(), entry);
        return entry;
    }

    /**
     * Create a new ConfigurationEntrySet of type T initialized with empty set.
     *
     * @param key the key for this entry
     * @param <T> Set of type T
     * @return a new ConfigurationEntrySet
     */
    <T> ConfigurationEntrySet createEntrySet(Entry key) {
        ConfigurationEntrySet entry = new<T> ConfigurationEntrySet();
        entries.put(key.toString(), entry);
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
    <T, U> ConfigurationEntryMap createEntryMap(Entry key) {
        ConfigurationEntryMap entry = new<T, U> ConfigurationEntryMap();
        entries.put(key.toString(), entry);
        return entry;
    }

    private DTNConfiguration() {
        // default configuration
        this.createEntry(Entry.LOCAL_EID, EID.generate());
        this.<EID>createEntrySet(Entry.ALIASES);
        this.createEntry(Entry.MAX_LIFETIME, (long) 0);
        this.createEntry(Entry.MAX_TIMESTAMP_FUTURE, (long) 0);
        this.createEntry(Entry.ALLOW_RECEIVE_ANONYMOUS_BUNDLE, false);
        this.createEntry(Entry.ENABLE_STATUS_REPORTING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_REGISTRATION, true);
        this.createEntry(Entry.COMPONENT_ENABLE_STATIC_API, true);
        this.createEntry(Entry.COMPONENT_ENABLE_DAEMON_API, false);
        this.createEntry(Entry.ENABLE_FORWARDING, true);
        this.createEntry(Entry.EID_SINGLETON_ONLY, false);
        this.createEntry(Entry.COMPONENT_ENABLE_CLA_STCP, true);
        this.createEntry(Entry.CLA_STCP_LISTENING_PORT, 4556);
        this.createEntry(Entry.COMPONENT_ENABLE_LINKLOCAL_ROUTING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_STATIC_ROUTING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_SMART_ROUTING, false);
        this.<EID, EID>createEntryMap(Entry.STATIC_ROUTE_CONFIGURATION);
        this.createEntry(Entry.COMPONENT_ENABLE_VOLATILE_STORAGE, true);
        this.createEntry(Entry.COMPONENT_ENABLE_SIMPLE_STORAGE, false);
        this.<String>createEntrySet(Entry.SIMPLE_STORAGE_PATH);
        this.createEntry(Entry.LIMIT_BLOCKSIZE, (long) 1000000000);
        this.createEntry(Entry.COMPONENT_ENABLE_LOGGING, false);
        this.<Log.LOGLevel>createEntry(Entry.LOG_LEVEL, Log.LOGLevel.ERROR);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigurationEntry<T> get(Entry key) {
        return getInstance().entries.get(key.toString());
    }
}
