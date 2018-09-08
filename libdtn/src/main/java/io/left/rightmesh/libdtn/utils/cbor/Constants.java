package io.left.rightmesh.libdtn.utils.cbor;

/**
 * Constant values used by the CBOR format.
 */
public interface Constants {

    interface CborType {
        /** Major type 0: unsigned integers. */
        byte CborIntegerType = (byte) 0x00;
        /** Major type 2: byte string. */
        byte CborByteStringType = (byte) 0x40;
        /** Major type 3: text/UTF8 string. */
        byte CborTextStringType = (byte) 0x60;
        /** Major type 4: array of items. */
        byte CborArrayType = (byte) 0x80;
        /** Major type 5: map of pairs. */
        byte CborMapType = (byte) 0xa0;
        /** Major type 6: semantic tags. */
        byte CborTagType = (byte) 0xc0;
        /** Major type 7: floating point, simple data types. */
        byte CborSimpleType      = (byte) 0xe0;

        byte CborBooleanType     = (byte) 0xf5;
        byte CborNullType        = (byte) 0xf6;
        byte CborUndefinedType   = (byte) 0xf7;
        byte CborHalfFloatType   = (byte) 0xf9;
        byte CborFloatType       = (byte) 0xfa;
        byte CborDoubleType      = (byte) 0xfb;

        /** equivalent to the break byte, so it will never be used */
        byte CborInvalidType     = (byte) 0xff;
    }

    interface CborKnownTags {
        /** Semantic tag value describing date/time values in the standard format (UTF8 string, RFC3339). */
        int CborDateTimeStringTag = 0;
        /** Semantic tag value describing date/time values as Epoch timestamp (numeric, RFC3339). */
        int CborUnixTime_tTag = 1;
        /** Semantic tag value describing a positive big integer value (byte string). */
        int CborPositiveBignumTag = 2;
        /** Semantic tag value describing a negative big integer value (byte string). */
        int CborNegativeBignumTag = 3;
        /** Semantic tag value describing a decimal fraction value (two-element array, base 10). */
        int CborDecimalTag = 4;
        /** Semantic tag value describing a big decimal value (two-element array, base 2). */
        int CborBigfloatTag = 5;
        /** Semantic tag value describing an expected conversion to base64url encoding. */
        int CborExpectedBase64urlTag = 21;
        /** Semantic tag value describing an expected conversion to base64 encoding. */
        int CborExpectedBase64Tag = 22;
        /** Semantic tag value describing an expected conversion to base16 encoding. */
        int CborExpectedBase16Tag = 23;
        /** Semantic tag value describing an encoded CBOR data item (byte string). */
        int CborEncodedCborTag = 24;
        /** Semantic tag value describing an URL (UTF8 string). */
        int CborUrlTag = 32;
        /** Semantic tag value describing a base64url encoded string (UTF8 string). */
        int CborBase64urlTag = 33;
        /** Semantic tag value describing a base64 encoded string (UTF8 string). */
        int CborBase64Tag = 34;
        /** Semantic tag value describing a regular expression string (UTF8 string, PCRE). */
        int CborRegularExpressionTag = 35;
        /** Semantic tag value describing a MIME message (UTF8 string, RFC2045). */
        int CborMimeMessageTag = 36;
        /** Semantic tag value describing CBOR content. */
        int CborSignatureTag = 55799;
    }
}