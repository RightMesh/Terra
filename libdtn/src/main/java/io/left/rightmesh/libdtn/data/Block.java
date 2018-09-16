package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.core.processor.ProcessingException;

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
