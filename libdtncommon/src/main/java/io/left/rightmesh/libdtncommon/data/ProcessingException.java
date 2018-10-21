package io.left.rightmesh.libdtncommon.data;

/**
 * @author Lucien Loiseau on 21/10/18.
 */
public class ProcessingException extends Exception {
    public ProcessingException() {
    }
    public ProcessingException(String reason) {
        super(reason);
    }
}
