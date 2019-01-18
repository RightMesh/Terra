package io.left.rightmesh.libdtn.common.data.eid;

import static io.left.rightmesh.libdtn.common.data.eid.Eid.RFC3986_URI_REGEXP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BaseEidFactory implements EidFactory and provides a factory for all the basic Eid scheme,
 * namely "api", "dtn" and "ipn".
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public class BaseEidFactory implements EidFactory, ClaEidParser {

    private EidSspParser ipnParser = new EidIpn.IpnParser();
    private EidSspParser dtnParser = new DtnEid.DtnParser();
    private EidSspParser apiParser = new ApiEid.ApiParser();

    private boolean throwExceptionForUnknownClaEid = false;

    public BaseEidFactory() {
    }

    public BaseEidFactory(boolean throwExceptionForUnknownClaEid) {
        this.throwExceptionForUnknownClaEid = throwExceptionForUnknownClaEid;
    }

    @Override
    public String getIanaScheme(int ianaScheme) throws UnknownIanaNumber {
        switch (ianaScheme) {
            case ApiEid.EID_API_IANA_VALUE:
                return ApiEid.EID_API_SCHEME;
            case DtnEid.EID_DTN_IANA_VALUE:
                return DtnEid.EID_DTN_SCHEME;
            case EidIpn.EID_IPN_IANA_VALUE:
                return EidIpn.EID_IPN_SCHEME;
            case ClaEid.EID_CLA_IANA_VALUE:
                return ClaEid.EID_CLA_SCHEME;
            default:
                throw new UnknownIanaNumber(ianaScheme);
        }
    }

    @Override
    public Eid create(String str) throws EidFormatException {
        String scheme;
        String ssp;
        Pattern r = Pattern.compile(RFC3986_URI_REGEXP);
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
            throw new EidFormatException("not a URI");
        }
        return create(scheme, ssp);
    }

    @Override
    public Eid create(String scheme, String ssp) throws EidFormatException {
        if (scheme.equals(ApiEid.EID_API_SCHEME)) {
            return apiParser.create(ssp);
        }
        if (scheme.equals(DtnEid.EID_DTN_SCHEME)) {
            return dtnParser.create(ssp);
        }
        if (scheme.equals(EidIpn.EID_IPN_SCHEME)) {
            return ipnParser.create(ssp);
        }
        if (scheme.equals(ClaEid.EID_CLA_SCHEME)) {
            final String regex = "^([^:/?#]+):([^/?#]+)(/.*)?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (!m.find()) {
                throw new EidFormatException("not a BaseClaEid");
            }
            String clName = m.group(1);
            String clSpecific = m.group(2);
            String clSink = m.group(3) == null ? "" : m.group(3);

            return create(clName, clSpecific, clSink);
        }
        throw new UnknownEidScheme(scheme);
    }

    @Override
    public ClaEid create(String claName, String claSpecific, String claSink)
            throws EidFormatException {
        if (throwExceptionForUnknownClaEid) {
            throw new UnknownClaName("claName unknown: " + claName);
        } else {
            return new UnknownClaEid(claName, claSpecific, claSink);
        }
    }


}
