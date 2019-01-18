package io.left.rightmesh.libdtn.common.utils;

/**
 * Supplier supplies a data.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public interface Supplier<R> {

    R get() throws Exception;

}
