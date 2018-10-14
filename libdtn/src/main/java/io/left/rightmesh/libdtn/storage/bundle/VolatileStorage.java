package io.left.rightmesh.libdtn.storage.bundle;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.MetaBundle;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_VOLATILE_STORAGE;

/**
 * VolatileStorage holds all the Bundle in memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class VolatileStorage extends Component implements BundleStorage {

    private static final String TAG = "VolatileStorage";

    // ---- SINGLETON ----
    private static VolatileStorage instance = new VolatileStorage();
    public static VolatileStorage getInstance() { return instance; }
    public static void init() {
        instance.initComponent(COMPONENT_ENABLE_VOLATILE_STORAGE);
    }

    @Override
    protected String getComponentName() {
        return TAG;
    }

    private static class IndexEntry extends MetaBundle {
        Bundle bundle;
        IndexEntry(Bundle bundle) {
            super(bundle);
            this.bundle = bundle;
        }
    }

    private Map<BundleID, IndexEntry> bundles = new HashMap<>();

    /**
     * Clear the entire volatile storage.
     *
     * @return Completable that completes once it is done
     */
    public static Completable clear() {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            getInstance().bundles.clear();
            s.onComplete();
        });
    }

    /**
     * Returns the number of bundle indexed in volatile storage
     * @return number of bundle
     * @throws StorageUnavailableException if VolatileStorage is disabled
     */
    public static int count() throws StorageUnavailableException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        return getInstance().bundles.size();
    }

    /**
     * Stores a bundle into VolatileStorage
     *
     * @param bundle to store
     * @return Completable that completes once it is done
     */
    public static Single<Bundle> store(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.create(s -> {
            if (getInstance().bundles.containsKey(bundle.bid)) {
                s.onError(new BundleAlreadyExistsException());
            } else {
                IndexEntry meta = new IndexEntry(bundle);
                getInstance().bundles.put(bundle.bid, meta);
                bundle.tag("in_storage");
                s.onSuccess(bundle);
            }
        });
    }

    /**
     * Check wether or not this bundle ID is already indexed in VolatileStorage
     *
     * @param id of the bundle
     * @return true if bundle is already indexed, false otherwise
     * @throws StorageUnavailableException if the storage is disabled
     */
    public static boolean contains(BundleID id) throws StorageUnavailableException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        return getInstance().bundles.containsKey(id);
    }

    /**
     * Pull the bundle from storage
     *
     * @param id of the bundle
     * @return Single RxJava
     */
    public static Single<Bundle> get(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        if (getInstance().bundles.containsKey(id)) {
            Bundle bundle = getInstance().bundles.get(id).bundle;
            bundle.tag("in_storage");
            return Single.just(bundle);
        } else {
            return Single.error(BundleNotFoundException::new);
        }
    }

    /**
     * Remove the bundle whose id is given as a parameter from VolatileStorage.
     *
     * @param id of the bundle
     * @return Completable that completes once it is done
     */
    public static Completable remove(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        if (getInstance().bundles.containsKey(id)) {
            getInstance().bundles.remove(id);
        }
        return Completable.complete();
    }
}
