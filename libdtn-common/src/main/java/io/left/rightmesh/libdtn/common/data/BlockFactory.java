package io.left.rightmesh.libdtn.common.data;

/**
 * A block factory instantiates a new CanonicalBlock based on its PAYLOAD_BLOCK_TYPE.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockFactory {

    class UnknownBlockTypeException extends Exception{
    }

    /**
     * Instantiate a new ExtensionBlock.
     *
     * @param type block PAYLOAD_BLOCK_TYPE
     * @return new canonical block
     * @throws UnknownBlockTypeException if PAYLOAD_BLOCK_TYPE is unknown
     */
    CanonicalBlock create(int type) throws UnknownBlockTypeException;

}
