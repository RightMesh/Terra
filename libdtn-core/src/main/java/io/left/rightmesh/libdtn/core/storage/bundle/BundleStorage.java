package io.left.rightmesh.libdtn.core.storage.bundle;

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
        public StorageException(String msg) {
            super(msg);
        }
    }

    class StorageFailedException extends StorageException {
        public StorageFailedException() {
            super("storage failed");
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
    }

    class BundleNotFoundException extends StorageException {
        public BundleNotFoundException() {
            super("bundle not found");
        }
    }

}
