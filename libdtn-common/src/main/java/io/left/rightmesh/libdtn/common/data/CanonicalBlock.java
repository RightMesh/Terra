package io.left.rightmesh.libdtn.common.data;

/**
 * Abstract generic CanonicalBlock object.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public abstract class CanonicalBlock extends BlockHeader {

    protected CanonicalBlock(int type) {
        super(type);
    }

    CanonicalBlock(CanonicalBlock block) {
        super(block);
    }

    /**
     * CanonicalBlock Factory.
     *
     * @param type of the block to create
     * @return an instance of a block for the given type
     */
    public static CanonicalBlock create(int type) {
        switch (type) {
            case PayloadBlock.type:
                return new PayloadBlock();
            case AgeBlock.type:
                return new AgeBlock();
            case ScopeControlHopLimitBlock.type:
                return new ScopeControlHopLimitBlock();
            default:
                return new UnknownExtensionBlock(type);
        }
    }
}
