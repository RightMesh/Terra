package io.left.rightmesh.libdtn.common.data;

/**
 * Base class for {@link StatusReport}.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public abstract class AdministrativeRecord {

    public int type;

    AdministrativeRecord(int type) {
        this.type = type;
    }

}