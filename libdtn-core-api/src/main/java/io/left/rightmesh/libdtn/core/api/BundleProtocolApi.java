package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * API for the BundleProcessor.
 *
 * @author Lucien Loiseau on 25/10/18.
 */
public interface BundleProtocolApi {

    /**
     * Process a bundle for transmission.
     *
     * @param bundle to process
     */
    void bundleTransmission(Bundle bundle);

    /**
     * Dispatch a bundle (for delivery or forwarding).
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
     * Call this method if another component attempted to deliver a bundle but failed.
     *
     * @param sink to deliver the bundle to
     * @param bundle to process
     */
    void bundleLocalDeliveryFailure(String sink, Bundle bundle);

    /**
     * Process Bundle that is expired.
     *
     * @param bundle to process
     */
    void bundleExpired(Bundle bundle);

    /**
     * Process Bundle that were received from a Convergence Layer Channel.
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
}
