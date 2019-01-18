package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.OutputStream;

/**
 * DTNInterface for a Read-only VolatileBlob.
 *
 * @author Lucien Loiseau on 30/07/18.
 */
public interface ReadableBlob {

    /**
     * Writes the binary encapsulated by this class to an OutputStream.
     *
     * @param stream the stream to write to
     * @throws IOException if reading the data or writing to the stream failed
     */
    void read(OutputStream stream) throws IOException;

    /**
     * After close() is called, no further read() is possible with this ReadableBlob.
     */
    void close();

}
