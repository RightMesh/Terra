package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EidIpn is a class of Eid whose scheme is "ipn". it identifies both endpoint node and
 * endpoint service with an integer.
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public class EidIpn extends BaseEid {

    public static final int EID_IPN_IANA_VALUE = 2;
    public static final String EID_IPN_SCHEME = "ipn";

    public int nodeNumber;
    public int serviceNumber;

    /**
     * Class to create a new IpnEid after parsing the ipn specific part.
     */
    public static class IpnParser implements EidSspParser {
        @Override
        public Eid create(String ssp) throws EidFormatException {
            final String regex = "^([0-9]+)\\.([0-9]+)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (m.find()) {
                String node = m.group(1);
                String service = m.group(2);
                int nodeNumber = Integer.valueOf(node);
                int serviceNumber = Integer.valueOf(service);
                return new EidIpn(nodeNumber, serviceNumber);
            } else {
                throw new EidFormatException("not an EidIpn");
            }
        }
    }

    public EidIpn(int node, int service) {
        this.nodeNumber = node;
        this.serviceNumber = service;
    }

    @Override
    public Eid copy() {
        return new EidIpn(nodeNumber, serviceNumber);
    }

    @Override
    public boolean matches(Eid other) {
        if (other == null) {
            return false;
        }
        if (other instanceof EidIpn) {
            EidIpn o = (EidIpn) other;
            return (nodeNumber == o.nodeNumber && serviceNumber == o.serviceNumber);
        } else {
            return false;
        }
    }

    @Override
    public int ianaNumber() {
        return EID_IPN_IANA_VALUE;
    }

    @Override
    public String getScheme() {
        return EID_IPN_SCHEME;
    }

    @Override
    public String getSsp() {
        return nodeNumber + "." + serviceNumber;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int getServiceNumber() {
        return serviceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof EidIpn) {
            return this.nodeNumber == ((EidIpn) o).nodeNumber
                    && this.serviceNumber == ((EidIpn) o).serviceNumber;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + nodeNumber;
        hash = hash * 31 + serviceNumber;
        return hash;
    }

    @Override
    public String toString() {
        return getScheme() + ":" + getSsp();
    }
}