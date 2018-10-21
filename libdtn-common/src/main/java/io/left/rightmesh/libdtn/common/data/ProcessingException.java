package io.left.rightmesh.libdtn.common.data;

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
