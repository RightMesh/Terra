package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.ExtensionBlock;

import java.util.LinkedList;

/**
 * SecurityAssociationBlock is an {@link ExtensionBlock} that lists SecurityAssociation as
 * described in BPSec.
 *
 * @author Lucien Loiseau on 03/11/18.
 */
public class SecurityAssociationBlock extends ExtensionBlock {

    public static final int SECURITY_ASSOCIATION_BLOCK_TYPE = 192;

    SecurityAssociationBlock() {
        super(SECURITY_ASSOCIATION_BLOCK_TYPE);
    }

    LinkedList<SecurityAssociation> securityAssociations;
}
