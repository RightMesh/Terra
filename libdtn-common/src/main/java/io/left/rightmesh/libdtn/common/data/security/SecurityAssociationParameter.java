package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public interface SecurityAssociationParameter {

    int getParameterId();

    CborEncoder getValueEncoder();
    CborParser  getValueParser();
}
