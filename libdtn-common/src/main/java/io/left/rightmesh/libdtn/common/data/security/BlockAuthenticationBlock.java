package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockAuthenticationBlock extends AbstractSecurityBlock implements SecurityBlock {

    public static int type = 195;

    BlockAuthenticationBlock() {
        super(type);
    }

    @Override
    public void addTo(Bundle bundle) {
    }

    @Override
    public void applyTo(Bundle bundle, SecurityContext context) {
    }

    @Override
    public void applyFrom(Bundle bundle, SecurityContext context) {
    }
}
