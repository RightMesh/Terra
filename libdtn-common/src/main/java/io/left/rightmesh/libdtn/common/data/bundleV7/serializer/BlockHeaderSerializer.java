package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockHeader;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockHeaderSerializer {

    static CborEncoder encode(BlockHeader block) {
        return CBOR.encoder()
                .cbor_start_array(getItemCount(block))
                .cbor_encode_int(block.type)
                .cbor_encode_int(block.number)
                .cbor_encode_int(block.procV7flags)
                .cbor_encode_int(block.crcType.ordinal());
    }


    private static int getItemCount(BlockHeader block) {
        int length = 5; // 6 in draft-BPbis v10, currently v11
        if (block.crcType != BlockHeader.CRCFieldType.NO_CRC) {
            length++;
        }
        return length;
    }
}
