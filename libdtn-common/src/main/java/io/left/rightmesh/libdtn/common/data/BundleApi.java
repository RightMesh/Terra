package io.left.rightmesh.libdtn.common.data;

import java.util.LinkedList;

/**
 * BundleApi describes the method that can be used to manipulate a single Bundle structure.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public interface BundleApi {

    /**
     * clear all extension from this bundle.
     */
    void clearBundle();

    /**
     * check if ths current Bundle has a block of given type.
     *
     * @param type of the block
     * @return true if one or more block is present in bundle, false otherwise
     */
    boolean hasBlock(int type);

    /**
     * return the list of {@link CanonicalBlock} that are encapsulated in this current Bundle.
     *
     * @return list of block
     */
    LinkedList<CanonicalBlock> getBlocks();

    /**
     * return the list of {@link CanonicalBlock} that are encapsulated in this current Bundle
     * of a given type.
     *
     * @param blockType type to filter
     * @return list of block
     */
    LinkedList<CanonicalBlock> getBlocks(int blockType);

    /**
     * Get a specific block by its block number.
     *
     * @param blockNumber of the block to query.
     * @return a Block if found, null otherwise.
     */
    CanonicalBlock getBlock(int blockNumber);

    /**
     * Replace a specific block with another.
     *
     * @param blockNumber of the block to query.
     * @param block       replacement block.
     */
    void updateBlock(int blockNumber, CanonicalBlock block);

    /**
     * adds a {@link CanonicalBlock} to the current Bundle.
     *
     * @param block to be added
     */
    void addBlock(CanonicalBlock block);

    /**
     * delete a {@link CanonicalBlock} to the current Bundle.
     *
     * @param block to be deleted
     */
    void delBlock(CanonicalBlock block);

    /**
     * getPayloadBlock return the bundle's payload bundle or null if no payload block exists.
     * Per the RFC, there is only one payload block per bundle.
     *
     * @return PayloadBlock
     */
    PayloadBlock getPayloadBlock();

}
