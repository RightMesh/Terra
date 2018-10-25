package io.left.rightmesh.libdtn.core.daemon.http;

import io.left.rightmesh.libdtn.core.utils.nettyrouter.Route;
import io.netty.buffer.ByteBuf;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public interface Action extends Route<ByteBuf, ByteBuf> {
}