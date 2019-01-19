package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ByteBufferBlob is a {@link Blob} that uses a ByteBuffer internally to hold the data.
 *
 * @author Lucien Loiseau on 20/09/18.
 */
public class ByteBufferBlob extends UntrackedByteBufferBlob {

    private VolatileMemory memory;

    /**
     * Constructor.
     *
     * @param memory a memory tracker.
     * @param expectedSize of the blob.
     * @throws IOException if the Blob could not be created.
     */
    public ByteBufferBlob(VolatileMemory memory, int expectedSize) throws IOException {
        this.memory = memory;
        this.data = memory.malloc(expectedSize);
        this.data.mark();
    }

    public ByteBufferBlob(byte[] data) throws IOException {
        this.data = memory.malloc(data);
        this.data.mark();
    }

    public ByteBufferBlob(ByteBuffer data) throws IOException {
        this.data = memory.malloc(data);
        this.data.mark();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        memory.free(data.capacity());
    }

}
