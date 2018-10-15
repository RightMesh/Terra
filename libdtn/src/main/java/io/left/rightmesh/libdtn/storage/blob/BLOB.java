package io.left.rightmesh.libdtn.storage.blob;

import io.left.rightmesh.libdtn.storage.bundle.BundleStorage;
import io.left.rightmesh.libdtn.storage.bundle.SimpleStorage;
import io.left.rightmesh.libdtn.storage.bundle.VolatileStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.Flowable;
import rx.Observable;

import java.nio.ByteBuffer;

/**
 * The Binary Large Object (BLOB) handles large amounts of data in a common way,
 * no matter if the data is stored on a persistent or volatile memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public abstract class BLOB {

    public static class StorageFullException extends Exception {
    }

    /**
     * Creates a new BLOB.
     *
     * @param expectedSize of the BLOB
     * @return a VolatileBLOB if expectedSize fits in Volatile memory, a FileBLOB otherwise
     * @throws BundleStorage.StorageFullException if BLOB cannot be created
     */
    public static BLOB createBLOB(int expectedSize) throws StorageFullException {
        try {
            return VolatileBLOB.createBLOB(expectedSize);
        } catch(BLOB.StorageFullException e) {
            // ignore, try simple storage
        }

        try {
            return SimpleStorage.createBLOB(expectedSize);
        } catch(BundleStorage.StorageException se) {
            throw new StorageFullException();
        }
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
     * - UGLY -
     * unfortunately, RxNetty uses RxJava 1.x so we have to make the conversion :(
     *
     * @return Flowable of ByteBuffer
     */
    public Observable<ByteBuf> netty() {
        return Observable.create(s -> {
            observe().toObservable().subscribe(
                    byteBuffer -> s.onNext(Unpooled.wrappedBuffer(byteBuffer)),
                    s::onError,
                    s::onCompleted
            );
        });
    }

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
