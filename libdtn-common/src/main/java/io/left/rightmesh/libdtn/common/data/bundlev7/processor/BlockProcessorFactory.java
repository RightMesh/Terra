package io.left.rightmesh.libdtn.common.data.bundlev7.processor;

/**
 * BlockProcessorFactory is an interface for a factory of BlockProcessor.
 *
 * @author Lucien Loiseau on 26/11/18.
 */
public interface BlockProcessorFactory {


    class ProcessorNotFoundException extends Exception{
    }

    /**
     * returns a processor for a CanonicalBlock.
     *
     * @param type block type
     * @return a new {@link BlockProcessor}
     * @throws ProcessorNotFoundException if type is unknown
     */
    BlockProcessor create(int type) throws ProcessorNotFoundException;



}
