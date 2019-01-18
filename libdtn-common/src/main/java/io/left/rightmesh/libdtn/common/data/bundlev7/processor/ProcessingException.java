package io.left.rightmesh.libdtn.common.data.bundlev7.processor;

/**
 * ProcessingException is thrown whenever an error happened during the processing of a Block.
 *
 * @author Lucien Loiseau on 21/10/18.
 */
public class ProcessingException extends Exception {

    public ProcessingException() {
    }

    public ProcessingException(String reason) {
        super(reason);
    }
}
