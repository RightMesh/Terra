package io.left.rightmesh.libdtn.common.data.bundleV7.processor;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public interface BlockProcessor {

    /**
     * This is called just before being queued for transmission
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processings
     */
    boolean onPrepareForTransmission(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException;

    /**
     * This is called during deserialization.
     *
     * @throws ProcessingException if there is any issue during processing
     */
    void onBlockDeserialized(CanonicalBlock block) throws ProcessingException;

    /**
     * This is called during bundle processing step 5.4 when the bundle is being processed by the
     * receiving node. Some block may modify other block and may require the bundle to be reprocess.
     * If it returns true, the whole bundle will be reprocessed.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    boolean onReceptionProcessing(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException;

    /**
     * This is called when the bundle is being parked into cold storage.
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    boolean onPutOnStorage(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException;

    /**
     * This is called when the bundle was pulled from storage.
     * If it returns true, the whole bundle will be reprocess again.
     *
     * @param bundle being processed
     * @return true if the bundle needs another processing pass, false otherwise
     * @throws ProcessingException if there is any issue during processing
     */
    boolean onPullFromStorage(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException;

}
