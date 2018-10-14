package io.left.rightmesh.libdtn.storage.bundle;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.MetaBundle;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
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

    @Override
    protected void componentDown() {
        super.componentDown();
        removeVolatileBundle();
    }

    /**
     * Count the number of VolatileBundle in Storage. This method iterates over the entire index.
     *
     * @return number of volatile bundle in storage
     */
    public static int count() {
        return (int)Storage.index.values().stream().filter(e -> e.isVolatile).count();
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

        if (Storage.containsVolatile(bundle.bid)) {
            return Single.error(new BundleAlreadyExistsException());
        } else {
            Storage.IndexEntry entry = Storage.addEntry(bundle.bid, bundle);
            entry.isVolatile = true;
            return Single.just(bundle);
        }
    }

    /**
     * Remove all volatile bundle. If the bundle has a persistent copy, replace the bundle with
     * the MetaBundle, otherwise delete from index.
     */
    public static Completable removeVolatileBundle() {
        return Completable.create(s -> {
            Storage.index.forEach((bid, entry) -> {
                if(!entry.isPersistent) {
                    Storage.index.remove(bid, entry);
                } else {
                    entry.bundle = new MetaBundle(entry.bundle);
                }
            });
            s.onComplete();
        });
    }
}
