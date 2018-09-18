package io.left.rightmesh.libdtn.data.bundleV7;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.CborParser.ExpectedType;
import io.left.rightmesh.libcbor.rxparser.ParserEmitter;
import io.left.rightmesh.libcbor.rxparser.ParserState;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.CRC;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.data.ManifestBlock;
import io.left.rightmesh.libdtn.data.PayloadBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.data.UnknownExtensionBlock;
import io.reactivex.Observer;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Parser extends ParserEmitter<Bundle> {

    private CborParser bundleParser;

    public BundleV7Parser(Observer<? super Bundle> downstream) {
        super(downstream);
    }

    @Override
    public void onReset() {
        bundleParser = CBOR.getParser()
                .cbor_parse_custom_item(BundleItem::new, (p, __, item) -> emit(item.bundle));
    }

    @Override
    public ParserState initState() {
        onReset();
        return parseBundle;
    }

    private ParserState parseBundle = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (bundleParser.read(next)) {
                return initState();
            }
            return this;
        }
    };

    public class BundleItem implements CborParser.ParseableItem {

        Bundle bundle = null;

        @Override
        public CborParser getItemParser() {
            return CBOR.getParser()
                    .cbor_open_array((__, ___, ____) -> {
                    })
                    .cbor_parse_custom_item(PrimaryBlockItem::new, (p, __, item) -> {
                        bundle = item.b;
                    })
                    .cbor_parse_array_items(CanonicalBlockItem::new, (p, __, item) -> {
                        bundle.addBlock(item.block);
                    });
        }
    }

    public class PrimaryBlockItem implements CborParser.ParseableItem {

        Bundle b;

        @Override
        public CborParser getItemParser() {
            return CBOR.getParser()
                    .cbor_open_array((__, ___, i) -> {
                        if ((i < 8) || (i > 11)) {
                            throw new RxParserException("wrong number of element in primary block");
                        } else {
                            this.b = new Bundle();
                        }
                    })
                    .cbor_parse_int((__, ___, i) -> b.version = (int) i)
                    .cbor_parse_int((__, ___, i) -> b.procV7Flags = i)
                    .cbor_parse_int((__, ___, i) -> {
                        switch ((int) i) {
                            case 0:
                                b.crcType = CRC.CRCType.NO_CRC;
                                break;
                            case 1:
                                b.crcType = CRC.CRCType.CRC16;
                                break;
                            case 2:
                                b.crcType = CRC.CRCType.CRC32;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
                        b.destination = item.eid;
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
                        b.source = item.eid;
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
                        b.reportto = item.eid;
                    })
                    .cbor_open_array(2)
                    .cbor_parse_int((__, ___, i) -> b.creationTimestamp = i)
                    .cbor_parse_int((__, ___, i) -> b.sequenceNumber = i)
                    .cbor_parse_int((__, ___, i) -> b.lifetime = i);
        }
    }


    public class CanonicalBlockItem implements CborParser.ParseableItem {

        Block block;
        CborParser payload;

        @Override
        public CborParser getItemParser() {
            return CBOR.getParser()
                    .cbor_open_array((__, ___, i) -> {
                        if ((i != 5) && (i != 6)) {
                            throw new RxParserException("wrong number of element in canonical block");
                        }
                    })
                    .cbor_parse_int((p, __, i) -> { // block type
                        switch ((int) i) {
                            case 0:
                                block = new PayloadBlock();
                                payload = blobBlock;
                                break;
                            case 2:
                                block = new BlockIntegrityBlock();
                                payload = blockIntegrityBlock;
                                break;
                            case 4:
                                block = new ManifestBlock();
                                payload = manifestBlock;
                                break;
                            case 6:
                                block = new FlowLabelBlock();
                                payload = flowLabelBlock;
                                break;
                            case 7:
                                block = new PreviousNodeBlock();
                                payload = previousNodeBlock;
                                break;
                            case 8:
                                block = new AgeBlock();
                                payload = ageBlock;
                                break;
                            case 9:
                                block = new ScopeControlHopLimitBlock();
                                payload = scopeControlLimitBlock;
                                break;
                            default:
                                block = new UnknownExtensionBlock((int) i);
                                payload = blobBlock;
                                break;
                        }
                    })
                    .cbor_parse_int((p, __, i) -> block.number = (int) i)
                    .cbor_parse_int((p, __, i) -> block.procV7flags = i)
                    .cbor_parse_int((p, __, i) -> {
                        switch ((int) i) {
                            case 0:
                                block.crcType = CRC.CRCType.NO_CRC;
                                break;
                            case 1:
                                block.crcType = CRC.CRCType.CRC16;
                                payload.cbor_parse_int((___, ____, _____) -> {
                                });
                                break;
                            case 2:
                                block.crcType = CRC.CRCType.CRC32;
                                payload.cbor_parse_int((___, ____, _____) -> {
                                });
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                        p.insert(payload);

                    })
                    .cbor_parse_int((p, ___, i) -> {
                        // check CRC
                    });
        }

        CborParser blobBlock = CBOR.getParser();

        CborParser blockIntegrityBlock = CBOR.getParser();

        CborParser manifestBlock = CBOR.getParser();

        CborParser flowLabelBlock = CBOR.getParser();

        CborParser previousNodeBlock = CBOR.getParser();

        CborParser ageBlock = CBOR.getParser();

        CborParser scopeControlLimitBlock = CBOR.getParser();
    }

    public class EIDItem implements CborParser.ParseableItem {

        public EID eid;

        @Override
        public CborParser getItemParser() {
            return CBOR.getParser()
                    .cbor_open_array(2)
                    .cbor_parse_int((p, __, i) -> {
                        if (i == 0) { // IPN
                            p.insert(parseIPN);
                        } else { // DTN or UNKNOWN
                            p.insert(parseDTN);
                        }
                    });
        }

        CborParser parseIPN = CBOR.getParser()
                .cbor_open_array(2)
                .cbor_parse_int((___, ____, node) -> eid = EID.createIPN((int) node, 0))
                .cbor_parse_int((___, ____, service) -> ((EID.IPN) eid).service_number = (int) service);

        CborParser parseDTN = CBOR.getParser()
                .cbor_parse_generic(
                        EnumSet.of(ExpectedType.Integer, ExpectedType.TextString),
                        (___, item) -> {
                            if (CBOR.IntegerItem.ofType(item)) {
                                eid = EID.NullEID();
                            } else {
                                eid = EID.createDTN(((CBOR.TextStringItem) item).value());
                            }
                        });
    }
}
