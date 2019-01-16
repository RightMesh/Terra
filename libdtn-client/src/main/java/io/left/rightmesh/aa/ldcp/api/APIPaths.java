package io.left.rightmesh.aa.ldcp.api;

/**
 * @author Lucien Loiseau on 01/11/18.
 */
public interface APIPaths {

    /* LDCP request from api to libdtn registrar */
    String ISREGISTERED = "/isregistered/";
    String REGISTER = "/register/";
    String UPDATE = "/register/update/";
    String UNREGISTER = "/unregister/";
    String GETBUNDLE = "/get/bundle/";
    String FETCHBUNDLE = "/fetch/bundle/";
    String DISPATCH = "/dispatch/";

    /* LDCP request from libdtn registrar to api */
    String DELIVER = "/deliver/";
}
