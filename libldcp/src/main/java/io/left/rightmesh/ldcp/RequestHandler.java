package io.left.rightmesh.ldcp;

import io.left.rightmesh.ldcp.messages.RequestMessage;
import io.left.rightmesh.ldcp.messages.ResponseMessage;
import io.reactivex.Completable;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface RequestHandler {

    Completable handle(RequestMessage req, ResponseMessage res);

}
