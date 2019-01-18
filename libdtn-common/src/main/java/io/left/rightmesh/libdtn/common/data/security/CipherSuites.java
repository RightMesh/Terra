package io.left.rightmesh.libdtn.common.data.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

/**
 * CipherSuites lists all authorized cipher.
 *
 * @author Lucien Loiseau on 03/11/18.
 */
public enum CipherSuites {

    BIB_HMAC256_SHA256(1),
    BCB_AES_GCM_256(2),
    BCB_ARC4(3),
    BCB_AES128_CBC_PKCS5(5),
    BIB_SHA256(6);

    int id;

    CipherSuites(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Find the cipher from its cipherId.
     *
     * @param id of the cipher.
     * @return the corresponding cipher.
     * @throws NoSuchAlgorithmException if this id is not found.
     */
    public static CipherSuites fromId(int id) throws NoSuchAlgorithmException {
        for (CipherSuites type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new NoSuchAlgorithmException();
    }

    /**
     * returns the number of expected result for this security block.
     *
     * @param cipherSuiteId id of the ciphersuite.
     * @return the number of expected SecurityResult.
     * @throws NoSuchAlgorithmException if the cipher Id is unknown.
     */
    public static int expectedResults(int cipherSuiteId) throws NoSuchAlgorithmException {
        switch (CipherSuites.fromId(cipherSuiteId)) {
            case BIB_SHA256:
                return 1;
            case BIB_HMAC256_SHA256:
                return 1;
            default:
                return 0;
        }
    }


    /**
     * returns the digest for this CipherSuites.
     *
     * @return MessageDigest
     * @throws NoSuchAlgorithmException if this is not a Integrity operation
     */
    public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        switch (this) {
            case BIB_SHA256:
                return MessageDigest.getInstance("SHA-256");
            default:
                throw new NoSuchAlgorithmException();
        }
    }


    /**
     * returns the cipher for this CipherSuites.
     *
     * @return Cipher
     * @throws NoSuchAlgorithmException if this is not a confidentiality operation
     * @throws NoSuchPaddingException if the padding algorithm doesn't exist.
     */
    public Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        switch (this) {
            case BCB_ARC4:
                return Cipher.getInstance("ARCFOUR/ECB");
            case BCB_AES_GCM_256:
                return Cipher.getInstance("AES/GCM/NoPadding");
            case BCB_AES128_CBC_PKCS5:
                return Cipher.getInstance("AES/CBC/PKCS5Padding");
            default:
                throw new NoSuchAlgorithmException();
        }
    }
}
