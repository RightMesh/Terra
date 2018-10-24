package io.left.rightmesh.libdtnagent;

import java.util.HashMap;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class RequestMessage {

    public enum RequestCode {
        REGISTER(0),
        UNREGISTER(1),
        GET(2),
        POST(3);

        int code;

        RequestCode(int id) {
            this.code = id;
        }

        public static RequestCode fromId(int id) {
            for (RequestCode type : values()) {
                if (type.code == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public RequestCode code;
    public HashMap<String, String> fields = new HashMap<>();
    public Object body;

    public RequestMessage(RequestCode code) {
        this.code = code;
    }

}
