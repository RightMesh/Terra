package io.left.rightmesh.libdtn.bundleV6;

/**
 * an ExtensionBlock is a Block that is not a PayloadBlock.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public abstract class ExtensionBlock extends Block {

    /**
     * Constructor: creates an empty ExtensionBlock.
     *
     * @param type of the block
     */
    public ExtensionBlock(int type) {
        super(type);
    }


}
