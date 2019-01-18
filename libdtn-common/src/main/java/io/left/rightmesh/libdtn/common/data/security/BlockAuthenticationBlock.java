package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * BlockAuthenticationBlock is an ExtensionBlock for authentication in the bpsec extension.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockAuthenticationBlock extends AbstractSecurityBlock implements SecurityBlock {

    public static final int BLOCK_AUTHENTICATION_BLOCK_TYPE = 195;

    public BlockAuthenticationBlock() {
        super(BLOCK_AUTHENTICATION_BLOCK_TYPE);
    }

    @Override
    public void addTo(Bundle bundle) {
    }

    public void applyTo(Bundle bundle,
                        SecurityContext context,
                        BlockDataSerializerFactory serializerFactory,
                        Log logger) {
    }

    public void applyFrom(Bundle bundle,
                          SecurityContext context,
                          BlockDataSerializerFactory serializerFactory,
                          Log logger) {
    }
}
