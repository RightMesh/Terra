package io.left.rightmesh.libdtn.storage;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * The BundleStorage takes care of storing individual bundle and exposes a high level API to
 * store and  retrieve  Bundle based on certain  parameter such as the Bundle IDs, Source ID,
 * destination ID, Geotag and some others.
 *
 * <p>The  BundleStorage also sends events whenever a new Bundle is inserted, updated, or whenever
 * a Bundle was destroyed.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public interface BundleStorage {

    class StorageException extends Exception {
    }

    class StorageFailedException extends StorageException {
    }

    class StorageCorruptedException extends StorageException {
    }

    class StorageUnavailableException extends StorageException {
    }

    class StorageFullException extends StorageException {
    }

    class BundleAlreadyExistsException extends StorageException {
    }

    class BundleNotFoundException extends StorageException {
    }

    /**
     * Store a new {@see Bundle} in this storage. Returns a Completable that will either return
     * complete, or onError with an exception.
     *
     * @param bundle to store
     * @return onComplete after the bundle has been stored
     * @throws BundleAlreadyExistsException if the bundle is already present in the database.
     * @throws StorageFullException if the storage capacity is full
     * @throws StorageUnavailableException if the storage is not available for writing
     * @throws StorageFailedException if an error occured while storing the bundle
     */
    Completable store(Bundle bundle);

    /**
     * Count the number of {@see Bundle} in this storage.
     *
     * @return the number of bundles stored
     * @throws StorageUnavailableException if the storage is not available for reading
     * @throws StorageFailedException if an error occured while counting the bundle
     */
    Single<Integer> count();

    /**
     * Returns true if the storage contains the {@see Bundle} whose ID is bundleID.
     *
     * @param id of the bundle
     * @return true if the storage contains the bundle, false otherwise
     * @throws StorageFailedException if an error occured while counting the bundle
     */
    Single<Boolean> contains(BundleID id);

    /**
     * Retrieve a specific {@see Bundle} using its bundleID.
     *
     * @param id of the bundle
     * @return the bundle
     * @throws BundleNotFoundException if the bundle wasn't found
     * @throws StorageUnavailableException if the storage is not available for reading
     * @throws StorageFailedException if an error occured while retrieving the bundle
     * @throws StorageCorruptedException if a bundle is missing a block or a blob
     */
    Single<Bundle> get(BundleID id);

    /**
     * Remove a specific {@see Bundle} using its bundleID.
     *
     * @param id of the bundle
     * @return onComplete if bundle is entirely deleted, onError otherwise
     * @throws BundleNotFoundException if the bundle wasn't found
     */
    Completable remove(BundleID id);
    
    /**
     * Clear the entire storage.
     *
     * @return onComplete if successful, onError otherwise
     * @throws StorageUnavailableException if the storage is not available for writing
     */
    Completable clear();

}
