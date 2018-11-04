package io.left.rightmesh.libdtn.common.data.security;

import java.util.LinkedList;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public class BlockIntegrityBlock extends AbstractSecurityBlock {


    public static final int type = 193;

    BlockIntegrityBlock() {
        super(type);
    }

    BlockIntegrityBlock(BlockIntegrityBlock bib) {
        super(bib);
    }


    private void checkBCBInteraction(Bundle bundle, BlockConfidentialityBlock bcb) throws ForbiddenOperationException {
        LinkedList<CanonicalBlock> matches = new LinkedList<>();
        for(int st : bcb.securityTargets) {

            /* 3.10 - condition 3 */
            if(this.securityTargets.contains(st)) {
                throw new ForbiddenOperationException();
            }
        }
    }

    @Override
    public void addTo(Bundle bundle) throws ForbiddenOperationException, NoSuchBlockException {
        for(int i : this.securityTargets) {
            if(bundle.getBlock(i) == null) {
                throw new NoSuchBlockException();
            }

            /* 3.10 - cond 6 */
            if(bundle.getBlock(i).type == BlockConfidentialityBlock.type) {
                throw new ForbiddenOperationException();
            }
        }

        for(CanonicalBlock block : bundle.getBlocks()) {
            if(block.type == BlockConfidentialityBlock.type) {
                checkBCBInteraction(bundle, (BlockConfidentialityBlock)block);
            }
        }
        bundle.addBlock(this);
    }

    @Override
    public void applyTo(Bundle bundle, SecurityContext context) {
    }

    @Override
    public void applyFrom(Bundle bundle, SecurityContext context) {
    }

}
