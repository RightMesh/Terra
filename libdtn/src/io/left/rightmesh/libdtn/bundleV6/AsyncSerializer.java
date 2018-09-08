package io.left.rightmesh.libdtn.bundleV6;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * This class serialize a Bundle to an Observable following the RFC5050 frame format.
 * See {@see RxDeserializer} for the deserialization process.
 *
 * <pre>
 *  Primary Bundle Block
 *  +----------------+----------------+----------------+----------------+
 *  |    Version     |                  Proc. BundleFlags (*)           |
 *  +----------------+----------------+----------------+----------------+
 *  |                          Block length (*)                         |
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
 *  Bundle Payload Block
 *  +----------------+----------------+----------------+----------------+
 *  |  Block type    | Proc. Flags (*)|        Block length(*)          |
 *  +----------------+----------------+----------------+----------------+
 *  /                     Bundle Payload (variable)                     /
 *  +-------------------------------------------------------------------+
 *
 * Generic Block Layout without EID Reference List
 *  +-----------+-----------+-----------+-----------+
 *  |Block type | Block processing ctrl flags (SDNV)|
 *  +-----------+-----------+-----------+-----------+
 *  |            Block length  (SDNV)               |
 *  +-----------+-----------+-----------+-----------+
 *  /          Block body data (variable)           /
 *  +-----------+-----------+-----------+-----------+
 *
 * Generic Block Layout Showing Two EID References
 *  +-----------+-----------+-----------+-----------+
 *  |Block Type | Block processing ctrl flags (SDNV)|
 *  +-----------+-----------+-----------+-----------+
 *  |        EID Reference Count  (SDNV)            |
 *  +-----------+-----------+-----------+-----------+
 *  |  Ref_scheme_1 (SDNV)  |    Ref_ssp_1 (SDNV)   |
 *  +-----------+-----------+-----------+-----------+
 *  |  Ref_scheme_2 (SDNV)  |    Ref_ssp_2 (SDNV)   |
 *   +-----------+-----------+-----------+-----------+
 *  |            Block length  (SDNV)               |
 *  +-----------+-----------+-----------+-----------+
 *  /          Block body data (variable)           /
 *  +-----------+-----------+-----------+-----------+
 *  </pre>
 *
 * @author Lucien Loiseau on 23/07/18.
 */
public class AsyncSerializer {

    private Dictionary dict;

    public AsyncSerializer() {
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
     * @throws IOException if issue with OutputStream
     */
    public Flowable<ByteBuffer> serialize(Bundle bundle) {
        rebuildDictionary(bundle);
        Flowable<ByteBuffer> ret = serialize((PrimaryBlock) bundle);
        for (Block block : bundle.getBlocks()) {
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
            buffer.write((byte) block.getVersion());
            buffer.write(new SDNV(block.procFlags).getBytes());

            // fill a buffer with the rest of the remaining header and then we can figure the length
            ByteArrayDataOutput tmp = ByteStreams.newDataOutput();
            try {
                tmp.write(new SDNV(dict.getOffset(block.getDestination().getScheme())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getDestination().getSsp())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getSource().getScheme())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getSource().getSsp())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getReportTo().getScheme())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getReportTo().getSsp())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getCustodian().getScheme())).getBytes());
                tmp.write(new SDNV(dict.getOffset(block.getCustodian().getSsp())).getBytes());
                tmp.write(new SDNV(block.getCreationTimestamp()).getBytes());
                tmp.write(new SDNV(block.getSequenceNumber()).getBytes());
                tmp.write(new SDNV(block.getLifetime()).getBytes());

                byte[] dictionnary = dict.getBytes();
                tmp.write(new SDNV(dictionnary.length).getBytes());
                tmp.write(dictionnary);

                if (block.getFlag(PrimaryBlock.BundleFlags.FRAGMENT)) {
                    tmp.write(new SDNV(block.getFragmentOffset()).getBytes());
                    tmp.write(new SDNV(block.getAppDataLength()).getBytes());
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
     * Serialize a {@see Block} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     * @throws IOException if issue with OutputStream
     */
    public Flowable<ByteBuffer> serialize(Block block) {
        return serialize((BlockHeader) block)
                .concatWith(
                        Flowable.just(ByteBuffer.wrap(new SDNV(block.getDataSize()).getBytes())))
                .concatWith(block.serializeBlockData());
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
            buffer.write(new SDNV(block.getType()).getBytes());
            buffer.write(new SDNV(block.getProcflags()).getBytes());

            if (block.getFlag(BlockHeader.BlockFlags.BLOCK_CONTAINS_EIDS)) {
                try {
                    Set<EID> eids = block.getEids();
                    buffer.write(new SDNV(eids.size()).getBytes());
                    for (EID eid : eids) {
                        buffer.write(new SDNV(dict.getOffset(eid.getScheme())).getBytes());
                        buffer.write(new SDNV(dict.getOffset(eid.getScheme())).getBytes());
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
