package io.left.rightmesh.libdtn.storage;

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

}
