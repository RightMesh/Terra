package io.left.rightmesh.libdtn.common.data;

/**
 * ScopeControlHopLimit CanonicalBlock is used to limit the propagation of the bundle to a maximum number
 * of hop away from the source. It contains a count value that is incremented at every hop and
 * a limit value that is set by the source. This block is described in the following ietf draft:
 * See <a href="https://tools.ietf.org/html/draft-fall-dtnrg-schl-00">draft-fall-dtnrg-schl-00</a>.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class ScopeControlHopLimitBlock extends ExtensionBlock {

    public static final int type = 9;

    public long count;
    public long limit;

    public ScopeControlHopLimitBlock() {
        super(type);
        setV6Flag(BlockV6Flags.REPLICATE_IN_EVERY_FRAGMENT, true);
        count = 0;
        limit = 0;
    }


    public long getHopsToLive() {
        return (limit < count) ? 0 : limit - count;
    }

    public void increment(long hops) {
        count += hops;
    }

    public void setLimit(long hops) {
        count = 0;
        limit = hops;
    }
}
