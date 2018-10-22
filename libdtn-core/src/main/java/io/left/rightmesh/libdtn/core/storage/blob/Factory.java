package io.left.rightmesh.libdtn.core.storage.blob;

import io.left.rightmesh.libdtn.core.storage.bundle.BundleStorage;
import io.left.rightmesh.libdtn.core.storage.bundle.SimpleStorage;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;

/**
 * The Binary Large Object (Factory) handles large amounts of data in a common way,
 * no matter if the data is stored on a persistent or volatile memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class Factory implements BLOBFactory {

    private static final String TAG = "DiscoveryAgent";

    // ---- SINGLETON ----
    private static Factory instance = new Factory();
    public static Factory getInstance() {
        return instance;
    }
    private Factory() {
    }

    @Override
    public BLOB createBLOB(int expectedSize) throws BLOBFactoryException{
        try {
            return VolatileBLOB.createBLOB(expectedSize);
        } catch(Factory.BLOBFactoryException e) {
            // ignore, try simple storage
        }

        try {
            return SimpleStorage.createBLOB(expectedSize);
        } catch(BundleStorage.StorageException se) {
            throw new BLOBFactoryException();
        }
    }

}
