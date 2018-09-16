package io.left.rightmesh.libcbor.rxparser;

import java.nio.ByteBuffer;

/**
 * ShortState deserialize a single Short value.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class ShortState extends ObjectState<Short> {

    byte b;
    boolean oneMore;

    @Override
    public void onEnter() {
        oneMore = false;
    }

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        if (!oneMore && next.remaining() >= 2) {
            return onSuccess(next.getShort());
        }
        if (oneMore) {
            return onSuccess(ByteBuffer.wrap(new byte[]{b, next.get()}).getShort());
        } else {
            b = next.get();
            oneMore = true;
            return this;
        }
    }

    public abstract ParserState onSuccess(Short s) throws RxParserException;
}