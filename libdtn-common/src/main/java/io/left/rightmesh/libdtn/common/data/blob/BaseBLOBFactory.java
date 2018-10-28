package io.left.rightmesh.libdtn.common.data.blob;

import java.io.File;
import java.io.IOException;

import static io.left.rightmesh.libdtn.common.utils.FileUtil.createNewFile;
import static io.left.rightmesh.libdtn.common.utils.FileUtil.spaceLeft;

/**
 * @author Lucien Loiseau on 28/10/18.
 */
public class BaseBLOBFactory implements BLOBFactory {

    private static final String TAG = "BaseBLOBFactory";

    private boolean enableVolatileBLOB = true;
    private int BLOBMemoryMaxUsage = 10000000;
    private int CurrentBLOBMemoryUsage = 0;

    private boolean enableFileBLOB = false;
    private String filePath = "./";

    public BaseBLOBFactory enableVolatile(int limit) {
        enableVolatileBLOB = true;
        BLOBMemoryMaxUsage = limit;
        return this;
    }

    public BaseBLOBFactory disableVolatile() {
        enableVolatileBLOB = false;
        return this;
    }

    public BaseBLOBFactory enablePersistent(String path) {
        enableFileBLOB = true;
        this.filePath = path;
        return this;
    }

    public BaseBLOBFactory disablePersistent() {
        enableFileBLOB = false;
        return this;
    }

    public boolean isVolatileEnabled() {
        return enableVolatileBLOB;
    }

    public boolean isPersistentEnabled() {
        return enableFileBLOB;
    }

    public BLOB createVolatileBLOB(int expectedSize) throws BLOBFactoryException {
        // try in volatile memory
        if (isVolatileEnabled() && expectedSize <= (BLOBMemoryMaxUsage - CurrentBLOBMemoryUsage)) {
            CurrentBLOBMemoryUsage += expectedSize;
            return new ByteBufferBLOB(expectedSize);
        }
        throw new BLOBFactoryException();
    }

    public BLOB createFileBLOB(int expectedSize) throws BLOBFactoryException {
        // try in persistent memory
        if(isPersistentEnabled()) {
            if (spaceLeft(filePath) > expectedSize) {
                try {
                    File fblob = createNewFile("blob-", ".blob", filePath);
                    if (fblob != null) {
                        return new FileBLOB(fblob);
                    }
                } catch (IOException io) {
                    /* ignore exception will be thrown after */
                }
            }
        }
        throw new BLOBFactoryException();
    }

    @Override
    public BLOB createBLOB(int expectedSize) throws BLOBFactoryException {
        try {
            return createVolatileBLOB(expectedSize);
        } catch(BLOBFactoryException e) {
            /* ignore */
        }

        try {
            return createFileBLOB(expectedSize);
        } catch(BLOBFactoryException e) {
            /* ignore */
        }

        return new NullBLOB();
    }


}
