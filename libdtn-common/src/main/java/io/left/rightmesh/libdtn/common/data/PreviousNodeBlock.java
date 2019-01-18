package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;

/**
 * PreviousNodeBlock holds information about the previous node holding this bundle.
 *
 * @author Lucien Loiseau on 17/09/18.
 */
public class PreviousNodeBlock extends CanonicalBlock {

    public static final int PREVIOUS_NODE_BLOCK_TYPE = 7;

    public Eid previous;

    public PreviousNodeBlock() {
        super(PREVIOUS_NODE_BLOCK_TYPE);
        previous = DtnEid.nullEid();
    }

    public PreviousNodeBlock(Eid previous) {
        super(7);
        this.previous = previous;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreviousNodeBlock");
        if (previous != null) {
            sb.append(": previous node=").append(previous.getEidString());
        } else {
            sb.append(": previous node is unset");
        }
        return sb.toString();
    }

}

