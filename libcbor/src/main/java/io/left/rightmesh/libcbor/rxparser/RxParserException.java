package io.left.rightmesh.libcbor.rxparser;

/**
 * Exception thrown during deserialization.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class RxParserException extends Exception {
    /**
     * Constructor.
     *
     * @param msg reason
     */
    public RxParserException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param from deserializer entity
     * @param msg reason
     */
    public RxParserException(String from, String msg) {
        super(from + ": " + msg);
    }
}
