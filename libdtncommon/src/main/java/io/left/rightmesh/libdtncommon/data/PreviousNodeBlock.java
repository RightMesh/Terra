package io.left.rightmesh.libdtncommon.data;

import io.left.rightmesh.libdtncommon.data.eid.DTN;
import io.left.rightmesh.libdtncommon.data.eid.EID;

/**
 * @author Lucien Loiseau on 17/09/18.
 */
public class PreviousNodeBlock extends CanonicalBlock {

    public static final int type = 7;

    public EID previous;

    public PreviousNodeBlock() {
        super(type);
        previous = DTN.NullEID();
    }

    public PreviousNodeBlock(EID previous) {
        super(7);
        this.previous = previous;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreviousNodeBlock");
        if(previous != null) {
            sb.append(": previous node=").append(previous.getEIDString());
        } else {
            sb.append(": previous node is unset");
        }
        return sb.toString();
    }

}

