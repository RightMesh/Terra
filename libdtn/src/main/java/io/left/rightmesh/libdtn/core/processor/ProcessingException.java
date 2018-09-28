package io.left.rightmesh.libdtn.core.processor;

/**
 * Exception raised during processing.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class ProcessingException extends Exception {
    String reason;

    public ProcessingException() {
    }

    /**
     * Constructor.
     *
     * @param reason for not processing this block
     */
    public ProcessingException(String reason) {
        this.reason = reason;
    }
}
