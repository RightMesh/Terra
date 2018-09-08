package io.left.rightmesh.libdtn.utils.rxdeserializer;

import io.left.rightmesh.libdtn.bundleV6.AsyncParser;
import io.left.rightmesh.libdtn.bundleV6.Bundle;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.nio.ByteBuffer;

/**
 * RxDeserializer is a generic class for event-driven deserialization following the state
 * pattern. It takes an Observable source and returns an Observable of deserialized item.
 *
 * <p>It isn't a Flowable and so it has no backpressure mechanism. The reasonning for this decision
 * was because it is supposed to be used to deserialize bundles received from
 * a network socket. Since the actual source is a remote peer, if any back pressure must be
 * implemented that would be on a protocol level but once bytes are received on the physical link,
 * it is better to process them as soon as possible and eventually storing them in cold storage
 * rather than letting the memory getting filled with unprocessed bytes.
 *
 * @author Lucien Loiseau on 17/08/18.
 */
public class RxDeserializer<T> extends Observable<T> {

    private Observable<? super ByteBuffer> source;
    private SubscriptionFactory factory;

    public static <T> RxDeserializer<T> create(Observable<? super ByteBuffer> source,
                                           SubscriptionFactory<T> factory) {
        return new RxDeserializer<>(source, factory);
    }

    @SuppressWarnings("unchecked")
    public static RxDeserializer<Bundle> bundle(Observable<? super ByteBuffer> source) {
        return RxDeserializer.<Bundle>create(source, AsyncParser::new);
    }

    @SuppressWarnings("unchecked")
    public RxDeserializer<Bundle> bundle() {
        return RxDeserializer.<Bundle>create((RxDeserializer<ByteBuffer>)this,
                AsyncParser::new);
    }

    protected RxDeserializer(Observable<? super ByteBuffer> source,
                             SubscriptionFactory<T> factory) {
        this.source = source;
        this.factory = factory;
    }

    @Override
    protected void subscribeActual(Observer<? super T> s) {
        DeserializerEmitter<T> emitter = factory.<T>create(s);
        s.onSubscribe((Disposable)emitter);
        source.subscribe((Observer)emitter);
    }


    /**
     * DeserializerEmitter factory.
     *
     * @param <T> deserializable item
     */
    public interface SubscriptionFactory<T> {
        DeserializerEmitter<T> create(Observer<? super T> s);
    }
}
