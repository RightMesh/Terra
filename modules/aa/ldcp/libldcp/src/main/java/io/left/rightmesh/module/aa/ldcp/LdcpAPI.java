package io.left.rightmesh.module.aa.ldcp;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface LdcpAPI {

    class RegistrarException extends Exception {
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
     * Check wether a sink is registered or not
     *
     * @param sink identifying this AA
     * @return true if the AA is registered, false otherwise
     */
    Single<Boolean> isRegistered(String sink) throws RegistrarDisabled, NullArgument;

    /**
     * Register a passive pull-based registration. A cookie is returned that can be used
     * to pull data passively.
     *
     * @param sink to register
     * @return a RegistrationHandler if registered, nu
     */
    Single<String> register(String sink) throws RegistrarDisabled, SinkAlreadyRegistered, NullArgument;

    /**
     * Register an active registration. It fails If the sink is already registered.
     *
     * @param sink to register
     * @param cb callback to receive data for this registration
     * @return a cookie for this registration upon success, null otherwise.
     */
    Single<String> register(String sink, ActiveLdcpRegistrationCallback cb) throws RegistrarDisabled, SinkAlreadyRegistered, NullArgument;


    /**
     * Unregister an application agent
     *
     * @param sink identifying the AA to be unregistered
     * @param cookie cookie for this registration
     * @return true if the AA was unregister, false otherwise
     */
    Single<Boolean> unregister(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * Send data using the services of the Bundle Protocol.
     *
     * @param bundle to send
     * @return true if the bundle is queued, false otherwise
     */
    Single<Boolean> send(String sink, String cookie, Bundle bundle) throws RegistrarDisabled, BadCookie, SinkNotRegistered, NullArgument;

    /**
     * Check how many bundles are queued for retrieval for a given sink.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return a list with all the bundle ids.
     */
    Set<BundleID> checkInbox(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * get a specific bundle but does not mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return number of data waiting to be retrieved
     */
    Single<Bundle> get(String sink, String cookie, BundleID bundleID) throws RegistrarDisabled, SinkNotRegistered, BadCookie, BundleNotFound, NullArgument;

    /**
     * get a specific bundle and mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return number of data waiting to be retrieved
     */
    Single<Bundle> fetch(String sink, String cookie, BundleID bundleID) throws RegistrarDisabled, SinkNotRegistered, BadCookie, BundleNotFound, NullArgument;

    /**
     * Turn a registration active. If the registration was already active it does nothing,
     * otherwise it set the active callbacks of the registration to the one provided as an
     * argument. Fail if the registration is passive but the cookie did not match or the cb is null.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @param cb the callback for the active registration
     * @return true if the registration was successfully activated, false otherwise.
     */
    Single<Boolean> setActive(String sink, String cookie, ActiveLdcpRegistrationCallback cb) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * Turn a registration passive. If the registration was already passive it does nothing,
     * Fails if the registration is active but the cookie did not match.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @return true if the registration was successfully activated, false otherwise.
     */
    Single<Boolean> setPassive(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;
}

