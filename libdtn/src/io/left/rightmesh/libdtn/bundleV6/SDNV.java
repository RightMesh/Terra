package io.left.rightmesh.libdtn.bundleV6;

import io.left.rightmesh.libdtn.utils.rxdeserializer.ObjectState;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxDeserializerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Class to handle parsing and formatting of self describing numeric
 * values (SDNVs).
 *
 * <p>The basic idea is to enable a compact byte representation of
 * numeric values that may widely vary in size. This encoding is based
 * on the ASN.1 specification for encoding Object Identifier Arcs.
 *
 * <p>Conceptually, the integer sdnv_value to be encoded is split into 7-bit
 * segments. These are encoded into the output byte stream, such that
 * the high order bit in each byte is set to one for all bytes except
 * the last one.
 *
 * @author Lucien Loiseau on 17/07/18.
 */
public class SDNV implements Comparable<SDNV>, Serializable {

    public static final int MAX_SDNV_BYTES = calculateLength(Long.MAX_VALUE);

    private long value;
    public int length;

    /**
     * Constructor: creates an SNDV from a long. The number must not be negative
     *
     * @param value of the number to be represented by this SDNV
     * @throws NumberFormatException if the number is negative
     */
    public SDNV(long value) throws NumberFormatException {
        if (value < 0) {
            throw new NumberFormatException("SDNVs cannot be negative.");
        }
        this.value = value;
        length = calculateLength(value);
    }

    /**
     * Constructor: reads an SDNV from an InputStream. It only reads what's necessary to decode the
     * SDNV.
     *
     * @param in InputStream to read the SDNV from
     * @throws NumberFormatException if the number is negative or malformed
     */
    SDNV(InputStream in) throws IOException, NumberFormatException {
        deserialize(in);
    }

    /**
     * returns the sdnv_value of the SDNV as a long.
     *
     * @return long sdnv_value of the SDNV
     */
    public long getValue() {
        return value;
    }

    /**
     * returns the SDNV as an array of bytes.
     *
     * @return byte array holding the SDNV
     */
    public byte[] getBytes() {
        byte[] ret = new byte[length];
        long v = value;
        for (int i = length - 1; i >= 0; i--) {
            ret[i] = (byte) (((byte) v & (byte) 0x7F) | (byte) 0x80);
            v = v >> 7;
        }
        ret[length - 1] = (byte) (ret[length - 1] & (byte) 0x7f);
        return ret;
    }

    /**
     * Deserialize an SDNV from an {@see InputStream}.
     *
     * @param in InputStream to read the SDNV from
     * @throws IOException           if an error occured during deserialization
     * @throws NumberFormatException if the SDNV was malformed
     */
    private void deserialize(InputStream in) throws IOException, NumberFormatException {
        int read = 0;
        int b;
        do {
            value = value << 7;
            b = in.read();
            if (b < 0) {
                throw new IOException();
            }
            value |= ((byte) b & (byte) 0x7F);
            read++;
        } while ((read <= MAX_SDNV_BYTES) && (((byte) b & (byte) 0x80) != 0));

        if (read > MAX_SDNV_BYTES) {
            throw new NumberFormatException();
        }

        this.length = read;
    }

    /**
     * Deserialize and return a new SDNV.
     */
    public abstract static class SDNVState extends ObjectState<SDNV> {

        private long sdnv_value = 0;
        private int read = 0;
        private int b;

        @Override
        public void onEnter() {
            sdnv_value = 0;
            read = 0;
        }

        @Override
        public void onNext(ByteBuffer next)
                throws RxDeserializerException {
            sdnv_value = sdnv_value << 7;
            b = next.get();
            sdnv_value |= ((byte) b & (byte) 0x7F);
            read++;

            if (read > SDNV.MAX_SDNV_BYTES) {
                throw new RxDeserializerException(
                        "RFC5050", "SDNV size exceeds maximum");
            }

            if (((byte) b & (byte) 0x80) == 0) {
                onSuccess(new SDNV(sdnv_value));
                sdnv_value = 0;
                read = 0;
            }
        }

        @Override
        public void onExit() {
        }
    }

    /**
     * returns the expected length of an SDNV.
     *
     * @param v the number
     * @return size of the expected SDNV-encoded byte array
     */
    private static int calculateLength(long v) {
        int l = 0;
        do {
            v = v >> 7;
            l++;
        } while (v > 0);
        return l;
    }

    @Override
    public int compareTo(SDNV o) {
        return Long.compare(value, o.value);
    }
}
