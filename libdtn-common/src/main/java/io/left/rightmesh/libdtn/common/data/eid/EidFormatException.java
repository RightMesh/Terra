package io.left.rightmesh.libdtn.common.data.eid;

/**
 * EidFormatException is thrown whenever EidFactory could not parse an Eid because the string
 * supplied was invalid.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public class EidFormatException extends Exception {

    public EidFormatException(String msg) {
        super(msg);
    }

}

