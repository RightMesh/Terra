package io.left.rightmesh.libdtn.utils.rxparser;

import io.left.rightmesh.libdtn.data.bundleV6.AsyncParser;
import io.left.rightmesh.libdtn.data.Bundle;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.nio.ByteBuffer;

/**
 * RxParser is a generic class for asynchronous parsing following the state
 * pattern. It takes an Observable of ByteBuffer as source and returns an Observable of
 * parsed item.
 *
 * <p>It isn't a Flowable and so it has no backpressure mechanism. The reasonning for this decision
 * was because it is supposed to be used to deserialize items received from a network socket.
 * Since the actual source is a remote peer, if any back pressure must be implemented that would be
 * on a protocol level but once bytes have reached current device, it is better to process
 * them as soon as possible and eventually storing them in cold storage rather than letting the
 * kernel TCP driver memory filling up with unprocessed bytes.
 *
 * @author Lucien Loiseau on 17/08/18.
 */
public class RxParser<T> extends Observable<T> {

    private Observable<? super ByteBuffer> source;
    private SubscriptionFactory factory;

    public static <T> RxParser<T> create(Observable<? super ByteBuffer> source,
                                         SubscriptionFactory<T> factory) {
        return new RxParser<>(source, factory);
    }

    @SuppressWarnings("unchecked")
    public static RxParser<Bundle> bundle(Observable<? super ByteBuffer> source) {
        return RxParser.<Bundle>create(source, AsyncParser::new);
    }

    @SuppressWarnings("unchecked")
    public RxParser<Bundle> bundle() {
        return RxParser.<Bundle>create((RxParser<ByteBuffer>)this,
                AsyncParser::new);
    }

    protected RxParser(Observable<? super ByteBuffer> source,
                       SubscriptionFactory<T> factory) {
        this.source = source;
        this.factory = factory;
    }

    @Override
    protected void subscribeActual(Observer<? super T> s) {
        ParserEmitter<T> emitter = factory.<T>create(s);
        s.onSubscribe((Disposable)emitter);
        source.subscribe((Observer)emitter);
    }


    /**
     * ParserEmitter factory.
     *
     * @param <T> deserializable item
     */
    public interface SubscriptionFactory<T> {
        ParserEmitter<T> create(Observer<? super T> s);
    }
}
