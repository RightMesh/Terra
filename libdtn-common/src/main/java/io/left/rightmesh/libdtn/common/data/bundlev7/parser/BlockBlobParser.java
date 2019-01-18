package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.NullBlob;
import io.left.rightmesh.libdtn.common.data.blob.WritableBlob;
import io.left.rightmesh.libdtn.common.utils.Log;

import java.io.IOException;

/**
 * Parser for a {@link BlockBlob}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockBlobParser {

    static CborParser getParser(BlockBlob block, BlobFactory factory, Log logger) {
        return CBOR.parser()
                .cbor_parse_byte_string(
                        (parser, tags, size) -> {
                            logger.v(TAG, ".. blob_byte_string_size=" + size);
                            try {
                                block.data = factory.createBlob((int) size);
                            } catch (BlobFactory.BlobFactoryException sfe) {
                                logger.v(TAG, ".. blob_create=NullBlob");
                                block.data = new NullBlob();
                            }
                            parser.setReg(3, block.data.getWritableBlob());
                        },
                        (p, chunk) -> {
                            logger.v(TAG, ".. blob_byte_chunk_size=" + chunk.remaining());
                            logger.v(TAG, ".. chunk=" + new String(chunk.array()));
                            try {
                                p.<WritableBlob>getReg(3).write(chunk);
                            } catch (WritableBlob.BlobOverflowException | IOException io) {
                                logger.v(TAG, ".. blob_write_error=" + io.getMessage());
                                p.<WritableBlob>getReg(3).close();
                                p.setReg(3, null);
                            }
                        },
                        (p) -> {
                            logger.v(TAG, ".. blob_byte_string_finish");
                            if (p.getReg(3) != null) {
                                p.<WritableBlob>getReg(3).close();
                            }
                        });
    }

}
