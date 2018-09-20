package io.left.rightmesh.libdtn.storage;

import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * The Binary Large Object (BLOB) handles large amounts of data in a common way,
 * no matter if the data is stored on a persistent or volatile memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public abstract class BLOB {

    /**
     * Creates a new BLOB.
     *
     * @param expectedSize of the BLOB
     * @return a VolatileBLOB if expectedSize fits in Volatile memory, a FileBLOB otherwise
     * @throws BundleStorage.StorageFullException if BLOB cannot be created
     */
    public static BLOB createBLOB(int expectedSize) throws BundleStorage.StorageFullException {
        try {
            return VolatileStorage.createBLOB(expectedSize);
        } catch(BundleStorage.StorageFullException sfe) {
            // ignore
        }
        return SimpleStorage.createBLOB();
    }

    /**
     * Creates a new BLOB of unknown size.
     *
     * @return a FileBLOB
     * @throws BundleStorage.StorageFullException if BLOB cannot be created
     */
    public static BLOB createBLOB() throws BundleStorage.StorageFullException {
        return SimpleStorage.createBLOB();
    }

    /**
     * Size of the current blob object.
     *
     * @return size
     */
    public abstract long size();

    /**
     * Return a cold Flowable, BackPressure-enabled, for the entire BLOB. On subscription, it
     * opens a ReadableBLOB and read it entirely.
     *
     * @return Flowable of ByteBuffer
     */
    public abstract Flowable<ByteBuffer> observe();


    /**
     * new {@see ReadableBLOB} from this BLOB. The ReadableBLOB will lock the BLOB for read-only
     * operations. calling close() on the ReadableBLOB will unlock the BLOB. Multiple concurrent
     * ReadableBLOB can be acquired from one single BLOB.
     *
     * @return ReadableBLOB
     */
    public abstract ReadableBLOB getReadableBLOB();

    /**
     * new {@see WritableBLOB} from this BLOB. The WritableBLOB will lock the BLOB for write-only
     * operations. calling close() on the WritableBLOB will unlock the BLOB. Only one WritableBLOB
     * can be acquired from this single BLOB at any given time.
     *
     * @return WritableBLOB
     */
    public abstract WritableBLOB getWritableBLOB();

}
