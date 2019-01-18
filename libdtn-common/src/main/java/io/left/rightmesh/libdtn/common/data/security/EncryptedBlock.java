package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * EncryptedBlock is a BlockBlob that holds encrypted data.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class EncryptedBlock extends BlockBlob {

    public EncryptedBlock(CanonicalBlock block) {
        super(block);
    }

}
