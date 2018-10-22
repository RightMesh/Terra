package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.utils.Logger;
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
        COMPONENT_ENABLE_CONNECTION_AGENT("component_enable_connection_agent"),
        ENABLE_AUTO_CONNECT_FOR_BUNDLE("dtn_enable_auto_connect_bundle"),
        ENABLE_COMPONENT_DETECT_PEER_ON_LAN("component_enable_detect_peer_lan"),
        ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT("dtn_enable_auto_connect_detect"),
        AUTO_CONNECT_USE_WHITELIST("dtn_auto_connect_use_whitelist"),
        AUTO_CONNECT_WHITELIST("dtn_auto_connect_whitelist"),
        COMPONENT_ENABLE_EVENT_PROCESSING("component_enable_event_processing"),
        COMPONENT_ENABLE_AA_REGISTRATION("component_enable_aa_registration"),
        COMPONENT_ENABLE_STATIC_API("component_enable_static_api"),
        COMPONENT_ENABLE_CBOR_DAEMON_API("component_enable_daemon_api"),
        API_CBOR_DAEMON_CHANNEL_PORT("api_daemon_channel_port"),
        COMPONENT_ENABLE_DAEMON_HTTP_API("component_enable_daemon_http_api"),
        API_DAEMON_HTTP_API_PORT("api_daemon_http_port"),
        EID_SINGLETON_ONLY("eid_singleton_only"),
        COMPONENT_ENABLE_CLA_LOAD_MODULES("component_enable_cla_load_modules"),
        MODULES_CLA_PATH("modules_cla_path"),
        COMPONENT_ENABLE_CLA_STCP("component_enable_cla_stcp"),
        CLA_STCP_LISTENING_PORT("cla_stcp_port"),
        COMPONENT_ENABLE_LINKLOCAL_ROUTING("component_enable_linklocal_routing"),
        COMPONENT_ENABLE_STATIC_ROUTING("component_enable_static_routing"),
        COMPONENT_ENABLE_SMART_ROUTING("component_enable_smart_routing"),
        STATIC_ROUTE_CONFIGURATION("static_routes_configuration"),
        COMPONENT_ENABLE_VOLATILE_STORAGE("component_enable_volatile_storage"),
        VOLATILE_BLOB_STORAGE_MAX_CAPACITY("volatile_blob_storage_max_capacity"),
        COMPONENT_ENABLE_SIMPLE_STORAGE("component_enable_simple_storage"),
        SIMPLE_STORAGE_PATH("simple_storage_paths"),
        LIMIT_BLOCKSIZE("limit_blocksize"),
        COMPONENT_ENABLE_LOGGING("component_enable_logging"),
        LOG_LEVEL("log_level"),
        ENABLE_LOG_FILE("enable_log_file"),
        LOG_FILE_PATH("log_file_path");

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

    public DTNConfiguration() {
        // default configuration
        this.createEntry(Entry.LOCAL_EID, EID.generate());
        this.<EID>createEntrySet(Entry.ALIASES);
        this.createEntry(Entry.MAX_LIFETIME, (long) 0);
        this.createEntry(Entry.MAX_TIMESTAMP_FUTURE, (long) 0);
        this.createEntry(Entry.ALLOW_RECEIVE_ANONYMOUS_BUNDLE, false);
        this.createEntry(Entry.ENABLE_STATUS_REPORTING, true);
        this.createEntry(Entry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN, true);
        this.createEntry(Entry.COMPONENT_ENABLE_CONNECTION_AGENT, true);
        this.createEntry(Entry.COMPONENT_ENABLE_EVENT_PROCESSING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_AA_REGISTRATION, true);
        this.createEntry(Entry.COMPONENT_ENABLE_STATIC_API, true);
        this.createEntry(Entry.COMPONENT_ENABLE_CBOR_DAEMON_API, false);
        this.createEntry(Entry.API_CBOR_DAEMON_CHANNEL_PORT, 4557);
        this.createEntry(Entry.COMPONENT_ENABLE_DAEMON_HTTP_API, true);
        this.createEntry(Entry.API_DAEMON_HTTP_API_PORT, 8080);
        this.createEntry(Entry.ENABLE_FORWARDING, true);
        this.createEntry(Entry.ENABLE_AUTO_CONNECT_FOR_BUNDLE, true);
        this.createEntry(Entry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT, true);
        this.createEntry(Entry.AUTO_CONNECT_USE_WHITELIST, true);
        this.<String>createEntrySet(Entry.AUTO_CONNECT_WHITELIST);
        this.createEntry(Entry.EID_SINGLETON_ONLY, false);
        this.createEntry(Entry.COMPONENT_ENABLE_CLA_LOAD_MODULES, false);
        this.createEntry(Entry.MODULES_CLA_PATH, "/etc/terra/modules/cla/");
        this.createEntry(Entry.COMPONENT_ENABLE_CLA_STCP, true);
        this.createEntry(Entry.CLA_STCP_LISTENING_PORT, 4556);
        this.createEntry(Entry.COMPONENT_ENABLE_LINKLOCAL_ROUTING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_STATIC_ROUTING, true);
        this.createEntry(Entry.COMPONENT_ENABLE_SMART_ROUTING, false);
        this.<EID, EID>createEntryMap(Entry.STATIC_ROUTE_CONFIGURATION);
        this.createEntry(Entry.COMPONENT_ENABLE_VOLATILE_STORAGE, true);
        this.createEntry(Entry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY, 10000000);
        this.createEntry(Entry.COMPONENT_ENABLE_SIMPLE_STORAGE, false);
        this.<String>createEntrySet(Entry.SIMPLE_STORAGE_PATH);
        this.createEntry(Entry.LIMIT_BLOCKSIZE, (long) 1000000000);
        this.createEntry(Entry.COMPONENT_ENABLE_LOGGING, true);
        this.<Logger.LOGLevel>createEntry(Entry.LOG_LEVEL, Logger.LOGLevel.VERBOSE);
        this.createEntry(Entry.ENABLE_LOG_FILE, false);
        this.createEntry(Entry.LOG_FILE_PATH, "");
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigurationEntry<T> get(Entry key) {
        return entries.get(key.toString());
    }
}
