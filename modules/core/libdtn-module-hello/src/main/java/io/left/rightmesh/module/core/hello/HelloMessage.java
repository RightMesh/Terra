package io.left.rightmesh.module.core.hello;

import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.EidItem;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.EidSerializer;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.utils.NullLogger;

/**
 * @author Lucien Loiseau on 13/11/18.
 */
public class HelloMessage implements CborParser.ParseableItem {

    public List<Eid> eids;

    HelloMessage(EidFactory eidFactory) {
        this.eidFactory = eidFactory;
        eids = new LinkedList<>();
    }

    private EidFactory eidFactory;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_parse_linear_array(
                        () -> new EidItem(eidFactory, new NullLogger()),
                        (__, ___, size) -> {},
                        (__, ___, item) -> eids.add(item.eid),
                        (__, ___, ____) -> {});
    }

    public CborEncoder encode() {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(eids.size());

        for(Eid eid : eids) {
            enc.merge(EidSerializer.encode(eid));
        }

        return enc;
    }
}
