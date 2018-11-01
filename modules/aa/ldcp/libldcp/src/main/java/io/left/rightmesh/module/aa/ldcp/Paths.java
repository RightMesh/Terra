package io.left.rightmesh.module.aa.ldcp;

/**
 * @author Lucien Loiseau on 01/11/18.
 */
public interface Paths {

    /* client to registrar */
    String ISREGISTERED = "/isregistered/";
    String REGISTER = "/register/";
    String UPDATE = "/register/update/";
    String UNREGISTER = "/unregister/";
    String GETBUNDLE = "/get/bundle/";
    String FETCHBUNDLE = "/fetch/bundle/";
    String DISPATCH = "/dispatch/";

    /* registrar to client */
    String DELIVER = "/deliver/";
}
