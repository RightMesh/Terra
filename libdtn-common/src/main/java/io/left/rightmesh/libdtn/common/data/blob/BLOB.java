package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.common.data.Taggable;
import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 21/10/18.
 */
public interface BLOB extends Taggable {

    class NotFileBLOB extends Exception {
    }

    /**
     * Size of the current blob object.
     *
     * @return size
     */
    long size();

    /**
     * Return a cold Flowable, BackPressure-enabled, for the entire BLOB. On subscription, it
     * opens a ReadableBLOB and read it entirely.
     *
     * @return Flowable of ByteBuffer
     */
    Flowable<ByteBuffer> observe();

    /**
     * new {@link ReadableBLOB} from this VolatileBLOB. The ReadableBLOB will lock the VolatileBLOB for read-only
     * operations. calling close() on the ReadableBLOB will unlock the VolatileBLOB. Multiple concurrent
     * ReadableBLOB can be acquired from one single VolatileBLOB.
     *
     * @return ReadableBLOB
     */
    ReadableBLOB getReadableBLOB();

    /**
     * new {@link WritableBLOB} from this VolatileBLOB. The WritableBLOB will lock the VolatileBLOB for write-only
     * operations. calling close() on the WritableBLOB will unlock the VolatileBLOB. Only one WritableBLOB
     * can be acquired from this single VolatileBLOB at any given time.
     *
     * @return WritableBLOB
     */
    WritableBLOB getWritableBLOB();

    /**
     * return true if the entire BLOB is hold into a file. A FileBLOB always returns true,
     * VersatileBLOB may return true only if its volatile part is null.
     *
     * @return true if blob is a file, false otherwise.
     */
    boolean isFileBLOB();

    /**
     * returns the path to the file holding this blob.
     *
     * @return a string to the file holding that blob
     * @throws NotFileBLOB exception if this blob is not a pure file based blob
     */
    String getFilePath() throws NotFileBLOB;

    /**
     * Move the blob into a file. If the file does not exists it creates it. For volatile blob
     * (such as UntrackedByteBufferBLOB, ByteBufferBLOB, GrowingBLOB), this method simply
     * serializes the blob into a file. For file-based BLOB (FileBLOB, VersatileBLOB), this
     * operation simply moves the file to its new location and update the fileBLOB accordingly.
     *
     * @param path
     * @throws IOException
     */
    Completable moveToFile(String path);

}
