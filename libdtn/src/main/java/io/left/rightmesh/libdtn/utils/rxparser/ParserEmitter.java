package io.left.rightmesh.libdtn.utils.rxparser;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.nio.ByteBuffer;

/**
 * ParserEmitter is the class that do the actual business of parsing and emitting items.
 * It is created by RxParser upon subscription. This class is abstract and child class are
 * expected to implement initState() to provide the first ParserState to send data to for
 * parsing. The logic of actual parsing is left to the child class however the following has
 * to be respected:
 *
 * <ul>
 *     <li>Empty ByteBuffer will not and must not be passed to ParserState.onNext</li>
 *     <li>An empty ByteBuffer received from upstream signals that state machine must be reset</li>
 *     <li>Every parsed item T must be emitted with a call to emit()</li>
 * </ul>
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class ParserEmitter<T> implements Observer<ByteBuffer>, Disposable {

    /** The upstream subscription. */
    private Disposable upstream;

    /** The downstream subscriber. */
    private Observer<? super T> downstream;

    /** The current deserialization state. */
    private ParserState state;

    /** disposed subscription. */
    private boolean disposed = false;

    protected ParserEmitter(Observer<? super T> downstream) {
        this.downstream = downstream;
    }

    @Override
    public void onSubscribe(Disposable upstream) {
        this.upstream = upstream;
        this.state = initState();
    }

    /**
     * The initial state for this State Machine.
     *
     * @return the initial {@see State}
     */
    public abstract ParserState initState();

    /**
     * If a zero-sized ByteBuffer is received, it is a signal that this state machine must reset.
     * Child class must implement onReset(). After this call, the state will go back to
     * initState().
     */
    public abstract void onReset();

    /**
     * Must be called by the child class whenever an item was deserialized.
     * If a ByteBuffer.remaining() of zero is received
     * from upstream, it triggers a reset. Similarly, emitting a ByteBuffer.allocate(0) will
     * trigger a reset from downstream.
     *
     * @param item to be emitted
     */
    protected void emit(T item) {
        downstream.onNext(item);
    }

    @Override
    public void onNext(ByteBuffer b) {
        try {
            if (disposed) {
                return;
            }

            // reset signal
            if (b.remaining() == 0) {
                onReset();
                state.onExit();
                state = initState();
                state.onEnter();
                return;
            }

            while (b.hasRemaining()) {
                ParserState next = state.onNext(b);

                if(next != state) {
                    state.onExit();
                    next.onEnter();
                    state = next;
                }
            }
        } catch (RxParserException rde) {
            dispose();
        }
    }

    @Override
    public void onComplete() {
        if (disposed) {
            return;
        }
        downstream.onComplete();
    }

    @Override
    public void onError(Throwable t) {
        if (disposed) {
            return;
        }
        downstream.onError(t);
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            upstream.dispose();
            downstream.onError(new Throwable("disposed"));
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * terminate may be called by a child class to terminate the deserialization.
     */
    protected void terminate() {
        if (!disposed) {
            disposed = true;
            upstream.dispose();
            downstream.onComplete();
        }
    }
}