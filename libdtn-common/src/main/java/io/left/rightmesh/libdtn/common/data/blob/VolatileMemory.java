package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * VolatileMemory is used to track the memory used by the various blob. It is not a real
 * memory manager as it does not perform the allocation and disallocation of memory itself
 * (that is managed by the JVM) but it does maintain a memory count and a limit and may
 * throw an exception if too much memory is requested.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class VolatileMemory {

    private int memoryLimit;
    private int currentMemoryUsage = 0;

    public VolatileMemory(int limit) {
        memoryLimit = limit;
    }


    /**
     * request to spend more volatile memory. Throws an exception if the array given as an
     * argument is bigger than the authorized memory remaining. Otherwise it returns a
     * ByteBuffer wrapping around the array.
     *
     * @param array requested
     * @return ByteBuffer wrapping around the array.
     * @throws IOException if memory limit is hit.
     */
    public ByteBuffer malloc(byte[] array) throws IOException {
        if (currentMemoryUsage + array.length > memoryLimit) {
            throw new IOException();
        }
        currentMemoryUsage += array.length;
        return ByteBuffer.wrap(array);
    }

    /**
     * request to spend more volatile memory. Throws an exception if the ByteBuffer given as an
     * argument is bigger than the authorized memory remaining. Otherwise it returns a
     * new ByteBuffer of same size as the remaining bytes from the buffer.
     *
     * @param buffer requested
     * @return newly allocated ByteBuffer
     * @throws IOException if memory limit is hit.
     */
    public ByteBuffer malloc(ByteBuffer buffer) throws IOException {
        if (currentMemoryUsage + buffer.remaining() > memoryLimit) {
            throw new IOException();
        }
        currentMemoryUsage += buffer.remaining();
        ByteBuffer ret = ByteBuffer.allocate(buffer.remaining());
        ret.put(buffer);
        ret.position(0);
        return ret;
    }

    /**
     * request to spend more volatile memory. Throws an exception if the size given as an
     * argument is bigger than the authorized memory remaining. Otherwise it returns a
     * new ByteBuffer of same size as the requested size.
     *
     * @param size requested
     * @return newly allocated ByteBuffer
     * @throws IOException if memory limit is hit.
     */
    public ByteBuffer malloc(int size) throws IOException {
        if (currentMemoryUsage + size > memoryLimit) {
            throw new IOException();
        }
        currentMemoryUsage += size;
        return ByteBuffer.allocate(size);
    }

    /**
     * Instruct VolatileMemory that some memory was freed.
     *
     * @param size newly available memory
     */
    public void free(int size) {
        currentMemoryUsage -= size;
        if (currentMemoryUsage < 0) {
            currentMemoryUsage = 0;
        }
    }

}
