package io.left.rightmesh.libdtn.bundleV6;

import io.left.rightmesh.libdtn.core.processor.CoreProcessor;
import io.left.rightmesh.libdtn.core.processor.RejectedException;
import io.left.rightmesh.libdtn.utils.rxdeserializer.BufferState;
import io.left.rightmesh.libdtn.utils.rxdeserializer.DeserializerEmitter;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxDeserializerException;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxState;
import io.reactivex.Observer;

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
public class AsyncParser extends DeserializerEmitter<Bundle> {

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
    private RxState deserializeBlockData;
    private boolean bundle_is_rejected = false;

    public AsyncParser(Observer<? super Bundle> downstream) {
        super(downstream);
    }

    @Override
    public RxState initState() {
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

    private RxState idle = new RxState() {
        @Override
        public void onNext(ByteBuffer next) throws RxDeserializerException {
            onReset();
            bundle = new Bundle();
            changeState(primary_block_version);
        }
    };

    private RxState primary_block_version = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.version = (int) sdnv_value.getValue();
            debug("AsyncParser", "version=" + bundle.version);
            changeState(primary_block_flag);
        }
    };

    private RxState primary_block_flag = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.procFlags = sdnv_value.getValue();
            debug("AsyncParser", "procflags=" + bundle.procFlags);
            changeState(primary_block_length);
        }
    };

    private RxState primary_block_length = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            // ignore block length
            debug("AsyncParser", "block_length=" + sdnv_value.getValue());
            changeState(primary_block_dest_scheme_offset);
        }
    };

    private RxState primary_block_dest_scheme_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            destinationScheme = sdnv_value.getValue();
            debug("AsyncParser", "destinationScheme=" + sdnv_value.getValue());
            changeState(primary_block_dest_ssp_offset);
        }
    };

    private RxState primary_block_dest_ssp_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            destinationSsp = sdnv_value.getValue();
            debug("AsyncParser", "destinationSsp=" + sdnv_value.getValue());
            changeState(primary_block_src_scheme_offset);
        }
    };

    private RxState primary_block_src_scheme_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            sourceScheme = sdnv_value.getValue();
            debug("AsyncParser", "sourceScheme=" + sdnv_value.getValue());
            changeState(primary_block_src_ssp_offset);
        }
    };

    private RxState primary_block_src_ssp_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            sourceSsp = sdnv_value.getValue();
            debug("AsyncParser", "sourceSsp=" + sdnv_value.getValue());
            changeState(primary_block_report_scheme_offset);
        }
    };

    private RxState primary_block_report_scheme_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            reportToScheme = sdnv_value.getValue();
            debug("AsyncParser", "reportToScheme=" + sdnv_value.getValue());
            changeState(primary_block_report_ssp_offset);
        }
    };

    private RxState primary_block_report_ssp_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            reportToSsp = sdnv_value.getValue();
            debug("AsyncParser", "reportToSsp=" + sdnv_value.getValue());
            changeState(primary_block_custodian_scheme_offset);
        }
    };

    private RxState primary_block_custodian_scheme_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            custodianScheme = sdnv_value.getValue();
            debug("AsyncParser", "custodianScheme=" + sdnv_value.getValue());
            changeState(primary_block_custodian_ssp_offset);
        }
    };

    private RxState primary_block_custodian_ssp_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            custodianSsp = sdnv_value.getValue();
            debug("AsyncParser", "custodianSsp=" + sdnv_value.getValue());
            changeState(primary_block_creation_timestamp);
        }
    };

    private RxState primary_block_creation_timestamp = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.creationTimestamp = sdnv_value.getValue();
            debug("AsyncParser", "creationTimestamp=" + sdnv_value.getValue());
            changeState(primary_block_creation_timestamp_seq);
        }
    };

    private RxState primary_block_creation_timestamp_seq = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.sequenceNumber = sdnv_value.getValue();
            debug("AsyncParser", "sequenceNumber=" + sdnv_value.getValue());
            changeState(primary_block_lifetime);
        }
    };

    private RxState primary_block_lifetime = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.lifetime = sdnv_value.getValue();
            debug("AsyncParser", "lifetime=" + sdnv_value.getValue());
            changeState(primary_block_dict_length);
        }
    };

    private RxState primary_block_dict_length = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            dictLength = (int) sdnv_value.getValue();
            debug("AsyncParser", "dictLength=" + sdnv_value.getValue());
            primary_block_dict_byte_array.resizeBuffer(dictLength);
            changeState(primary_block_dict_byte_array);
        }
    };

    private BufferState primary_block_dict_byte_array = new BufferState() {
        @Override
        public void onSuccess(ByteBuffer buffer) throws RxDeserializerException {
            dict = new Dictionary(buffer.array());
            try {
                bundle.source = new EID(dict.lookup((int) sourceScheme),
                        dict.lookup((int) sourceSsp));
                debug("AsyncParser", "source=" + bundle.source.toString());
                bundle.destination = new EID(dict.lookup((int) destinationScheme),
                        dict.lookup((int) destinationSsp));
                debug("AsyncParser", "destination=" + bundle.destination.toString());
                bundle.reportto = new EID(dict.lookup((int) reportToScheme),
                        dict.lookup((int) reportToSsp));
                debug("AsyncParser", "reportto=" + bundle.reportto.toString());
                bundle.custodian = new EID(dict.lookup((int) custodianScheme),
                        dict.lookup((int) custodianSsp));
                debug("AsyncParser", "custodian=" + bundle.custodian.toString());
            } catch (Dictionary.EntryNotFoundException e) {
                throw new RxDeserializerException("RFC5050", e.getMessage());
            } catch (EID.EIDFormatException e) {
                throw new RxDeserializerException("RFC5050", e.getMessage());
            }

            if (bundle.getFlag(PrimaryBlock.BundleFlags.FRAGMENT)) {
                changeState(primary_block_fragment_offset);
            } else {
                onPrimaryBlockReceived();
                changeState(block_type);
            }
        }
    };

    private RxState primary_block_fragment_offset = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.fragmentOffset = sdnv_value.getValue();
            debug("AsyncParser", "fragmentOffset=" + bundle.fragmentOffset);
            changeState(primary_block_application_data_length);
        }
    };

    private RxState primary_block_application_data_length = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            bundle.appDataLength = sdnv_value.getValue();
            debug("AsyncParser", "appDataLength=" + bundle.appDataLength);
            onPrimaryBlockReceived();
            changeState(block_type);
        }
    };


    private RxState block_type = new RxState() {
        @Override
        public void onNext(ByteBuffer next) throws RxDeserializerException {
            block = Block.create(next.get());
            debug("AsyncParser", "block_type=" + block.getType());
            changeState(block_proc_flags);
        }
    };

    private RxState block_proc_flags = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            block.procflags = sdnv_value.getValue();
            debug("AsyncParser", "procflags=" + block.procflags);
            if (block.getFlag(BlockHeader.BlockFlags.BLOCK_CONTAINS_EIDS)) {
                changeState(block_eid_ref_count);
            } else {
                changeState(block_length);
            }
        }
    };

    private RxState block_eid_ref_count = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            eidCount = sdnv_value.getValue();
            debug("AsyncParser", "eidCount=" + eidCount);
            changeState(block_eid_ref_scheme);
        }
    };

    private RxState block_eid_ref_scheme = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            blockRefScheme = sdnv_value.getValue();
            debug("AsyncParser", "blockRefScheme=" + blockRefScheme);
            changeState(block_eid_ref_ssp);
        }
    };

    private RxState block_eid_ref_ssp = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            try {
                long blockRefSsp = sdnv_value.getValue();
                debug("AsyncParser", "blockRefSsp=" + blockRefSsp);
                block.addEID(new EID(dict.lookup((int) blockRefScheme),
                        dict.lookup((int) blockRefSsp)));
            } catch (Dictionary.EntryNotFoundException enfe) {
                throw new RxDeserializerException("RFC5050", enfe.getMessage());
            } catch (EID.EIDFormatException efe) {
                throw new RxDeserializerException("RFC5050", efe.getMessage());
            }
            if (--eidCount > 0) {
                changeState(block_eid_ref_scheme);
            } else {
                changeState(block_length);
            }
        }
    };

    private RxState block_length = new SDNV.SDNVState() {
        @Override
        public void onSuccess(SDNV sdnv_value) throws RxDeserializerException {
            debug("AsyncParser", "blockDataSize=" + sdnv_value.getValue());
            block.setDataSize(sdnv_value.getValue());
            onBlockHeaderReceived();
            if (bundle_is_rejected) {
                changeState(block_payload);
            } else {
                changeState(block_payload_ignore);
            }
        }
    };

    /**
     * Basically just a wrapper around deserializedBlockData to check that it doesn't read more
     * than what it is allowed to (blockDataSize).
     */
    private RxState block_payload = new RxState() {
        @Override
        public void onEnter() throws RxDeserializerException {
            debug("AsyncParser", "start_bundle_payload");
            blockDataSize = block.getDataSize();
            deserializeBlockData = block.deserializeBlockData();
            deserializeBlockData.onEnter();
        }

        @Override
        public void onNext(ByteBuffer next) throws RxDeserializerException {
            if (blockDataSize >= next.remaining()) {
                blockDataSize -= next.remaining();
                deserializeBlockData.onNext(next);

                if (blockDataSize == 0) {
                    onSuccess();
                }
            } else {
                ByteBuffer slice = next.duplicate();
                slice.limit(slice.position() + (int) blockDataSize);
                deserializeBlockData.onNext(slice);
                next.position(next.position() + (int) blockDataSize);
                onSuccess();
            }
        }

        void onSuccess() throws RxDeserializerException {
            deserializeBlockData.onExit();
            bundle.addBlock(block);
            onBlockDataReceived();
            debug("AsyncParser", "end_bundle_payload");
            if (block.getFlag(BlockHeader.BlockFlags.LAST_BLOCK)) {
                onBundleReceived();
                changeState(idle);
            } else {
                changeState(block_type);
            }
        }
    };

    /**
     * If the bundle was rejected, we ignore the payload.
     */
    private RxState block_payload_ignore = new RxState() {
        @Override
        public void onEnter() throws RxDeserializerException {
            debug("AsyncParser", "start_ignoring_payload");
            blockDataSize = block.getDataSize();
        }

        @Override
        public void onNext(ByteBuffer next) throws RxDeserializerException {
            if (blockDataSize >= next.remaining()) {
                next.position(next.limit());
            } else {
                next.position(next.position() + (int) blockDataSize);
                onSuccess();
            }
        }

        void onSuccess() throws RxDeserializerException {
            debug("AsyncParser", "end_ignoring_payload");
            if (block.getFlag(BlockHeader.BlockFlags.LAST_BLOCK)) {
                changeState(idle);
            } else {
                changeState(block_type);
            }
        }
    };
}
