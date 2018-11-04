package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import java.io.IOException;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.NullBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockBLOBParser {

    static CborParser getParser(BlockBLOB block, BLOBFactory factory, Log logger) {
        return CBOR.parser()
                .cbor_parse_byte_string(
                        (p, ___, size) -> {
                            logger.v(TAG, ".. blob_byte_string_size=" + size);
                            try {
                                block.data = factory.createBLOB((int) size);
                            } catch (BLOBFactory.BLOBFactoryException sfe) {
                                logger.v(TAG, ".. blob_create=NullBLOB");
                                block.data = new NullBLOB();
                            }
                            p.setReg(3, block.data.getWritableBLOB());
                        },
                        (p, chunk) -> {
                            logger.v(TAG, ".. blob_byte_chunk_size=" + chunk.remaining());
                            if (p.getReg(3) != null) {
                                try {
                                    p.<WritableBLOB>getReg(3).write(chunk);
                                } catch (WritableBLOB.BLOBOverflowException | IOException io) {
                                    logger.v(TAG, ".. blob_write_error=" + io.getMessage());
                                    p.<WritableBLOB>getReg(3).close();
                                    p.setReg(3, null);
                                }
                            }
                        },
                        (p) -> {
                            logger.v(TAG, ".. blob_byte_string_finish");
                            if (p.getReg(3) != null) {
                                p.<WritableBLOB>getReg(3).close();
                            }
                        });
    }

}
