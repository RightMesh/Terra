package io.left.rightmesh.libdtn.common.data;

/**
 * an ExtensionBlock is a CanonicalBlock that is not a PayloadBlock.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public abstract class ExtensionBlock extends CanonicalBlock {

    /**
     * Constructor: creates an empty ExtensionBlock.
     *
     * @param type of the block
     */
    public ExtensionBlock(int type) {
        super(type);
    }

}
