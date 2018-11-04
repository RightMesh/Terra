package io.left.rightmesh.libdtn.common.data.blob;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public interface Supplier<R> {

    R get() throws Exception;

}
