package io.left.rightmesh.libdtn.common.data;

/**
 * FlowLabelBlock is used to tag quality of service for the bundle.
 *
 * @author Lucien Loiseau on 17/09/18.
 */
public class FlowLabelBlock extends CanonicalBlock {

    public static final int FLOW_LABEL_BLOCK_TYPE = 6;

    /**
     * Constructor.
     */
    public FlowLabelBlock() {
        super(FLOW_LABEL_BLOCK_TYPE);
    }

}
