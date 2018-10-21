package io.left.rightmesh.libdtncommon.data;

import io.left.rightmesh.libdtncommon.data.blob.BLOB;

/**
 * UnknownExtensionBlock is used to create a generic Extension CanonicalBlock in case when a block type is
 * unknown.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class UnknownExtensionBlock extends BlockBLOB {

    public class ProcessorNotFoundException extends ProcessingException {
        public ProcessorNotFoundException() {
            super("Processor not found");
        }
    }

    /**
     * Constructor: creates an empty UnknownExtensionBlock.
     *
     * @param type of the block
     */
    public UnknownExtensionBlock(int type) {
        super(type);
    }

    /**
     * Constructor: creates an UnknownExtensionBlock with a BLOB as data.
     *
     * @param type of the block
     * @param data payload
     */
    public UnknownExtensionBlock(int type, BLOB data) {
        super(type, data);
    }

    @Override
    public void onBlockDataDeserialized() throws ProcessingException {
        throw new ProcessorNotFoundException();
    }

    @Override
    public boolean onReceptionProcessing(Bundle bundle) throws ProcessingException {
        throw new ProcessorNotFoundException();
    }

    @Override
    public boolean onPrepareForTransmission(Bundle bundle) throws ProcessingException {
        throw new ProcessorNotFoundException();
    }

    @Override
    public boolean onPutOnStorage(Bundle bundle) throws ProcessingException {
        throw new ProcessorNotFoundException();
    }

    @Override
    public boolean onPullFromStorage(Bundle bundle) throws ProcessingException {
        throw new ProcessorNotFoundException();
    }
}
