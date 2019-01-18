package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Interface to write data into a Blob.
 *
 * @author Lucien Loiseau on 30/07/18.
 */
public interface WritableBlob {

    class BlobOverflowException extends Exception {
    }

    /**
     * Clear the current VolatileBlob.
     */
    void clear();

    /**
     * Read size bytes from the InputStream and store it in the VolatileBlob.
     * Note that there is a natural limit to the size of a Blob because we can read
     * up to {@link Integer#MAX_VALUE} bytes.
     *
     * @param stream read the data from
     * @return int number of bytes read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BlobOverflowException if write size exceed VolatileBlob capacity
     */
    int write(InputStream stream) throws IOException, BlobOverflowException;

    /**
     * Read size bytes from the InputStream and store it in the VolatileBlob.
     * Note that there is a natural limit to the size of a VolatileBlob because
     * we can read up to {@link Integer#MAX_VALUE} bytes.
     *
     * @param stream read the data from
     * @param size   of the data to read
     * @return int number of byte read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BlobOverflowException if write size exceed VolatileBlob capacity
     */
    int write(InputStream stream, int size) throws IOException, BlobOverflowException;

    /**
     * copy one byte to the VolatileBlob.
     *
     * @param b the byte
     * @return 1
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BlobOverflowException if write size exceed VolatileBlob capacity
     */
    int write(byte b) throws IOException, BlobOverflowException;

    /**
     * read all the bytes from the array and copy them to the VolatileBlob.
     *
     * @param a the byte array to write to the VolatileBlob
     * @return number of bytes read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BlobOverflowException if write size exceed VolatileBlob capacity
     */
    int write(byte[] a) throws IOException, BlobOverflowException;

    /**
     * read all the bytes from the ByteBuffer and copy them to the VolatileBlob.
     *
     * @param buffer the bytebyffer to write to the VolatileBlob
     * @return number of bytes read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BlobOverflowException if write size exceed VolatileBlob capacity
     */
    int write(ByteBuffer buffer) throws IOException, BlobOverflowException;

    /**
     * After close() is called, no further write call is possible.
     */
    void close();
}
