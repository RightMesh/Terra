package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Completable;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface RequestHandler {

    Completable handle(RequestMessage req, ResponseMessage res);

}
