package io.left.rightmesh.libdtn.common.data.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public interface CipherSuites {

    /* authentication */
    enum BabCipherSuites {

        BIB_HMAC_SHA256(2);

        int id;
        BabCipherSuites(int id) {
            this.id = id;
        }

        public Mac getMac() throws NoSuchAlgorithmException {
            switch(this) {
                case BIB_HMAC_SHA256:
                    return Mac.getInstance("HmacSHA256");
                default:
                    throw new NoSuchAlgorithmException();
            }
        }

        public static BabCipherSuites fromId(int id) throws NoSuchAlgorithmException {
            for (BabCipherSuites type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new NoSuchAlgorithmException();
        }
    }

    /* integrity */
    enum BibCipherSuites {

        BIB_HMAC_SHA256(2);

        int id;
        BibCipherSuites(int id) {
            this.id = id;
        }

        public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
            switch(this) {
                case BIB_HMAC_SHA256:
                    return MessageDigest.getInstance("SHA256");
                default:
                    throw new NoSuchAlgorithmException();
            }
        }

        public static BibCipherSuites fromId(int id) throws NoSuchAlgorithmException {
            for (BibCipherSuites type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new NoSuchAlgorithmException();
        }
    }

    /* confidentiality */
    enum BcbCipherSuites {

        BCB_ARC4(3),
        BCB_AES128_CBC_PKCS5(5);

        int id;
        BcbCipherSuites(int id) {
            this.id = id;
        }

        public Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
            switch(this) {
                case BCB_ARC4:
                    return Cipher.getInstance("ARCFOUR/ECB");
                case BCB_AES128_CBC_PKCS5:
                    return Cipher.getInstance("AES/CBC/PKCS5Padding");
                default:
                    throw new NoSuchAlgorithmException();
            }
        }

        public static BcbCipherSuites fromId(int id) throws NoSuchAlgorithmException  {
            for (BcbCipherSuites type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new NoSuchAlgorithmException();
        }
    }


}
