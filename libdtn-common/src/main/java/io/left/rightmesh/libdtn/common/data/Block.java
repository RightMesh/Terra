package io.left.rightmesh.libdtn.common.data;

/**
 * Base block class.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public abstract class Block extends Tag {

    /**
     * optional method to overload to clear resources taken from a block.
     */
    public void clearBlock() {
    }

}
