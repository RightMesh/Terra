package io.left.rightmesh.libcbor;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static io.left.rightmesh.libcbor.CBOR.DataItem;
import static io.left.rightmesh.libcbor.CBOR.IntegerItem;
import static io.left.rightmesh.libcbor.CBOR.TagItem;
import static io.left.rightmesh.libcbor.CBOR.SimpleValueItem;
import static io.left.rightmesh.libcbor.CBOR.FloatingPointItem;
import static io.left.rightmesh.libcbor.CBOR.ByteStringItem;
import static io.left.rightmesh.libcbor.CBOR.TextStringItem;
import static io.left.rightmesh.libcbor.CBOR.ArrayItem;
import static io.left.rightmesh.libcbor.CBOR.MapItem;

import io.left.rightmesh.libcbor.rxparser.BufferState;
import io.left.rightmesh.libcbor.rxparser.ByteState;
import io.left.rightmesh.libcbor.rxparser.IntegerState;
import io.left.rightmesh.libcbor.rxparser.LongState;
import io.left.rightmesh.libcbor.rxparser.ParserState;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libcbor.rxparser.ShortState;

import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.IndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value16Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value32Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value64Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value8Bit;
import static io.left.rightmesh.libcbor.Constants.CborInternals.MajorTypeMask;
import static io.left.rightmesh.libcbor.Constants.CborInternals.MajorTypeShift;
import static io.left.rightmesh.libcbor.Constants.CborInternals.SmallValueMask;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBooleanFalse;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBooleanTrue;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborDoublePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborHalfPrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSimpleValue1ByteFollow;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSinglePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ArrayType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ByteStringType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.MapType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.NegativeIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.SimpleTypesType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TagType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TextStringType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.UnsignedIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.Break;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.NullValue;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.UndefinedValue;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.SimpleTypeInNextByte;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.HalfPrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.SinglePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.DoublePrecisionFloat;


public class CborParser {

    private LinkedList<ParserState> doneQueue = new LinkedList<>();
    private LinkedList<ParserState> parserQueue = new LinkedList<>();
    private ParserState state = null;
    private ParserState next = null;

    private boolean dequeue() {
        if (parserQueue.isEmpty()) {
            return false;
        } else {
            state = parserQueue.poll();
            doneQueue.add(state);
            return true;
        }
    }

    /* parser method */

    public void reset() {
        doneQueue.addAll(parserQueue);
        parserQueue.clear();
        LinkedList<ParserState> swap = parserQueue;
        parserQueue = doneQueue;
        doneQueue = swap;
    }

    /**
     * Parse the buffer given as parameter. If the parser has finished parsing before the end
     * of this buffer, it returns true and the buffer position is left to where it was last
     * consumes and can be reuse for another parsing job. If the buffer was entirely consummed
     * but the current parser hasn't finished, it returns false and further read may be needed
     * to finish the parsing job.
     * <p>
     * Note that it is safe to call merge() or insert() from within one of the parsing callback.
     *
     * @param buffer
     * @return true if the object was successfully parsed, false otherwise.
     * @throws RxParserException if an error occured during parsing
     */
    public boolean read(ByteBuffer buffer) throws RxParserException {
        while (buffer.hasRemaining()) {
            if (state == null) {
                if (!dequeue()) {
                    return true;
                } else {
                    state.onEnter();
                }
            }

            int remaining = buffer.remaining();
            next = state.onNext(buffer);

            if (next == state) {
                if (buffer.remaining() == remaining) {
                    throw new RxParserException("Failsafe! parser is not consuming buffer: " + next.getClass().getName());
                }
            } else {
                state.onExit();
                state = next;
                if (state != null) {
                    state.onEnter();
                }
            }
        }

        if (state == null && parserQueue.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Add a parsing sequence at the end of this parser sequence.
     *
     * @param parser sequence to add at the end.
     */
    public CborParser merge(CborParser parser) {
        parserQueue.addAll(parser.parserQueue);
        return this;
    }

    /**
     * Add a parsing sequence at the front of this parser. If called from within one
     * of the callback, the parsing sequence will be added right after the current
     * parsing state that called the callback.
     *
     * @param parser to add at the front of the sequence
     */
    public CborParser insert(CborParser parser) {
        Deque<ParserState> d = new ArrayDeque<>();
        for (ParserState s : parser.parserQueue) {
            d.push(s);
        }
        for (ParserState s : d) {
            parserQueue.addFirst(s);
        }
        return this;
    }

    /**
     * returns the parser as a parser state. It is similar to read as it will consume buffer to
     * parse but returns the same state until it is fully parsed at which point it calls onSuccess();
     *
     * @return ParserState
     */
    public ParserState asParserState(ParsingDoneCallback cb) {
        return new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (read(next)) {
                    cb.onParsingDone(CborParser.this);
                    return null;
                }
                return this;
            }
        };
    }

    /* callback interfaces */


    public interface ParseableItem {
        CborParser getItemParser();
    }

    public interface ItemFactory<T extends ParseableItem> {
        T createItem();
    }

    public interface ContainerIsOpenCallback {
        void onContainerIsOpen(CborParser parser, LinkedList<Long> tags, long size) throws RxParserException;
    }

    public interface ContainerIsCloseCallback {
        void onContainerIsClose(CborParser parser) throws RxParserException;
    }

    public interface ContainerIsCloseWithCollectionCallback<T> {
        void onContainerIsClose(CborParser parser, LinkedList<Long> tags, Collection<T> c) throws RxParserException;
    }

    public interface ContainerIsCloseWithMapCallback<T, U> {
        void onContainerIsClose(CborParser parser, LinkedList<Long> tags, Map<T, U> c) throws RxParserException;
    }

    public interface ChunkCallback<T> {
        void onChunk(CborParser parser, T obj) throws RxParserException;
    }

    public interface ParsedItemCallback<T> {
        void onItemParsed(CborParser parser, T obj) throws RxParserException;
    }

    public interface ParsedItemWithTagsCallback<T> {
        void onItemParsed(CborParser parser, LinkedList<Long> tags, T obj) throws RxParserException;
    }

    public interface ParsedMapEntryCallback<T, U> {
        void onMapEntryParsed(CborParser parser, T key, U value) throws RxParserException;
    }

    public interface ParsedIntWithTagsCallback {
        void onIntParsed(CborParser parser, LinkedList<Long> tags, long i) throws RxParserException;
    }

    public interface ParsedFloatWithTagsCallback {
        void onFloatParsed(CborParser parser, LinkedList<Long> tags, double d) throws RxParserException;
    }

    public interface ParsedBoolean {
        void onBooleanParsed(CborParser parser, boolean b) throws RxParserException;
    }

    public interface ParsingDoneCallback {
        void onParsingDone(CborParser parser) throws RxParserException;
    }

    /* parser API */

    public enum ExpectedType {
        Integer, Float, ByteString, TextString, Array, Map, Tag, Simple
    }

    public CborParser cbor_parse_generic(ParsedItemCallback<DataItem> cb) {
        parserQueue.add(new CborParseGenericItem() {
            @Override
            public ParserState onSuccess(DataItem item) throws RxParserException {
                cb.onItemParsed(CborParser.this, item);
                return null;
            }
        });
        return this;
    }

    public <T extends ParseableItem> CborParser cbor_parse_custom_item(ItemFactory<T> factory, ParsedItemWithTagsCallback<T> cb) {
        parserQueue.add(new CborParseCustomItem<T>(factory) {
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(T item) throws RxParserException {
                cb.onItemParsed(CborParser.this, tags, item);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_generic(EnumSet<ExpectedType> types, ParsedItemCallback<DataItem> cb) {
        parserQueue.add(new CborParseGenericItem(types) {
            @Override
            public ParserState onSuccess(DataItem item) throws RxParserException {
                cb.onItemParsed(CborParser.this, item);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_boolean(ParsedBoolean cb) {
        parserQueue.add(new CborParseBoolean() {
            @Override
            public void onTagFound(long tag) {
                // a boolean should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onSuccess(boolean b) throws RxParserException {
                cb.onBooleanParsed(CborParser.this, b);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_break() {
        return cbor_parse_break(null);
    }

    public CborParser cbor_parse_break(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                // a break should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onBreak() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_undefined() {
        return cbor_parse_undefined(null);
    }

    public CborParser cbor_parse_undefined(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseUndefined() {
            @Override
            public void onTagFound(long tag) {
                // an undefined value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onUndefined() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_null() {
        return cbor_parse_null(null);
    }

    public CborParser cbor_parse_null(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseNull() {
            @Override
            public void onTagFound(long tag) {
                // a null value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onNull() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_simple_value(ParsedItemCallback cb) {
        parserQueue.add(new CborParseSimpleValue() {
            @Override
            public void onTagFound(long tag) {
                // a null value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onSimplevalue(int value) throws RxParserException {
                if (cb != null) {
                    cb.onItemParsed(CborParser.this, value);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_int(ParsedIntWithTagsCallback cb) {
        parserQueue.add(new CborParseInteger() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(long l) throws RxParserException {
                cb.onIntParsed(CborParser.this, tags, l);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_float(ParsedFloatWithTagsCallback cb) {
        parserQueue.add(new CborParseFloat() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(Double obj) throws RxParserException {
                cb.onFloatParsed(CborParser.this, tags, obj);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb) {
        return cbor_parse_byte_string(null, cb, null);
    }

    public CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1, ChunkCallback<ByteBuffer> cb2) {
        return cbor_parse_byte_string(cb1, cb2, null);
    }

    public CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1,
                                             ChunkCallback<ByteBuffer> cb2,
                                             ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseByteString() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(CborParser.this, tags, size);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) throws RxParserException {
                if (cb2 != null) {
                    cb2.onChunk(CborParser.this, next);
                }
            }

            @Override
            public ParserState onSuccess() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_byte_string_unsafe(ParsedItemWithTagsCallback<ByteBuffer> cb) {
        parserQueue.add(new CborParseByteStringUnsafe() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onContainerOpen(long size) {
                // ignore because unsafe
            }

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                cb.onItemParsed(CborParser.this, tags, obj);
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_text_string(ChunkCallback<String> cb) {
        return cbor_parse_text_string(null, cb, null);
    }

    public CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1, ChunkCallback<String> cb2) {
        return cbor_parse_text_string(cb1, null, null);
    }

    public CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1,
                                             ChunkCallback<String> cb2,
                                             ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseTextString() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(CborParser.this, tags, size);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) throws RxParserException {
                if (cb2 != null) {
                    cb2.onChunk(CborParser.this, StandardCharsets.UTF_8.decode(next).toString());
                }
            }

            @Override
            public ParserState onSuccess() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_text_string_unsafe(ParsedItemWithTagsCallback<String> cb) {
        parserQueue.add(new CborParseTextStringUnsafe() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) {
                // ignore because unsafe
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer next) throws RxParserException {
                cb.onItemParsed(CborParser.this, tags, StandardCharsets.UTF_8.decode(next).toString());
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_parse_tag(ParsedItemCallback<Long> cb) {
        parserQueue.add(new CborParseTag() {
            @Override
            public ParserState onSuccess(long tag) throws RxParserException {
                cb.onItemParsed(CborParser.this, tag);
                return null;
            }
        });
        return this;
    }

    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsCloseWithCollectionCallback<T> cb) {
        return cbor_parse_linear_array(factory, null, null, cb);
    }

    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseWithCollectionCallback<T> cb3) {
        parserQueue.add(new CborParseLinearArray<T>(factory) {

            Collection<T> c;
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onArrayIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(CborParser.this, tags, size);
                }
                c = new LinkedList<>();
            }

            @Override
            public void onArrayItem(LinkedList<Long> tags, T item)  throws RxParserException {
                if (cb2 != null) {
                    cb2.onItemParsed(CborParser.this, tags, item);
                }
                c.add(item);
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose(CborParser.this, tags, c);
                }
                return null;
            }
        });
        return this;
    }

    public <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb) {
        return cbor_parse_linear_array_stream(factory, null, cb, null);
    }

    public <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseLinearArray<T>(factory) {
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onArrayIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(CborParser.this, tags, size);
                }
            }

            @Override
            public void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException {
                if (cb2 != null) {
                    cb2.onItemParsed(CborParser.this, tags, item);
                }
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsCloseWithMapCallback<T, U> cb) {
        return cbor_parse_linear_map(keyFactory, valueFactory, null, null, cb);
    }

    public <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsOpenCallback cb1,
            ParsedMapEntryCallback<T, U> cb2,
            ContainerIsCloseWithMapCallback<T, U> cb3) {
        parserQueue.add(new CborParseLinearMap<T, U>(keyFactory, valueFactory) {

            Map<T, U> m;
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onMapIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen(CborParser.this, tags, size);
                }
                m = new HashMap<>();
            }

            @Override
            public void onMapEntry(T keyItem, U valueItem) throws RxParserException {
                if (cb2 != null) {
                    cb2.onMapEntryParsed(CborParser.this, keyItem, valueItem);
                }
                m.put(keyItem, valueItem);
            }

            @Override
            public ParserState onMapIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose(CborParser.this, tags, m);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_open_map(ContainerIsOpenCallback cb) {
        return cbor_open_container(cb, MapType);
    }

    public CborParser cbor_close_map() {
        return cbor_close_container(null);
    }

    public CborParser cbor_close_map(ContainerIsCloseCallback cb) {
        return cbor_close_container(cb);
    }

    public CborParser cbor_open_array(ContainerIsOpenCallback cb) {
        return cbor_open_container(cb, ArrayType);
    }

    public CborParser cbor_open_array(int expectedSize) {
        return cbor_open_container_expected_size(expectedSize, ArrayType);
    }

    public CborParser cbor_open_container(ContainerIsOpenCallback cb,
                                          int majorType) {
        parserQueue.add(new ExtractContainerSize(majorType) {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                if (cb != null) {
                    cb.onContainerIsOpen(CborParser.this, tags, size);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_open_container_expected_size(int expectedSize, int majorType) {
        parserQueue.add(new ExtractContainerSize(majorType) {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                if (size != expectedSize) {
                    throw new RxParserException("wrong array length, expected: " + expectedSize + " got: " + size);
                }
                return null;
            }
        });
        return this;
    }

    public <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb) {
        return cbor_parse_array_items(factory, cb, null);
    }

    public <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb1,
            ContainerIsCloseCallback cb2) {
        parserQueue.add(new CborParseArrayItems<T>(factory) {
            @Override
            public void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException {
                if (cb1 != null) {
                    cb1.onItemParsed(CborParser.this, tags, item);
                }
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb2 != null) {
                    cb2.onContainerIsClose(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }

    public CborParser cbor_close_array() {
        return cbor_close_container(null);
    }

    public CborParser cbor_close_array(ContainerIsCloseCallback cb) {
        return cbor_close_container(cb);
    }

    public CborParser cbor_close_container(ContainerIsCloseCallback cb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                // do nothing but it is probably an error
            }

            @Override
            public ParserState onBreak() throws RxParserException {
                if (cb != null) {
                    cb.onContainerIsClose(CborParser.this);
                }
                return null;
            }
        });
        return this;
    }



    /* internal parser utils */


    private abstract static class CborParseCustomItem<T extends ParseableItem> extends ExtractTagItem {
        ItemFactory<T> factory;

        CborParseCustomItem(ItemFactory<T> factory) {
            super(true);
            this.factory = factory;
        }

        @Override
        public ParserState onItemFound(int majorType, byte b) {
            return extractCustomItem;
        }

        ParserState extractCustomItem = new ParserState() {
            T item;
            CborParser itemParser;

            @Override
            public void onEnter() throws RxParserException {
                item = factory.createItem();
                itemParser = item.getItemParser();
            }

            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (itemParser.read(next)) {
                    return onSuccess(item);
                }
                return this;
            }
        };

        public abstract ParserState onSuccess(T item) throws RxParserException;
    }

    private abstract static class CborParseGenericItem extends ExtractTagItem {

        LinkedList<Long> tags;
        boolean filter_int = true;
        boolean filter_float = true;
        boolean filter_byte_string = true;
        boolean filter_text_string = true;
        boolean filter_array = true;
        boolean filter_tag = true;
        boolean filter_map = true;
        boolean filter_simple = true;

        CborParseGenericItem() {
            super(true);
            tags = new LinkedList<>();
        }

        CborParseGenericItem(EnumSet<ExpectedType> types) {
            this();
            filter_int = types.contains(ExpectedType.Integer);
            filter_float = types.contains(ExpectedType.Float);
            filter_byte_string = types.contains(ExpectedType.ByteString);
            filter_text_string = types.contains(ExpectedType.TextString);
            filter_array = types.contains(ExpectedType.Array);
            filter_map = types.contains(ExpectedType.Map);
            filter_tag = types.contains(ExpectedType.Tag);
            filter_simple = types.contains(ExpectedType.Simple);
        }

        @Override
        public void onTagFound(long tag) {
            tags.add(tag);
        }

        @Override
        public ParserState onItemFound(int mt, byte b) throws RxParserException {
            if (((mt == UnsignedIntegerType) || (mt == NegativeIntegerType)) && (filter_int)) {
                return parse_integer;
            }
            if ((mt == ByteStringType) && (filter_byte_string)) {
                return parse_byte_string;
            }
            if ((mt == TextStringType) && (filter_text_string)) {
                return parse_text_string;
            }

            if ((mt == ArrayType) && (filter_array)) {
                return parse_array_size;
            }
            if ((mt == MapType) && (filter_map)) {
                return parse_map_size;
            }

            if ((mt == TagType) && (filter_tag)) {
                return parse_tag;
            }
            if (mt == SimpleTypesType) {
                switch (b & SmallValueMask) {
                    case SimpleTypeInNextByte:
                        if (filter_simple) {
                            return parse_simple_value;
                        }
                    case HalfPrecisionFloat:
                        if (filter_float) {
                            return parse_float;
                        }
                    case SinglePrecisionFloat:
                        if (filter_float) {
                            return parse_float;
                        }
                    case DoublePrecisionFloat:
                        if (filter_float) {
                            return parse_float;
                        }
                    default:
                        if (filter_simple) {
                            return parse_simple_value;
                        }
                }
            }
            throw new RxParserException("Unknown major type: " + mt);
        }

        CborParseInteger parse_integer = new CborParseInteger() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(long obj) throws RxParserException {
                return CborParseGenericItem.this.onSuccess(new IntegerItem(tags, obj));
            }
        };

        CborParseFloat parse_float = new CborParseFloat() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public ParserState onSuccess(Double obj) throws RxParserException {
                return CborParseGenericItem.this.onSuccess(new FloatingPointItem(tags, obj));
            }
        };

        CborParseByteStringUnsafe parse_byte_string = new CborParseByteStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public void onContainerOpen(long size) {
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                return CborParseGenericItem.this.onSuccess(new ByteStringItem(tags, obj));
            }
        };

        CborParseTextStringUnsafe parse_text_string = new CborParseTextStringUnsafe() {
            @Override
            public void onTagFound(long tag) {
                // do nothing
            }

            @Override
            public void onContainerOpen(long size) {

            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                String str = StandardCharsets.UTF_8.decode(obj).toString();
                return CborParseGenericItem.this.onSuccess(new TextStringItem(tags, str));
            }
        };

        ExtractContainerSize parse_array_size = new ExtractContainerSize(ArrayType) {
            long size;
            Collection<DataItem> array;

            @Override
            public void onTagFound(long tag) {
                // ignore tags
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                this.size = size;
                array = new LinkedList<>();
                if (size < 0) {
                    return checkBreak;
                }
                if (size == 0) {
                    return CborParseGenericItem.this.onSuccess(new ArrayItem(tags, array));
                }
                // if size > 0
                return extractNestedItem();
            }

            ParserState checkBreak = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    byte b = peek(next);
                    if ((b & 0xff) == CborBreak) {
                        return CborParseGenericItem.this.onSuccess(new ArrayItem(tags, array));
                    } else {
                        return extractNestedItem();
                    }
                }
            };

            CborParseGenericItem outer = CborParseGenericItem.this;

            CborParseGenericItem extractNestedItem() {
                return new CborParseGenericItem() {
                    @Override
                    public ParserState onSuccess(DataItem item) throws RxParserException {
                        array.add(item);
                        size--;
                        if (size < 0) {
                            return checkBreak;
                        }
                        if (size == 0) {
                            // it is a win, exit the recursion
                            return outer.onSuccess(new ArrayItem(tags, array));
                        }
                        // size > 0
                        return extractNestedItem();
                    }
                };
            }
        };

        ExtractContainerSize parse_map_size = new ExtractContainerSize(MapType) {
            long size;
            Map<DataItem, DataItem> map;
            DataItem currentKey;

            @Override
            public void onTagFound(long tag) {
                // ignore tags
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                this.size = size;
                map = new HashMap<>();
                if (size < 0) {
                    return checkBreak;
                }
                if (size == 0) {
                    return CborParseGenericItem.this.onSuccess(new MapItem(tags, map));
                }
                // if size > 0
                return extractNextNestedKey();
            }

            ParserState checkBreak = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    byte b = peek(next);
                    if ((b & 0xff) == CborBreak) {
                        return CborParseGenericItem.this.onSuccess(new MapItem(tags, map));
                    } else {
                        return extractNextNestedKey();
                    }
                }
            };

            CborParseGenericItem extractNextNestedKey() {
                return new CborParseGenericItem() {
                    @Override
                    public ParserState onSuccess(DataItem item) {
                        currentKey = item;
                        return extractNextNestedValue();
                    }
                };
            }

            CborParseGenericItem outer = CborParseGenericItem.this;

            CborParseGenericItem extractNextNestedValue() {
                return new CborParseGenericItem() {
                    @Override
                    public ParserState onSuccess(DataItem item) throws RxParserException {
                        map.put(currentKey, item);
                        size--;
                        if (size < 0) {
                            return checkBreak;
                        }
                        if (size == 0) {
                            return outer.onSuccess(new MapItem(tags, map));
                        }
                        // size > 0
                        return extractNextNestedKey();
                    }
                };
            }
        };

        CborParseTag parse_tag = new CborParseTag() {
            @Override
            public ParserState onSuccess(long tag) throws RxParserException {
                return CborParseGenericItem.this.onSuccess(new TagItem(tag));
            }
        };

        CborParseSimpleValue parse_simple_value = new CborParseSimpleValue() {
            @Override
            public void onTagFound(long tag) {
                // ignore
            }

            @Override
            public ParserState onSimplevalue(int value) throws RxParserException {
                return CborParseGenericItem.this.onSuccess(new SimpleValueItem(value));
            }
        };

        public abstract ParserState onSuccess(DataItem item) throws RxParserException;
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

        CborParseSimpleValue() {
            super(false);
        }

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
                byte b = next.get();
                return onSimplevalue(b & 0xff);
            }
        };

        public abstract ParserState onSimplevalue(int value) throws RxParserException;
    }

    private abstract static class CborParseLinearArray<T extends ParseableItem> extends ExtractContainerSize {

        long size;
        ItemFactory<T> factory;

        CborParseLinearArray(ItemFactory<T> factory) {
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
                    next.get();
                    return onArrayIsClose();
                } else {
                    return extractOneItem;
                }
            }
        };


        ParserState extractOneItem = new ExtractTagItem(true) {
            T item;
            CborParser parser;
            LinkedList<Long> tags;

            @Override
            public void onEnter() throws RxParserException {
                tags = new LinkedList<>();
            }

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onItemFound(int majorType, byte b) {
                return actualExtract;
            }

            ParserState actualExtract = new ParserState() {
                @Override
                public void onEnter() {
                    item = factory.createItem();
                    parser = item.getItemParser();
                }

                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    if (parser.read(next)) {
                        onArrayItem(tags, item);
                        size--;
                        if (size < 0) {
                            return checkBreak;
                        }
                        if (size == 0) {
                            return onArrayIsClose();
                        }
                        if (size > 0) {
                            this.onEnter(); // create a new item
                            return this;
                        }
                    }
                    return this;
                }
            };
        };

        public abstract void onArrayIsOpen(long l) throws RxParserException;

        public abstract void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException;

        public abstract ParserState onArrayIsClose() throws RxParserException;

    }

    private abstract static class CborParseArrayItems<T extends ParseableItem> extends ParserState {

        ItemFactory<T> factory;

        CborParseArrayItems(ItemFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            return checkBreak;
        }

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = peek(next);
                if ((b & 0xff) == CborBreak) {
                    next.get();
                    return onArrayIsClose();
                } else {
                    return extractOneItem;
                }
            }
        };

        ParserState extractOneItem = new ExtractTagItem(true) {
            T item;
            CborParser parser;
            LinkedList<Long> tags;

            @Override
            public void onEnter() throws RxParserException {
                tags = new LinkedList<>();
            }

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onItemFound(int majorType, byte b) {
                return actualExtract;
            }

            ParserState actualExtract = new ParserState() {
                @Override
                public void onEnter() {
                    item = factory.createItem();
                    parser = item.getItemParser();
                }

                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    if (parser.read(next)) {
                        onArrayItem(tags, item);
                        return checkBreak;
                    }
                    return this;
                }
            };
        };

        public abstract void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException;

        public abstract ParserState onArrayIsClose() throws RxParserException;

    }

    private abstract static class CborParseLinearMap<T extends ParseableItem, U extends ParseableItem> extends ExtractContainerSize {

        long size;
        ItemFactory<T> keyFactory;
        ItemFactory<U> valueFactory;
        T currentKey;

        CborParseLinearMap(ItemFactory<T> keyFactory,
                           ItemFactory<U> valueFactory) {
            super(MapType);
            this.keyFactory = keyFactory;
            this.valueFactory = valueFactory;
        }

        @Override
        public ParserState onContainerOpen(long size) throws RxParserException {
            this.size = size;
            onMapIsOpen(size);
            if (size == 0) {
                return onMapIsClose();
            }
            if (size > 0) {
                return extractKey;
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
                    next.get();
                    return onMapIsClose();
                } else {
                    return extractKey;
                }
            }
        };

        ParserState extractKey = new ParserState() {
            CborParser parser;

            @Override
            public void onEnter() {
                currentKey = keyFactory.createItem();
                parser = currentKey.getItemParser();
            }

            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (parser.read(next)) {
                    return extractValue;
                }
                return this;
            }
        };

        ParserState extractValue = new ParserState() {
            U value;
            CborParser parser;

            @Override
            public void onEnter() {
                value = valueFactory.createItem();
                parser = value.getItemParser();
            }

            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (parser.read(next)) {
                    onMapEntry(currentKey, value);
                    size--;
                    if (size < 0) {
                        return checkBreak;
                    }
                    if (size == 0) {
                        return onMapIsClose();
                    }
                    if (size > 0) {
                        return extractKey;
                    }
                }
                return this;
            }
        };

        public abstract void onMapIsOpen(long l) throws RxParserException;

        public abstract void onMapEntry(T key, U value) throws RxParserException;

        public abstract ParserState onMapIsClose() throws RxParserException;

    }

    private abstract static class CborParseInteger extends ExtractTagItem {

        int mt;

        CborParseInteger() {
            super(true);
        }

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

    private abstract static class CborParseFloat extends ExtractTagItem {

        CborParseFloat() {
            super(true);
        }

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

    private abstract static class CborParseBoolean extends ExtractTagItem {

        CborParseBoolean() {
            super(false);
        }

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

    private abstract static class CborParseTextString extends CborParseString {
        public CborParseTextString() {
            super(TextStringType);
        }
    }

    private abstract static class CborParseByteString extends CborParseString {
        public CborParseByteString() {
            super(ByteStringType);
        }
    }

    private abstract static class CborParseTextStringUnsafe extends CborParseStringUnsafe {
        CborParseTextStringUnsafe() {
            super(TextStringType);
        }
    }

    private abstract static class CborParseByteStringUnsafe extends CborParseStringUnsafe {
        CborParseByteStringUnsafe() {
            super(ByteStringType);
        }
    }

    private abstract static class CborParseStringUnsafe extends CborParseString {
        ByteArrayDataOutput output;

        CborParseStringUnsafe(int expectedType) {
            super(expectedType);
        }

        @Override
        public void onEnter() throws RxParserException {
            output = ByteStreams.newDataOutput();
        }

        @Override
        public void onNextChunk(ByteBuffer buffer) throws RxParserException {
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

    private abstract static class CborParseString extends ExtractTagItem {

        int expectedType;
        long bytesExpected;
        int max_chunk_size = 2048;
        ByteBuffer chunk = null;

        CborParseString(int expectedType) {
            super(true);
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
                onContainerOpen(stringSize);
                if (bytesExpected == 0) {
                    return CborParseString.this.onSuccess();
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
                    CborParseString.this.onNextChunk(buffer);
                    return CborParseString.this.onSuccess();
                } else {
                    CborParseString.this.onNextChunk(buffer);
                    realloc(Math.min((int) bytesExpected, max_chunk_size));
                    onEnter();
                    return this;
                }
            }
        };

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = peek(next);
                if ((b & 0xff) == CborBreak) {
                    return CborParseString.this.onSuccess();
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
                    CborParseString.this.onNextChunk(ByteBuffer.allocate(0));
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
                CborParseString.this.onNextChunk(buffer);
                if (bytesExpected == 0) {
                    return checkBreak;
                } else {
                    realloc(Math.min((int) bytesExpected, max_chunk_size));
                    return this;
                }
            }
        };

        public abstract void onContainerOpen(long size) throws RxParserException;

        public abstract void onNextChunk(ByteBuffer buffer) throws RxParserException;

        public abstract ParserState onSuccess() throws RxParserException;
    }

    private abstract static class ExtractContainerSize extends ExtractTagItem {

        int expectedType;

        ExtractContainerSize(int type) {
            super(true);
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

        boolean must_peek;

        ExtractTagItem(boolean peek) {
            this.must_peek = peek;
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = must_peek ? peek(next) : next.get();
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