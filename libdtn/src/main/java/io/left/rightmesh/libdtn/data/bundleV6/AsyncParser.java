package io.left.rightmesh.libdtn.data.bundleV6;

import io.left.rightmesh.libdtn.core.processor.CoreProcessor;
import io.left.rightmesh.libdtn.core.processor.RejectedException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockBLOB;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.Dictionary;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.BundleStorage;
import io.left.rightmesh.libdtn.storage.WritableBLOB;
import io.left.rightmesh.libcbor.rxparser.BufferState;
import io.left.rightmesh.libcbor.rxparser.ParserEmitter;
import io.left.rightmesh.libcbor.rxparser.ParserState;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.reactivex.Observer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class deserialize a bundle (RFC5050) in a reactive way using an Observable as the source.
 * See {@see AsyncSerializer} for the serialization process.
 *
 * <p>Upon deserialization of the bundle's part (PrimaryBlock, BlockHeader and BlockData), a call
 * to CoreProcessor.onDeserialized will be made. If an RejectedException occurs, the bundle will
 * still be emitted (though it may be incomplete) and will be marked with the tag "rejected".
 *
 * <p>This class is mostly used for deserialization from an asynchronous source such as RxTCP.
 * to deserialize from a synchronous source (like a file), see {@see StreamParser}.
 *
 * @author Lucien Loiseau on 10/08/18.
 */
public class AsyncParser extends ParserEmitter<Bundle> {

    private Bundle bundle = null;
    private long sourceScheme;
    private long sourceSsp;
    private long destinationScheme;
    private long destinationSsp;
    private long reportToScheme;
    private long reportToSsp;
    private long custodianScheme;
    private long custodianSsp;
    private int dictLength;
    private Dictionary dict = null;
    private Block block = null;
    private long eidCount;
    private long blockRefScheme;
    private long blockDataSize;
    private ParserState deserializeBlockData;
    private boolean bundle_is_rejected = false;

    public AsyncParser(Observer<? super Bundle> downstream) {
        super(downstream);
    }

    @Override
    public ParserState initState() {
        return idle;
    }

    @Override
    public void onReset() {
        bundle = null;
        dict = null;
        block = null;
        bundle_is_rejected = false;
    }

    private void onPrimaryBlockReceived() {
        try {
            CoreProcessor.onDeserialized(bundle);
        } catch (RejectedException e) {
            bundle.mark("rejected");
            bundle_is_rejected = true;
            emit(bundle);
        }
    }

    private void onBlockHeaderReceived() {
        if (bundle_is_rejected) {
            return;
        }
        try {
            CoreProcessor.onDeserialized((BlockHeader) block);
        } catch (RejectedException e) {
            bundle.mark("rejected");
            bundle_is_rejected = true;
            emit(bundle);
        }
    }

    private void onBlockDataReceived() {
        if (bundle_is_rejected) {
            return;
        }
        try {
            CoreProcessor.onDeserialized(block);
            bundle.addBlock(block);
        } catch (RejectedException e) {
            bundle.mark("rejected");
            bundle_is_rejected = true;
            emit(bundle);
        }
    }

    private void onBundleReceived() {
        if (bundle_is_rejected) {
            return;
        }
        emit(bundle);
    }

    private ParserState idle = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            onReset();
            bundle = new Bundle();
            return primary_block_version;
        }
    };

    private ParserState primary_block_version = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.version = (int) sdnv_value.getValue();
            debug("AsyncParser", "version=" + bundle.version);
            return primary_block_flag;
        }
    };

    private ParserState primary_block_flag = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.procFlags = sdnv_value.getValue();
            debug("AsyncParser", "procflags=" + bundle.procFlags);
            return primary_block_length;
        }
    };

    private ParserState primary_block_length = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            // ignore block length
            debug("AsyncParser", "block_length=" + sdnv_value.getValue());
            return primary_block_dest_scheme_offset;
        }
    };

    private ParserState primary_block_dest_scheme_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            destinationScheme = sdnv_value.getValue();
            debug("AsyncParser", "destinationScheme=" + sdnv_value.getValue());
            return primary_block_dest_ssp_offset;
        }
    };

    private ParserState primary_block_dest_ssp_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            destinationSsp = sdnv_value.getValue();
            debug("AsyncParser", "destinationSsp=" + sdnv_value.getValue());
            return primary_block_src_scheme_offset;
        }
    };

    private ParserState primary_block_src_scheme_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            sourceScheme = sdnv_value.getValue();
            debug("AsyncParser", "sourceScheme=" + sdnv_value.getValue());
            return primary_block_src_ssp_offset;
        }
    };

    private ParserState primary_block_src_ssp_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            sourceSsp = sdnv_value.getValue();
            debug("AsyncParser", "sourceSsp=" + sdnv_value.getValue());
            return primary_block_report_scheme_offset;
        }
    };

    private ParserState primary_block_report_scheme_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            reportToScheme = sdnv_value.getValue();
            debug("AsyncParser", "reportToScheme=" + sdnv_value.getValue());
            return primary_block_report_ssp_offset;
        }
    };

    private ParserState primary_block_report_ssp_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            reportToSsp = sdnv_value.getValue();
            debug("AsyncParser", "reportToSsp=" + sdnv_value.getValue());
            return primary_block_custodian_scheme_offset;
        }
    };

    private ParserState primary_block_custodian_scheme_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            custodianScheme = sdnv_value.getValue();
            debug("AsyncParser", "custodianScheme=" + sdnv_value.getValue());
            return primary_block_custodian_ssp_offset;
        }
    };

    private ParserState primary_block_custodian_ssp_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            custodianSsp = sdnv_value.getValue();
            debug("AsyncParser", "custodianSsp=" + sdnv_value.getValue());
            return primary_block_creation_timestamp;
        }
    };

    private ParserState primary_block_creation_timestamp = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.creationTimestamp = sdnv_value.getValue();
            debug("AsyncParser", "creationTimestamp=" + sdnv_value.getValue());
            return primary_block_creation_timestamp_seq;
        }
    };

    private ParserState primary_block_creation_timestamp_seq = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.sequenceNumber = sdnv_value.getValue();
            debug("AsyncParser", "sequenceNumber=" + sdnv_value.getValue());
            return primary_block_lifetime;
        }
    };

    private ParserState primary_block_lifetime = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.lifetime = sdnv_value.getValue();
            debug("AsyncParser", "lifetime=" + sdnv_value.getValue());
            return primary_block_dict_length;
        }
    };

    private ParserState primary_block_dict_length = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            dictLength = (int) sdnv_value.getValue();
            debug("AsyncParser", "dictLength=" + sdnv_value.getValue());
            primary_block_dict_byte_array.realloc(dictLength);
            return primary_block_dict_byte_array;
        }
    };

    private BufferState primary_block_dict_byte_array = new BufferState() {
        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            dict = new Dictionary(buffer.array());
            try {
                bundle.source = EID.create(dict.lookup((int) sourceScheme),
                        dict.lookup((int) sourceSsp));
                debug("AsyncParser", "source=" + bundle.source.toString());
                bundle.destination = EID.create(dict.lookup((int) destinationScheme),
                        dict.lookup((int) destinationSsp));
                debug("AsyncParser", "destination=" + bundle.destination.toString());
                bundle.reportto = EID.create(dict.lookup((int) reportToScheme),
                        dict.lookup((int) reportToSsp));
                debug("AsyncParser", "reportto=" + bundle.reportto.toString());
                bundle.custodian = EID.create(dict.lookup((int) custodianScheme),
                        dict.lookup((int) custodianSsp));
                debug("AsyncParser", "custodian=" + bundle.custodian.toString());
            } catch (Dictionary.EntryNotFoundException e) {
                throw new RxParserException("RFC5050", e.getMessage());
            } catch (EID.EIDFormatException e) {
                throw new RxParserException("RFC5050", e.getMessage());
            }

            if (bundle.getV6Flag(PrimaryBlock.BundleV6Flags.FRAGMENT)) {
                return primary_block_fragment_offset;
            } else {
                onPrimaryBlockReceived();
                return block_type;
            }
        }
    };

    private ParserState primary_block_fragment_offset = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.fragmentOffset = sdnv_value.getValue();
            debug("AsyncParser", "fragmentOffset=" + bundle.fragmentOffset);
            return primary_block_application_data_length;
        }
    };

    private ParserState primary_block_application_data_length = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            bundle.appDataLength = sdnv_value.getValue();
            debug("AsyncParser", "appDataLength=" + bundle.appDataLength);
            onPrimaryBlockReceived();
            return block_type;
        }
    };


    private ParserState block_type = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            block = Block.create(next.get());
            debug("AsyncParser", "block_type=" + block.type);
            return block_proc_flags;
        }
    };

    private ParserState block_proc_flags = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            block.procV6flags = sdnv_value.getValue();
            debug("AsyncParser", "procflags=" + block.procV6flags);
            if (block.getV6Flag(BlockHeader.BlockV6Flags.BLOCK_CONTAINS_EIDS)) {
                return block_eid_ref_count;
            } else {
                return block_length;
            }
        }
    };

    private ParserState block_eid_ref_count = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            eidCount = sdnv_value.getValue();
            debug("AsyncParser", "eidCount=" + eidCount);
            return block_eid_ref_scheme;
        }
    };

    private ParserState block_eid_ref_scheme = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            blockRefScheme = sdnv_value.getValue();
            debug("AsyncParser", "blockRefScheme=" + blockRefScheme);
            return block_eid_ref_ssp;
        }
    };

    private ParserState block_eid_ref_ssp = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            try {
                long blockRefSsp = sdnv_value.getValue();
                debug("AsyncParser", "blockRefSsp=" + blockRefSsp);
                block.addEID(EID.create(dict.lookup((int) blockRefScheme),
                        dict.lookup((int) blockRefSsp)));
            } catch (Dictionary.EntryNotFoundException enfe) {
                throw new RxParserException("RFC5050", enfe.getMessage());
            } catch (EID.EIDFormatException efe) {
                throw new RxParserException("RFC5050", efe.getMessage());
            }
            if (--eidCount > 0) {
                return block_eid_ref_scheme;
            } else {
                return block_length;
            }
        }
    };

    private ParserState block_length = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            debug("AsyncParser", "blockDataSize=" + sdnv_value.getValue());
            block.dataSize = sdnv_value.getValue();
            onBlockHeaderReceived();
            if (bundle_is_rejected) {
                return block_ignore_payload;
            } else {
                return block_parse_payload;
            }
        }
    };

    private ParserState block_parse_payload = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) {
            switch (block.type) {
                case AgeBlock.type:
                    return age_block_payload;
                case ScopeControlHopLimitBlock.type:
                    return scope_control_block_payload;
                default:
                    return blob_block_payload;
            }
        }
    };

    private ParserState age_block_payload = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            ((AgeBlock)block).age = sdnv_value.getValue();
            return block_finish;
        }
    };

    private ParserState scope_control_block_payload = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            ((ScopeControlHopLimitBlock)block).count = sdnv_value.getValue();
            return scope_control_block_payload_limit;
        }
    };

    private ParserState scope_control_block_payload_limit = new SDNV.SDNVState() {
        @Override
        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
            ((ScopeControlHopLimitBlock)block).limit = sdnv_value.getValue();
            return block_finish;
        }
    };

    private ParserState blob_block_payload = new ParserState() {
        WritableBLOB writableData = null;

        @Override
        public void onEnter() throws RxParserException {
            debug("AsyncParser", "start_bundle_payload");
            try {
                ((BlockBLOB) block).data = BLOB.createBLOB((int) block.dataSize);
                writableData = ((BlockBLOB) block).data.getWritableBLOB();
            } catch (BundleStorage.StorageFullException e) {
                throw new RxParserException("BlockBLOB", e.getMessage());
            }
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (blockDataSize >= next.remaining()) {
                blockDataSize -= next.remaining();

                writeBuffer(next);

                if (blockDataSize == 0) {
                    return block_finish;
                } else {
                    return this;
                }
            } else {
                ByteBuffer slice = next.duplicate();
                slice.limit(slice.position() + (int) blockDataSize);

                writeBuffer(slice);

                next.position(next.position() + (int) blockDataSize);
                return block_finish;
            }
        }

        private void writeBuffer(ByteBuffer buf) throws RxParserException  {
            try {
                while (buf.hasRemaining()) {
                    writableData.write(buf.get());
                }
            } catch (IOException io) {
                throw new RxParserException("BlockBLOB", io.getMessage());
            } catch (WritableBLOB.BLOBOverflowException boe) {
                throw new RxParserException("BlockBLOB", boe.getMessage());
            }
        }
    };

    /**
     * If the bundle was rejected, we ignore the payload.
     */
    private ParserState block_ignore_payload = new ParserState() {
        @Override
        public void onEnter() throws RxParserException {
            debug("AsyncParser", "start_ignoring_payload");
            blockDataSize = block.dataSize;
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (blockDataSize >= next.remaining()) {
                next.position(next.limit());
                return this;
            } else {
                next.position(next.position() + (int) blockDataSize);
                return block_finish;
            }
        }
    };

    private ParserState block_finish = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            debug("AsyncParser", "end_bundle_payload");
            onBlockDataReceived();

            if (block.getV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK)) {
                return idle;
            } else {
                return block_type;
            }
        }
    };
}
