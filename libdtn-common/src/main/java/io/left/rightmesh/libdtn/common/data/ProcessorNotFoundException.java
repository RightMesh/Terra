package io.left.rightmesh.libdtn.common.data;

/**
 * @author Lucien Loiseau on 21/10/18.
 */
public class ProcessorNotFoundException extends ProcessingException {

    public ProcessorNotFoundException() {
        super("Processor not found");
    }

}
