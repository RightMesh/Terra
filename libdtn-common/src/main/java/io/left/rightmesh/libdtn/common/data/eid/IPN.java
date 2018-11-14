package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class IPN extends BaseEID {

    public int node_number;
    public int service_number;

    public static IPN create(String ssp) throws EIDFormatException {
        final String regex = "^([0-9]+)\\.([0-9]+)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(ssp);
        if (m.find()) {
            String node = m.group(1);
            String service = m.group(2);
            int node_number = Integer.valueOf(node);
            int service_number = Integer.valueOf(service);
            return new IPN(node_number, service_number);
        } else {
            throw new EIDFormatException("not an IPN");
        }
    }

    public IPN(int node, int service) {
        this.node_number = node;
        this.service_number = service;
    }

    @Override
    public EID copy() {
        return new IPN(node_number, service_number);
    }

    @Override
    public boolean matches(EID other) {
        if (other == null) {
            return false;
        }
        if (other instanceof IPN) {
            IPN o = (IPN) other;
            return (node_number == o.node_number && service_number == o.service_number);
        } else {
            return false;
        }
    }

    @Override
    public int IANA() {
        return EID_IPN_IANA_VALUE;
    }

    @Override
    public EIDScheme getSchemeCode() {
        return EIDScheme.IPN;
    }

    @Override
    public String getScheme() {
        return "ipn";
    }

    @Override
    public String getSsp() {
        return node_number+"."+service_number;
    }

    public int getNodeNumber() {
        return node_number;
    }

    public int getServiceNumber() {
        return service_number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof IPN) {
            return this.node_number == ((IPN) o).node_number
                    && this.service_number == ((IPN) o).service_number;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + node_number;
        hash = hash * 31 + service_number;
        return hash;
    }

    @Override
    public String toString() {
        return getScheme()+":"+getSsp();
    }
}