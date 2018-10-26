package io.left.rightmesh.module.aa.ldcp.messages;

import java.util.HashMap;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class ResponseMessage {

    public enum ResponseCode {
        OK(0),
        ERROR(1);

        int code;

        ResponseCode(int id) {
            this.code = id;
        }

        public static ResponseMessage.ResponseCode fromId(int id) {
            for (ResponseMessage.ResponseCode type : values()) {
                if (type.code == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public ResponseCode code;
    public HashMap<String, String> fields = new HashMap<>();
    public Object body;

    public ResponseMessage(ResponseCode code) {
        this.code = code;
    }

}
