package io.left.rightmesh.libdtn.data.bundleV7.cbor;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.DataItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.IntegerItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.TagItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.NullItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.UndefinedItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.BreakItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.FloatingPointItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.ByteStringItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.TextStringItem;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.CBOR.BooleanItem;

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
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborSimpleValues.Break;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborSimpleValues.NullValue;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborSimpleValues.UndefinedValue;
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

public class CborParser {


    Queue<ParserState> parserQueue = new LinkedList<>();
    ParserState state = null;
    ParserState next = null;

    /**
     * Callback for container (Array and Hash)
     *
     * @param <T> chunk (either ByteBuffer or String)
     */
    public interface ContainerIsOpenCallback<T> {
        void onContainerIsOpen(long s);
    }

    public interface ParseableItem {
        CborParser getItemParser();
    }

    public interface LinearContainerItemFactory<T extends ParseableItem> {
        T createItem();
    }

    public interface ItemCallback<T> {
        void onItemParsed(T item);
    }

    /**
     * Callback for container (Array and Hash)
     *
     * @param <T> chunk (either ByteBuffer or String)
     */
    public interface ContainerIsCloseCallback<T> {
        void onContainerIsClose();
    }

    /**
     * Callback for chunked data (byte string and text string)
     *
     * @param <T> chunk (either ByteBuffer or String)
     */
    public interface ChunkCallback<T> {
        void onChunk(T obj);
    }

    /**
     * Callback for when a tag is found
     */
    public interface TagCallback {
        void onTag(long tag);
    }

    /**
     * Callback for parsed item
     *
     * @param <T> parsed item
     */
    public interface ParsedCallback<T> {
        void onParsed(T obj);
    }

    /**
     * Callback for parsed item
     *
     * @param <T> parsed item
     */
    public interface ParsingDoneCallback<T> {
        void parsingDone();
    }

    /**
     * Try to consume the data. returns true if the schema is entirely parsed.
     *
     * @param buffer
     * @return
     * @throws RxParserException
     */
    public boolean read(ByteBuffer buffer) throws RxParserException {
        if (parserQueue.size() == 0) {
            return true;
        }
        if (!buffer.hasRemaining()) {
            return false;
        }

        while (buffer.hasRemaining()) {
            if (state == null) {
                if (parserQueue.size() == 0) {
                    return true;
                } else {
                    state = parserQueue.poll();
                    state.onEnter();
                }
            }
            next = state.onNext(buffer);
            if (next != state) {
                state.onExit();
                state = next;
            }
        }

        if (parserQueue.size() == 0) {
            return true;
        }
        return false;
    }

    public CborParser cbor_parse_break() {
        return cbor_parse_break(null, null);
    }

    public CborParser cbor_parse_break(ParsingDoneCallback<Long> cb, TagCallback tb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onBreak() throws RxParserException {
                if (cb != null) {
                    cb.parsingDone();
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_undefined() {
        return cbor_parse_undefined(null, null);
    }

    public CborParser cbor_parse_undefined(ParsingDoneCallback<Long> cb, TagCallback tb) {
        parserQueue.add(new CborParseUndefined() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onUndefined() throws RxParserException {
                if (cb != null) {
                    cb.parsingDone();
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_null() {
        return cbor_parse_null(null, null);
    }

    public CborParser cbor_parse_null(ParsingDoneCallback<Long> cb, TagCallback tb) {
        parserQueue.add(new CborParseNull() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onNull() throws RxParserException {
                if (cb != null) {
                    cb.parsingDone();
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_int(ParsedCallback<Long> cb) {
        return cbor_parse_int(cb, null);
    }

    public CborParser cbor_parse_int(ParsedCallback<Long> cb, TagCallback tb) {
        parserQueue.add(new CborParseInteger() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccess(long l) {
                cb.onParsed(l);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_float(ParsedCallback<Double> cb) {
        return cbor_parse_float(cb, null);
    }

    public CborParser cbor_parse_float(ParsedCallback<Double> cb, TagCallback tb) {
        parserQueue.add(new CborParseFloat() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccess(Double obj) {
                cb.onParsed(obj);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_boolean(ParsedCallback<Boolean> cb) {
        return cbor_parse_boolean(cb, null);
    }

    public CborParser cbor_parse_boolean(ParsedCallback<Boolean> cb, TagCallback tb) {
        parserQueue.add(new CborParseBoolean() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccess(boolean b) {
                cb.onParsed(b);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb1) {
        return cbor_parse_byte_string(cb1, null, null);
    }

    public CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb1,
                                             ParsingDoneCallback cb2) {
        return cbor_parse_byte_string(cb1, cb2, null);
    }

    public CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb1,
                                             ParsingDoneCallback cb2,
                                             TagCallback tb) {
        parserQueue.add(new CborParseByteString() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) {
                cb1.onChunk(next);
            }

            @Override
            public ParserState onSuccess() {
                if (cb2 != null) {
                    cb2.parsingDone();
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_byte_string_unsafe(ParsedCallback<ByteBuffer> cb) {
        return cbor_parse_byte_string_unsafe(cb, null);
    }

    public CborParser cbor_parse_byte_string_unsafe(ParsedCallback<ByteBuffer> cb2,
                                                    TagCallback tb) {
        parserQueue.add(new CborParseByteStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) {
                cb2.onParsed(obj);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_text_string(ChunkCallback<String> cb1) {
        return cbor_parse_text_string(cb1, null, null);
    }

    public CborParser cbor_parse_text_string(ChunkCallback<String> cb1,
                                             ParsingDoneCallback cb2) {
        return cbor_parse_text_string(cb1, cb2, null);
    }

    public CborParser cbor_parse_text_string(ChunkCallback<String> cb1,
                                             ParsingDoneCallback cb2,
                                             TagCallback tb) {
        parserQueue.add(new CborParseTextString() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) {
                cb1.onChunk(StandardCharsets.UTF_8.decode(next).toString());
            }

            @Override
            public ParserState onSuccess() {
                if (cb2 != null) {
                    cb2.parsingDone();
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_text_string_unsafe(ParsedCallback<String> cb) {
        return cbor_parse_text_string_unsafe(cb, null);
    }

    public CborParser cbor_parse_text_string_unsafe(ParsedCallback<String> cb,
                                                    TagCallback tb) {
        parserQueue.add(new CborParseTextStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer next) {
                cb.onParsed(StandardCharsets.UTF_8.decode(next).toString());
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_tag(ParsedCallback<Long> cb) {
        parserQueue.add(new CborParseTag() {
            @Override
            public ParserState onSuccess(long tag) {
                cb.onParsed(tag);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_open_map(ContainerIsOpenCallback cb) {
        return cbor_open_map(cb, null);
    }

    public CborParser cbor_open_map(ContainerIsOpenCallback cb,
                                    TagCallback tb) {
        return cbor_open_container(cb, tb, MapType);
    }

    public CborParser cbor_close_map() {
        return cbor_close_container(null);
    }

    public CborParser cbor_close_map(ParsingDoneCallback cb) {
        return cbor_close_container(cb);
    }

    public CborParser cbor_open_array(ContainerIsOpenCallback cb) {
        return cbor_open_container(cb, null, ArrayType);
    }

    public CborParser cbor_open_array(ContainerIsOpenCallback cb,
                                      TagCallback tb) {
        return cbor_open_container(cb, tb, ArrayType);
    }

    public CborParser cbor_close_array() {
        return cbor_close_container(null);
    }

    public CborParser cbor_close_array(ParsingDoneCallback cb) {
        return cbor_close_container(cb);
    }

    public CborParser cbor_open_container(ContainerIsOpenCallback cb,
                                          TagCallback tb,
                                          int majorType) {
        parserQueue.add(new ExtractContainerSize(majorType) {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onContainerOpen(long size) {
                if (cb != null) {
                    cb.onContainerIsOpen(size);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_close_container(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                // do nothing but it is probably an error
            }

            @Override
            public ParserState onBreak() {
                if (cb != null) {
                    cb.parsingDone();
                }
                return null;
            }
        });
        return this;
    }


    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            LinearContainerItemFactory<T> factory,
            ItemCallback<T> cb) {
        return cbor_parse_linear_array(factory, null, null, cb, null);
    }

    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            LinearContainerItemFactory<T> factory,
            TagCallback tb,
            ContainerIsOpenCallback cb1,
            ItemCallback<T> cb2,
            ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseLinearArray<T>(factory) {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public void onArrayIsOpen(long size) {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(size);
                }
            }

            @Override
            public void onArrayItem(T item) {
                if (cb2 != null) {
                    cb2.onItemParsed(item);
                }
            }

            @Override
            public ParserState onArrayIsClose() {
                if (cb3 != null) {
                    cb3.onContainerIsClose();
                }
                return null;
            }
        });
        return this;
    }

    private abstract static class CborParseBreak extends CborParseSimpleValue {
        @Override
        public ParserState onSimplevalue(int value) throws RxParserException {
            if (value != Break) {
                throw new RxParserException("Not a Break Value");
            } else {
                return onBreak();
            }
        }

        public abstract ParserState onBreak() throws RxParserException;
    }


    private abstract static class CborParseNull extends CborParseSimpleValue {
        @Override
        public ParserState onSimplevalue(int value) throws RxParserException {
            if (value != NullValue) {
                throw new RxParserException("Not a Null Value");
            } else {
                return onNull();
            }
        }

        public abstract ParserState onNull() throws RxParserException;
    }


    private abstract static class CborParseUndefined extends CborParseSimpleValue {
        @Override
        public ParserState onSimplevalue(int value) throws RxParserException {
            if (value != UndefinedValue) {
                throw new RxParserException("Not an Undefined Value");
            } else {
                return onUndefined();
            }
        }

        public abstract ParserState onUndefined() throws RxParserException;
    }

    private abstract static class CborParseSimpleValue extends ExtractTagItem {
        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if (majorType != SimpleTypesType) {
                throw new RxParserException("Unexpected major type: " + majorType);
            }
            if ((b & 0xff) == CborSimpleValue1ByteFollow) {
                return extractNextByte;
            }
            return onSimplevalue(b & SmallValueMask);
        }

        ParserState extractNextByte = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                return onSimplevalue(next.get());
            }
        };

        public abstract ParserState onSimplevalue(int value) throws RxParserException;
    }

    private abstract static class CborParseLinearArray<T extends ParseableItem> extends ExtractContainerSize {

        long size;
        LinearContainerItemFactory<T> factory;

        CborParseLinearArray(LinearContainerItemFactory<T> factory) {
            super(ArrayType);
            this.factory = factory;
        }

        @Override
        public ParserState onContainerOpen(long size) throws RxParserException {
            this.size = size;
            onArrayIsOpen(size);
            if (size == 0) {
                return onArrayIsClose();
            }
            if (size > 0) {
                return extractOneItem;
            } else {
                // a negative integer means indefinite size
                return checkBreak;
            }
        }

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = peek(next);
                if ((b & 0xff) == CborBreak) {
                    return onArrayIsClose();
                } else {
                    return extractOneItem;
                }
            }
        };

        ParserState extractOneItem = new ParserState() {
            T item;
            CborParser parser;

            @Override
            public void onEnter() {
                System.out.println("coucou");
                item = factory.createItem();
                parser = item.getItemParser();
            }

            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (parser.read(next)) {
                    return this;
                } else {
                    onArrayItem(item);
                    size--;
                    if (size < 0) {
                        return checkBreak;
                    }
                    if (size == 0) {
                        return onArrayIsClose();
                    } else {
                        this.onEnter();
                        return this;
                    }
                }
            }
        };

        public abstract void onArrayIsOpen(long l);

        public abstract void onArrayItem(T item);

        public abstract ParserState onArrayIsClose();

    }

    /*
    public CborParser cbor_parse_generic_array_unsafe(ParsedCallback<Collection<CborGenericItem>> cb) {
        return cbor_parse_array_unsafe(cb, null);
    }

    public CborParser cbor_parse_generic_array_unsafe(ParsedCallback<Collection<CborGenericItem>> cb,
                                       TagCallback tb) {
        parserQueue.add(new CborParseCollectionUnsafe() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccess(Collection<CborGenericItem> obj) {
                cb.onParsed(obj);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_generic_map_unsafe(ParsedCallback<Map<CborGenericItem, CborGenericItem>> cb) {
        return cbor_parse_map_unsafe(cb, null);
    }

    public CborParser cbor_parse_generic_map_unsafe(ParsedCallback<Map<CborGenericItem, CborGenericItem>> cb,
                                     TagCallback tb) {
        parserQueue.add(new CborParseMapUnsafe() {
            @Override
            public void onTagFound(long tag) {
                if (tb != null) {
                    tb.onTag(tag);
                }
            }

            @Override
            public ParserState onSuccess(Map<CborGenericItem, CborGenericItem> obj) {
                cb.onParsed(obj);
                return null;
            }
        });
        return this;
    }

    */

    /**
     * ParseGeneric parse one data item. If the data item is a container, it will recursively
     * parse the data until break. This is an unsafe method as it can read unlimited data and
     * gets outofmemory.
     */
    public abstract static class CborParseGeneric extends ObjectState<DataItem> {

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = peek(next);
            int mt = (((b & MajorTypeMask) & 0xff) >>> MajorTypeShift);

            if ((mt == UnsignedIntegerType) || (mt == NegativeIntegerType)) {
                return parse_integer;
            }
            if (mt == ByteStringType) {
                return parse_byte_string;
            }
            if (mt == TextStringType) {
                return parse_text_string;
            }
            /*
            if (mt == ArrayType) {
                return parse_collection;
            }
            if (mt == MapType) {
                return parse_map;
            }
            */
            if (mt == TagType) {
                return parse_tag;
            }
            if (mt == SimpleTypesType) {
                // Using CborJumpTable
                if (((b & 0xff) == CborHalfPrecisionFloat) || ((b & 0xff) == CborSinglePrecisionFloat) || ((b & 0xff) == CborDoublePrecisionFloat)) {
                    return parse_float;
                }
                if ((b & 0xff) == CborBooleanTrue) {
                    return CborParseGeneric.this.onSuccess(new BooleanItem(true));
                }
                if ((b & 0xff) == CborBooleanFalse) {
                    return CborParseGeneric.this.onSuccess(new BooleanItem(false));
                }
                if ((b & 0xff) == CborNull) {
                    return CborParseGeneric.this.onSuccess(new NullItem());
                }
                if ((b & 0xff) == CborUndefined) {
                    return CborParseGeneric.this.onSuccess(new UndefinedItem());
                }
                if ((b & 0xff) == CborBreak) {
                    return CborParseGeneric.this.onSuccess(new BreakItem());
                }
                if ((b & 0xff) == CborSimpleValue1ByteFollow) {
                    // ignoring this value and next value
                    return ignore_next_byte;
                } else {
                    // ignoring this value
                    return this;
                }
            }
            throw new RxParserException("Unknown generic: " + mt);
        }

        CborParseInteger parse_integer = new CborParseInteger() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(long obj) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new IntegerItem(obj));
            }
        };

        CborParseFloat parse_float = new CborParseFloat() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(Double obj) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new FloatingPointItem(obj));
            }
        };

        CborParseByteStringUnsafe parse_byte_string = new CborParseByteStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new ByteStringItem(obj));
            }
        };

        CborParseTextStringUnsafe parse_text_string = new CborParseTextStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                String str = StandardCharsets.UTF_8.decode(obj).toString();
                return CborParseGeneric.this.onSuccess(new TextStringItem(str));
            }
        };

        /*
        CborParseCollectionUnsafe parse_collection = new CborParseCollectionUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(Collection obj) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new CborGenericItem(CborArrayType, obj));
            }
        };

        CborParseMapUnsafe parse_map = new CborParseMapUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(Map m) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new CborGenericItem(CborMapType, m));
            }
        };
        */

        CborParseTag parse_tag = new CborParseTag() {
            @Override
            public ParserState onSuccess(long tag) throws RxParserException {
                return CborParseGeneric.this.onSuccess(new TagItem(tag));
            }
        };

        ParserState ignore_next_byte = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) {
                byte b = next.get();
                return CborParseGeneric.this;
            }
        };
    }

    /**
     * ParseIntegers parse one negative/positive byte/short/int/long item.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseInteger extends ExtractTagItem {

        int mt;

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if ((majorType == UnsignedIntegerType) || (majorType == NegativeIntegerType)) {
                this.mt = majorType;
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long l) throws RxParserException {
                if (l < 0) {
                    throw new RxParserException("The extracted integer should be absolute");
                }
                return CborParseInteger.this.onSuccess(l ^ -mt);
            }
        };

        public abstract ParserState onSuccess(long d) throws RxParserException;
    }

    /**
     * ParseFloat parse one half/single/double precision float.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseFloat extends ExtractTagItem {

        @Override
        public ParserState onItemFound(int majorType, byte b) {
            return extractFloatType;
        }

        ParserState extractFloatType = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = next.get();
                if ((b & 0xff) == CborHalfPrecisionFloat) {
                    return getUInt16;
                }
                if ((b & 0xff) == CborSinglePrecisionFloat) {
                    return getUInt32;
                }
                if ((b & 0xff) == CborDoublePrecisionFloat) {
                    return getUInt64;
                }
                throw new RxParserException("Expected Float-Family major type");
            }
        };

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

                return CborParseFloat.this.onSuccess(((s & 0x8000) == 0) ? val : -val);
            }
        };

        IntegerState getUInt32 = new IntegerState() {
            @Override
            public ParserState onSuccess(Integer i) throws RxParserException {
                return CborParseFloat.this.onSuccess((double) Float.intBitsToFloat(i));
            }
        };

        LongState getUInt64 = new LongState() {
            @Override
            public ParserState onSuccess(Long l) throws RxParserException {
                return CborParseFloat.this.onSuccess(Double.longBitsToDouble(l));
            }
        };

        public abstract ParserState onSuccess(Double d) throws RxParserException;
    }

    /**
     * ParseBoolean parse a true/false boolean.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseBoolean extends ExtractTagItem {

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if ((b & 0xff) == CborBooleanFalse) {
                return onSuccess(false);
            }
            if ((b & 0xff) == CborBooleanTrue) {
                return onSuccess(true);
            }
            throw new RxParserException("Non boolean type: " + b);
        }

        public abstract ParserState onSuccess(boolean tag) throws RxParserException;
    }

    /**
     * ParseTag parse a single 1/2/4/8 bytes tag.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseTag extends ParserState {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = peek(next);
            int mt = (((b & MajorTypeMask) & 0xff) >>> MajorTypeShift);
            if (mt == TagType) {
                return extractInteger;
            }
            throw new RxParserException("Unexpected major type: " + mt + " expected " + TagType);
        }

        ExtractInteger extractInteger = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long l) throws RxParserException {
                if (l < 0) {
                    throw new RxParserException("not a tag");
                }
                return CborParseTag.this.onSuccess(l);
            }
        };

        public abstract ParserState onSuccess(long tag) throws RxParserException;
    }

    /**
     * ParseTextString parses a text String in a safely manner. This however decoder doesn't check
     * if the String truly is a UTF-8 sequence.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseTextString extends ParseString {
        public CborParseTextString() {
            super(TextStringType);
        }
    }

    /**
     * ParseTextString parses a byte String in a safely manner.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseByteString extends ParseString {
        public CborParseByteString() {
            super(ByteStringType);
        }
    }

    /**
     * ParseTextString parses a text String in an unsafe manner as it doesn't check the size of
     * the string and returns a single ByteBuffer onSuccess, if the string is very long, it may
     * trigger a OutOfMemory error.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseTextStringUnsafe extends CborParseStringUnsafe {
        CborParseTextStringUnsafe() {
            super(TextStringType);
        }
    }

    /**
     * ParseTextString parses a byte String in an unsafe manner as it doesn't check the size of
     * the string and returns a single ByteBuffer onSuccess, if the string is very long, it may
     * trigger a OutOfMemory error.
     * It throws an exception if the data is not of of expected types.
     */
    private abstract static class CborParseByteStringUnsafe extends CborParseStringUnsafe {
        CborParseByteStringUnsafe() {
            super(ByteStringType);
        }
    }

    /**
     * ParseCollection parses an array of CborGenericItem. Note that it doesn't check the size of the array
     * and can create an OutOfMemory so it must only be used with trusted source. For a more
     * careful parsing, first check the size with {@see CheckTextStringLength}
     * and {@see ExtractTextString}. It throws an exception if the data is not of of expected types
     * or if an unexpected break is found.
     */
    private abstract static class CborParseCollectionUnsafe extends ExtractTagItem {

        long size;
        private Collection<DataItem> c;

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if (majorType != ArrayType) {
                throw new RxParserException("Expected major type: " + MapType + " but " + majorType + " found");
            }
            return extractArraySize;
        }

        ExtractInteger extractArraySize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long mapSize) throws RxParserException {
                size = mapSize;
                c = new LinkedList<>();
                if (size == 0) {
                    return CborParseCollectionUnsafe.this.onSuccess(c);
                } else {
                    return extractItems;
                }
            }
        };

        CborParseGeneric extractItems = new CborParseGeneric() {
            @Override
            public ParserState onSuccess(DataItem i) throws RxParserException {
                if ((size < 0) && (i.cborType == CborBreakType)) {
                    return CborParseCollectionUnsafe.this.onSuccess(c);
                }
                if ((size > 0) && (i.cborType == CborBreakType)) {
                    throw new RxParserException("Unexpected break");
                }
                c.add(i);
                if (--size == 0) {
                    return CborParseCollectionUnsafe.this.onSuccess(c);
                }
                return this;
            }
        };

        @Override
        public void onExit() throws RxParserException {
            c = null;
        }

        public abstract ParserState onSuccess(Collection<DataItem> c) throws RxParserException;
    }

    /**
     * ParseMap parses a map of CborGenericItem. Note that this decoder doesn't check for key duplicate
     * or if keys are of different type. It doesn't check the size of the map
     * and can create an OutOfMemory so it must only be used with trusted source. For a more
     * careful parsing, first check the size with {@see CheckMapLength}
     * and {@see ExtractMap}. It throws an exception if the data is not of of expected types
     * or if an unexpected break is found or if a break is found while a key is left orphan.
     */
    private abstract static class CborParseMapUnsafe extends ExtractTagItem {

        long size;
        private Map<DataItem, DataItem> m;
        private DataItem current_key = null;

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if (majorType != MapType) {
                throw new RxParserException("Expected major type: " + MapType + " but " + majorType + " found");
            }
            return extractMapSize;
        }

        ExtractInteger extractMapSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long mapSize) throws RxParserException {
                size = mapSize;
                m = new HashMap<>();
                if (size == 0) {
                    return CborParseMapUnsafe.this.onSuccess(m);
                } else {
                    return extractItems;
                }
            }
        };

        CborParseGeneric extractItems = new CborParseGeneric() {
            @Override
            public ParserState onSuccess(DataItem i) throws RxParserException {
                if ((size < 0) && (i.cborType == CborBreakType)) {
                    if (current_key != null) {
                        throw new RxParserException("Unexpected break");
                    } else {
                        return CborParseMapUnsafe.this.onSuccess(m);
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
                    return CborParseMapUnsafe.this.onSuccess(m);
                }
                return this;
            }
        };

        @Override
        public void onExit() {
            m = null;
            current_key = null;
        }

        public abstract ParserState onSuccess(Map<DataItem, DataItem> map) throws RxParserException;
    }

    private abstract static class CborParseStringUnsafe extends ParseString {
        ByteArrayDataOutput output;

        CborParseStringUnsafe(int expectedType) {
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
        public ParserState onSuccess() throws RxParserException {
            return onSuccessUnsafe(ByteBuffer.wrap(output.toByteArray()));
        }

        public abstract ParserState onSuccessUnsafe(ByteBuffer buffer) throws RxParserException;
    }

    private abstract static class ParseString extends ExtractTagItem {

        int expectedType;
        long bytesExpected;
        int max_chunk_size = 2048;
        ByteBuffer chunk = null;

        ParseString(int expectedType) {
            this.expectedType = expectedType;
        }

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if (majorType != expectedType) {
                throw new RxParserException("Expected major type: " + expectedType + " but " + majorType + " found");
            }
            return extractStringSize;
        }

        ExtractInteger extractStringSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long stringSize) throws RxParserException {
                bytesExpected = stringSize;
                if (bytesExpected == 0) {
                    return ParseString.this.onSuccess();
                }
                if (bytesExpected > 0) {
                    extractDefiniteLengthString.realloc(Math.min((int) bytesExpected, max_chunk_size));
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
                    ParseString.this.onNextChunk(buffer);
                    return ParseString.this.onSuccess();
                } else {
                    ParseString.this.onNextChunk(buffer);
                    realloc(Math.min((int) bytesExpected, max_chunk_size));
                    return this;
                }
            }
        };

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = peek(next);
                if ((b & 0xff) == CborBreak) {
                    return ParseString.this.onSuccess();
                } else {
                    return extractChunkSize;
                }
            }
        };

        ExtractInteger extractChunkSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long size) throws RxParserException {
                bytesExpected = size;
                if (size == 0) {
                    ParseString.this.onNextChunk(ByteBuffer.allocate(0));
                    return checkBreak;
                }
                if (size > 0) {
                    extractChunk.realloc(Math.min((int) bytesExpected, max_chunk_size));
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
                    realloc(Math.min((int) bytesExpected, max_chunk_size));
                    return this;
                }
            }
        };

        public abstract void onNextChunk(ByteBuffer buffer);

        public abstract ParserState onSuccess() throws RxParserException;
    }

    private abstract static class ExtractContainerSize extends ExtractTagItem {

        int expectedType;

        ExtractContainerSize(int type) {
            this.expectedType = type;
        }

        @Override
        public ParserState onItemFound(int majorType, byte b) throws RxParserException {
            if (majorType != expectedType) {
                throw new RxParserException("Expected major type: " + MapType + " but " + majorType + " found");
            }
            return extractContainerSize;
        }

        ExtractInteger extractContainerSize = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long size) throws RxParserException {
                return onContainerOpen(size);
            }
        };

        public abstract ParserState onContainerOpen(long size) throws RxParserException;
    }

    private abstract static class ExtractInteger extends ParserState {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = next.get();
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
            throw new RxParserException("Wrong additional value: " + adv);
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

        public abstract ParserState onSuccess(long i) throws RxParserException;
    }

    private abstract static class ExtractTagItem extends ParserState {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = peek(next);
            int mt = (((b & MajorTypeMask) & 0xff) >>> MajorTypeShift);
            if (mt == TagType) {
                return extractTag;
            } else {
                return onItemFound(mt, b);
            }
        }

        ExtractInteger extractTag = new ExtractInteger() {
            @Override
            public ParserState onSuccess(long tag) {
                onTagFound(tag);
                return ExtractTagItem.this;
            }
        };

        public abstract void onTagFound(long tag);

        public abstract ParserState onItemFound(int majorType, byte b) throws RxParserException;

    }

    private static byte peek(ByteBuffer buffer) {
        return buffer.get(buffer.position());
    }
}