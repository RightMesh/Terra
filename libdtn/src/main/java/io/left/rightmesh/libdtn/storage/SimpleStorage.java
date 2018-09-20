package io.left.rightmesh.libdtn.storage;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class SimpleStorage implements BundleStorage {

    /**
     * Create a new {@see FileBLOB}.
     *
     * @return a new VolatileBLOB with capacity of expectedSize
     * @throws StorageFullException if there isn't enough space in Volatile Memory
     */
    public static FileBLOB createBLOB() throws StorageFullException {
        return null;
    }

    @Override
    public Single<Integer> count() {
        return null;
    }

    @Override
    public Completable store(Bundle bundle) {
        return null;
    }

    @Override
    public Single<Boolean> contains(BundleID id) {
        return null;
    }

    @Override
    public Single<Bundle> get(BundleID id) {
        return null;
    }

    @Override
    public Completable remove(BundleID id) {
        return null;
    }

    @Override
    public Completable clear() {
        return null;
    }
}
