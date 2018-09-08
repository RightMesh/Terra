package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.data.bundleV6.SDNV;
import io.left.rightmesh.libdtn.utils.rxparser.RxState;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * AgeBlock is a block that can be used to track lifetime for DTN node that doesn't have access to
 * UTC time but have a mean to track the elapsed time between reception and delivery of the block.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class AgeBlock extends ExtensionBlock {

    public static final int type = 10;

    long age = 0;
    long time_start;
    long time_end;

    AgeBlock() {
        super(type);
        setFlag(BlockFlags.REPLICATE_IN_EVERY_FRAGMENT, true);
        start();
    }

    AgeBlock(long age) {
        super(type);
        setFlag(BlockFlags.REPLICATE_IN_EVERY_FRAGMENT, true);
        this.age = age;
        start();
    }

    /**
     * Start aging this AgeBlock.
     */
    public void start() {
        time_start = System.nanoTime();
    }

    /**
     * Stop aging this AgeBlock.
     */
    public void stop() {
        time_end = System.nanoTime();
    }

    /** BundleV6 */
    @Override
    public long getDataSize() {
        return new SDNV(age + (time_end - time_start)).getBytes().length;
    }

    @Override
    public Flowable<ByteBuffer> serializeData() {
        return Flowable.just(
                ByteBuffer.wrap(new SDNV(age + (time_end - time_start)).getBytes()));
    }

    @Override
    public RxState parseData() {
        return deserializeAgeBlockData;
    }

    private RxState deserializeAgeBlockData = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) {
            age = sdnv_value.getValue();
        }
    };
}
