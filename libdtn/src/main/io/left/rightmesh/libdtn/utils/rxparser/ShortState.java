package io.left.rightmesh.libdtn.utils.rxparser;

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
    public void onNext(ByteBuffer next) throws RxParserException {
        if (!oneMore && next.remaining() >= 2) {
            onSuccess(next.getShort());
            return;
        }
        if (oneMore) {
            onSuccess(ByteBuffer.wrap(new byte[]{b, next.get()}).getShort());
        } else {
            b = next.get();
            oneMore = true;
        }
    }

    public abstract void onSuccess(Short s) throws RxParserException;
}