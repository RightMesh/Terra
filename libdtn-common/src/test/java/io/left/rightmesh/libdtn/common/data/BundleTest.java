package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class BundleTest {
    @Test
    public void createBundleFromPrimaryBlock() {
        PrimaryBlock primaryBlock = new PrimaryBlock();
        Bundle bundle = new Bundle(primaryBlock);
        assertEquals(bundle.getCreationTimestamp(), primaryBlock.getCreationTimestamp());
        assertEquals(bundle.getSource(), primaryBlock.getSource());
        assertEquals(bundle.getReportto(), primaryBlock.getReportto());
        assertEquals(bundle.getSequenceNumber(), primaryBlock.getSequenceNumber());
    }

    @Test
    public void createBundleWithDestination() {
        try {
            Eid destination = new BaseEidFactory().create("dtn:marsOrbital");
            Bundle bundle = new Bundle(destination);
            assertEquals(bundle.getDestination(), destination);

            long lifetime = 5000;
            Bundle bundle2 = new Bundle(destination, lifetime);
            assertEquals(bundle2.getDestination(), destination);
            assertEquals(bundle2.getLifetime(), lifetime);

        } catch (EidFormatException e) {
            System.err.println("error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void blockOperations() {
        Bundle bundle = new Bundle();
        String payload = "This is a test for testing block operations";

        bundle.addBlock(new PayloadBlock(payload));
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());

        // adding a second Payload block shouldn't go through
        bundle.addBlock(new PayloadBlock(payload + "2"));
        assertEquals(3 ,bundle.getBlocks().size());

        // there should only be 3 blocks
        assertNull(bundle.getBlock(3));

        // be able to get a specific block
        List<CanonicalBlock> ageBlocks = bundle.getBlocks(AgeBlock.AGE_BLOCK_TYPE);
        assertEquals(1, ageBlocks.size());
        assertEquals(AgeBlock.AGE_BLOCK_TYPE ,((LinkedList<CanonicalBlock>) ageBlocks).getFirst().type);

        assertTrue(bundle.hasBlock(ScopeControlHopLimitBlock.SCOPE_CONTROL_HOP_LIMIT_BLOCK_TYPE));
        assertFalse(bundle.hasBlock(BlockIntegrityBlock.BLOCK_INTEGRITY_BLOCK_TYPE));

        bundle.delBlock(((LinkedList<CanonicalBlock>) ageBlocks).getFirst());
        ageBlocks = bundle.getBlocks(AgeBlock.AGE_BLOCK_TYPE);
        assertEquals(0, ageBlocks.size());
        assertEquals(2, bundle.getBlocks().size());

        bundle.clearBundle();
        assertEquals(0, bundle.getBlocks().size());
    }
}
