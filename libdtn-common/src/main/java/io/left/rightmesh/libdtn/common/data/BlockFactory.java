package io.left.rightmesh.libdtn.common.data;

/**
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockFactory {

    class UnknownBlockTypeException extends Exception{
    }

    /**
     * Instantiate a new ExtensionBlock.
     *
     * @param type block type
     * @return new canonical block
     * @throws UnknownBlockTypeException if type is unknown
     */
    CanonicalBlock create(int type) throws UnknownBlockTypeException;

}
