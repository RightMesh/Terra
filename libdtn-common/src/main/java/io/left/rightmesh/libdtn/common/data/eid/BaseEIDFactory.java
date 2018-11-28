package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.left.rightmesh.libdtn.common.data.eid.EID.RFC3986URIRegExp;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class BaseEIDFactory implements EIDFactory, CLAEIDParser {

    private EIDSspParser ipnParser = new IPN.IPNParser();
    private EIDSspParser dtnParser = new DTN.DTNParser();
    private EIDSspParser apiParser = new API.APIParser();

    private boolean throwExceptionForUnknownCLAEID = false;

    public BaseEIDFactory() {
    }

    public BaseEIDFactory(boolean throwExceptionForUnknownCLAEID) {
        this.throwExceptionForUnknownCLAEID = throwExceptionForUnknownCLAEID;
    }

    @Override
    public String getIANAScheme(int iana_scheme) throws UnknownIanaNumber {
        switch (iana_scheme) {
            case API.EID_API_IANA_VALUE:
                return API.EID_API_SCHEME;
            case DTN.EID_DTN_IANA_VALUE:
                return DTN.EID_DTN_SCHEME;
            case IPN.EID_IPN_IANA_VALUE:
                return IPN.EID_IPN_SCHEME;
            case CLAEID.EID_CLA_IANA_VALUE:
                return CLAEID.EID_CLA_SCHEME;
        }
        throw new UnknownIanaNumber(iana_scheme);
    }

    @Override
    public EID create(String str) throws EIDFormatException {
        String scheme;
        String ssp;
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(str);
        if (m.find()) {
            scheme = m.group(2);
            String slashedAuthority = m.group(3) == null ? "" : m.group(3);
            String authority = m.group(4) == null ? "" : m.group(4);
            String path = m.group(5) == null ? "" : m.group(5);
            String undef = m.group(6) == null ? "" : m.group(6);
            String query = m.group(7) == null ? "" : m.group(7);
            String related = m.group(8) == null ? "" : m.group(8);
            String fragment = m.group(9) == null ? "" : m.group(9);
            ssp = slashedAuthority + path + undef + query + related;
        } else {
            throw new EIDFormatException("not a URI");
        }
        return create(scheme, ssp);
    }

    @Override
    public EID create(String scheme, String ssp) throws EIDFormatException {
        if (scheme.equals(API.EID_API_SCHEME)) {
            return apiParser.create(ssp);
        }
        if (scheme.equals(DTN.EID_DTN_SCHEME)) {
            return dtnParser.create(ssp);
        }
        if (scheme.equals(IPN.EID_IPN_SCHEME)) {
            return ipnParser.create(ssp);
        }
        if (scheme.equals(CLAEID.EID_CLA_SCHEME)) {
            final String regex = "^([^:/?#]+):([^/?#]+)(/.*)?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (!m.find()) {
                throw new EIDFormatException("not a BaseCLAEID");
            }
            String cl_name = m.group(1);
            String cl_specific = m.group(2);
            String cl_sink = m.group(3) == null ? "" : m.group(3);

            return create(cl_name, cl_specific, cl_sink);
        }
        throw new UnknownEIDScheme(scheme);
    }

    @Override
    public CLAEID create(String cl_name, String cl_specific, String cl_sink) throws EIDFormatException {
        if(throwExceptionForUnknownCLAEID) {
            throw new UnknownCLName("cl_name unknown: " + cl_name);
        } else {
            return new UnknownCLAEID(cl_name, cl_specific, cl_sink);
        }
    }


}
