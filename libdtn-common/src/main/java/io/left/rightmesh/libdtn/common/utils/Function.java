package io.left.rightmesh.libdtn.common.utils;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public interface Function<T, R> {

    R apply(T t) throws Exception;

}
