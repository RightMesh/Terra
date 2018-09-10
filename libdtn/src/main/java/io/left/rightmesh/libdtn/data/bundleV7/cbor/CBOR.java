package io.left.rightmesh.libdtn.data.bundleV7.cbor;

import java.nio.ByteBuffer;

/**
 * @author Lucien Loiseau on 09/09/18.
 */
public class CBOR {

    public static class CborErrorIllegalSimpleType extends Exception {
    }

    public static class CborEncodingUnknown extends Exception {
    }

    public static Encoder getEncoder(ByteBuffer buffer) {
        return new Encoder(buffer);
    }

}
