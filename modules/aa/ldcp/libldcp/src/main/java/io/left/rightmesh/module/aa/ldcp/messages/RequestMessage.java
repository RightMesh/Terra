package io.left.rightmesh.module.aa.ldcp.messages;

import java.util.HashMap;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class RequestMessage {

    public enum RequestCode {
        GET(0),
        POST(1);

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
    public String path;
    public HashMap<String, String> fields = new HashMap<>();
    public Object body;

    public RequestMessage(RequestCode code) {
        this.code = code;
    }

}
