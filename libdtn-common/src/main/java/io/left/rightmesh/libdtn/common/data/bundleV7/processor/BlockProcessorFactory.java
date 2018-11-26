package io.left.rightmesh.libdtn.common.data.bundleV7.processor;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public interface BlockProcessorFactory {


    class ProcessorNotFoundException extends Exception{
    }

    /**
     * returns a processor for a CanonicalBlock.
     *
     * @param type block type
     * @throws ProcessorNotFoundException if type is unknown
     */
    BlockProcessor create(int type) throws ProcessorNotFoundException;



}
