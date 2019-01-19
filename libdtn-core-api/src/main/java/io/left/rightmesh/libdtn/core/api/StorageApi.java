package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * API for storage component.
 *
 * @author Lucien Loiseau on 24/10/18.
 */
public interface StorageApi extends CoreComponentApi {

    class StorageException extends Exception {
        public StorageException(String msg) {
            super(msg);
        }
    }

    class StorageFailedException extends StorageException {
        public StorageFailedException() {
            super("storage failed");
        }

        public StorageFailedException(String msg) {
            super(msg);
        }
    }

    class StorageCorruptedException extends StorageException {
        public StorageCorruptedException() {
            super("storage corrupted");
        }
    }

    class StorageUnavailableException extends StorageException {
        public StorageUnavailableException() {
            super("storage unavailable");
        }
    }

    class StorageFullException extends StorageException {
        public StorageFullException() {
            super("storage full");
        }
    }

    class BundleAlreadyExistsException extends StorageException {
        public BundleAlreadyExistsException() {
            super("bundle already exists");
        }

        public BundleAlreadyExistsException(BundleId bid) {
            super("bundle already exists: " + bid);
        }
    }

    class BundleNotFoundException extends StorageException {
        public BundleNotFoundException() {
            super("bundle not found");
        }

        public BundleNotFoundException(BundleId bid) {
            super("bundle not found: " + bid);
        }
    }

    /**
     * Return a Blob factory.
     *
     * @return BlobFactory
     */
    BlobFactory getBlobFactory();

    /**
     * count the total number of bundle indexed, whether in persistant or volatile storage.
     *
     * @return total number of bundle indexed
     */
    int count();

    /**
     * check if a Bundle is in storage.
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in volatile storage, false otherwise
     */
    boolean contains(BundleId bid);

    /**
     * Try to store in volatile storage first and then copy in persistent storage whatever happens
     * If Volatile Storage is enabled, it will return the whole Bundle, otherwise it returns
     * a MetaBundle.
     *
     * <p>Whenever the Single completes, the caller can expect that no further operations is needed
     * in background to store the bundle.
     *
     * @param bundle to store
     * @return Completable that complete whenever the bundle is stored, error otherwise
     */
    Single<Bundle> store(Bundle bundle);

    /**
     * Pull a MetaBundle from Storage. If the bundle is available in VolatileStorage it pulls
     * the real Bundle from the index, otherwise it returns the MetaBundle.
     *
     * @param id of the bundle to pull from storage
     * @return a Bundle, either a real one or a MetaBundle
     */
    Single<Bundle> getMeta(BundleId id);

    /**
     * Pull a Bundle from StorageApi. It will try to pull it from Volatile if it exists, or from
     * SimpleStorage otherwise.
     *
     * @param id of the bundle to pull from storage
     * @return a Single that completes if the Bundle was successfully pulled, onError otherwise
     */
    Single<Bundle> get(BundleId id);

    /**
     * Delete a Bundle from all storage and removes all event registration to it.
     *
     * @param id of the bundle to delete
     * @return Completable
     */
    Completable remove(BundleId id);

    /**
     * Clear all bundles.
     *
     * @return Completable
     */
    Completable clear();

    // todo remove this
    String print();

}
