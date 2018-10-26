package io.left.rightmesh.libdtn.core.storage.blob;

import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.storage.bundle.SimpleStorage;
import io.left.rightmesh.libdtn.core.api.StorageAPI;

/**
 * The Binary Large Object (CoreBLOBFactory) handles large amounts of data in a common way,
 * no matter if the data is stored on a persistent or volatile memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class CoreBLOBFactory implements BLOBFactory {

    private static final String TAG = "CoreBLOBFactory";

    private SimpleStorage simpleStorage;

    public CoreBLOBFactory(ConfigurationAPI conf, SimpleStorage simpleStorage) {
        this.simpleStorage = simpleStorage;
        VolatileBLOB.setLimitConf(conf);
    }

    @Override
    public BLOB createBLOB(int expectedSize) throws BLOBFactoryException{
        try {
            return VolatileBLOB.createBLOB(expectedSize);
        } catch(CoreBLOBFactory.BLOBFactoryException e) {
            // ignore, try simple storage
        }

        try {
            return simpleStorage.createBLOB(expectedSize);
        } catch(StorageAPI.StorageException se) {
            throw new BLOBFactoryException();
        }
    }

}
