package io.left.rightmesh.core.module.cla.stcp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.left.rightmesh.libdtn.common.data.eid.BaseCLAEID;
import io.left.rightmesh.libdtn.common.data.eid.CLAEIDParser;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class CLASTCPParser implements CLAEIDParser {

    @Override
    public BaseCLAEID create(String cl_name, String cl_specific, String cl_sink) throws EIDFormatException {
        final String regex = "^([^:/?#]+):([0-9]+)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(cl_specific);
        if (!m.find()) {
            throw new EIDFormatException("not an CLASTCP EID specific host: " + cl_specific);
        }
        String host = m.group(1);
        int port = Integer.valueOf(m.group(2));
        return new CLASTCP(host, port, cl_sink);
    }
}
