package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public abstract class BaseEID implements EID {

    public String getEIDString() {
        return getScheme() + ":" + getSsp();
    }

    public void checkValidity() throws EIDFormatException {
        if(!EID.isValidEID(this.getEIDString())) {
            throw new EIDFormatException("not an URI");
        }
    }
}
