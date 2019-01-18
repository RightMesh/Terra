package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.util.LinkedList;

/**
 * The format of a bundle and its specifications are described in RFC 5050.
 * According to this RFC each bundle consists of a linked sequence of block structures,
 * there should be at least two extension in a bundle and the first block must be a primary
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
    private int blockNumber = 1;

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
     * Constructor: creates a Bundle set with a destination.
     *
     * @param destination of the bundle
     */
    public Bundle(Eid destination) {
        super();
        this.setDestination(destination);
    }


    /**
     * Constructor: creates a Bundle out of a PrimaryBlock.
     *
     * @param destination of the bundle
     * @param lifetime    of the bundle
     */
    public Bundle(Eid destination, long lifetime) {
        super(destination, lifetime);
    }

    /**
     * clear all extension from this bundle.
     */
    public void clearBundle() {
        for (Block block : blocks) {
            block.clearBlock();
        }
        blocks.clear();
    }

    /**
     * return the list of {@link CanonicalBlock} that are encapsulated in this current Bundle.
     *
     * @return list of extension
     */
    public LinkedList<CanonicalBlock> getBlocks() {
        return blocks;
    }

    /**
     * Get a specific block by its block number.
     *
     * @param blockNumber of the block to query.
     * @return a Block if found, null otherwise.
     */
    public CanonicalBlock getBlock(int blockNumber) {
        for (CanonicalBlock block : blocks) {
            if (block.number == blockNumber) {
                return block;
            }
        }
        return null;
    }

    /**
     * Replace a specific block with another.
     *
     * @param blockNumber of the block to query.
     * @param block replacement block.
     */
    public void updateBlock(int blockNumber, CanonicalBlock block) {
        int nb = -1;
        boolean found = false;
        while (!found && (nb < blocks.size())) {
            if (blocks.get(++nb).number == blockNumber) {
                found = true;
            }
        }

        if (found) {
            blocks.get(nb).clearBlock();
            blocks.set(nb, block);
        }
    }

    /**
     * adds a {@link CanonicalBlock} to the current Bundle.
     *
     * @param block to be added
     */
    public void addBlock(CanonicalBlock block) {
        // there can be only one payload block
        if ((getPayloadBlock() != null) && (block.type == PayloadBlock.PAYLOAD_BLOCK_TYPE)) {
            return;
        }

        // v7
        if (block.type == PayloadBlock.PAYLOAD_BLOCK_TYPE) {
            block.number = 0;
        } else {
            block.number = blockNumber++;
        }

        blocks.add(block);
    }

    /**
     * delete a {@link CanonicalBlock} to the current Bundle.
     *
     * @param block to be deleted
     */
    public void delBlock(CanonicalBlock block) {
        blocks.remove(block);
    }

    /**
     * getPayloadBlock return the bundle's payload bundle or null if no payload block exists.
     * Per the RFC, there is only one payload block per bundle.
     *
     * @return PayloadBlock
     */
    public PayloadBlock getPayloadBlock() {
        for (CanonicalBlock block : blocks) {
            if (block.type == PayloadBlock.PAYLOAD_BLOCK_TYPE) {
                return (PayloadBlock) block;
            }
        }
        return null;
    }
}
