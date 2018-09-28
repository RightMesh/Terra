package io.left.rightmesh.libdtn.data;

import java.util.LinkedList;

import io.left.rightmesh.libdtn.core.processor.ProcessingException;

/**
 * The format of a bundle and its specifications are described in RFC 5050.
 * According to this RFC each bundle consists of a linked sequence of block structures,
 * there should be at least two blocks in a bundle and the first block must be a primary
 * bundle block. There cannot be more than one primary bundle block in a single bundle.
 *
 * <p>This Bundle class also allows the bundle to be "marked" or have some object attached.
 * This is useful for bundle processing pipelines. Those attachement will not get serialized
 * neither to peers nor to storage.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public class Bundle extends PrimaryBlock {

    public LinkedList<CanonicalBlock> blocks = new LinkedList<>();
    private int block_number = 1;

    /**
     * Default Constructor.
     */
    public Bundle() {
        super();
    }

    /**
     * Constructor: creates a Bundle out of a PrimaryBlock.
     *
     * @param block the primary block
     */
    public Bundle(PrimaryBlock block) {
        super(block);
    }

    /**
     * return the list of {@see CanonicalBlock} that are encapsulated in this current Bundle.
     *
     * @return list of blocks
     */
    public LinkedList<CanonicalBlock> getBlocks() {
        return blocks;
    }

    /**
     * adds a {@see CanonicalBlock} to the current Bundle.
     *
     * @param block to be added
     */
    public void addBlock(CanonicalBlock block) {
        // there can be only one payload block
        if ((getPayloadBlock() != null) && (block.type == PayloadBlock.type)) {
            return;
        }

        // v7
        if(block.type == PayloadBlock.type) {
            block.number = 0;
        } else {
            block.number = block_number++;
        }

        // v6
        if (blocks.size() > 0) {
            blocks.getLast().setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, false);
        }

        blocks.add(block);

        // v6
        block.setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, true);
    }

    /**
     * delete a {@see CanonicalBlock} to the current Bundle.
     *
     * @param block to be deleted
     */
    public void delBlock(CanonicalBlock block) {
        blocks.remove(block);

        // v6
        if (block.getV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK) && (blocks.size() > 0)) {
            blocks.getLast().setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, true);
        }
    }

    /**
     * getPayloadBlock return the bundle's payload bundle or null if no payload block exists.
     * Per the RFC, there is only one payload block per bundle.
     *
     * @return PayloadBlock
     */
    public PayloadBlock getPayloadBlock() {
        for (CanonicalBlock block : blocks) {
            if (block.type == PayloadBlock.type) {
                return (PayloadBlock) block;
            }
        }
        return null;
    }

    public void onReceptionProcessing(Bundle bundle) throws ProcessingException {
        for(CanonicalBlock block : getBlocks()) {
            block.onReceptionProcessing(bundle);
        }
    }

    public void onPrepareForTransmission(Bundle bundle) throws ProcessingException {
        for(CanonicalBlock block : getBlocks()) {
            block.onPrepareForTransmission(bundle);
        }
    }

    public void onPutOnStorage(Bundle bundle) throws ProcessingException {
        for(CanonicalBlock block : getBlocks()) {
            block.onPutOnStorage(bundle);
        }
    }

    public void onPullFromStorage(Bundle bundle) throws ProcessingException {
        for(CanonicalBlock block : getBlocks()) {
            block.onPullFromStorage(bundle);
        }
    }

    /**
     * print debug information about the current bundle.
     */
    public void printDebug() {
        System.out.print("bundle to= " + destination.toString() + " content=");
        getPayloadBlock().data.observe().subscribe(
                b -> {
                    System.out.print(new String(b.array()));
                }
        );
        System.out.println();
    }
}
