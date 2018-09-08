package io.left.rightmesh.libdtn.bundleV6;

import io.left.rightmesh.libdtn.utils.rxdeserializer.MetaState;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxDeserializerException;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxState;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * ScopeControlHopLimit Block is used to limit the propagation of the bundle to a maximum number
 * of hop away from the source. It contains a count value that is incremented at every hop and
 * a limit value that is set by the source. This block is described in the following ietf draft:
 * {@href https://tools.ietf.org/html/draft-fall-dtnrg-schl-00}.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class ScopeControlHopLimitBlock extends ExtensionBlock {

    public static final int type = 9;

    private long count;
    private long limit;

    public ScopeControlHopLimitBlock() {
        super(type);
        setFlag(BlockFlags.REPLICATE_IN_EVERY_FRAGMENT, true);
    }


    public long getHopsToLive() {
        return (limit < count) ? 0 : limit - count;
    }

    public void increment(long hops) {
        count += hops;
    }

    public void setLimit(long hops) {
        count = 0;
        limit = hops;
    }

    @Override
    public long getDataSize() {
        return new SDNV(count).getBytes().length + new SDNV(limit).getBytes().length;
    }

    @Override
    public Flowable<ByteBuffer> serializeBlockData() {
        return Flowable.just(
                ByteBuffer.wrap(new SDNV(count).getBytes()),
                ByteBuffer.wrap(new SDNV(limit).getBytes()));
    }

    @Override
    public RxState deserializeBlockData() {
        return deserialize_scope_control;
    }

    private RxState deserialize_scope_control = new MetaState() {
        @Override
        public RxState initState() {
            return count_sdnv;
        }

        private RxState count_sdnv = new SDNV.SDNVState() {
            @Override
            public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
                count = sdnv_value.getValue();
                changeState(limit_sdnv);
            }
        };

        private RxState limit_sdnv = new SDNV.SDNVState() {
            @Override
            public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
                limit = sdnv_value.getValue();
                done();
            }
        };
    };
}
