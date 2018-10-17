package io.left.rightmesh.libdtn.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public abstract class BaseEID implements EID {

    public String getEIDString() {
        return getScheme() + ":" + getSsp();
    }

}
