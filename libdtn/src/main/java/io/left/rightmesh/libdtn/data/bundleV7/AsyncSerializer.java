package io.left.rightmesh.libdtn.data.bundleV7;

import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockBLOB;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class AsyncSerializer {

    /**
     * Turn a Bundle into a serialized Flowable of ByteBuffer.
     *
     * @param bundle to serialize
     * @return Flowable
     */
    public Flowable<ByteBuffer> serialize(Bundle bundle) {
        Flowable<ByteBuffer> ret = serialize((PrimaryBlock) bundle);
        for (Block block : bundle.getBlocks()) {
            ret = ret.concatWith(serialize(block));
        }
        return ret;
    }

    /**
     * Serialize the {@see PrimaryBlock} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     */
    public Flowable<ByteBuffer> serialize(PrimaryBlock block) {
        return null;
    }

    /**
     * Serialize a {@see Block} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     */
    public Flowable<ByteBuffer> serialize(AgeBlock block) {
        return null;
    }

    public Flowable<ByteBuffer> serialize(ScopeControlHopLimitBlock block) {
        return null;
    }

    public Flowable<ByteBuffer> serialize(BlockBLOB block) {
        return null;
    }

    /**
     * Serialize a {@see BlockHeader} into the output stream.
     *
     * @param block to serialize
     * @return Flowable
     */
    public Flowable<ByteBuffer> serialize(BlockHeader block) {
        return null;
    }

}
