package io.left.rightmesh.ldcp;

import io.left.rightmesh.ldcp.messages.ResponseMessage;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.librxtcp.RxTCP;

/**
 * LdcpRequest is used to send an LDCP response to a client.
 *
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

    /**
     * Configure the response to be OK.
     *
     * @return this LdcpResponse
     */
    // CHECKSTYLE IGNORE AbbreviationAsWordInName MethodName
    public static LdcpResponse OK() {
        ResponseMessage responseMessage = new ResponseMessage(ResponseMessage.ResponseCode.OK);
        return new LdcpResponse(responseMessage);
    }
    // CHECKSTYLE END IGNORE

    /**
     * Configure the response to be an ERROR.
     *
     * @return this LdcpResponse
     */
    // CHECKSTYLE IGNORE AbbreviationAsWordInName MethodName
    public static LdcpResponse ERROR() {
        ResponseMessage responseMessage = new ResponseMessage(ResponseMessage.ResponseCode.ERROR);
        return new LdcpResponse(responseMessage);
    }
    // CHECKSTYLE END IGNORE

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
