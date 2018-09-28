package io.left.rightmesh.libdtn.data.bundleV6;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.CanonicalBlock;
import io.left.rightmesh.libdtn.data.BlockBLOB;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.Dictionary;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class serialize a Bundle to an Observable following the RFC5050 frame format.
 * See {@see RxParser} for the deserialization process.
 *
 * <pre>
 *  Primary Bundle CanonicalBlock
 *  +----------------+----------------+----------------+----------------+
 *  |    Version     |                  Proc. BundleFlags (*)           |
 *  +----------------+----------------+----------------+----------------+
 *  |                          CanonicalBlock length (*)                         |
 *  +----------------+----------------+---------------------------------+
 *  |   Destination scheme offset (*) |     Destination SSP offset (*)  |
 *  +----------------+----------------+----------------+----------------+
 *  |      Source scheme offset (*)   |        Source SSP offset (*)    |
 *  +----------------+----------------+----------------+----------------+
 *  |    Report-to scheme offset (*)  |      Report-to SSP offset (*)   |
 *  +----------------+----------------+----------------+----------------+
 *  |    Custodian scheme offset (*)  |      Custodian SSP offset (*)   |
 *  +----------------+----------------+----------------+----------------+
 *  |                    Creation Timestamp time (*)                    |
 *  +---------------------------------+---------------------------------+
 *  |             Creation Timestamp sequence number (*)                |
 *  +---------------------------------+---------------------------------+
 *  |                           Lifetime (*)                            |
 *  +----------------+----------------+----------------+----------------+
 *  |                        Dictionary length (*)                      |
 *  +----------------+----------------+----------------+----------------+
 *  |                  Dictionary byte array (variable)                 |
 *  +----------------+----------------+---------------------------------+
 *  |                      [Fragment offset (*)]                        |
 *  +----------------+----------------+---------------------------------+
 *  |              [Total application data unit length (*)]             |
 *  +----------------+----------------+---------------------------------+
 *
 *
 *  Bundle Payload CanonicalBlock
 *  +----------------+----------------+----------------+----------------+
 *  |  CanonicalBlock type    | Proc. Flags (*)|        CanonicalBlock length(*)          |
 *  +----------------+----------------+----------------+----------------+
 *  /                     Bundle Payload (variable)                     /
 *  +-------------------------------------------------------------------+
 *
 * Generic CanonicalBlock Layout without EID Reference List
 *  +-----------+-----------+-----------+-----------+
 *  |CanonicalBlock type | CanonicalBlock processing ctrl flags (SDNV)|
 *  +-----------+-----------+-----------+-----------+
 *  |            CanonicalBlock length  (SDNV)               |
 *  +-----------+-----------+-----------+-----------+
 *  /          CanonicalBlock body data (variable)           /
 *  +-----------+-----------+-----------+-----------+
 *
 * Generic CanonicalBlock Layout Showing Two EID References
 *  +-----------+-----------+-----------+-----------+
 *  |CanonicalBlock Type | CanonicalBlock processing ctrl flags (SDNV)|
 *  +-----------+-----------+-----------+-----------+
 *  |        EID Reference Count  (SDNV)            |
 *  +-----------+-----------+-----------+-----------+
 *  |  Ref_scheme_1 (SDNV)  |    Ref_ssp_1 (SDNV)   |
 *  +-----------+-----------+-----------+-----------+
 *  |  Ref_scheme_2 (SDNV)  |    Ref_ssp_2 (SDNV)   |
 *   +-----------+-----------+-----------+-----------+
 *  |            CanonicalBlock length  (SDNV)               |
 *  +-----------+-----------+-----------+-----------+
 *  /          CanonicalBlock body data (variable)           /
 *  +-----------+-----------+-----------+-----------+
 *  </pre>
 *
 * @author Lucien Loiseau on 23/07/18.
 */
public class BundleV6Serializer {

    public static final byte BUNDLE_VERSION_6 = 0x06;

    private Dictionary dict;

    public BundleV6Serializer() {
        this.dict = new Dictionary();
    }

    /**
     * rebuildDictionary will reset current dictionary and rebuild it using EID from the
     * {@see Bundle} given as a parameter.
     *
     * @param bundle to be used to reconstruct the dictionary
     */
    void rebuildDictionary(Bundle bundle) {
        dict.clear();
        dict.add(bundle);
    }

    /**
     * Turn a Bundle into a serialized Flowable of ByteBuffer.
     *
     * @param bundle to serialize
     * @return Flowable
     */
    public Flowable<ByteBuffer> serialize(Bundle bundle) {
        rebuildDictionary(bundle);
        Flowable<ByteBuffer> ret = serialize((PrimaryBlock) bundle);
        for (CanonicalBlock block : bundle.getBlocks()) {
            ret = ret.concatWith(serialize(block));
        }
        return ret;
    }

    /**
     * Serialize the {@see PrimaryBlock} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     * @throws IOException if issue with OutputStream
     */
    public Flowable<ByteBuffer> serialize(PrimaryBlock block) {
        return Flowable.create(s -> {
            ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
            // Version is normally an SDNV but doesn't make a difference with a byte
            buffer.write(BUNDLE_VERSION_6);
            buffer.write(new SDNV(block.procV6Flags).getBytes());

            // fill a buffer with the rest of the remaining header and then we can figure the length
            ByteArrayDataOutput tmp = ByteStreams.newDataOutput();
            try {
                tmp.write(new SDNV(dict.getOffset(block.destination.scheme)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.destination.ssp)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.source.scheme)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.source.ssp)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.reportto.scheme)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.reportto.ssp)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.custodian.scheme)).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.custodian.ssp)).getBytes());
                tmp.write(new SDNV(block.creationTimestamp).getBytes());
                tmp.write(new SDNV(block.sequenceNumber).getBytes());
                tmp.write(new SDNV(block.lifetime).getBytes());

                byte[] dictionnary = dict.getBytes();
                tmp.write(new SDNV(dictionnary.length).getBytes());
                tmp.write(dictionnary);

                if (block.getV6Flag(PrimaryBlock.BundleV6Flags.FRAGMENT)) {
                    tmp.write(new SDNV(block.fragmentOffset).getBytes());
                    tmp.write(new SDNV(block.appDataLength).getBytes());
                }
            } catch (Dictionary.EntryNotFoundException e) {
                throw new IOException("entry not found in dictionary");
            }

            byte[] tmpByteArray = tmp.toByteArray();
            buffer.write(new SDNV(tmpByteArray.length).getBytes());
            buffer.write(tmpByteArray);

            s.onNext(ByteBuffer.wrap(buffer.toByteArray()));
            s.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * Serialize a {@see CanonicalBlock} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     * @throws IOException if issue with OutputStream
     */
    public Flowable<ByteBuffer> serialize(AgeBlock block) {
        ByteBuffer age = ByteBuffer.wrap(new SDNV(block.age).getBytes());
        return serialize((BlockHeader) block)
                .concatWith(
                        Flowable.just(
                                ByteBuffer.wrap(new SDNV(age.remaining()).getBytes()),
                                age));
    }

    public Flowable<ByteBuffer> serialize(ScopeControlHopLimitBlock block) {
        ByteBuffer count = ByteBuffer.wrap(new SDNV(block.count).getBytes());
        ByteBuffer limit = ByteBuffer.wrap(new SDNV(block.limit).getBytes());
        return serialize((BlockHeader) block)
                .concatWith(
                        Flowable.just(
                                ByteBuffer.wrap(new SDNV(count.remaining() + limit.remaining()).getBytes()),
                                count,
                                limit));
    }

    public Flowable<ByteBuffer> serialize(BlockBLOB block) {
        return serialize((BlockHeader) block)
                .concatWith(
                        Flowable.just(ByteBuffer.wrap(new SDNV(block.data.size()).getBytes())))
                .concatWith(block.data.observe());
    }

    /**
     * Serialize a {@see BlockHeader} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     * @throws IOException if issue with OutputStream
     */
    public Flowable<ByteBuffer> serialize(BlockHeader block) {
        return Flowable.create(s -> {
            ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
            buffer.write(new SDNV(block.type).getBytes());
            buffer.write(new SDNV(block.procV6flags).getBytes());

            if (block.getV6Flag(BlockHeader.BlockV6Flags.BLOCK_CONTAINS_EIDS)) {
                try {
                    buffer.write(new SDNV(block.eids.size()).getBytes());
                    for (EID eid : block.eids) {
                        buffer.write(new SDNV(dict.getOffset(eid.scheme)).getBytes());
                        buffer.write(new SDNV(dict.getOffset(eid.ssp)).getBytes());
                    }
                } catch (Dictionary.EntryNotFoundException e) {
                    // it should never happen or there was an issue with rebuildDictionary
                    // FIXME we should probably do something... maybe check beforehand?
                }
            }
            s.onNext(ByteBuffer.wrap(buffer.toByteArray()));
            s.onComplete();
        }, BackpressureStrategy.BUFFER);
    }
}
