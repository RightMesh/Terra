package io.left.rightmesh.libdtn.storage;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
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

    public static Single<Bundle> get(BundleID id) {
        return VolatileStorage.get(id)
                .onErrorResumeNext(SimpleStorage.get(id));
    }

    public static Completable remove(BundleID id) {
        return VolatileStorage.remove(id)
                .onErrorComplete(e -> true)
                .andThen(SimpleStorage.remove(id));
    }
}
