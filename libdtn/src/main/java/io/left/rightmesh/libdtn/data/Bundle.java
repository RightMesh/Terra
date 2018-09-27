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

    public LinkedList<Block> blocks = new LinkedList<>();

    // for processing the bundle
    public  Map<String, Object> attachement;
    private int block_number = 1;

    /**
     * Default Constructor.
     */
    public Bundle() {
        attachement = new HashMap<>();
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
     * return the list of {@see Block} that are encapsulated in this current Bundle.
     *
     * @return list of blocks
     */
    public LinkedList<Block> getBlocks() {
        return blocks;
    }

    /**
     * adds a {@see Block} to the current Bundle.
     *
     * @param block to be added
     */
    public void addBlock(Block block) {
        if (blocks.size() > 0) {
            blocks.getLast().setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, false);
        }
        if(block.type == PayloadBlock.type) {
            block.number = 0;
        } else {
            block.number = block_number++;
        }
        blocks.add(block);
        block.setV6Flag(BlockHeader.BlockV6Flags.LAST_BLOCK, true);
    }

    /**
     * delete a {@see Block} to the current Bundle.
     *
     * @param block to be deleted
     */
    public void delBlock(Block block) {
        blocks.remove(block);
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
        for (Block block : blocks) {
            if (block.type == PayloadBlock.type) {
                return (PayloadBlock) block;
            }
        }
        return null;
    }

    /**
     * Add a TAG on this Bundle. It is useful for Bundle processing.
     *
     * @param tag to add to this bundle
     * @return true if the tag was added, false if the bundle was already tagged with this tag.
     */
    public boolean mark(String tag) {
        return attach(tag, null);
    }

    /**
     * Check wether this bundle is tagged.
     *
     * @param tag to asses
     * @return true if the bundle is tagged with this tag, false otherwise.
     */
    public boolean isMarked(String tag) {
        return attachement.containsKey(tag);
    }

    /**
     * attach an object to this bundle. It is useful for Bundle processing.
     *
     * @param key for this attachement
     * @param o   the attached object
     * @return false if there was already an object attached under this key, true otherwise.
     */
    public boolean attach(String key, Object o) {
        if (attachement.containsKey(key)) {
            return false;
        }
        attachement.put(key, o);
        return true;
    }

    /**
     * get the attachement under this key.
     *
     * @param key for this attachement
     * @param <T> type of the attachement
     * @return the attached object under this key
     */
    public <T> T getAttachement(String key) {
        if (attachement.containsKey(key)) {
            return (T) attachement.get(key);
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
