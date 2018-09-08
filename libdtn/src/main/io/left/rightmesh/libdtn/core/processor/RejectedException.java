package io.left.rightmesh.libdtn.core.processor;

/**
 * RejectedException is raised if an error during processing must discard the entire bundle.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class RejectedException extends ProcessingException {

    /**
     * Constructor.
     *
     * @param reason for not validating the block
     */
    public RejectedException(String reason) {
        super(reason);
    }
}
