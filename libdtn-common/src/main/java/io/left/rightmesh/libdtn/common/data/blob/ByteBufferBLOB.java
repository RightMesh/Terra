package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class ByteBufferBLOB extends UntrackedByteBufferBLOB {

    private VolatileMemory memory;

    public ByteBufferBLOB(VolatileMemory memory, int expectedSize) throws IOException {
        this.memory = memory;
        this.data = memory.malloc(expectedSize);
        this.data.mark();
    }

    public ByteBufferBLOB(byte[] data) throws IOException {
        this.data = memory.malloc(data);
        this.data.mark();
    }

    public ByteBufferBLOB(ByteBuffer data) throws IOException {
        this.data = memory.malloc(data);
        this.data.mark();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        memory.free(data.capacity());
    }

}
