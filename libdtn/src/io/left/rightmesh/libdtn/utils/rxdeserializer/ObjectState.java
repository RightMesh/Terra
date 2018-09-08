package io.left.rightmesh.libdtn.utils.rxdeserializer;

/**
 * ObjectState is a generic state that deserialize a single object.
 * 
 * @author Lucien Loiseau on 03/09/18.
 */

public abstract class ObjectState<T> extends RxState {
    public abstract void onSuccess(T obj) throws RxDeserializerException;
}