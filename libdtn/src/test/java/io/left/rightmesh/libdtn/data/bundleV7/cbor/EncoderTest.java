package io.left.rightmesh.libdtn.data.bundleV7.cbor;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Lucien Loiseau on 11/09/18.
 */
public class EncoderTest {

    @Test
    public void AppendixA() {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        Encoder enc = CBOR.getEncoder(buffer);
        enc.cbor_encode_int(1)
                .cbor_encode_int(10)
                .cbor_encode_int(23)
                .cbor_encode_int(24)
                .cbor_encode_int(25)
                .cbor_encode_int(100)
                .cbor_encode_int(1000)
                .cbor_encode_int(1000000);
        buffer.flip();
        byteArrayToHex(buffer.array());
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


    /*
     0                            | 0x00                               |
             |                              |                                    |
             | 1                            | 0x01                               |
             |                              |                                    |
             | 10                           | 0x0a                               |
             |                              |                                    |
             | 23                           | 0x17                               |
             |                              |                                    |
             | 24                           | 0x1818                             |
             |                              |                                    |
             | 25                           | 0x1819                             |
             |                              |                                    |
             | 100                          | 0x1864                             |
             |                              |                                    |
             | 1000                         | 0x1903e8                           |
             |                              |                                    |
             | 1000000                      | 0x1a000f4240                       |
             |                              |                                    |
             | 1000000000000                | 0x1b000000e8d4a51000               |
             |                              |                                    |
             | 18446744073709551615         | 0x1bffffffffffffffff               |
             |                              |                                    |
             | 18446744073709551616         | 0xc249010000000000000000           |
             |                              |                                    |
             | -18446744073709551616        | 0x3bffffffffffffffff               |
             |                              |                                    |



    Bormann & Hoffman            Standards Track                   [Page 41]

    RFC 7049                          CBOR                      October 2013


            | -18446744073709551617        | 0xc349010000000000000000           |
            |                              |                                    |
            | -1                           | 0x20                               |
            |                              |                                    |
            | -10                          | 0x29                               |
            |                              |                                    |
            | -100                         | 0x3863                             |
            |                              |                                    |
            | -1000                        | 0x3903e7                           |
            |                              |                                    |
            | 0.0                          | 0xf90000                           |
            |                              |                                    |
            | -0.0                         | 0xf98000                           |
            |                              |                                    |
            | 1.0                          | 0xf93c00                           |
            |                              |                                    |
            | 1.1                          | 0xfb3ff199999999999a               |
            |                              |                                    |
            | 1.5                          | 0xf93e00                           |
            |                              |                                    |
            | 65504.0                      | 0xf97bff                           |
            |                              |                                    |
            | 100000.0                     | 0xfa47c35000                       |
            |                              |                                    |
            | 3.4028234663852886e+38       | 0xfa7f7fffff                       |
            |                              |                                    |
            | 1.0e+300                     | 0xfb7e37e43c8800759c               |
            |                              |                                    |
            | 5.960464477539063e-8         | 0xf90001                           |
            |                              |                                    |
            | 0.00006103515625             | 0xf90400                           |
            |                              |                                    |
            | -4.0                         | 0xf9c400                           |
            |                              |                                    |
            | -4.1                         | 0xfbc010666666666666               |
            |                              |                                    |
            | Infinity                     | 0xf97c00                           |
            |                              |                                    |
            | NaN                          | 0xf97e00                           |
            |                              |                                    |
            | -Infinity                    | 0xf9fc00                           |
            |                              |                                    |
            | Infinity                     | 0xfa7f800000                       |
            |                              |                                    |
            | NaN                          | 0xfa7fc00000                       |
            |                              |                                    |
            | -Infinity                    | 0xfaff800000                       |
            |                              |                                    |



    Bormann & Hoffman            Standards Track                   [Page 42]

    RFC 7049                          CBOR                      October 2013


            | Infinity                     | 0xfb7ff0000000000000               |
            |                              |                                    |
            | NaN                          | 0xfb7ff8000000000000               |
            |                              |                                    |
            | -Infinity                    | 0xfbfff0000000000000               |
            |                              |                                    |
            | false                        | 0xf4                               |
            |                              |                                    |
            | true                         | 0xf5                               |
            |                              |                                    |
            | null                         | 0xf6                               |
            |                              |                                    |
            | undefined                    | 0xf7                               |
            |                              |                                    |
            | simple(16)                   | 0xf0                               |
            |                              |                                    |
            | simple(24)                   | 0xf818                             |
            |                              |                                    |
            | simple(255)                  | 0xf8ff                             |
            |                              |                                    |
            | 0("2013-03-21T20:04:00Z")    | 0xc074323031332d30332d32315432303a |
            |                              | 30343a30305a                       |
            |                              |                                    |
            | 1(1363896240)                | 0xc11a514b67b0                     |
            |                              |                                    |
            | 1(1363896240.5)              | 0xc1fb41d452d9ec200000             |
            |                              |                                    |
            | 23(h'01020304')              | 0xd74401020304                     |
            |                              |                                    |
            | 24(h'6449455446')            | 0xd818456449455446                 |
            |                              |                                    |
            | 32("http://www.example.com") | 0xd82076687474703a2f2f7777772e6578 |
            |                              | 616d706c652e636f6d                 |
            |                              |                                    |
            | h''                          | 0x40                               |
            |                              |                                    |
            | h'01020304'                  | 0x4401020304                       |
            |                              |                                    |
            | ""                           | 0x60                               |
            |                              |                                    |
            | "a"                          | 0x6161                             |
            |                              |                                    |
            | "IETF"                       | 0x6449455446                       |
            |                              |                                    |
            | "\"\\"                       | 0x62225c                           |
            |                              |                                    |
            | "\u00fc"                     | 0x62c3bc                           |
            |                              |                                    |



    Bormann & Hoffman            Standards Track                   [Page 43]

    RFC 7049                          CBOR                      October 2013


            | "\u6c34"                     | 0x63e6b0b4                         |
            |                              |                                    |
            | "\ud800\udd51"               | 0x64f0908591                       |
            |                              |                                    |
            | []                           | 0x80                               |
            |                              |                                    |
            | [1, 2, 3]                    | 0x83010203                         |
            |                              |                                    |
            | [1, [2, 3], [4, 5]]          | 0x8301820203820405                 |
            |                              |                                    |
            | [1, 2, 3, 4, 5, 6, 7, 8, 9,  | 0x98190102030405060708090a0b0c0d0e |
            | 10, 11, 12, 13, 14, 15, 16,  | 0f101112131415161718181819         |
            | 17, 18, 19, 20, 21, 22, 23,  |                                    |
            | 24, 25]                      |                                    |
            |                              |                                    |
            | {}                           | 0xa0                               |
            |                              |                                    |
            | {1: 2, 3: 4}                 | 0xa201020304                       |
            |                              |                                    |
            | {"a": 1, "b": [2, 3]}        | 0xa26161016162820203               |
            |                              |                                    |
            | ["a", {"b": "c"}]            | 0x826161a161626163                 |
            |                              |                                    |
            | {"a": "A", "b": "B", "c":    | 0xa5616161416162614261636143616461 |
   | "C", "d": "D", "e": "E"}     | 4461656145                         |
            |                              |                                    |
            | (_ h'0102', h'030405')       | 0x5f42010243030405ff               |
            |                              |                                    |
            | (_ "strea", "ming")          | 0x7f657374726561646d696e67ff       |
            |                              |                                    |
            | [_ ]                         | 0x9fff                             |
            |                              |                                    |
            | [_ 1, [2, 3], [_ 4, 5]]      | 0x9f018202039f0405ffff             |
            |                              |                                    |
            | [_ 1, [2, 3], [4, 5]]        | 0x9f01820203820405ff               |
            |                              |                                    |
            | [1, [2, 3], [_ 4, 5]]        | 0x83018202039f0405ff               |
            |                              |                                    |
            | [1, [_ 2, 3], [4, 5]]        | 0x83019f0203ff820405               |
            |                              |                                    |
            | [_ 1, 2, 3, 4, 5, 6, 7, 8,   | 0x9f0102030405060708090a0b0c0d0e0f |
            | 9, 10, 11, 12, 13, 14, 15,   | 101112131415161718181819ff         |
            | 16, 17, 18, 19, 20, 21, 22,  |                                    |
            | 23, 24, 25]                  |                                    |
            |                              |                                    |
            | {_ "a": 1, "b": [_ 2, 3]}    | 0xbf61610161629f0203ffff           |
            |                              |                                    |




    Bormann & Hoffman            Standards Track                   [Page 44]

    RFC 7049                          CBOR                      October 2013


            | ["a", {_ "b": "c"}]          | 0x826161bf61626163ff               |
            |                              |                                    |
            | {_ "Fun": true, "Amt": -2}   | 0xbf6346756ef563416d7421ff
*/
}
