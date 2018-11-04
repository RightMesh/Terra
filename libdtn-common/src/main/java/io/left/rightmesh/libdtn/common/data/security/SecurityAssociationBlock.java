package io.left.rightmesh.libdtn.common.data.security;

import java.util.LinkedList;

import io.left.rightmesh.libdtn.common.data.ExtensionBlock;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public class SecurityAssociationBlock extends ExtensionBlock {

    public static final int type = 192;

    SecurityAssociationBlock() {
        super(type);
    }

    LinkedList<SecurityAssociation> securityAssociations;
}
