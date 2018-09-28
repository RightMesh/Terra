package io.left.rightmesh.libdtn.core.processor;

/**
 * Exception is thrown whenever a processor couldn't be found for a given CanonicalBlock.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public class ProcessorNotFoundException extends ProcessingException {
    public ProcessorNotFoundException() {
        super("Processor not found");
    }
}
