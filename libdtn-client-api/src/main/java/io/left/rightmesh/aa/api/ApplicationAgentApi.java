package io.left.rightmesh.aa.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.reactivex.Single;

import java.util.Set;

/**
 * ApplicationAgentApi exposes the public API that the application layer can use to interact
 * with the LibDTN library.
 *
 * @author Lucien Loiseau on 26/10/18.
 */
public interface ApplicationAgentApi {

    class RegistrarException extends Exception {
        public RegistrarException() {
        }

        public RegistrarException(String msg) {
            super(msg);
        }
    }

    class RegistrarDisabled extends RegistrarException {
    }

    class SinkAlreadyRegistered extends RegistrarException {
    }

    class RegistrationAlreadyActive extends RegistrarException {
    }

    class SinkNotRegistered extends RegistrarException {
    }

    class BadCookie extends RegistrarException {
    }

    class BundleNotFound extends RegistrarException {
    }

    class NullArgument extends RegistrarException {
    }

    /**
     * Check whether a sink is registered or not.
     *
     * @param sink identifying this AA
     * @return true if the AA is registered, false otherwise
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows NullArgument if sink is null
     */
    Single<Boolean> isRegistered(String sink);

    /**
     * Register a passive pull-based registration. A cookie is returned that can be used
     * to pull data passively.
     *
     * @param sink to register
     * @return a RegistrationHandler if registered, nu
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkAlreadyRegistered if the sink is already registered by another AA
     * @rxthrows NullArgument if sink is null
     */
    Single<String> register(String sink);

    /**
     * Register an active registration. The cookie for this registration is returned.
     *
     * @param sink to register
     * @param cb registration callback
     * @return a RegistrationHandler if registered
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkAlreadyRegistered if the sink is already registered by another AA
     * @rxthrows NullArgument if sink or the cb is null
     */
    Single<String> register(String sink, ActiveRegistrationCallback cb);

    /**
     * Unregister an application agent.
     *
     * @param sink identifying the AA to be unregistered
     * @param cookie cookie for this registration
     * @return true if the AA was unregister, false otherwise
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows NullArgument if sink is null or the cookie is null
     */
    Single<Boolean> unregister(String sink, String cookie);

    /**
     * Send data using the services of the Bundle Protocol from a registered application-agent.
     *
     * @param sink registered sink for the AA
     * @param cookie for the registration
     * @param bundle to send
     * @return true if the bundle is queued, false otherwise
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows NullArgument if sink is null or the cookie is null
     */
    Single<Boolean> send(String sink, String cookie, Bundle bundle);

    /**
     * Send data using the services of the Bundle Protocol from an anonymous application-agent.
     *
     * @param bundle to send
     * @return true if the bundle is queued, false otherwise
     */
    Single<Boolean> send(Bundle bundle);

    /**
     * Check how many bundles are queued for retrieval for a given sink.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return a list with all the bundle ids.
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows NullArgument if sink or the cookie is null
     */
    Set<BundleId> checkInbox(String sink, String cookie);

    /**
     * get a specific bundle but does not mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @param bundleId id of the bundle to request
     * @return number of data waiting to be retrieved
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows BundleNotFound if the bundle does not exist
     * @rxthrows NullArgument if sink or the cookie is null
     */
    Single<Bundle> get(String sink, String cookie, BundleId bundleId);

    /**
     * fetch a specific bundle and mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @param bundleId id of the bundle to request
     * @return number of data waiting to be retrieved
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows BundleNotFound if the bundle does not exist
     * @rxthrows NullArgument if sink or the cookie is null
     */
    Single<Bundle> fetch(String sink, String cookie, BundleId bundleId);

    /**
     * Turn a registration active. If the registration was already active it does nothing,
     * otherwise it sets the active callbacks of the registration to the one provided as an
     * argument. Fail if the registration is passive but the cookie did not match or the cb is null.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @param cb the callback for the active registration
     * @return true if the registration was successfully activated, false otherwise.
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows NullArgument if sink or the cookie is null
     */
    Single<Boolean> reAttach(String sink, String cookie, ActiveRegistrationCallback cb);

    /**
     * Turn a registration passive. If the registration was already passive it does nothing,
     * Fails if the registration is active but the cookie did not match.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @return true if the registration was successfully activated, false otherwise.
     * @rxthrows RegistrarDisabled if the registration service is disabled
     * @rxthrows SinkNotRegistered if the sink does not exist
     * @rxthrows BadCookie if the cookie supplied is invalid
     * @rxthrows NullArgument if sink or the cookie is null
     */
    Single<Boolean> setPassive(String sink, String cookie);
}

