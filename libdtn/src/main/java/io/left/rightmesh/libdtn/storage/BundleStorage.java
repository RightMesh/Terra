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
        StorageException(String msg) {
            super(msg);
        }
    }

    class StorageFailedException extends StorageException {
        StorageFailedException() {
            super("storage failed");
        }
    }

    class StorageCorruptedException extends StorageException {
        StorageCorruptedException() {
            super("storage corrupted");
        }
    }

    class StorageUnavailableException extends StorageException {
        StorageUnavailableException() {
            super("storage unavailable");
        }
    }

    class StorageFullException extends StorageException {
        StorageFullException() {
            super("storage full");
        }
    }

    class BundleAlreadyExistsException extends StorageException {
        BundleAlreadyExistsException() {
            super("bundle already exists");
        }
    }

    class BundleNotFoundException extends StorageException {
        BundleNotFoundException() {
            super("bundle not found");
        }
    }

}
