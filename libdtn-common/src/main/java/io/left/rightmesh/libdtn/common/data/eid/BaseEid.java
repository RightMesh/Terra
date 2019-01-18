package io.left.rightmesh.libdtn.common.data.eid;

/**
 * BaseEid is the base implementation of an Eid.
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public abstract class BaseEid implements Eid {

    public String getEidString() {
        return getScheme() + ":" + getSsp();
    }

    /**
     * check wether or not this Eid is compatible with the URI scheme.
     *
     * @throws EidFormatException if the current Eid is not valid.
     */
    public void checkValidity() throws EidFormatException {
        if (!Eid.isValidEid(this.getEidString())) {
            throw new EidFormatException("not an URI");
        }
    }
}
