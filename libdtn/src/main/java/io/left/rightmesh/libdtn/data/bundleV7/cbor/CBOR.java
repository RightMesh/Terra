package io.left.rightmesh.libdtn.data.bundleV7.cbor;

import java.nio.ByteBuffer;

import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborByteStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborDoubleType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborIntegerType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborNullType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborTagType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborTextStringType;
import static io.left.rightmesh.libdtn.data.bundleV7.cbor.Constants.CborType.CborUndefinedType;

/**
 * @author Lucien Loiseau on 09/09/18.
 */
public class CBOR {

    public static class CborEncodingUnknown extends Exception {
    }

    public static CborEncoder getCborEncoder(ByteBuffer buffer) {
        return new CborEncoder(buffer);
    }

    public static CborParser getCborDecoder() {
        return new CborParser();
    }

    /**
     * A decoded Cbor data item.
     */
    public static class DataItem<T> {
        public int cborType;
        public T item;

        public DataItem(int cborType) {
            this.cborType = cborType;
        }

        public DataItem(int cborType, T item) {
            this.cborType = cborType;
            setItem(item);
        }

        void setItem(T item) {
            this.item = item;
        }
    }


    public static class NullItem extends DataItem<Long> implements CborParser.ParseableItem {
        public NullItem() {
            super(CborNullType);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_null();
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
    }

    public static class UndefinedItem extends DataItem<Long> implements CborParser.ParseableItem {
        public UndefinedItem() {
            super(CborUndefinedType);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_undefined();
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
    }

    public static class BreakItem extends DataItem<Long> implements CborParser.ParseableItem {
        public BreakItem() {
            super(CborUndefinedType);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_break();
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
    }


    public static class IntegerItem extends DataItem<Long> implements CborParser.ParseableItem {
        public IntegerItem() {
            super(CborIntegerType);
        }

        public IntegerItem(long l) {
            super(CborIntegerType, l);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_int(this::setItem);
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
    }


    public static class TagItem extends DataItem<Long> implements CborParser.ParseableItem {
        public TagItem() {
            super(CborTagType);
        }

        public TagItem(long l) {
            super(CborTagType, l);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_tag(this::setItem);
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
    }



    public static class FloatingPointItem extends DataItem<Double> implements CborParser.ParseableItem {
        public FloatingPointItem() {
            super(CborDoubleType);
        }

        public FloatingPointItem(double d) {
            super(CborDoubleType, d);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_float(this::setItem);
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
    }

    public static class ByteStringItem extends DataItem<ByteBuffer> implements CborParser.ParseableItem {
        public ByteStringItem() {
            super(CborByteStringType);
        }

        public ByteStringItem(ByteBuffer b) {
            super(CborByteStringType, b);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_byte_string_unsafe(this::setItem);
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
    }

    public static class TextStringItem extends DataItem<String> implements CborParser.ParseableItem {
        public TextStringItem() {
            super(CborTextStringType);
        }

        public TextStringItem(String str) {
            super(CborTextStringType, str);
        }


        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_text_string_unsafe(this::setItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof TextStringItem) {
                return ((TextStringItem) o).item.equals(this.item);
            }
            return false;
        }
    }

    public static class BooleanItem extends DataItem<Boolean> implements CborParser.ParseableItem {
        public BooleanItem() {
            super(CborTextStringType);
        }

        public BooleanItem(boolean b) {
            super(CborTextStringType, b);
        }

        @Override
        public CborParser getItemParser() {
            return getCborDecoder().cbor_parse_boolean(this::setItem);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) {
                return false;
            }
            if(o instanceof BooleanItem) {
                return ((BooleanItem) o).item.equals(this.item);
            }
            return false;
        }
    }
}
