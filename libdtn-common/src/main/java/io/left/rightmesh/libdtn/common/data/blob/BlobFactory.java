package io.left.rightmesh.libdtn.common.data.blob;

/**
 * A {@link Blob} factory creates a new Blob structure on demand.
 *
 * @author Lucien Loiseau on 21/10/18.
 */
public interface BlobFactory {

    class BlobFactoryException extends Exception {
    }

    /**
     * creates a new {@link Blob} of expected size.
     *
     * @param size expected
     * @return a new Blob instance
     * @throws BlobFactoryException if the Blob could not be created.
     */
    Blob createBlob(int size) throws BlobFactoryException;

}
