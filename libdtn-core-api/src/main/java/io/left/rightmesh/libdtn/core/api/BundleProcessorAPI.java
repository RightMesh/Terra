package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public interface BundleProcessorAPI {

    /**
     * Process Bundle that were received from the Application Agent
     *
     * @param bundle to process
     */
    void bundleDispatching(Bundle bundle);

    /**
     * Call this method if delivery were successfully performed from another component.
     *
     * @param bundle to process
     */
    void bundleLocalDeliverySuccessful(Bundle bundle);

    /**
     * Call this method if another component attempted to deliver a bundle but failed
     *
     * @param bundle to process
     */
    void bundleLocalDeliveryFailure(String sink, Bundle bundle);

    /**
     * Process Bundle that were received from a Convergence Layer Channel
     *
     * @param bundle to process
     */
    void bundleReception(Bundle bundle);

    /**
     * Call this method if forwarding were successfully performed from another component.
     *
     * @param bundle to process
     */
    void bundleForwardingSuccessful(Bundle bundle);

    /**
     * Call this method if another component attempted to forward this bundle but failed
     *
     * @param bundle to process
     */
    void bundleForwardingContraindicated(Bundle bundle);

}
