package io.left.rightmesh.libdtn.common.data.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public interface SecurityContext {

    class NoSecurityContextFound extends Exception {
        public NoSecurityContextFound() {
        }
        public NoSecurityContextFound(String msg) {
            super(msg);
        }
    }

    MessageDigest initDigestForIntegrity(int cipherSuiteId, EID securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    MessageDigest initDigestForVerification(int cipherSuiteId, EID securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    Cipher initCipherForEncryption(int cipherSuiteId, EID securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    Cipher initCipherForDecryption(int cipherSuiteId, EID securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

}
