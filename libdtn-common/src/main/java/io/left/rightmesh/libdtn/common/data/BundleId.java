package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * BundleId uniquely identifies a Bundle.
 *
 * @author Lucien Loiseau on 27/07/18.
 */
public class BundleId {

    protected String bid;

    private BundleId(String bid) {
        this.bid = bid;
    }

    public static BundleId create(String bid) {
        return new BundleId(bid);
    }

    /**
     * Creates a BundleId.
     *
     * @param bundle to compute BundleId from.
     * @return a new BundleId object.
     */
    public static BundleId create(PrimaryBlock bundle) {
        return create(
                bundle.getSource(),
                bundle.getCreationTimestamp(),
                bundle.getSequenceNumber());
    }

    /**
     * Creates a BundleId.
     *
     * @param source of the bundle
     * @param timestamp of the bundle
     * @param sequence of the bundle
     * @return a new BundleId object.
     */
    public static BundleId create(Eid source, long timestamp, long sequence) {
        String[] algorithms = {"SHA-256", "SHA-512", "SHA-384", "SHA-1", "MD5"};
        for (String algo : algorithms) {
            try {
                MessageDigest md = MessageDigest.getInstance(algo);
                md.update(source.getEidString().getBytes());
                md.update(String.valueOf(timestamp).getBytes());
                md.update(String.valueOf(sequence).getBytes());
                String bid = UUID.nameUUIDFromBytes(md.digest()).toString();
                return new BundleId(bid);
            } catch (NoSuchAlgorithmException ignore) {
                // that should never happen
            }
        }

        // no algorithm were found so we make something up (that should never happen though)
        // FIXME this method provides a unique bid but can be long, should probably use some XOR
        String sb = new StringBuilder("s=" + source.getEidString())
                + "t=" + Long.toString(timestamp)
                + "s=" + Long.toString(sequence);
        String bid = Base64.getEncoder().encodeToString(sb.getBytes());
        return new BundleId(bid);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof BundleId) {
            BundleId id = (BundleId) o;
            return this.bid.equals(id.bid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.bid.hashCode();
    }

    /**
     * returns the BundleId in a human readable from.
     * @return a String representing the BundleId.
     */
    public String getBidString() {
        return bid;
    }
}
