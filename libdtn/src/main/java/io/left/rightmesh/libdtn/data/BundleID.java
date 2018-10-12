package io.left.rightmesh.libdtn.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * BundleID uniquely identifies a Bundle.
 *
 * @author Lucien Loiseau on 27/07/18.
 */
public class BundleID {

    protected String bid;

    public BundleID(String bid) {
        this.bid = bid;
    }

    public BundleID(PrimaryBlock bundle) {
        this(bundle.source, bundle.creationTimestamp, bundle.sequenceNumber);
    }

    public BundleID(EID source, long timestamp, long sequence) {
        computeBundleUID(source, timestamp, sequence);
    }

    private void computeBundleUID(EID source, long timestamp, long sequence) {
        String[] algorithms = {"SHA-256", "SHA-512", "SHA-384", "SHA-1", "MD5"};
        for (String algo : algorithms) {
            try {
                MessageDigest md = MessageDigest.getInstance(algo);
                md.update(source.toString().getBytes());
                md.update(String.valueOf(timestamp).getBytes());
                md.update(String.valueOf(sequence).getBytes());
                this.bid = Base64.getEncoder().encodeToString(md.digest());
                return;
            } catch (NoSuchAlgorithmException ignore) {
                // that should never happen
            }
        }

        // no algorithm were found so we make something up (that should never happen though)
        // FIXME this method provides a unique bid but can be long, should probably use some XOR
        String sb = new StringBuilder("s=" + source.toString())
                + "t=" + Long.toString(timestamp)
                + "s=" + Long.toString(sequence);
        this.bid = Base64.getEncoder().encodeToString(sb.getBytes());
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof BundleID) {
            BundleID id = (BundleID) o;
            return this.bid.equals(id.bid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.bid.hashCode();
    }

    @Override
    public String toString() {
        return bid;
    }
}
