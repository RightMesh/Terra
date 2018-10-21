package io.left.rightmesh.libdtn.common.data;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public abstract class AdministrativeRecord {

    int type;

    AdministrativeRecord(int type) {
        this.type = type;
    }
}