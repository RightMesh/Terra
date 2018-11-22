package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockAuthenticationBlock extends AbstractSecurityBlock implements SecurityBlock {

    public static final int type = 195;

    public BlockAuthenticationBlock() {
        super(type);
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
