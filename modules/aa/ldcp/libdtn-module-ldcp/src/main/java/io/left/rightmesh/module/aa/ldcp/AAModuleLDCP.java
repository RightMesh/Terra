package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.NullBLOB;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.DeliveryAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSPI;
import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Completable;

import static io.left.rightmesh.module.aa.ldcp.Configuration.LDCPEntry.LDCP_TCP_PORT;
import static io.left.rightmesh.module.aa.ldcp.Configuration.LDCP_TCP_PORT_DEFAULT;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class AAModuleLDCP implements ApplicationAgentAdapterSPI {

    private static final String TAG = "ldcp";

    private static class RequestException extends Exception {
        RequestException(String msg) {
            super("Request: "+msg);
        }
    }

    RegistrarAPI registrar;
    Log logger;

    public AAModuleLDCP() {
    }

    @Override
    public String getModuleName() {
        return TAG;
    }

    @Override
    public void init(RegistrarAPI api, ConfigurationAPI conf, Log logger, BLOBFactory factory) {
        int port = conf.getModuleConf(this, LDCP_TCP_PORT, LDCP_TCP_PORT_DEFAULT).value();
        this.registrar = api;
        this.logger = logger;
        logger.i(TAG, "starting a ldcp server on port " + port);
        new LdcpServer().start(port, factory, logger,
                Router.create()
                        .GET("/isregistered/", isregistered)
                        .POST("/register/", register)
                        .POST("/unregister/", unregister)
                        .GET("/get/bundle/", get)
                        .GET("/fetch/bundle/", fetch)
                        .POST("/bundle/", dispatch)
                        .POST("/register/active/", registeractive)
                        .POST("/register/passive", registerpassive));
    }

    private String checkField(RequestMessage req, String key) throws RequestException {
        if(!req.fields.containsKey(key)) {
            throw new RequestException("missing field");
        }
        return req.fields.get(key);
    }

    private RequestHandler isregistered = (req, res) ->
            Completable.create(s -> {
                try {
                    String sink = checkField(req, "sink");
                    res.setCode(registrar.isRegistered(sink) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
                    s.onComplete();
                } catch (RegistrarAPI.RegistrarException | RequestException re) {
                    s.onError(re);
                }
            });

    private RequestHandler register = (req, res) ->
            Completable.create(s -> {
                try {
                    String sink = checkField(req, "sink");
                    boolean active = checkField(req, "active").equals("true");

                    if (active) {
                        String host = checkField(req, "active-host");
                        int port = Integer.valueOf(checkField(req, "active-port"));

                        String cookie = registrar.register(sink, (bundle) ->
                                LdcpRequest.POST("/deliver/")
                                        .setBundle(bundle)
                                        .send(host, port, NullBLOB::new, logger)
                                        .flatMapCompletable(d ->
                                                d.code.equals(ResponseMessage.ResponseCode.ERROR)
                                                        ? Completable.error(new DeliveryAPI.DeliveryRefused())
                                                        : Completable.complete()));
                        res.setCode(ResponseMessage.ResponseCode.OK);
                        res.setHeader("cookie", cookie);
                        s.onComplete();
                    } else {
                        String cookie = registrar.register(sink);
                        res.setCode(ResponseMessage.ResponseCode.OK);
                        res.setHeader("cookie", cookie);
                        s.onComplete();
                    }
                } catch (RegistrarAPI.RegistrarException | RequestException re) {
                    s.onError(re);
                }
            });

    private RequestHandler unregister = (req, res) -> {
        try {
            res.setCode(registrar.isRegistered(req.fields.get("sink")) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
            return Completable.complete();
        } catch (RegistrarAPI.RegistrarException re) {
            return Completable.error(re);
        }
    };

    private RequestHandler get = (req, res) -> {
        try {
            res.setCode(registrar.isRegistered(req.fields.get("sink")) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
            return Completable.complete();
        } catch (RegistrarAPI.RegistrarException re) {
            return Completable.error(re);
        }
    };

    private RequestHandler fetch = (req, res) -> {
        try {
            res.setCode(registrar.isRegistered(req.fields.get("sink")) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
            return Completable.complete();
        } catch (RegistrarAPI.RegistrarException re) {
            return Completable.error(re);
        }
    };

    private RequestHandler dispatch = (req, res) ->
        Completable.create(s -> {
            try {
                res.setCode(registrar.send(req.bundle) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
                s.onComplete();
            } catch (RegistrarAPI.RegistrarException re) {
                logger.w(TAG, "registrar exception: "+re.getMessage());
                s.onError(re);
            }
        });

    private RequestHandler registeractive = (req, res) -> {
        try {
            res.setCode(registrar.isRegistered(req.fields.get("sink")) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
            return Completable.complete();
        } catch (RegistrarAPI.RegistrarException re) {
            return Completable.error(re);
        }
    };

    private RequestHandler registerpassive = (req, res) -> {
        try {
            res.setCode(registrar.isRegistered(req.fields.get("sink")) ? ResponseMessage.ResponseCode.OK : ResponseMessage.ResponseCode.ERROR);
            return Completable.complete();
        } catch (RegistrarAPI.RegistrarException re) {
            return Completable.error(re);
        }
    };

}
