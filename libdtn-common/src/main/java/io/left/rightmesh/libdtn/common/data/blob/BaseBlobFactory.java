package io.left.rightmesh.libdtn.common.data.blob;

import static io.left.rightmesh.libdtn.common.utils.FileUtil.createNewFile;
import static io.left.rightmesh.libdtn.common.utils.FileUtil.spaceLeft;

import java.io.File;
import java.io.IOException;

/**
 * BaseBlobFactory is a Blob factory that can creates volatile or persistent {@link Blob}.
 *
 * @author Lucien Loiseau on 28/10/18.
 */
public class BaseBlobFactory implements BlobFactory {

    private static final String TAG = "BaseBlobFactory";

    private VolatileMemory memory;
    private boolean enableVolatileBlob = false;
    private boolean enableFileBlob = false;
    private String filePath = "./";

    /**
     * enable volatile Blob to be created.
     *
     * @param limit in terms of memory consumption all the volatile Blob can take together.
     * @return the current BaseBlobFactory.
     */
    public BaseBlobFactory enableVolatile(int limit) {
        this.memory = new VolatileMemory(limit);
        enableVolatileBlob = true;
        return this;
    }

    /**
     * Disable volatile Blob to be created.
     *
     * @return the current BaseBlobFactory.
     */
    public BaseBlobFactory disableVolatile() {
        enableVolatileBlob = false;
        return this;
    }

    /**
     * Enable persistent Blob to be created. A directory must be supplied that will hold
     * the persistent blob in independent files.
     *
     * @param path of the directory where persistent Blob will be created.
     * @return the current BaseBlobFactory.
     */
    public BaseBlobFactory enablePersistent(String path) {
        enableFileBlob = true;
        this.filePath = path;
        return this;
    }

    /**
     * Disable persistent Blob to be created.
     *
     * @return the current BaseBlobFactory.
     */
    public BaseBlobFactory disablePersistent() {
        enableFileBlob = false;
        return this;
    }

    /**
     * Check wether volatile blob is enabled.
     *
     * @return true if volatile blob are enabled, false otherwise.
     */
    public boolean isVolatileEnabled() {
        return enableVolatileBlob;
    }

    /**
     * Check wether persistent blob is enabled.
     *
     * @return true if persistent blob are enabled, false otherwise.
     */
    public boolean isPersistentEnabled() {
        return enableFileBlob;
    }

    // ----- definite size blob -------

    /**
     * Creates a volatile {@link Blob}.
     *
     * @param expectedSize of the {@link Blob}
     * @return a new {@link ByteBufferBlob}
     * @throws BlobFactoryException if volatile {@link Blob} are disabled or if there is no memory.
     */
    public Blob createVolatileBlob(int expectedSize) throws BlobFactoryException {
        // try in volatile memory
        if (isVolatileEnabled()) {
            try {
                return new ByteBufferBlob(memory, expectedSize);
            } catch (IOException io) {
                throw new BlobFactoryException();
            }
        }
        throw new BlobFactoryException();
    }

    /**
     * Creates a persistent {@link Blob}.
     *
     * @param expectedSize of the {@link Blob}
     * @return a new {@link FileBlob}
     * @throws BlobFactoryException if persistent {@link Blob} are disabled or if disk is full.
     */
    public Blob createFileBlob(int expectedSize) throws BlobFactoryException {
        // try in persistent memory
        if (isPersistentEnabled()) {
            if (spaceLeft(filePath) > expectedSize) {
                try {
                    File fblob = createNewFile("blob-", ".blob", filePath);
                    return new FileBlob(fblob);
                } catch (IOException io) {
                    throw new BlobFactoryException();
                }
            }
        }
        throw new BlobFactoryException();
    }

    @Override
    public Blob createBlob(int expectedSize) throws BlobFactoryException {
        if (expectedSize < 0) {
            // indefinite size Blob
            return createGrowingBlob();
        }

        try {
            return createVolatileBlob(expectedSize);
        } catch (BlobFactoryException e) {
            /* ignore */
        }

        try {
            return createFileBlob(expectedSize);
        } catch (BlobFactoryException e) {
            /* ignore */
        }

        return new NullBlob();
    }

    // ----- indefinite size blob -------

    private Blob createGrowingBlob() throws BlobFactoryException {
        return new VersatileGrowingBuffer(this);
    }

}
