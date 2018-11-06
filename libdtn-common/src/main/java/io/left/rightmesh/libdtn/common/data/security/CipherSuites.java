package io.left.rightmesh.libdtn.common.data.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

/**
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

    public static CipherSuites fromId(int id) throws NoSuchAlgorithmException {
        for (CipherSuites type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new NoSuchAlgorithmException();
    }

    public static int expectedResults(int cipherSuiteId) throws NoSuchAlgorithmException {
        switch (CipherSuites.fromId(cipherSuiteId)) {
            case BIB_SHA256:
                return 1;
            case BIB_HMAC256_SHA256:
                return 1;
        }
        return 0;
    }

    public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        switch (this) {
            case BIB_SHA256:
                return MessageDigest.getInstance("SHA-256");
            default:
                throw new NoSuchAlgorithmException();
        }
    }

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
