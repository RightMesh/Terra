package io.left.rightmesh.libdtn.utils.rxdeserializer;

/**
 * Exception thrown during deserialization.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class RxDeserializerException extends Exception {
    /**
     * Constructor.
     *
     * @param msg reason
     */
    public RxDeserializerException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param from deserializer entity
     * @param msg reason
     */
    public RxDeserializerException(String from, String msg) {
        super(from + ": " + msg);
    }
}
