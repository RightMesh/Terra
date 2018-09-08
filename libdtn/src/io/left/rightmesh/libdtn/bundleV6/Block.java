package io.left.rightmesh.libdtn.bundleV6;

import io.left.rightmesh.libdtn.core.processor.ProcessingException;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxState;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * Abstract generic Block object.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public abstract class Block extends BlockHeader {

    protected Block(int type) {
        super(type);
    }

    /**
     * Block Factory.
     *
     * @param type of the block to create
     * @return an instance of a block for the given type
     */
    public static Block create(int type) {
        switch (type) {
            case PayloadBlock.type:
                return new PayloadBlock();
            case AgeBlock.type:
                return new AgeBlock();
            case ScopeControlHopLimitBlock.type:
                return new ScopeControlHopLimitBlock();
            default:
                return new UnknownExtensionBlock(type);
        }
    }

    /**
     * serializeBlockData is called by AsyncSerializer.
     *
     * @return RxState instance
     */
    public abstract Flowable<ByteBuffer> serializeBlockData();

    /**
     * deserializeBlockData is called by the AsyncParser whenever the current block data
     * must be deserialized. It SHOULD only thrown RxDeserializerException if the data cannot be
     * properly deserialized. For validation and processing of this block see the other callbacks.
     *
     * @return RxState instance
     */
    public abstract RxState deserializeBlockData();


    /**
     * This is called during deserialization.
     *
     * @throws ProcessingException if there is any issue during processing
     */
    public void onBlockDataDeserialized() throws ProcessingException {
    }

    /**
     * This is called when the bundle carrying this block has reached the processing phase.
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    public boolean onBundleProcessing(Bundle bundle) throws ProcessingException {
        return false;
    }

    /**
     * This is called just before being parked into cold storage.
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    public boolean onPutOnStorage(Bundle bundle) throws ProcessingException {
        return false;
    }

    /**
     * This is called when the bundle was pulled from storage.
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    public boolean onPullFromStorage(Bundle bundle) throws ProcessingException {
        return false;
    }

    /**
     * This is called just before being queued for transmission
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processings
     */
    public boolean onPrepareForTransmission(Bundle bundle) throws ProcessingException {
        return false;
    }
}
