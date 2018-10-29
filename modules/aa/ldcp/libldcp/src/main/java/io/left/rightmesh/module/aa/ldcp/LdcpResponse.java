package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.librxtcp.RxTCP;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class LdcpResponse {

    private ResponseMessage responseMessage;

    private LdcpResponse(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public static LdcpResponse wrap(ResponseMessage responseMessage) {
        return new LdcpResponse(responseMessage);
    }


    public static LdcpResponse OK() {
        ResponseMessage responseMessage = new ResponseMessage(ResponseMessage.ResponseCode.OK);
        return new LdcpResponse(responseMessage);
    }

    public static LdcpResponse ERROR() {
        ResponseMessage responseMessage = new ResponseMessage(ResponseMessage.ResponseCode.ERROR);
        return new LdcpResponse(responseMessage);
    }

    public LdcpResponse setHeader(String field, String value) {
        responseMessage.fields.put(field, value);
        return this;
    }

    public LdcpResponse setBundle(Bundle bundle) {
        responseMessage.bundle = bundle;
        return this;
    }

    public LdcpResponse setBody(String body) {
        responseMessage.body = body;
        return this;
    }

    public void send(RxTCP.Connection con) {
        con.send(responseMessage.encode());
        con.closeJobsDone();
    }

}
