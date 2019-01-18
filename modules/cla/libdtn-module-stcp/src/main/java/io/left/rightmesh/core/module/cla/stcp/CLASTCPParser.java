package io.left.rightmesh.core.module.cla.stcp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class CLASTCPParser implements ClaEidParser {

    @Override
    public BaseClaEid create(String claName, String claSpecific, String cl_sink) throws EidFormatException {
        final String regex = "^([^:/?#]+):([0-9]+)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(claSpecific);
        if (!m.find()) {
            throw new EidFormatException("not an CLASTCP Eid specific host: " + claSpecific);
        }
        String host = m.group(1);
        int port = Integer.valueOf(m.group(2));
        return new CLASTCP(host, port, cl_sink);
    }
}
