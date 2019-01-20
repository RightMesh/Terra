package io.left.rightmesh.ldcp;

import io.left.rightmesh.ldcp.messages.RequestMessage;
import io.left.rightmesh.ldcp.messages.ResponseMessage;
import io.reactivex.Completable;

import java.util.HashMap;

/**
 * A Router maps a LDCP request path to a RequestHandler.
 *
 * @author Lucien Loiseau on 26/10/18.
 */
public class Router implements RequestHandler {

    HashMap<String, RequestHandler> routes;

    private Router() {
        routes = new HashMap<>();
    }

    public static Router create() {
        return new Router();
    }

    public Router GET(String path, RequestHandler action) {
        routes.put(path, action);
        return this;
    }

    public Router POST(String path, RequestHandler action) {
        routes.put(path, action);
        return this;
    }

    @Override
    public Completable handle(RequestMessage req, ResponseMessage res) {
        try {
            for (String path : routes.keySet()) {
                if (req.path.startsWith(path)) {
                    return routes.get(path).handle(req, res);
                }
            }
            res.code = ResponseMessage.ResponseCode.ERROR;
            return Completable.complete();
        } catch (Exception e) {
            return Completable.error(e);
        }
    }
}
