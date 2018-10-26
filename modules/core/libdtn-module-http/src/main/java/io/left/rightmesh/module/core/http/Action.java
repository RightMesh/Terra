package io.left.rightmesh.module.core.http;

import io.left.rightmesh.module.core.http.nettyrouter.Route;
import io.netty.buffer.ByteBuf;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public interface Action extends Route<ByteBuf, ByteBuf> {
}