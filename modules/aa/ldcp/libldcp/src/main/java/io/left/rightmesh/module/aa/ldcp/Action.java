package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Observable;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Action {

    Observable<Void> handle(RequestMessage req, ResponseMessage res);

}
