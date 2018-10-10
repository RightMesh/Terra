package io.left.rightmesh.libdtn.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.events.BundlePulled;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.events.DTNEvent;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 29/09/18.
 */
public class Storage {

    // ---- SINGLETON ----
    private static Storage instance = new Storage();
    public static Storage getInstance() {
        return instance;
    }
    public static void init() {
        VolatileStorage.init();
        SimpleStorage.init();
    }

    private Storage() {
        RxBus.register(this);
    }

    /**
     * Try to store in persistant storage first and then in volatile storage.
     *
     * @param bundle to store
     * @return Completable that complete whenever the bundle is stored, error otherwise
     */
    public static Single<Bundle> store(Bundle bundle) {
        return SimpleStorage.store(bundle)
                .onErrorResumeNext(e -> {
                    if(e instanceof BundleStorage.BundleAlreadyExistsException) {
                        return Single.error(e);
                    }
                    return VolatileStorage.store(bundle);
                });
    }

    /**
     * Pull a MetaBundle from Storage. If the bundle is available in VolatileStorage it pulls
     * the real Bundle from the index, otherwise it returns the MetaBundle from SimpleStorage index.
     *
     * @param id of the bundle to pull from storage
     * @return a Bundle, either a real one or a MetaBundle
     */
    public static Single<Bundle> getMeta(BundleID id) {
        return VolatileStorage.get(id)
                .onErrorResumeNext(SimpleStorage.getMetaBundle(id));
    }

    /**
     * Pull a Bundle from Storage. It will try to pull it from Volatile if it exists, or from
     * SimpleStorage otherwise.
     *
     * @param id of the bundle to pull from storage
     * @return a Single that completes if the Bundle was successfully pulled, onError otherwise
     */
    public static Single<Bundle> get(BundleID id) {
        return VolatileStorage.get(id)
                .onErrorResumeNext(SimpleStorage.get(id));
    }

    /**
     * Delete a Bundle from all storage and removes all event registration to it.
     *
     * @param id of the bundle to delete
     * @return Completable
     */
    public static Completable remove(BundleID id) {
        /* remove bundle from all storage */
        return VolatileStorage.remove(id)
                .onErrorComplete(e -> true)
                .andThen(SimpleStorage.remove(id));

    }
}
