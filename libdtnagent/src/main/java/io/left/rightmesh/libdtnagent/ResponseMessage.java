package io.left.rightmesh.libdtnagent;

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

    public ResponseMessage.ResponseCode code;

}
