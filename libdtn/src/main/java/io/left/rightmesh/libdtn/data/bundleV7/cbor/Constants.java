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
        int CborTextBasedDateTag = 0;
        int CborEpochBasedDateTag = 1;
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
        int CborUInt8Zero = 0x00;
        int CborUInt8Type = 0x18;
        int CborUInt16Type = 0x19;
        int CborUInt32Type = 0x1a;
        int CborUInt64Type = 0x1b;
        int CborNegativeOne = 0x20;
        int CborNegativeInt8Type = 0x38;
        int CborNegativeInt16Type = 0x39;
        int CboroNegativeInt32Type = 0x3a;
        int CborNegativeInt64Type = 0x3b;
        int CborByteStringWithLengthZero = 0x40;
        int CborByteStringWith1ByteLength = 0x58;
        int CborByteStringWith2BytesLength = 0x59;
        int CborByteStringWith4BytesLength = 0x5a;
        int CborByteStringWith8BytesLength = 0x5b;
        int CborByteStringWithIndefiniteLength = 0x5f;
        int CborTextStringWithLengthZero = 0x60;
        int CborTextStringWith1ByteLength = 0x78;
        int CborTextStringWith2BytesLength = 0x79;
        int CborTextStringWith4BytesLength = 0x7a;
        int CborTextStringWith8BytesLength = 0x7b;
        int CborTextStringWithIndefiniteLength = 0x7f;
        int CborArrayWithLengthZero = 0x80;
        int CborArrayWith1ByteLength = 0x98;
        int CborArrayWith2BytesLength = 0x99;
        int CborArrayWith4BytesLength = 0x9a;
        int CborArrayWith8BytesLength = 0x9b;
        int CborArrayWithIndefiniteLength = 0x9f;
        int CborMapWithLengthZero = 0xa0;
        int CborMapWith1ByteLength = 0xb8;
        int CborMapWith2BytesLength = 0xb9;
        int CborMapWith4BytesLength = 0xba;
        int CborMapWith8BytesLength = 0xbb;
        int CborMapWithIndefiniteLength = 0xbf;
        int CborTextBasedDate = 0xc0;
        int CborEpochBasedDate = 0xc1;
        int CborPositiveBignum = 0xc2;
        int CborNegativeBignum = 0xc3;
        int CborDecimalFraction = 0xc4;
        int CborBigFloat = 0xc5;
        int CborStartRangeTaggedItem = 0xc6;
        int CborEndRangeTaggedItem = 0xd4;
        int CborTagExpectedBase16Conversion = 0xd5;
        int CborTagExpectedBase32Conversion = 0xd6;
        int CborTagExpectedBase64Conversion = 0xd7;
        int CborTagWith1ByteLength = 0xd8;
        int CborTagWith2BytesLength = 0xd9;
        int CborTagWith4BytesLength = 0xda;
        int CborTagWith8BytesLength = 0xdb;
        int CborStartSimpleValueRange = 0xe0;
        int CborEndSimpleValueRange = 0xf3;
        int CborBooleanFalse = 0xf4;
        int CborBooleanTrue = 0xf5;
        int CborNull = 0xf6;
        int CborUndefined = 0xf7;
        int CborSimpleValue1ByteFollow = 0xf8;
        int CborHalfPrecisionFloat = 0xf9;
        int CborSinglePrecisionFloat = 0xfa;
        int CborDoublePrecisionFloat = 0xfb;
        int CborBreak = 0xff;
    }
}