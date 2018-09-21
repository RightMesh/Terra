package io.left.rightmesh.libcbor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static io.left.rightmesh.libcbor.Constants.CborType.CborArrayType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborBooleanType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborBreakType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborByteStringType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborDoubleType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborMapType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborNullType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborSimpleType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborTagType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborTextStringType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborUndefinedType;

/**
 * @author Lucien Loiseau on 09/09/18.
 */
public class CBOR {

    public static class CborEncodingUnknown extends Exception {
    }

    public static CborEncoder encoder() {
        return new CborEncoder();
    }

    public static CborParser parser() {
        return CborParser.create();
    }

    /**
     * A decoded Cbor data item.
     */
    public static class DataItem {
        public int cborType;
        public Object item;
        public LinkedList<Long> tags;

        public DataItem(int cborType) {
            this.cborType = cborType;
            tags = new LinkedList<>();
        }

        public DataItem(int cborType, Object item) {
            this.cborType = cborType;
            setItem(null, item);
        }

        public DataItem(int cborType, Object item, LinkedList<Long> tags) {
            this.cborType = cborType;
            setTaggedItem(null, tags, item);
        }

        void addTags(LinkedList<Long> tags) {
            this.tags = tags;
        }

        void setItem(CborParser.ParserInCallback parser, Object item) {
            this.item = item;
        }

        void setTaggedItem(CborParser.ParserInCallback parser, LinkedList<Long> tags, Object item) {
            addTags(tags);
            setItem(null, item);
        }
    }

    public static class IntegerItem extends DataItem implements CborParser.ParseableItem {
        public IntegerItem() {
            super(CborIntegerType);
        }

        public IntegerItem(long l) {
            super(CborIntegerType, l);
        }

        public IntegerItem(LinkedList<Long> tags, long l) {
            super(CborIntegerType, l, tags);
        }

        public long value() {
            return (Long)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_int(this::setTaggedItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof IntegerItem) {
                return ((IntegerItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborIntegerType;
        }
    }

    public static class FloatingPointItem extends DataItem implements CborParser.ParseableItem {
        public FloatingPointItem() {
            super(CborDoubleType);
        }

        public FloatingPointItem(double d) {
            super(CborDoubleType, d);
        }

        public FloatingPointItem(LinkedList<Long> tags, double d) {
            super(CborDoubleType, d, tags);
        }

        public double value() {
            return (Double)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_float(this::setTaggedItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof FloatingPointItem) {
                return ((FloatingPointItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborDoubleType;
        }
    }

    public static class ByteStringItem extends DataItem implements CborParser.ParseableItem {
        public ByteStringItem() {
            super(CborByteStringType);
        }

        public ByteStringItem(ByteBuffer b) {
            super(CborByteStringType, b);
        }

        public ByteStringItem(LinkedList<Long> tags, ByteBuffer b) {
            super(CborByteStringType, b, tags);
        }

        public ByteBuffer value() {
            return (ByteBuffer)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_byte_string_unsafe(this::setTaggedItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof ByteStringItem) {
                return ((ByteStringItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborByteStringType;
        }
    }

    public static class TextStringItem extends DataItem implements CborParser.ParseableItem {
        public TextStringItem() {
            super(CborTextStringType);
        }

        public TextStringItem(String str) {
            super(CborTextStringType, str);
        }

        public TextStringItem(LinkedList<Long> tags, String str) {
            super(CborTextStringType, str, tags);
        }

        public String value() {
            return (String)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_text_string_unsafe(this::setTaggedItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof String) {
                return item.equals(o);
            }
            if(o instanceof TextStringItem) {
                return ((TextStringItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborTextStringType;
        }
    }

    public static class ArrayItem extends DataItem implements CborParser.ParseableItem {
        public ArrayItem() {
            super(CborArrayType);
        }

        public ArrayItem(Collection c) {
            super(CborArrayType, c);
        }

        public ArrayItem(LinkedList<Long> tags, Collection c) {
            super(CborArrayType, c, tags);
        }

        public Collection value() {
            return (Collection) item;
        }

        @Override
        public CborParser getItemParser() {
            // todo
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof ArrayItem) {
                return ((ArrayItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborArrayType;
        }
    }

    public static class MapItem extends DataItem implements CborParser.ParseableItem {
        public MapItem() {
            super(CborMapType);
        }

        public MapItem(Map m) {
            super(CborMapType, m);
        }

        public MapItem(LinkedList<Long> tags, Map m) {
            super(CborMapType, m, tags);
        }

        public Map value() {
            return (Map)item;
        }

        @Override
        public CborParser getItemParser() {
            // todo
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof MapItem) {
                return ((MapItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborMapType;
        }
    }

    public static class TagItem extends DataItem implements CborParser.ParseableItem {
        public TagItem() {
            super(CborTagType);
        }

        public TagItem(long l) {
            super(CborTagType, l);
        }

        public long value() {
            return (Long)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_tag(this::setItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof TagItem) {
                return ((TagItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborTagType;
        }
    }

    public static class SimpleValueItem extends DataItem implements CborParser.ParseableItem {
        SimpleValueItem() {
            super(CborSimpleType);
        }

        SimpleValueItem(int value) {
            super(CborSimpleType);
            setItem(null, value);
        }

        SimpleValueItem(LinkedList<Long> tags, int value) {
            super(CborSimpleType, value, tags);
        }

        public long value() {
            return (Long)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_simple_value(this::setItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof SimpleValueItem) {
                return this.item.equals(((SimpleValueItem) o).item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborSimpleType;
        }
    }

    public static class BooleanItem extends DataItem implements CborParser.ParseableItem {
        public BooleanItem() {
            super(CborBooleanType);
        }

        public BooleanItem(boolean b) {
            super(CborBooleanType, b);
        }

        public boolean value() {
            return (Boolean)item;
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_boolean(this::setItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof Boolean) {
                return item.equals(o);
            }
            if(o instanceof BooleanItem) {
                return ((BooleanItem) o).item.equals(this.item);
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborBooleanType;
        }
    }

    public static class NullItem extends DataItem implements CborParser.ParseableItem {
        public NullItem() {
            super(CborNullType);
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_null();
        }

        public Object value() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof NullItem) {
                return true;
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborNullType;
        }
    }

    public static class UndefinedItem extends DataItem implements CborParser.ParseableItem {
        public UndefinedItem() {
            super(CborUndefinedType);
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_undefined();
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof UndefinedItem) {
                return true;
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborUndefinedType;
        }
    }

    public static class BreakItem extends DataItem implements CborParser.ParseableItem {
        public BreakItem() {
            super(CborBreakType);
        }

        @Override
        public CborParser getItemParser() {
            return parser().cbor_parse_break();
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof BreakItem) {
                return true;
            }
            return false;
        }

        public static boolean ofType(DataItem item) {
            return item.cborType == CborBreakType;
        }
    }

}
