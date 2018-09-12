package io.left.rightmesh.libdtn.data.cbor;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

import io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR;
import io.left.rightmesh.libdtn.data.bundleV7.cbor.CborParser;
import io.left.rightmesh.libdtn.utils.rxparser.RxParserException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 11/09/18.
 */
public class CBORParserTest {

    @Test
    public void parseAppendixA_PositiveInteger() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_int((obj) -> assertEquals(0, (long) obj))
                    .read(hexToBuf("0x00"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(1, (long) obj))
                    .read(hexToBuf("0x01"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(10, (long) obj))
                    .read(hexToBuf("0x0a"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(23, (long) obj))
                    .read(hexToBuf("0x17"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(24, (long) obj))
                    .read(hexToBuf("0x1818"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(25, (long) obj))
                    .read(hexToBuf("0x1819"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(100, (long) obj))
                    .read(hexToBuf("0x1864"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(1000, (long) obj))
                    .read(hexToBuf("0x1903e8"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(1000000, (long) obj))
                    .read(hexToBuf("0x1a000f4240"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(1000000000000L, (long) obj))
                    .read(hexToBuf("0x1b000000e8d4a51000"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }

    @Test
    public void parseAppendixA_NegativeInteger() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_int((obj) -> assertEquals(-1, (long) obj))
                    .read(hexToBuf("0x20"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(-10, (long) obj))
                    .read(hexToBuf("0x29"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(-100, (long) obj))
                    .read(hexToBuf("0x3863"));
            assertEquals(true, b);

            b = dec.cbor_parse_int((obj) -> assertEquals(-1000L, (long) obj))
                    .read(hexToBuf("0x3903e7"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }


    @Test
    public void parseAppendixA_HalfFloats() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_float((obj) -> assertEquals(0.0d, obj))
                    .read(hexToBuf("0xf90000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(-0.0d, obj))
                    .read(hexToBuf("0xf98000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(1.0d, obj))
                    .read(hexToBuf("0xf93c00"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(1.5d, obj))
                    .read(hexToBuf("0xf93e00"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(65504.0d, obj))
                    .read(hexToBuf("0xf97bff"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(5.960464477539063e-8d, obj))
                    .read(hexToBuf("0xf90001"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(0.00006103515625d, obj))
                    .read(hexToBuf("0xf90400"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(-4.0d, obj))
                    .read(hexToBuf("0xf9c400"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.POSITIVE_INFINITY, obj))
                    .read(hexToBuf("0xf97c00"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NaN, obj))
                    .read(hexToBuf("0xf97e00"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NEGATIVE_INFINITY, obj))
                    .read(hexToBuf("0xf9fc00"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }

    @Test
    public void parseAppendixA_Floats() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_float((obj) -> assertEquals(100000.0d, obj))
                    .read(hexToBuf("0xfa47c35000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(3.4028234663852886e+38d, obj))
                    .read(hexToBuf("0xfa7f7fffff"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.POSITIVE_INFINITY, obj))
                    .read(hexToBuf("0xfa7f800000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NaN, obj))
                    .read(hexToBuf("0xfa7fc00000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NEGATIVE_INFINITY, obj))
                    .read(hexToBuf("0xfaff800000"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }


    @Test
    public void parseAppendixA_Double() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_float((obj) -> assertEquals(1.1d, obj))
                    .read(hexToBuf("0xfb3ff199999999999a"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(-4.1d, obj))
                    .read(hexToBuf("0xfbc010666666666666"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.POSITIVE_INFINITY, obj))
                    .read(hexToBuf("0xfb7ff0000000000000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NaN, obj))
                    .read(hexToBuf("0xfb7ff8000000000000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float((obj) -> assertEquals(Double.NEGATIVE_INFINITY, obj))
                    .read(hexToBuf("0xfbfff0000000000000"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }


    @Test
    public void parseAppendixA_SimpleValues() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_boolean((obj) -> assertEquals(false, (boolean) obj))
                    .read(hexToBuf("0xf4"));
            assertEquals(true, b);

            b = dec.cbor_parse_boolean((obj) -> assertEquals(true, (boolean) obj))
                    .read(hexToBuf("0xf5"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }


    @Test
    public void parseAppendixA_Byte_Text_Strings() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            byte[] a0 = {};
            b = dec.cbor_parse_byte_string_unsafe((chunk) -> assertArrayEquals(a0, chunk.array()))
                    .read(hexToBuf("0x40"));
            assertEquals(true, b);

            byte[] a1 = {0x01, 0x02, 0x03, 0x04};
            b = dec.cbor_parse_byte_string_unsafe((str) -> assertArrayEquals(a1, str.array()))
                    .read(hexToBuf("0x4401020304"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("", str))
                    .read(hexToBuf("0x60"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("a", str))
                    .read(hexToBuf("0x6161"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("IETF", str))
                    .read(hexToBuf("0x6449455446"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("\"\\", str))
                    .read(hexToBuf("0x62225c"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("\u00fc", str))
                    .read(hexToBuf("0x62c3bc"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("\u6c34", str))
                    .read(hexToBuf("0x63e6b0b4"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe((str) -> assertEquals("\ud800\udd51", str))
                    .read(hexToBuf("0x64f0908591"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }

    @Test
    public void parseAppendixA_Tags() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_parse_tag((tag) -> assertEquals(0L, (long) tag))
                    .cbor_parse_text_string_unsafe((str) -> assertEquals("2013-03-21T20:04:00Z", str))
                    .read(hexToBuf("0xc074323031332d30332d32315432303a30343a30305a"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe(
                    (str) -> assertEquals("2013-03-21T20:04:00Z", str),
                    (tag) -> assertEquals(0L, tag))
                    .read(hexToBuf("0xc074323031332d30332d32315432303a30343a30305a"));
            assertEquals(true, b);

            b = dec.cbor_parse_tag((tag) -> assertEquals(1, (long) tag))
                    .cbor_parse_int((d) -> assertEquals(1363896240, (long) d))
                    .read(hexToBuf("0xc11a514b67b0"));
            assertEquals(true, b);

            b = dec.cbor_parse_int(
                    (d) -> assertEquals(1363896240, (long) d),
                    (tag) -> assertEquals(1, tag))
                    .read(hexToBuf("0xc11a514b67b0"));
            assertEquals(true, b);

            b = dec.cbor_parse_tag((tag) -> assertEquals(1, (long) tag))
                    .cbor_parse_float((d) -> assertEquals(1363896240.5d, d))
                    .read(hexToBuf("0xc1fb41d452d9ec200000"));
            assertEquals(true, b);

            b = dec.cbor_parse_float(
                    (d) -> assertEquals(1363896240.5d, d),
                    (tag) -> assertEquals(1, tag))
                    .read(hexToBuf("0xc1fb41d452d9ec200000"));
            assertEquals(true, b);

            byte[] a1 = {0x01, 0x02, 0x03, 0x04};
            b = dec.cbor_parse_tag((tag) -> assertEquals(23, (long) tag))
                    .cbor_parse_byte_string_unsafe((d) -> assertArrayEquals(a1, d.array()))
                    .read(hexToBuf("0xd74401020304"));
            assertEquals(true, b);

            b = dec.cbor_parse_byte_string_unsafe(
                    (d) -> assertArrayEquals(a1, d.array()),
                    (tag) -> assertEquals(23, tag))
                    .read(hexToBuf("0xd74401020304"));
            assertEquals(true, b);

            b = dec.cbor_parse_tag((tag) -> assertEquals(32, (long) tag))
                    .cbor_parse_text_string_unsafe((s) -> assertEquals("http://www.example.com", s))
                    .read(hexToBuf("0xd82076687474703a2f2f7777772e6578616d706c652e636f6d"));
            assertEquals(true, b);

            b = dec.cbor_parse_text_string_unsafe(
                    (s) -> assertEquals("http://www.example.com", s),
                    (tag) -> assertEquals(32, (long) tag))
                    .read(hexToBuf("0xd82076687474703a2f2f7777772e6578616d706c652e636f6d"));
            assertEquals(true, b);
        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }

    @Test
    public void parseAppendixA_Array_And_Hashes_Definite() {
        try {
            CborParser dec = CBOR.getCborDecoder();
            boolean b;

            b = dec.cbor_open_array(s -> assertEquals(0, s))
                    .read(hexToBuf("0x80"));
            assertEquals(true, b);


            Collection c1 = new LinkedList();
            for (int i = 1; i < 26; i++) {
                c1.add(i);
            }
            Collection c2 = new LinkedList();
            b = dec.<CBOR.IntegerItem>cbor_parse_linear_array(CBOR.IntegerItem::new, c2::add)
                    .read(hexToBuf("0x98190102030405060708090a0b0c0d0e0f101112131415161718181819"));
            assertArrayEquals(c1.toArray(), c2.toArray());
            assertEquals(true, b);

        } catch (RxParserException rpe) {
            rpe.printStackTrace();
            fail();
        }
    }

    public ByteBuffer hexToBuf(String s) {
        s = s.replaceFirst("0x", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return ByteBuffer.wrap(data);
    }

}
