package io.left.rightmesh.libdtn.utils.rxdeserializer;

import java.nio.ByteBuffer;

/**
 * BufferState is a RxState that buffers data and returns it.
 *
 * @author Lucien Loiseau on 03/09/18.
 */

public abstract class BufferState extends ObjectState<ByteBuffer> {
    private ByteBuffer buffer;

    public void resizeBuffer(int size) {
        buffer = ByteBuffer.allocate(size);
    }

    public BufferState() {
        buffer = ByteBuffer.allocate(0);
    }

    public BufferState(byte[] array) {
        buffer = ByteBuffer.wrap(array);
    }

    @Override
    public void onEnter() {
        buffer.clear();
    }

    @Override
    public void onNext(ByteBuffer next) throws RxDeserializerException {
        if (buffer.remaining() >= next.remaining()) {
            buffer.put(next);
        } else {
            while (buffer.hasRemaining()) {
                buffer.put(next.get());
            }
        }

        if (!buffer.hasRemaining()) {
            buffer.flip();
            onSuccess(buffer);
        }
    }

    @Override
    public void onExit() throws RxDeserializerException {
        buffer = null;
    }

    /**
     * onSuccess is called whenever the buffer is filled.
     *
     * @param buffer of data
     * @throws RxDeserializerException if an exception happened during deserialization.
     */
    public abstract void onSuccess(ByteBuffer buffer) throws RxDeserializerException;
}
