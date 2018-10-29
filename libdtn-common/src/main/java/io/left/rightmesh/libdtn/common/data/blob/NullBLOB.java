package io.left.rightmesh.libdtn.common.data.blob;

import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * NullBlob is a BaseBLOB of size zero and that contains no data.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class NullBLOB implements BLOB {

    public NullBLOB() {
    }
    
    public NullBLOB(int expectedSize) {
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.empty();
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new NullReadableBLOB();
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new NullWritableBLOB();
    }
}
