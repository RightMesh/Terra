package io.left.rightmesh.libcbor.rxparser;

import java.nio.ByteBuffer;

/**
 * ShortState deserialize a single Integer value.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class IntegerState extends ObjectState<Integer> {

    byte[] buf = new byte[4];

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        return buffer;
    }

    BufferState buffer = new BufferState(buf) {
        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            return IntegerState.this.onSuccess(buffer.getInt());
        }
    };

    public abstract ParserState onSuccess(Integer s) throws RxParserException;
}
