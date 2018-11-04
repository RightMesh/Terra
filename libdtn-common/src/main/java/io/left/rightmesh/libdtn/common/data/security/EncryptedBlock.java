package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class EncryptedBlock extends BlockBLOB {

    public EncryptedBlock(CanonicalBlock block) {
        super(block);
    }

}
