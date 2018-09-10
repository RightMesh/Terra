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

    interface CborType {
        int CborIntegerType = 0x00;
        int CborByteStringType = 0x40;
        int CborTextStringType = 0x60;
        int CborArrayType = 0x80;
        int CborMapType = 0xa0;
        int CborTagType = 0xc0;
        int CborSimpleType = 0xe0;
        int CborBooleanType = 0xf5;
        int CborNullType = 0xf6;
        int CborUndefinedType = 0xf7;
        int CborHalfFloatType = 0xf9;
        int CborFloatType = 0xfa;
        int CborDoubleType = 0xfb;
        int CborInvalidType = 0xff;
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
}