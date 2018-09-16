package io.left.rightmesh.libcbor;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 14/09/18.
 */
public class CBOREncoderTest {
    CborEncoder enc = CBOR.getEncoder();

    @Test
    public void encodeAppendixA_PositiveInteger() {
        /* test positive integer */
        enc.cbor_encode_int(0);
        assertEquals("0x00", getEncodedString());

        enc.cbor_encode_int(1);
        assertEquals("0x01", getEncodedString());

        enc.cbor_encode_int(10);
        assertEquals("0x0a", getEncodedString());

        enc.cbor_encode_int(23);
        assertEquals("0x17", getEncodedString());

        enc.cbor_encode_int(24);
        assertEquals("0x1818", getEncodedString());

        enc.cbor_encode_int(25);
        assertEquals("0x1819", getEncodedString());

        enc.cbor_encode_int(100);
        assertEquals("0x1864", getEncodedString());

        enc.cbor_encode_int(1000);
        assertEquals("0x1903e8", getEncodedString());

        enc.cbor_encode_int(1000000);
        assertEquals("0x1a000f4240", getEncodedString());

        enc.cbor_encode_int(1000000000000L);
        assertEquals("0x1b000000e8d4a51000", getEncodedString());
    }

    @Test
    public void encodeAppendixA_NegativeInteger() {
        /* test negative integer */
        enc.cbor_encode_int(-1);
        assertEquals("0x20", getEncodedString());

        enc.cbor_encode_int(-10);
        assertEquals("0x29", getEncodedString());

        enc.cbor_encode_int(-100);
        assertEquals("0x3863", getEncodedString());

        enc.cbor_encode_int(-1000);
        assertEquals("0x3903e7", getEncodedString());
    }

    @Test
    public void encodeAppendixA_HalfFloat() {
        /* test float (half, single and double) */
        enc.cbor_encode_half_float(0.0f);
        assertEquals("0xf90000", getEncodedString());

        enc.cbor_encode_half_float(-0.0f);
        assertEquals("0xf98000", getEncodedString());

        enc.cbor_encode_half_float(1.0f);
        assertEquals("0xf93c00", getEncodedString());

        enc.cbor_encode_half_float(1.5f);
        assertEquals("0xf93e00", getEncodedString());

        enc.cbor_encode_half_float(65504.0f);
        assertEquals("0xf97bff", getEncodedString());

        enc.cbor_encode_half_float(5.960464477539063e-8f);
        assertEquals("0xf90001", getEncodedString());

        enc.cbor_encode_half_float(0.00006103515625f);
        assertEquals("0xf90400", getEncodedString());

        enc.cbor_encode_half_float(-4.0f);
        assertEquals("0xf9c400", getEncodedString());

        /* test half float special values */
        enc.cbor_encode_half_float(Float.POSITIVE_INFINITY);
        assertEquals("0xf97c00", getEncodedString());

        enc.cbor_encode_half_float(Float.NaN);
        assertEquals("0xf97e00", getEncodedString());

        enc.cbor_encode_half_float(Float.NEGATIVE_INFINITY);
        assertEquals("0xf9fc00", getEncodedString());
    }

    @Test
    public void encodeAppendixA_Float() {
        enc.cbor_encode_float(100000.0f);
        assertEquals("0xfa47c35000", getEncodedString());

        enc.cbor_encode_float(3.4028234663852886e+38f);
        assertEquals("0xfa7f7fffff", getEncodedString());
        
        /* test float special values */
        enc.cbor_encode_float(Float.POSITIVE_INFINITY);
        assertEquals("0xfa7f800000", getEncodedString());

        enc.cbor_encode_float(Float.NaN);
        assertEquals("0xfa7fc00000", getEncodedString());

        enc.cbor_encode_float(Float.NEGATIVE_INFINITY);
        assertEquals("0xfaff800000", getEncodedString());
    }


    @Test
    public void encodeAppendixA_Double() {
        enc.cbor_encode_double(1.1d);
        assertEquals("0xfb3ff199999999999a", getEncodedString());

        enc.cbor_encode_double(-4.1d);
        assertEquals("0xfbc010666666666666", getEncodedString());

        enc.cbor_encode_double(Double.POSITIVE_INFINITY);
        assertEquals("0xfb7ff0000000000000", getEncodedString());

        enc.cbor_encode_double(Double.NaN);
        assertEquals("0xfb7ff8000000000000", getEncodedString());

        
        enc.cbor_encode_double(Double.NEGATIVE_INFINITY);
        assertEquals("0xfbfff0000000000000", getEncodedString());
    }
    

    @Test
    public void encodeAppendixA_SimpleValues() {
        enc.cbor_encode_boolean(false);
        assertEquals("0xf4", getEncodedString());
        
        enc.cbor_encode_boolean(true);
        assertEquals("0xf5", getEncodedString());

        enc.cbor_encode_null();
        assertEquals("0xf6", getEncodedString());

        enc.cbor_encode_undefined();
        assertEquals("0xf7", getEncodedString());

        enc.cbor_encode_simple_value((byte) 16);
        assertEquals("0xf0", getEncodedString());

        enc.cbor_encode_simple_value((byte) 24);
        assertEquals("0xf818", getEncodedString());

        enc.cbor_encode_simple_value((byte) 255);
        assertEquals("0xf8ff", getEncodedString());
    }

    @Test
    public void encodeAppendixA_Byte_Text_Strings() {
        byte[] a3 = {};
        enc.cbor_encode_byte_string(a3);
        assertEquals("0x40", getEncodedString());

        byte[] a1 = {0x01, 0x02, 0x03, 0x04};
        enc.cbor_encode_byte_string(a1);
        assertEquals("0x4401020304", getEncodedString());

        enc.cbor_encode_text_string("");
        assertEquals("0x60", getEncodedString());

        enc.cbor_encode_text_string("a");
        assertEquals("0x6161", getEncodedString());

        enc.cbor_encode_text_string("IETF");
        assertEquals("0x6449455446", getEncodedString());

        enc.cbor_encode_text_string("\"\\");
        assertEquals("0x62225c", getEncodedString());

        enc.cbor_encode_text_string("\u00fc");
        assertEquals("0x62c3bc", getEncodedString());

        enc.cbor_encode_text_string("\u6c34");
        assertEquals("0x63e6b0b4", getEncodedString());

        enc.cbor_encode_text_string("\ud800\udd51");
        assertEquals("0x64f0908591", getEncodedString());
    }

    @Test
    public void encodeAppendixA_Byte_Text_Strings_Indefinite() {
        byte[] a4 = {0x01, 0x02};
        byte[] a5 = {0x03, 0x04, 0x05};
        enc.cbor_start_byte_string(-1)
                .cbor_encode_byte_string(a4)
                .cbor_encode_byte_string(a5)
                .cbor_stop_byte_string();
        assertEquals("0x5f42010243030405ff", getEncodedString());

        enc.cbor_start_text_string(-1)
                .cbor_encode_text_string("strea")
                .cbor_encode_text_string("ming")
                .cbor_stop_text_string();
        assertEquals("0x7f657374726561646d696e67ff", getEncodedString());
    }

    @Test
    public void encodeAppendixA_Tags() {
        /* test tag */
        enc.cbor_encode_tag(0)
                .cbor_encode_text_string("2013-03-21T20:04:00Z");
        assertEquals("0xc074323031332d30332d32315432303a30343a30305a", getEncodedString());

        enc.cbor_encode_tag(1)
                .cbor_encode_int(1363896240);
        assertEquals("0xc11a514b67b0", getEncodedString());

        enc.cbor_encode_tag(1)
                .cbor_encode_double(1363896240.5d);
        assertEquals("0xc1fb41d452d9ec200000", getEncodedString());

        byte[] a1 = {0x01, 0x02, 0x03, 0x04};
        enc.cbor_encode_tag(23)
                .cbor_encode_byte_string(a1);
        assertEquals("0xd74401020304", getEncodedString());

        byte[] a2 = {0x64, 0x49, 0x45, 0x54, 0x46};
        enc.cbor_encode_tag(24)
                .cbor_encode_byte_string(a2);
        assertEquals("0xd818456449455446", getEncodedString());

        enc.cbor_encode_tag(32)
                .cbor_encode_text_string("http://www.example.com");
        assertEquals("0xd82076687474703a2f2f7777772e6578616d706c652e636f6d", getEncodedString());
    }

    @Test
    public void encodeAppendixA_Array_And_Hashes_Definite() {
        enc.cbor_start_array(0);
        assertEquals("0x80", getEncodedString());

        try {
            enc.cbor_encode_collection(new LinkedList());
            assertEquals("0x80", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_array(3)
                .cbor_encode_int(1)
                .cbor_encode_int(2)
                .cbor_encode_int(3);
        assertEquals("0x83010203", getEncodedString());

        try {
            Collection c1 = new LinkedList();
            c1.add(1);
            c1.add(2);
            c1.add(3);
            enc.cbor_encode_collection(c1);
            assertEquals("0x83010203", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_array(3)
                .cbor_encode_int(1)
                .cbor_start_array(2)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_start_array(2)
                .cbor_encode_int(4)
                .cbor_encode_int(5);
        assertEquals("0x8301820203820405", getEncodedString());

        try {
            Collection c2 = new LinkedList();
            Collection c3 = new LinkedList();
            Collection c4 = new LinkedList();
            c2.add(1);
            c3.add(2);
            c3.add(3);
            c4.add(4);
            c4.add(5);
            c2.add(c3);
            c2.add(c4);
            enc.cbor_encode_collection(c2);
            assertEquals("0x8301820203820405", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_array(25);
        for (int i = 1; i < 26; i++) {
            enc.cbor_encode_int(i);
        }
        assertEquals("0x98190102030405060708090a0b0c0d0e0f101112131415161718181819", getEncodedString());


        try {
            Collection c5 = new LinkedList();
            for (int i = 1; i < 26; i++) {
                c5.add(i);
            }
            enc.cbor_encode_collection(c5);
            assertEquals("0x98190102030405060708090a0b0c0d0e0f101112131415161718181819", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_map(0);
        assertEquals("0xa0", getEncodedString());

        try {
            Map m1 = new HashMap<>();
            enc.cbor_encode_map(m1);
            assertEquals("0xa0", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_map(2)
                .cbor_encode_int(1)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_encode_int(4);
        assertEquals("0xa201020304", getEncodedString());
        try {
            Map m1 = new HashMap<>();
            m1.put(1, 2);
            m1.put(3, 4);
            enc.cbor_encode_map(m1);
            assertEquals("0xa201020304", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_map(2)
                .cbor_encode_text_string("a")
                .cbor_encode_int(1)
                .cbor_encode_text_string("b")
                .cbor_start_array(2)
                .cbor_encode_int(2)
                .cbor_encode_int(3);
        assertEquals("0xa26161016162820203", getEncodedString());

        try {
            Map m2 = new HashMap<>();
            Collection c3 = new LinkedList();
            c3.add(2);
            c3.add(3);
            m2.put("a", 1);
            m2.put("b", c3);
            enc.cbor_encode_map(m2);
            assertEquals("0xa26161016162820203", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_array(2)
                .cbor_encode_text_string("a")
                .cbor_start_map(1)
                .cbor_encode_text_string("b")
                .cbor_encode_text_string("c");
        assertEquals("0x826161a161626163", getEncodedString());
        try {
            Map m3 = new HashMap();
            m3.put("b", "c");
            Collection c6 = new LinkedList();
            c6.add("a");
            c6.add(m3);
            enc.cbor_encode_collection(c6);
            assertEquals("0x826161a161626163", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }

        enc.cbor_start_map(5)
                .cbor_encode_text_string("a")
                .cbor_encode_text_string("A")
                .cbor_encode_text_string("b")
                .cbor_encode_text_string("B")
                .cbor_encode_text_string("c")
                .cbor_encode_text_string("C")
                .cbor_encode_text_string("d")
                .cbor_encode_text_string("D")
                .cbor_encode_text_string("e")
                .cbor_encode_text_string("E");
        assertEquals("0xa56161614161626142616361436164614461656145", getEncodedString());
        try {
            Map m4 = new HashMap();
            m4.put("a", "A");
            m4.put("b", "B");
            m4.put("c", "C");
            m4.put("d", "D");
            m4.put("e", "E");
            enc.cbor_encode_map(m4);
            assertEquals("0xa56161614161626142616361436164614461656145", getEncodedString());
        } catch (CBOR.CborEncodingUnknown c) {
            fail();
        }
    }

    @Test
    public void encodeAppendixA_Array_And_Hashes_Indefinite() {
        enc.cbor_start_array(-1)
                .cbor_stop_array();
        assertEquals("0x9fff", getEncodedString());

        enc.cbor_start_array(-1)
                .cbor_encode_int(1)
                .cbor_start_array(2)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_start_array(-1)
                .cbor_encode_int(4)
                .cbor_encode_int(5)
                .cbor_stop_array()
                .cbor_stop_array();
        assertEquals("0x9f018202039f0405ffff", getEncodedString());

        enc.cbor_start_array(-1)
                .cbor_encode_int(1)
                .cbor_start_array(2)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_start_array(2)
                .cbor_encode_int(4)
                .cbor_encode_int(5)
                .cbor_stop_array();
        assertEquals("0x9f01820203820405ff", getEncodedString());

        enc.cbor_start_array(3)
                .cbor_encode_int(1)
                .cbor_start_array(2)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_start_array(-1)
                .cbor_encode_int(4)
                .cbor_encode_int(5)
                .cbor_stop_array();
        assertEquals("0x83018202039f0405ff", getEncodedString());

        enc.cbor_start_array(3)
                .cbor_encode_int(1)
                .cbor_start_array(-1)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_stop_array()
                .cbor_start_array(2)
                .cbor_encode_int(4)
                .cbor_encode_int(5);
        assertEquals("0x83019f0203ff820405", getEncodedString());

        enc.cbor_start_array(-1);
        for (int i = 1; i < 26; i++) {
            enc.cbor_encode_int(i);
        }
        enc.cbor_stop_array();
        assertEquals("0x9f0102030405060708090a0b0c0d0e0f101112131415161718181819ff", getEncodedString());

        enc.cbor_start_map(-1)
                .cbor_encode_text_string("a")
                .cbor_encode_int(1)
                .cbor_encode_text_string("b")
                .cbor_start_array(-1)
                .cbor_encode_int(2)
                .cbor_encode_int(3)
                .cbor_stop_array()
                .cbor_stop_map();
        assertEquals("0xbf61610161629f0203ffff", getEncodedString());

        enc.cbor_start_array(2)
                .cbor_encode_text_string("a")
                .cbor_start_map(-1)
                .cbor_encode_text_string("b")
                .cbor_encode_text_string("c")
                .cbor_stop_map();
        assertEquals("0x826161bf61626163ff", getEncodedString());

        enc.cbor_start_map(-1)
                .cbor_encode_text_string("Fun")
                .cbor_encode_boolean(true)
                .cbor_encode_text_string("Amt")
                .cbor_encode_int(-2)
                .cbor_stop_map();
        assertEquals("0xbf6346756ef563416d7421ff", getEncodedString());
    }

    private String getEncodedString() {
        // get all in one buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enc.encode().subscribe(b -> {
            while(b.hasRemaining()) {
                baos.write(b.get());
            }
        });

        // reset the encoder
        enc = new CborEncoder();

        // return the string
        Formatter formatter = new Formatter();
        formatter.format("0x");
        for(byte b : baos.toByteArray()) {
            formatter.format("%02x", b);
        }
        return (formatter.toString());
    }

}
