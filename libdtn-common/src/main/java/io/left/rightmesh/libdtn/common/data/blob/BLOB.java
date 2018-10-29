package io.left.rightmesh.libdtn.common.data.blob;

import java.nio.ByteBuffer;

import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 21/10/18.
 */
public interface BLOB {

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
     * new {@link ReadableBLOB} from this BaseBLOB. The ReadableBLOB will lock the BaseBLOB for read-only
     * operations. calling close() on the ReadableBLOB will unlock the BaseBLOB. Multiple concurrent
     * ReadableBLOB can be acquired from one single BaseBLOB.
     *
     * @return ReadableBLOB
     */
    ReadableBLOB getReadableBLOB();

    /**
     * new {@link WritableBLOB} from this BaseBLOB. The WritableBLOB will lock the BaseBLOB for write-only
     * operations. calling close() on the WritableBLOB will unlock the BaseBLOB. Only one WritableBLOB
     * can be acquired from this single BaseBLOB at any given time.
     *
     * @return WritableBLOB
     */
    WritableBLOB getWritableBLOB();


}
