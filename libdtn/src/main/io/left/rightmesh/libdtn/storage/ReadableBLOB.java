package io.left.rightmesh.libdtn.storage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * DTNInterface for a Read-only BLOB.
 *
 * @author Lucien Loiseau on 30/07/18.
 */
public interface ReadableBLOB {

    /**
     * Writes the binary encapsulated by this class to an OutputStream.
     *
     * @param stream the stream to write to
     * @throws IOException if reading the data or writing to the stream failed
     */
    void read(OutputStream stream) throws IOException;

    /**
     * After close() is called, no further read() is possible with this ReadableBLOB.
     */
    void close();

}
