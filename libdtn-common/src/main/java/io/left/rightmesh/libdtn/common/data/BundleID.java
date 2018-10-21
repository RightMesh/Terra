package io.left.rightmesh.libdtn.common.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * BundleID uniquely identifies a Bundle.
 *
 * @author Lucien Loiseau on 27/07/18.
 */
public class BundleID {

    protected String bid;

    private BundleID(String bid) {
        this.bid = bid;
    }

    public static BundleID create(String bid) {
        return new BundleID(bid);
    }

    public static BundleID create(PrimaryBlock bundle) {
        return create(bundle.source, bundle.creationTimestamp, bundle.sequenceNumber);
    }

    public static BundleID create(EID source, long timestamp, long sequence) {
        String[] algorithms = {"SHA-256", "SHA-512", "SHA-384", "SHA-1", "MD5"};
        for (String algo : algorithms) {
            try {
                MessageDigest md = MessageDigest.getInstance(algo);
                md.update(source.getEIDString().getBytes());
                md.update(String.valueOf(timestamp).getBytes());
                md.update(String.valueOf(sequence).getBytes());
                String bid = UUID.nameUUIDFromBytes(md.digest()).toString();
                return new BundleID(bid);
            } catch (NoSuchAlgorithmException ignore) {
                // that should never happen
            }
        }

        // no algorithm were found so we make something up (that should never happen though)
        // FIXME this method provides a unique bid but can be long, should probably use some XOR
        String sb = new StringBuilder("s=" + source.getEIDString())
                + "t=" + Long.toString(timestamp)
                + "s=" + Long.toString(sequence);
        String bid = Base64.getEncoder().encodeToString(sb.getBytes());
        return new BundleID(bid);
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

    public String getBIDString() {
        return bid;
    }
}
