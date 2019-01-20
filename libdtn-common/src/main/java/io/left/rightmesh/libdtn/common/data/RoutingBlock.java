package io.left.rightmesh.libdtn.common.data;

/**
 * The RoutingBlock is an extension {@link Block} that can be used in a {@link Bundle} to
 * select the appropriate routing strategy.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public class RoutingBlock extends CanonicalBlock {

    public static final int ROUTING_BLOCK_TYPE = 42;

    public int strategyId;

    /**
     * Constructor.
     */
    public RoutingBlock() {
        super(ROUTING_BLOCK_TYPE);
    }
}
