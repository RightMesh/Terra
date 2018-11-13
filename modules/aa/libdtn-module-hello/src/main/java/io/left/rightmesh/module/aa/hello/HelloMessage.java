package io.left.rightmesh.module.aa.hello;

import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.EIDItem;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.EIDSerializer;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.utils.NullLogger;

/**
 * @author Lucien Loiseau on 13/11/18.
 */
public class HelloMessage implements CborParser.ParseableItem {

    public List<EID> eids;

    HelloMessage() {
        eids = new LinkedList<>();
    }

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_parse_linear_array(
                        () -> new EIDItem(new NullLogger()),
                        (__, ___, size) -> {},
                        (__, ___, item) -> eids.add(item.eid),
                        (__, ___, ____) -> {});
    }

    public CborEncoder encode() {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(eids.size());

        for(EID eid : eids) {
            enc.merge(EIDSerializer.encode(eid));
        }

        return enc;
    }
}
