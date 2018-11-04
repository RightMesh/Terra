package io.left.rightmesh.libdtn.common.data.security;

import javax.crypto.Cipher;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public interface SecurityContext {

    class NoSecurityContextFound extends Exception {
        NoSecurityContextFound() {
        }
        NoSecurityContextFound(String msg) {
            super(msg);
        }
    }

    void initCipher(Cipher cipher, SecurityBlock block, Bundle bundle) throws NoSecurityContextFound;

}
