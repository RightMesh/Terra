package io.left.rightmesh.libdtn.common.utils;

/**
 * Function applies a transformation.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public interface Function<T, R> {

    R apply(T t) throws Exception;

}
