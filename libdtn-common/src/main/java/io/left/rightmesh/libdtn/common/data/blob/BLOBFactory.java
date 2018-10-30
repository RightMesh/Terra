package io.left.rightmesh.libdtn.common.data.blob;

/**
 * @author Lucien Loiseau on 21/10/18.
 */
public interface BLOBFactory {

    class BLOBFactoryException extends Exception {
    }

    // finite size blob
    BLOB createBLOB(int size) throws BLOBFactoryException;

}
