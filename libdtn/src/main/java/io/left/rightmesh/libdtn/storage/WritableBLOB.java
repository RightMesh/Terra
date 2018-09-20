package io.left.rightmesh.libdtn.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * DTNInterface to write data to a BLOB.
 *
 * @author Lucien Loiseau on 30/07/18.
 */
public interface WritableBLOB {

    class BLOBOverflowException extends Exception {
    }

    /**
     * Clear the current BLOB.
     */
    void clear();

    /**
     * Read size bytes from the InputStream and store it in the BLOB. Note that there is a natural
     * limit to the size of a BLOB because we can read up to {@see Integer.MAX_VALUE} bytes.
     *
     * @param stream read the data from
     * @param size   of the data to read
     * @return int number of byte read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BLOBOverflowException if write size exceed BLOB capacity
     */
    int write(InputStream stream, int size) throws IOException, BLOBOverflowException;

    /**
     * copy one byte to the BLOB.
     *
     * @param b the byte
     * @return 1
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BLOBOverflowException if write size exceed BLOB capacity
     */
    int write(byte b) throws IOException, BLOBOverflowException;

    /**
     * read all the bytes from the array and copy them to the BLOB.
     *
     * @param a the byte array to write to the BLOB
     * @return number of bytes read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BLOBOverflowException if write size exceed BLOB capacity
     */
    int write(byte[] a) throws IOException, BLOBOverflowException;

    /**
     * read all the bytes from the ByteBuffer and copy them to the BLOB.
     *
     * @param buffer the bytebyffer to write to the BLOB
     * @return number of bytes read
     * @throws IOException if low-level reading the data or writing to the blob failed
     * @throws BLOBOverflowException if write size exceed BLOB capacity
     */
    int write(ByteBuffer buffer) throws IOException, BLOBOverflowException;

    /**
     * After close() is called, no further write call is possible.
     */
    void close();
}
