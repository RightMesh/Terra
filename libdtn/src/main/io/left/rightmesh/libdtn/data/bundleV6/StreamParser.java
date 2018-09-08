package io.left.rightmesh.libdtn.data.bundleV6;

import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.Dictionary;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.utils.rxparser.RxParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

/**
 * This class deserialize a bundle from an InputStream following the RFC5050 format. Mostly used
 * to deserialize blocks or header whereas AsyncParser deserialize whole bundle only.
 * This class is used primarily by the SQLStorage to serialize header and payload in different
 * directory.
 *
 * @author Lucien Loiseau on 28/07/18.
 */
public class StreamParser {

    private InputStream in;
    private Dictionary dict;

    public StreamParser(InputStream in) {
        this.in = in;
        this.dict = new Dictionary();
    }

    /**
     * Set the InputStream to serialize the bundle from.
     *
     * @param in InputStream to use for deserialization
     */
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * get the dictionary used for the deserialization.
     *
     * @return Dictionary the dictionary, may return null
     */
    public Dictionary getDictionary() {
        return this.dict;
    }


    /**
     * Use this dictionary during the deserialization.
     *
     * @param dict dictionary to use
     */
    public void useDictionary(Dictionary dict) {
        this.dict.clear();
        this.dict = new Dictionary(dict);
    }

    /**
     * Deserialize a whole {@see Bundle}.
     *
     * @param bundle to deserialize
     * @throws IOException if issue with InputStream
     */
    public void deserialize(Bundle bundle) throws IOException {
        deserialize((PrimaryBlock) bundle);

        Block block;
        PushbackInputStream pbis = new PushbackInputStream(in, 1);
        InputStream tmp = in;

        try {
            setInputStream(pbis);
            do {
                int type = pbis.read(); // peak the type of the next block
                pbis.unread((byte) type);

                block = Block.create(type);
                deserialize((BlockHeader) block);

            } while (!block.getFlag(BlockHeader.BlockFlags.LAST_BLOCK));
        } finally {
            setInputStream(tmp);
        }
    }

    /**
     * Deserialize a {@see Block}. If the Block EID Reference flag is set in the header,
     * it looks up the EID reference using the dictionary.
     *
     * @param block to deserialize
     * @throws IOException if issue with InputStream
     */
    public void deserialize(Block block) throws IOException {
        deserialize((BlockHeader) block);
        try {
            block.parseData().onEnter();
            byte[] buf = new byte[2048];
            while (in.read(buf) > 0) {
                block.parseData().onNext(ByteBuffer.wrap(buf));
            }
            block.parseData().onExit();
        } catch (RxParserException rde) {
            throw new IOException(rde.getMessage());
        }
    }

    /**
     * Deserialize a {@see PrimaryBlock} into block and sets the dictionary for this deserializer.
     *
     * @param block to deserialize
     * @throws IOException if issue with InputStream
     */
    public void deserialize(PrimaryBlock block) throws IOException {

        // extract fields
        block.version = (int) new SDNV(in).getValue();
        block.procFlags = new SDNV(in).getValue();
        SDNV length = new SDNV(in);
        SDNV sourceScheme = new SDNV(in);
        SDNV sourceSsp = new SDNV(in);
        SDNV destinationScheme = new SDNV(in);
        SDNV destinationSsp = new SDNV(in);
        SDNV reportToScheme = new SDNV(in);
        SDNV reportToSsp = new SDNV(in);
        SDNV custodianScheme = new SDNV(in);
        SDNV custodianSsp = new SDNV(in);
        block.creationTimestamp = new SDNV(in).getValue();
        block.sequenceNumber = new SDNV(in).getValue();
        block.lifetime = new SDNV(in).getValue();

        // extract dictionary
        int dictlength = (int) new SDNV(in).getValue();
        byte[] dictarray = new byte[dictlength];
        if (in.read(dictarray, 0, dictlength) < 0) {
            throw new IOException();
        }
        this.dict = new Dictionary(dictarray);

        // lookup the EID
        try {
            block.source = new EID(dict.lookup((int) sourceScheme.getValue()),
                    dict.lookup((int) sourceSsp.getValue()));
            block.destination = new EID(dict.lookup((int) destinationScheme.getValue()),
                    dict.lookup((int) destinationSsp.getValue()));
            block.reportto = new EID(dict.lookup((int) reportToScheme.getValue()),
                    dict.lookup((int) reportToSsp.getValue()));
            block.custodian = new EID(dict.lookup((int) custodianScheme.getValue()),
                    dict.lookup((int) custodianSsp.getValue()));
        } catch (Dictionary.EntryNotFoundException e) {
            throw new IOException();
        } catch (EID.EIDFormatException e) {
            throw new IOException();
        }

        if (block.getFlag(PrimaryBlock.BundleFlags.FRAGMENT)) {
            block.fragmentOffset = new SDNV(in).getValue();
            block.appDataLength = new SDNV(in).getValue();
        }
    }

    /**
     * Deserialize a {@see BlockHeader}. If the Block EID Reference flag is set, it looks up
     * the EID reference using the dictionary.
     *
     * @param header to deserialize
     * @throws IOException if issue with InputStream
     */
    public void deserialize(BlockHeader header) throws IOException {
        header.type = in.read();
        header.procflags = new SDNV(in).getValue();

        try {
            if (header.getFlag(BlockHeader.BlockFlags.BLOCK_CONTAINS_EIDS)) {
                long eidcount = new SDNV(in).getValue();
                for (int i = 0; i < eidcount; i++) {
                    String scheme = dict.lookup((int) new SDNV(in).getValue());
                    String ssp = dict.lookup((int) new SDNV(in).getValue());
                    header.addEID(new EID(scheme, ssp));
                }
            }
        } catch (Dictionary.EntryNotFoundException enf) {
            throw new IOException();
        } catch (EID.EIDFormatException eid) {
            throw new IOException();
        }

        header.dataSize = new SDNV(in).getValue();
    }
}
