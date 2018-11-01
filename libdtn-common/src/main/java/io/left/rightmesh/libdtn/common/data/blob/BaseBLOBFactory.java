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

    private VolatileMemory memory;
    private boolean enableVolatileBLOB = false;
    private boolean enableFileBLOB = false;
    private String filePath = "./";

    public BaseBLOBFactory enableVolatile(int limit) {
        this.memory = new VolatileMemory(limit);
        enableVolatileBLOB = true;
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

    // ----- definite size blob -------

    public BLOB createVolatileBLOB(int expectedSize) throws BLOBFactoryException {
        // try in volatile memory
        if (isVolatileEnabled()) {
            try {
                return new ByteBufferBLOB(memory, expectedSize);
            } catch (IOException io) {
                throw new BLOBFactoryException();
            }
        }
        throw new BLOBFactoryException();
    }

    public BLOB createFileBLOB(int expectedSize) throws BLOBFactoryException {
        // try in persistent memory
        if (isPersistentEnabled()) {
            if (spaceLeft(filePath) > expectedSize) {
                try {
                    File fblob = createNewFile("blob-", ".blob", filePath);
                    return new FileBLOB(fblob);
                } catch (IOException io) {
                    throw new BLOBFactoryException();
                }
            }
        }
        throw new BLOBFactoryException();
    }

    @Override
    public BLOB createBLOB(int expectedSize) throws BLOBFactoryException {
        if(expectedSize < 0) {
            // indefinite size BLOB
            return createGrowingBLOB();
        }

        try {
            return createVolatileBLOB(expectedSize);
        } catch (BLOBFactoryException e) {
            /* ignore */
        }

        try {
            return createFileBLOB(expectedSize);
        } catch (BLOBFactoryException e) {
            /* ignore */
        }

        return new NullBLOB();
    }

    // ----- indefinite size blob -------

    private BLOB createGrowingBLOB() throws BLOBFactoryException {
        return new VersatileGrowingBuffer(this);
    }

}
