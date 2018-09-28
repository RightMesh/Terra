package io.left.rightmesh.libdtn.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

    public enum RetentionContraint {
        DISPATCH_PENDING("dispatch_pending"),
        FORWARD_PENDING("forward_pending");

        String value;
        RetentionContraint(String v) {
            this.value = v;
        }
        public String value() {
            return this.value;
        }
    }

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
        // v6
        if (blocks.size() > 0) {
            blocks.getLast().setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, false);
        }

        // v7
        if(block.type == PayloadBlock.type) {
            block.number = 0;
        } else {
            block.number = block_number++;
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
