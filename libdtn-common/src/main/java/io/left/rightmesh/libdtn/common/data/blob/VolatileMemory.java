package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * VolatileMemory is used to track used memory by the various blob.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class VolatileMemory {

    private int MemoryLimit;
    private int CurrentMemoryUsage = 0;

    public VolatileMemory(int limit) {
        MemoryLimit = limit;
    }


    public ByteBuffer malloc(byte[] array) throws IOException {
        if(CurrentMemoryUsage + array.length > MemoryLimit) {
            throw new IOException();
        }
        CurrentMemoryUsage += array.length;
        return ByteBuffer.wrap(array);
    }

    public ByteBuffer malloc(ByteBuffer buffer) throws IOException {
        if(CurrentMemoryUsage + buffer.remaining() > MemoryLimit) {
            throw new IOException();
        }
        CurrentMemoryUsage += buffer.remaining();
        ByteBuffer ret = ByteBuffer.allocate(buffer.remaining());
        ret.put(buffer);
        ret.position(0);
        return ret;
    }

    public ByteBuffer malloc(int size) throws IOException {
        if(CurrentMemoryUsage + size > MemoryLimit) {
            throw new IOException();
        }
        CurrentMemoryUsage += size;
        return ByteBuffer.allocate(size);
    }

    public void free(int size) {
        CurrentMemoryUsage -= size;
        if(CurrentMemoryUsage < 0) {
            CurrentMemoryUsage = 0;
        }
    }

}
