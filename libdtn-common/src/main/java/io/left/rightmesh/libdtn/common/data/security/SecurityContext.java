package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * SecurityContext provides all the BPSec security context.
 *
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

    MessageDigest initDigestForIntegrity(int cipherSuiteId, Eid securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    MessageDigest initDigestForVerification(int cipherSuiteId, Eid securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    Cipher initCipherForEncryption(int cipherSuiteId, Eid securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

    Cipher initCipherForDecryption(int cipherSuiteId, Eid securitySource)
            throws NoSecurityContextFound, NoSuchAlgorithmException, NoSuchPaddingException;

}
