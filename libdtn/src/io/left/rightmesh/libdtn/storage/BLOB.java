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

    protected final Object lock = new Object();

    public class BLOBException extends Exception {
        BLOBException(String msg) {
            super(msg);
        }
    }

    public class BLOBStateException extends BLOBException {
        BLOBStateException(String msg) {
            super(msg);
        }
    }

    protected enum BLOBMode {
        CLOSED,
        LOCKED_READ,
        LOCKED_WRITE
    }

    protected BLOBMode state = BLOBMode.CLOSED;
    protected int lockCounter = 0;

    protected void switchBLOBMode(BLOBMode mode) throws BLOBStateException {
        // close -> close : nothing to do
        // close -> read  : lock for reading, increase read lock
        // close -> write : lock for writing
        // write -> close : close
        // write -> read  : FORBIDDEN !
        // write -> write : FORBIDDEN !
        // read  -> close : decrease read lock, close only if readlock = 0
        // read  -> read  : increase read lock
        // read  -> write : FORBIDDEN !

        synchronized (lock) {
            if (this.state.equals(BLOBMode.CLOSED) && mode.equals(BLOBMode.CLOSED)) {
                return;
            }
            if (this.state.equals(BLOBMode.CLOSED) && mode.equals(BLOBMode.LOCKED_READ)) {
                state = BLOBMode.LOCKED_READ;
                lockCounter = 1;
                return;
            }
            if (this.state.equals(BLOBMode.CLOSED) && mode.equals(BLOBMode.LOCKED_WRITE)) {
                state = BLOBMode.LOCKED_WRITE;
                lockCounter = 1;
                return;
            }
            if (this.state.equals(BLOBMode.LOCKED_WRITE) && mode.equals(BLOBMode.CLOSED)) {
                state = BLOBMode.CLOSED;
                lockCounter = 0;
                return;
            }
            if (this.state.equals(BLOBMode.LOCKED_WRITE) && mode.equals(BLOBMode.LOCKED_READ)) {
                throw new BLOBStateException("BLOB is locked for writing");
            }
            if (this.state.equals(BLOBMode.LOCKED_WRITE) && mode.equals(BLOBMode.LOCKED_WRITE)) {
                throw new BLOBStateException("BLOB is already locked for writing");
            }
            if (this.state.equals(BLOBMode.LOCKED_READ) && mode.equals(BLOBMode.CLOSED)) {
                lockCounter--;
                if (lockCounter == 0) {
                    state = BLOBMode.CLOSED;
                }
                return;
            }
            if (this.state.equals(BLOBMode.LOCKED_READ) && mode.equals(BLOBMode.LOCKED_READ)) {
                lockCounter++;
                return;
            }
            if (this.state.equals(BLOBMode.LOCKED_READ) && mode.equals(BLOBMode.LOCKED_WRITE)) {
                throw new BLOBStateException("BLOB is locked for reading");
            }
        }
    }


    /**
     * Creates a new BLOB.
     *
     * @param expectedSize of the BLOB
     * @return a VolatileBLOB if expectedSize fits in Volatile memory, a FileBLOB otherwise
     * @throws BundleStorage.StorageFullException if BLOB cannot be created
     */
    public static BLOB createBLOB(int expectedSize) throws BundleStorage.StorageFullException {
        return VolatileStorage.createBLOB(expectedSize);
    }


    /**
     * Size of the current blob object.
     *
     * @return size
     */
    public abstract long size();

    /**
     * Return a cold Flowable, BackPressure-enabled, for the entire BLOB. On subscription, it
     * locks a ReadableBLOB, read it entirely and closes it once completed.
     *
     * @return Flowable
     */
    public abstract Flowable<ByteBuffer> observe();


    /**
     * new {@see ReadableBLOB} from this BLOB. The ReadableBLOB will lock the BLOB for read-only
     * operations. calling close() on the ReadableBLOB will unlock the BLOB. Multiple concurrent
     * ReadableBLOB can be acquired from one single BLOB.
     *
     * @return ReadableBLOB
     * @throws BLOBStateException if the BLOB is already locked for writing
     * @throws BLOBException if the BLOB cannot be read for some reason
     */
    public ReadableBLOB getReadableBLOB() throws BLOBStateException, BLOBException {
        return readableBLOBFactory();
    }

    protected abstract InnerReadableBLOB readableBLOBFactory()
            throws BLOBStateException, BLOBException;

    protected abstract class InnerReadableBLOB implements ReadableBLOB {
        InnerReadableBLOB() throws BLOBStateException {
            switchBLOBMode(BLOBMode.LOCKED_READ);
        }

        @Override
        public void close() {
            try {
                switchBLOBMode(BLOBMode.CLOSED);
            } catch (BLOBStateException bse) {
                //ignore
            }
        }
    }

    /**
     * new {@see WritableBLOB} from this BLOB. The WritableBLOB will lock the BLOB for write-only
     * operations. calling close() on the WritableBLOB will unlock the BLOB. Only one WritableBLOB
     * can be acquired from this single BLOB at any given time.
     *
     * @return WritableBLOB
     * @throws BLOBStateException if the BLOB is already locked for Reading or Writing
     * @throws BLOBException if the BLOB cannot be write for some reason
     */
    public WritableBLOB getWritableBLOB() throws BLOBStateException, BLOBException {
        return writableBLOBFactory();
    }

    protected abstract InnerWritableBLOB writableBLOBFactory()
            throws BLOBStateException, BLOBException;

    protected abstract class InnerWritableBLOB implements WritableBLOB {
        InnerWritableBLOB() throws BLOBStateException {
            switchBLOBMode(BLOBMode.LOCKED_WRITE);
        }

        @Override
        public void close() {
            try {
                switchBLOBMode(BLOBMode.CLOSED);
            } catch (BLOBStateException bse) {
                //ignore
            }
        }
    }

}
