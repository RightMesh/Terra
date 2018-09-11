package io.left.rightmesh.libdtn.data.bundleV7.cbor;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libdtn.utils.rxparser.BufferState;
import io.left.rightmesh.libdtn.utils.rxparser.ByteState;
import io.left.rightmesh.libdtn.utils.rxparser.IntegerState;
import io.left.rightmesh.libdtn.utils.rxparser.LongState;
import io.left.rightmesh.libdtn.utils.rxparser.ObjectState;
import io.left.rightmesh.libdtn.utils.rxparser.ParserState;
import io.left.rightmesh.libdtn.utils.rxparser.RxParserException;
import io.left.rightmesh.libdtn.utils.rxparser.ShortState;

import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborAdditionalInfo.IndefiniteLength;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborAdditionalInfo.Value16Bit;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborAdditionalInfo.Value32Bit;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborAdditionalInfo.Value64Bit;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborAdditionalInfo.Value8Bit;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborInternals.MajorTypeMask;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborInternals.MajorTypeShift;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborInternals.SmallValueMask;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborBooleanFalse;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborBooleanTrue;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborBreak;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborDoublePrecisionFloat;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborHalfPrecisionFloat;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborNull;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborSimpleValue1ByteFollow;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborSinglePrecisionFloat;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborJumpTable.CborUndefined;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.ArrayType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.ByteStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.MapType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.NegativeIntegerType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.SimpleTypesType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.TagType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.TextStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborMajorTypes.UnsignedIntegerType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborArrayType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborBooleanType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborBreakType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborByteStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborDoubleType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborIntegerType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborMapType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborNullType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborTagType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborTextStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborUndefinedType;

public class Decoder {

    /**
     * A decoded Cbor data item.
     */
    public static class CborItem {
        int cborType;
        Object obj;

        CborItem(int cborType, Object obj) {
            this.cborType = cborType;
            this.obj = obj;
        }
    }

    /**
     * ParseGeneric parse one data item. If the data item is a container, it will recursively
     * parse the data until break. This is an unsafe method as it can read unlimited data and
     * gets outofmemory.
     */
    public abstract static class ParseGeneric extends ObjectState<CborItem> {

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = next.get();
            int mt = ((b & MajorTypeMask) >>> MajorTypeShift);

            if ((mt == UnsignedIntegerType) || (mt == NegativeIntegerType)) {
                parse_integer.setFirst(b);
                return parse_integer;
            }
            if (mt == ByteStringType) {
                parse_byte_string.setFirst(b);
                return parse_byte_string;
            }
            if (mt == TextStringType) {
                parse_text_string.setFirst(b);
                return parse_text_string;
            }
            if (mt == ArrayType) {
                parse_collection.setFirst(b);
                return parse_collection;
            }
            if (mt == MapType) {
                parse_map.setFirst(b);
                return parse_map;
            }
            if (mt == TagType) {
                parse_tag.setFirst(b);
                return parse_tag;
            }
            if (mt == SimpleTypesType) {
                // Using CborJumpTable
                if ((b == CborHalfPrecisionFloat) || (b == CborSinglePrecisionFloat) || (b == CborDoublePrecisionFloat)) {
                    parse_float.setFirst(b);
                    return parse_float;
                }
                if (b == CborBooleanTrue) {
                    return ParseGeneric.this.onSuccess(new CborItem(CborBooleanType, true));
                }
                if (b == CborBooleanFalse) {
                    return ParseGeneric.this.onSuccess(new CborItem(CborBooleanType, false));
                }
                if (b == CborNull) {
                    return ParseGeneric.this.onSuccess(new CborItem(CborNullType, null));
                }
                if (b == CborUndefined) {
                    return ParseGeneric.this.onSuccess(new CborItem(CborUndefinedType, null));
                }
                if (b == CborBreak) {
                    return ParseGeneric.this.onSuccess(new CborItem(CborBreakType, null));
                }
                if (b == CborSimpleValue1ByteFollow) {
                    // ignoring this value and next value
                    return ignore_next_byte;
                } else {
                    // ignoring this value
                    return this;
                }
            }
            throw new RxParserException("Unknown generic: " + mt);
        }

        ParseInteger parse_integer = new ParseInteger() {
            @Override
            public ParserState onSuccess(Long obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborIntegerType, obj));
            }
        };

        ParseFloat parse_float = new ParseFloat() {
            @Override
            public ParserState onSuccess(Double obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborDoubleType, obj));
            }
        };

        ParseByteStringUnsafe parse_byte_string = new ParseByteStringUnsafe() {
            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborByteStringType, obj));
            }
        };

        ParseTextStringUnsafe parse_text_string = new ParseTextStringUnsafe() {
            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborTextStringType, obj));
            }
        };

        ParseCollection parse_collection = new ParseCollection() {
            @Override
            public ParserState onSuccess(Collection obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborArrayType, obj));
            }
        };

        ParseMap parse_map = new ParseMap() {
            @Override
            public ParserState onSuccess(Map obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborMapType, obj));
            }
        };

        ParseTag parse_tag = new ParseTag() {
            @Override
            public ParserState onSuccess(Long obj) throws RxParserException {
                return ParseGeneric.this.onSuccess(new CborItem(CborTagType, obj));
            }
        };

        ParserState ignore_next_byte = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) {
                byte b = next.get();
                return ParseGeneric.this;
            }
        };

    }

    /**
     * ParseIntegers parse one negative/positive byte/short/int/long item.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseInteger extends PeekFirst<Long> {

        int mt;

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            mt = ((b & MajorTypeMask) >>> MajorTypeShift);
            if ((mt == UnsignedIntegerType) || (mt == NegativeIntegerType)) {
                extractInteger.first = b;
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                if(l < 0) {
                    throw new RxParserException("UInt64 outside of range operation");
                }
                return ParseInteger.this.onSuccess(l ^ mt);
            }
        };
    }

    /**
     * ParseFloat parse one half/single/double precision float.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseFloat extends PeekFirst<Double> {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            if (b == CborHalfPrecisionFloat) {
                return getUInt16;
            }
            if (b == CborSinglePrecisionFloat) {
                return getUInt32;
            }
            if (b == CborDoublePrecisionFloat) {
                return getUInt64;
            }
            throw new RxParserException("Unexpected Cbor type: " + b);
        }

        ShortState getUInt16 = new ShortState() {
            @Override
            public ParserState onSuccess(Short s) throws RxParserException {
                int exp = (s >> 10) & 0x1f;
                int mant = s & 0x3ff;

                double val;
                if (exp == 0) {
                    val = mant * Math.pow(2, -24);
                } else if (exp != 31) {
                    val = (mant + 1024) * Math.pow(2, exp - 25);
                } else if (mant != 0) {
                    val = Double.NaN;
                } else {
                    val = Double.POSITIVE_INFINITY;
                }

                return ParseFloat.this.onSuccess(((s & 0x8000) == 0) ? val : -val);
            }
        };

        IntegerState getUInt32 = new IntegerState() {
            @Override
            public ParserState onSuccess(Integer i) throws RxParserException {
                return ParseFloat.this.onSuccess((double) Float.intBitsToFloat(i));
            }
        };

        LongState getUInt64 = new LongState() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                return ParseFloat.this.onSuccess(Double.longBitsToDouble(l));
            }
        };
    }

    /**
     * ParseBoolean parse a true/false boolean.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseBoolean extends PeekFirst<Boolean> {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            if (b == CborBooleanFalse) {
                return onSuccess(false);
            }
            if (b == CborBooleanTrue) {
                return onSuccess(true);
            }
            throw new RxParserException("Non boolean type: " + b);
        }
    }

    /**
     * ParseTag parse a single 1/2/4/8 bytes tag.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseTag extends PeekFirst<Long> {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            int mt = ((b & MajorTypeMask) >>> MajorTypeShift);
            if (mt == TagType) {
                extractInteger.setFirst(b);
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                if(l < 0) {
                    throw new RxParserException("UInt64 outside of range operation");
                }
                return ParseTag.this.onSuccess(l);
            }
        };
    }


    /**
     * ParseTextString parses a text String in a safely manner. This however decoder doesn't check
     * if the String truly is a UTF-8 sequence.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseTextString extends ParseString {
        ParseTextString() {
            super(TextStringType);
        }
    }

    /**
     * ParseTextString parses a byte String in a safely manner.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseByteString extends ParseString {
        ParseByteString() {
            super(ByteStringType);
        }
    }

    /**
     * ParseTextString parses a text String in an unsafe manner as it doesn't check the size of
     * the string and returns a single ByteBuffer onSuccess, if the string is very long, it may
     * trigger a OutOfMemory error.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseTextStringUnsafe extends ParseStringUnsafe {
        ParseTextStringUnsafe() {
            super(TextStringType);
        }
    }

    /**
     * ParseTextString parses a byte String in an unsafe manner as it doesn't check the size of
     * the string and returns a single ByteBuffer onSuccess, if the string is very long, it may
     * trigger a OutOfMemory error.
     * It throws an exception if the data is not of of expected types.
     */
    public abstract static class ParseByteStringUnsafe extends ParseStringUnsafe {
        ParseByteStringUnsafe() {
            super(ByteStringType);
        }
    }

    /**
     * ParseCollection parses an array of CborItem. Note that it doesn't check the size of the array
     * and can create an OutOfMemory so it must only be used with trusted source. For a more
     * careful parsing, first check the size with {@see CheckTextStringLength}
     * and {@see ExtractTextString}. It throws an exception if the data is not of of expected types
     * or if an unexpected break is found.
     */
    public abstract static class ParseCollection extends PeekFirst<Collection<CborItem>> {

        long size;
        private Collection<CborItem> c;

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            int mt = ((b & MajorTypeMask) >>> MajorTypeShift);
            if (mt == ArrayType) {
                extractInteger.setFirst(b);
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                size = l;
                c = new LinkedList<>();
                if (size == 0) {
                    return ParseCollection.this.onSuccess(c);
                } else {
                    return extractItems;
                }
            }
        };

        ParseGeneric extractItems = new ParseGeneric() {
            @Override
            public ParserState onSuccess(CborItem i) throws RxParserException {
                if ((size < 0) && (i.cborType == CborBreakType)) {
                    return ParseCollection.this.onSuccess(c);
                }
                if ((size > 0) && (i.cborType == CborBreakType)) {
                    throw new RxParserException("Unexpected break");
                }
                c.add(i);
                if (--size == 0) {
                    return ParseCollection.this.onSuccess(c);
                }
                return this;
            }
        };

        @Override
        public void onExit() throws RxParserException {
            c = null;
        }
    }

    /**
     * ParseMap parses a map of CborItem. Note that this decoder doesn't check for key duplicate
     * or if keys are of different type. It doesn't check the size of the map
     * and can create an OutOfMemory so it must only be used with trusted source. For a more
     * careful parsing, first check the size with {@see CheckMapLength}
     * and {@see ExtractMap}. It throws an exception if the data is not of of expected types
     * or if an unexpected break is found or if a break is found while a key is left orphan.
     */
    public abstract static class ParseMap extends PeekFirst<Map> {

        long size;
        private Map<CborItem, CborItem> m;
        private CborItem current_key = null;

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            int mt = ((b & MajorTypeMask) >>> MajorTypeShift);
            if (mt == MapType) {
                extractInteger.setFirst(b);
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                size = l;
                m = new HashMap<>();
                if (size == 0) {
                    return ParseMap.this.onSuccess(m);
                } else {
                    return extractItems;
                }
            }
        };

        ParseGeneric extractItems = new ParseGeneric() {
            @Override
            public ParserState onSuccess(CborItem i) throws RxParserException {
                if ((size < 0) && (i.cborType == CborBreakType)) {
                    if (current_key != null) {
                        throw new RxParserException("Unexpected break");
                    } else {
                        return ParseMap.this.onSuccess(m);
                    }
                }
                if ((size > 0) && (i.cborType == CborBreakType)) {
                    throw new RxParserException("Unexpected break");
                }
                if (current_key == null) {
                    current_key = i;
                } else {
                    m.put(current_key, i);
                    current_key = null;
                }
                if (--size == 0) {
                    return ParseMap.this.onSuccess(m);
                }
                return this;
            }
        };

        @Override
        public void onExit() throws RxParserException {
            m = null;
            current_key = null;
        }
    }

    private abstract static class ExtractInteger extends PeekFirst<Long> {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            int adv = (b & SmallValueMask);
            if (adv < Value8Bit) {
                return onSuccess((long) adv);
            }
            if (adv == Value8Bit) {
                return getUInt8;
            }
            if (adv == Value16Bit) {
                return getUInt16;
            }
            if (adv == Value32Bit) {
                return getUInt32;
            }
            if (adv == Value64Bit) {
                return getUInt64;
            }
            if (adv == IndefiniteLength) {
                // indefinite
                return ExtractInteger.this.onSuccess(-1L);
            }
            throw new RxParserException("Wrong additional value: "+adv);
        }

        ByteState getUInt8 = new ByteState() {
            @Override
            public ParserState onSuccess(Byte b) throws RxParserException {
                return ExtractInteger.this.onSuccess((long) (b & 0xff));
            }
        };

        ShortState getUInt16 = new ShortState() {
            @Override
            public ParserState onSuccess(Short s) throws RxParserException {
                return ExtractInteger.this.onSuccess((long) (s & 0xffff));
            }
        };

        IntegerState getUInt32 = new IntegerState() {
            @Override
            public ParserState onSuccess(Integer i) throws RxParserException {
                return ExtractInteger.this.onSuccess((i & 0xffffffffL));
            }
        };

        LongState getUInt64 = new LongState() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                return ExtractInteger.this.onSuccess(l);
            }
        };
    }


    private abstract static class ParseString extends PeekFirst<ByteBuffer> {

        int expectedType;
        int bytesExpected;
        int max_chunk_size = 2048;

        /**
         * Set maximum chunk size.
         *
         * @param chunk_size
         */
        public void setMaxChunkSize(int chunk_size) {
            this.max_chunk_size = chunk_size;
        }

        ParseString(int expectedType) {
            this.expectedType = expectedType;
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = firstByteSet ? first : next.get();
            int mt = ((b & MajorTypeMask) >>> MajorTypeShift);
            if (mt == expectedType) {
                extractStringSize.setFirst(b);
                return extractStringSize;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractStringSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                bytesExpected = l.intValue();
                if (l == 0) {
                    return ParseString.this.onSuccess(ByteBuffer.allocate(0));
                }
                if (l > 0) {
                    extractDefiniteLengthString.realloc(Math.min(bytesExpected, max_chunk_size));
                    return extractDefiniteLengthString;
                } else {
                    // a negative integer means indefinite size
                    return checkBreak;
                }
            }
        };

        BufferState extractDefiniteLengthString = new BufferState() {
            @Override
            public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
                bytesExpected -= buffer.remaining();
                if (bytesExpected == 0) {
                    return ParseString.this.onSuccess(buffer);
                } else {
                    ParseString.this.onNextChunk(buffer);
                    realloc(Math.min(bytesExpected, max_chunk_size));
                    return this;
                }
            }
        };

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = firstByteSet ? first : next.get();
                if (b == CborBreak) {
                    return ParseString.this.onSuccess(ByteBuffer.allocate(0));
                } else {
                    extractChunkSize.setFirst(b);
                    return extractChunkSize;
                }
            }
        };

        ExtractInteger extractChunkSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                bytesExpected = l.intValue();
                if (l == 0) {
                    ParseString.this.onNextChunk(ByteBuffer.allocate(0));
                    return checkBreak;
                }
                if (l > 0) {
                    extractChunk.realloc(Math.min(bytesExpected, max_chunk_size));
                    return extractChunk;
                }
                throw new RxParserException("Byte string chunk must be definite-length");
            }
        };

        BufferState extractChunk = new BufferState() {
            @Override
            public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
                bytesExpected -= buffer.remaining();
                ParseString.this.onNextChunk(buffer);
                if (bytesExpected == 0) {
                    return checkBreak;
                } else {
                    realloc(Math.min(bytesExpected, max_chunk_size));
                    return this;
                }
            }
        };

        public abstract void onNextChunk(ByteBuffer buffer);
    }

    public abstract static class ParseStringUnsafe extends ParseString {
        ByteArrayDataOutput output;

        ParseStringUnsafe(int expectedType) {
            super(expectedType);
        }

        @Override
        public void onEnter() throws RxParserException {
            output = ByteStreams.newDataOutput();
        }

        @Override
        public void onNextChunk(ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                output.write(buffer.get());
            }
        }

        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            while (buffer.hasRemaining()) {
                output.write(buffer.get());
            }
            return onSuccessUnsafe(ByteBuffer.wrap(output.toByteArray()));
        }

        public abstract ParserState onSuccessUnsafe(ByteBuffer buffer) throws RxParserException;
    }

    private static abstract class PeekFirst<T> extends ObjectState<T> {

        boolean firstByteSet = false;
        byte first;

        public void setFirst(byte first) {
            this.first = first;
            firstByteSet = true;
        }

        @Override
        public void onExit() throws RxParserException {
            firstByteSet = false;
        }
    }
}