package io.left.rightmesh.libdtn.data.bundleV7.cbor;

/**
 * Constant values used by the CBOR format.
 */
public interface Constants {

    interface CborInternals {
        int SmallValueBitLength = 5;
        int SmallValueMask = (1 << SmallValueBitLength) - 1;      /* 31 */
        int MajorTypeShift = SmallValueBitLength;
        int MajorTypeMask = (int) (~0 << MajorTypeShift);
        int BreakByte = (CborMajorTypes.SimpleTypesType << MajorTypeShift) | CborSimpleValues.Break;
    }

    interface CborMajorTypes {
        int UnsignedIntegerType = 0;
        int NegativeIntegerType = 1;
        int ByteStringType = 2;
        int TextStringType = 3;
        int ArrayType = 4;
        int MapType = 5;
        int TagType = 6;
        int SimpleTypesType = 7;
    }

    interface CborAdditionalInfo {
        int Value8Bit = 24;
        int Value16Bit = 25;
        int Value32Bit = 26;
        int Value64Bit = 27;
        int IndefiniteLength = 31;
    }

    interface CborType {                 // Parser returns:
                                         // ---------------
        int CborIntegerType = 0x00;      // Long
        int CborByteStringType = 0x40;   // ByteBuffer
        int CborTextStringType = 0x60;   // ByteBuffer
        int CborArrayType = 0x80;        // Collection
        int CborMapType = 0xa0;          // Map
        int CborTagType = 0xc0;          // Long
        int CborSimpleType = 0xe0;       //
        int CborBooleanType = 0xf5;      // Boolean
        int CborNullType = 0xf6;         // null
        int CborUndefinedType = 0xf7;    // null
        int CborHalfFloatType = 0xf9;    // Double
        int CborFloatType = 0xfa;        // Double
        int CborDoubleType = 0xfb;       // Double
        int CborBreakType = 0xff;        // null
    }

    interface CborKnownTags {
        int CborDateTimeStringTag = 0;
        int CborUnixTime_tTag = 1;
        int CborPositiveBignumTag = 2;
        int CborNegativeBignumTag = 3;
        int CborDecimalTag = 4;
        int CborBigfloatTag = 5;
        int CborExpectedBase64urlTag = 21;
        int CborExpectedBase64Tag = 22;
        int CborExpectedBase16Tag = 23;
        int CborEncodedCborTag = 24;
        int CborUrlTag = 32;
        int CborBase64urlTag = 33;
        int CborBase64Tag = 34;
        int CborRegularExpressionTag = 35;
        int CborMimeMessageTag = 36;
        int CborSignatureTag = 55799;
    }

    interface CborSimpleValues {
        int FalseValue = 20;
        int TrueValue = 21;
        int NullValue = 22;
        int UndefinedValue = 23;
        int SimpleTypeInNextByte = 24;
        int HalfPrecisionFloat = 25;
        int SinglePrecisionFloat = 26;
        int DoublePrecisionFloat = 27;
        int Break = 31;
    }

    interface CborJumpTable {
        byte CborUInt8Zero = (byte) 0x00;
        byte CborUInt8Type = (byte) 0x18;
        byte CborUInt16Type = (byte) 0x19;
        byte CborUInt32Type = (byte) 0x1a;
        byte CborUInt64Type = (byte) 0x1b;
        byte CborNegativeOne = (byte) 0x20;
        byte CborNegativeInt8Type = (byte) 0x38;
        byte CborNegativeInt16Type = (byte) 0x39;
        byte CboroNegativeInt32Type = (byte) 0x3a;
        byte CborNegativeInt64Type = (byte) 0x3b;
        byte CborByteStringWithLengthZero = (byte) 0x40;
        byte CborByteStringWith1ByteLength = (byte) 0x58;
        byte CborByteStringWith2BytesLength = (byte) 0x59;
        byte CborByteStringWith4BytesLength = (byte) 0x5a;
        byte CborByteStringWith8BytesLength = (byte) 0x5b;
        byte CborByteStringWithIndefiniteLength = (byte) 0x5f;
        byte CborTextStringWithLengthZero = (byte) 0x60;
        byte CborTextStringWith1ByteLength = (byte) 0x78;
        byte CborTextStringWith2BytesLength = (byte) 0x79;
        byte CborTextStringWith4BytesLength = (byte) 0x7a;
        byte CborTextStringWith8BytesLength = (byte) 0x7b;
        byte CborTextStringWithIndefiniteLength = (byte) 0x7f;
        byte CborArrayWithLengthZero = (byte) 0x80;
        byte CborArrayWith1ByteLength = (byte) 0x98;
        byte CborArrayWith2BytesLength = (byte) 0x99;
        byte CborArrayWith4BytesLength = (byte) 0x9a;
        byte CborArrayWith8BytesLength = (byte) 0x9b;
        byte CborArrayWithIndefiniteLength = (byte) 0x9f;
        byte CborMapWithLengthZero = (byte) 0xa0;
        byte CborMapWith1ByteLength = (byte) 0xb8;
        byte CborMapWith2BytesLength = (byte) 0xb9;
        byte CborMapWith4BytesLength = (byte) 0xba;
        byte CborMapWith8BytesLength = (byte) 0xbb;
        byte CborMapWithIndefiniteLength = (byte) 0xbf;
        byte CborTextBasedDate = (byte) 0xc0;
        byte CborEpochBasedDate = (byte) 0xc1;
        byte CborPositiveBignum = (byte) 0xc2;
        byte CborNegativeBignum = (byte) 0xc3;
        byte CborDecimalFraction = (byte) 0xc4;
        byte CborBigFloat = (byte) 0xc5;
        byte CborStartRangeTaggedItem = (byte) 0xc6;
        byte CborEndRangeTaggedItem = (byte) 0xd4;
        byte CborExpectedBase16Conversion = (byte) 0xd5;
        byte CborExpectedBase32Conversion = (byte) 0xd6;
        byte CborExpectedBase64Conversion = (byte) 0xd7;
        byte CborTagWith1ByteLength = (byte) 0xd8;
        byte CborTagWith2BytesLength = (byte) 0xd9;
        byte CborTagWith4BytesLength = (byte) 0xda;
        byte CborTagWith8BytesLength = (byte) 0xdb;
        byte CborStartSimpleValueRange = (byte) 0xe0;
        byte CborEndSimpleValueRange = (byte) 0xf3;
        byte CborBooleanFalse = (byte) 0xf4;
        byte CborBooleanTrue = (byte) 0xf5;
        byte CborNull = (byte) 0xf6;
        byte CborUndefined = (byte) 0xf7;
        byte CborSimpleValue1ByteFollow = (byte) 0xf8;
        byte CborHalfPrecisionFloat = (byte) 0xf9;
        byte CborSinglePrecisionFloat = (byte) 0xfa;
        byte CborDoublePrecisionFloat = (byte) 0xfb;
        byte CborBreak = (byte) 0xff;
    }
}