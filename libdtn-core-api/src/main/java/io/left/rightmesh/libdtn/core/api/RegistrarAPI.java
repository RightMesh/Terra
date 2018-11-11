package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.core.spi.aa.ActiveRegistrationCallback;
import io.reactivex.Flowable;

/**
 * API to access the services of the Bundle Protocol from an Application Agent.
 *
 * @author Lucien Loiseau on 23/10/18.
 */
public interface RegistrarAPI {

    class RegistrarException extends Exception {
        public RegistrarException(String msg) {
            super(msg);
        }
    }

    class BundleMalformed extends RegistrarException {
        public BundleMalformed() {
            super("bundle is malformed");
        }
    }

    class RegistrarDisabled extends RegistrarException {
        public RegistrarDisabled() {
            super("registrar is disabled");
        }
    }

    class SinkAlreadyRegistered extends RegistrarException {
        public SinkAlreadyRegistered() {
            super("sink is already registered");
        }
    }

    class SinkNotRegistered extends RegistrarException {
        public SinkNotRegistered() {
            super("sink is not registered");
        }
    }

    class BadCookie extends RegistrarException {
        public BadCookie() {
            super("bad cookie");
        }
    }

    class BundleNotFound extends RegistrarException {
        public BundleNotFound() {
            super("bundle not found");
        }
    }

    class NullArgument extends RegistrarException {
        public NullArgument() {
            super("null argument is forbidden");
        }
    }

    /**
     * Check wether a sink is registered or not
     *
     * @param sink identifying this AA
     * @return true if the AA is registered, false otherwise
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws NullArgument if one of the argument is null
     */
    boolean isRegistered(String sink) throws RegistrarDisabled, NullArgument;

    /**
     * Register a passive pull-based registration. A cookie is returned that can be used
     * to pull data passively.
     *
     * @param sink to register
     * @return a cookir for this registration upon success, null otherwise
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkAlreadyRegistered if the sink is already registered
     * @throws NullArgument if one of the argument is null
     */
    String register(String sink) throws RegistrarDisabled, SinkAlreadyRegistered, NullArgument;

    /**
     * Register an active registration. It fails If the sink is already registered.
     *
     * @param sink to register
     * @param cb callback to receive data for this registration
     * @return a cookie for this registration upon success, null otherwise.
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkAlreadyRegistered if the sink is already registered
     * @throws NullArgument if one of the argument is null
     */
    String register(String sink, ActiveRegistrationCallback cb) throws RegistrarDisabled, SinkAlreadyRegistered, NullArgument;

    /**
     * Unregister an application agent
     *
     * @param sink identifying the AA to be unregistered
     * @param cookie cookie for this registration
     * @return true if the AA was unregister, false otherwise
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink to unregister is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    boolean unregister(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * Allow a registered application-agent to send data using the services of the Bundle Protocol.
     *
     * @param sink identifying the registered AA.
     * @param cookie cookie for this registration
     * @param bundle to send
     * @return true if the bundle is queued, false otherwise
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    boolean send(String sink, String cookie, Bundle bundle) throws RegistrarDisabled, BadCookie, SinkNotRegistered, NullArgument, BundleMalformed;

    /**
     * Allow an anonymous application-agent to send data using the services of the Bundle Protocol.
     *
     * @param bundle to send
     * @return true if the bundle is queued, false otherwise
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws NullArgument if one of the argument is null
     */
    boolean send(Bundle bundle)  throws RegistrarDisabled, NullArgument, BundleMalformed;


    /**
     * Check how many bundles are queued for retrieval for a given sink.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return a list with all the bundle ids.
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    Set<BundleID> checkInbox(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * get a specific bundle but does not mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @param bundleID id of the bundle to retrieve
     * @return number of data waiting to be retrieved
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws BundleNotFound if the bundle was not found
     * @throws NullArgument if one of the argument is null
     */
    Bundle get(String sink, String cookie, String bundleID) throws RegistrarDisabled, SinkNotRegistered, BadCookie, BundleNotFound, NullArgument;

    /**
     * get a specific bundle and mark it as delivered.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @param bundleID id of the bundle to retrieve
     * @return number of data waiting to be retrieved
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws BundleNotFound if the bundle was not found
     * @throws NullArgument if one of the argument is null
     */
    Bundle fetch(String sink, String cookie, String bundleID) throws RegistrarDisabled, SinkNotRegistered, BadCookie, BundleNotFound, NullArgument;

    /**
     * fetch all the bundle from the inbox.
     *
     * @param sink to check
     * @param cookie that was returned upon registration.
     * @return Flowable of BLOB
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    Flowable<Bundle> fetch(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie,NullArgument;

    /**
     * Turn a registration active. If the registration was already active it does nothing,
     * otherwise it set the active callbacks of the registration to the one provided as an
     * argument. Fail if the registration is passive but the cookie did not match or the cb is null.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @param cb the callback for the active registration
     * @return true if the registration was successfully activated, false otherwise.
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    boolean setActive(String sink, String cookie, ActiveRegistrationCallback cb) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * Turn a registration passive. If the registration was already passive it does nothing,
     * Fails if the registration is active but the cookie did not match.
     *
     * @param sink to the registration
     * @param cookie of the registration
     * @return true if the registration was successfully activated, false otherwise.
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    boolean setPassive(String sink, String cookie) throws RegistrarDisabled, SinkNotRegistered, BadCookie, NullArgument;

    /**
     * Privileged method! Turn a registration passive. If the registration was already passive it
     * does nothing.
     *
     *
     * @param sink to the registration
     * @return true if the registration was successfully activated, false otherwise.
     * @throws RegistrarDisabled if the registrar is disabled
     * @throws SinkNotRegistered if the sink is not register
     * @throws BadCookie if the cookie provided does not match registration cookie
     * @throws NullArgument if one of the argument is null
     */
    boolean setPassive(String sink) throws RegistrarDisabled, SinkNotRegistered, NullArgument;


    //todo: remove this
    String printTable();

}
