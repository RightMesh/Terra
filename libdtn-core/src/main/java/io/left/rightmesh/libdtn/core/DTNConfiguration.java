package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.spi.ModuleSPI;
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
public class DTNConfiguration implements ConfigurationAPI {

    private HashMap<String, ConfigurationEntry> entries = new HashMap<>();

    public static class ConfigurationEntry<T> implements EntryInterface<T> {
        BehaviorSubject<T> entry = BehaviorSubject.create();

        ConfigurationEntry(T value) {
            entry.onNext(value);
        }

        public T value() {
            return entry.getValue();
        }

        public Observable<T> observe() {
            return entry;
        }

        public void update(T value) {
            entry.onNext(value);
        }
    }

    public static class ConfigurationEntrySet<T> extends ConfigurationEntry implements EntrySetInterface<T> {

        private Set<T> set;

        ConfigurationEntrySet() {
            super(new HashSet<T>());
            set = new HashSet<T>();
        }

        public void add(T value) {
            set.add(value);
            update(set);
        }

        public void del(T value) {
            if (set.remove(value)) {
                update(set);
            }
        }
    }

    public static class ConfigurationEntryMap<T, U> extends ConfigurationEntry implements EntryMapInterface<T,U> {

        private Map<T, U> map;

        ConfigurationEntryMap() {
            super(new HashMap<T, U>());
            map = new HashMap<T, U>();
        }

        public void add(T key, U value) {
            map.put(key, value);
            update(map);
        }

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
    <T> ConfigurationEntry createCoreEntry(CoreEntry key, T initValue) {
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
    <T> ConfigurationEntrySet createCoreEntrySet(CoreEntry key) {
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
    <T, U> ConfigurationEntryMap createCoreEntryMap(CoreEntry key) {
        ConfigurationEntryMap entry = new<T, U> ConfigurationEntryMap();
        entries.put(key.toString(), entry);
        return entry;
    }

    /**
     * Create a new ConfigurationEntry of type T.
     *
     * @param key       the key for this entry
     * @param initValue default value
     * @param <T>       type of entry
     * @return a new ConfigurationEntry
     */
    <T> ConfigurationEntry createEntryCustom(String key, T initValue) {
        ConfigurationEntry entry = new ConfigurationEntry(initValue);
        entries.put(key, entry);
        return entry;
    }

    public DTNConfiguration() {
        // default configuration
        this.createCoreEntry(CoreEntry.LOCAL_EID, DTN.generate());
        this.<EID>createCoreEntrySet(CoreEntry.ALIASES);
        this.createCoreEntry(CoreEntry.MAX_LIFETIME, (long) 0);
        this.createCoreEntry(CoreEntry.MAX_TIMESTAMP_FUTURE, (long) 0);
        this.createCoreEntry(CoreEntry.ALLOW_RECEIVE_ANONYMOUS_BUNDLE, false);
        this.createCoreEntry(CoreEntry.ENABLE_STATUS_REPORTING, true);
        this.createCoreEntry(CoreEntry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN, true);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_CONNECTION_AGENT, true);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_EVENT_PROCESSING, true);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_AA_REGISTRATION, true);
        this.createCoreEntry(CoreEntry.ENABLE_FORWARDING, true);
        this.createCoreEntry(CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE, true);
        this.createCoreEntry(CoreEntry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT, true);
        this.createCoreEntry(CoreEntry.AUTO_CONNECT_USE_WHITELIST, true);
        this.<String>createCoreEntrySet(CoreEntry.AUTO_CONNECT_WHITELIST);
        this.createCoreEntry(CoreEntry.EID_SINGLETON_ONLY, false);
        this.createCoreEntry(CoreEntry.ENABLE_CLA_MODULES, false);
        this.createCoreEntry(CoreEntry.MODULES_CLA_PATH, "/etc/terra/modules/cla/");
        this.createCoreEntry(CoreEntry.ENABLE_AA_MODULES, false);
        this.createCoreEntry(CoreEntry.MODULES_AA_PATH, "/etc/terra/modules/aa/");
        this.createCoreEntry(CoreEntry.ENABLE_CORE_MODULES, false);
        this.createCoreEntry(CoreEntry.MODULES_CORE_PATH, "/etc/terra/modules/core/");
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_LINKLOCAL_ROUTING, true);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_STATIC_ROUTING, true);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_SMART_ROUTING, false);
        this.<EID, EID>createCoreEntryMap(CoreEntry.STATIC_ROUTE_CONFIGURATION);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE, true);
        this.createCoreEntry(CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY, 10000000);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE, false);
        this.<String>createCoreEntrySet(CoreEntry.SIMPLE_STORAGE_PATH);
        this.createCoreEntry(CoreEntry.LIMIT_BLOCKSIZE, (long) 1000000000);
        this.createCoreEntry(CoreEntry.COMPONENT_ENABLE_LOGGING, true);
        this.createCoreEntry(CoreEntry.LOG_LEVEL, Log.LOGLevel.VERBOSE);
        this.createCoreEntry(CoreEntry.ENABLE_LOG_FILE, false);
        this.createCoreEntry(CoreEntry.LOG_FILE_PATH, "");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ConfigurationEntry<T> get(CoreEntry key) {
        return entries.get(key.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntryInterface<Boolean> getModuleEnabled(String name, boolean default_value) {
        EntryInterface ret = entries.get("module:"+name+":enable");
        if(ret == null) {
            ret = this.createEntryCustom("module:"+name+":enable", default_value);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> EntryInterface<T> getModuleConf(ModuleSPI module, ModuleEntry entry, T default_value) {
        EntryInterface ret = entries.get(module.getModuleName()+":conf:"+entry.toString());
        if(ret == null) {
            ret = this.createEntryCustom(module.getModuleName()+":conf:"+entry.toString(), default_value);
        }
        return ret;
    }
}
