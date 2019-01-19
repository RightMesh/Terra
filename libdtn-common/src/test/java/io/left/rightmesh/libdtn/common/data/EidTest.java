package io.left.rightmesh.libdtn.common.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EidIpn;

import org.junit.Test;

/**
 * Test class for Endpoint IDs.
 *
 * @author Lucien Loiseau on 20/09/18.
 */
public class EidTest {

    EidFactory eidFactory = new BaseEidFactory();

    @Test
    public void testIpnEid() {
        System.out.println("[+] eid: testing EidIpn Scheme");
        EidIpn eidIpn = new EidIpn(0, 0);
        assertEquals("ipn:0.0", eidIpn.getEidString());
        assertEquals(0, eidIpn.nodeNumber);
        assertEquals(0, eidIpn.serviceNumber);

        eidIpn = new EidIpn(15, 32);
        assertEquals("ipn:15.32", eidIpn.getEidString());
        assertEquals(15, eidIpn.nodeNumber);
        assertEquals(32, eidIpn.serviceNumber);

        try {
            Eid eid = eidFactory.create("ipn:0.0");
            assertEquals("ipn:0.0", eid.getEidString());
        } catch (EidFormatException eid) {
            fail(eid.getMessage());
        }
    }

    @Test
    public void testDtnEid() {
        System.out.println("[+] eid: testing DtnEid Scheme");
        try {
            Eid dtn = new DtnEid("marsOrbital");
            Eid dtnping = new DtnEid("marsOrbital/pingservice");
            assertEquals("dtn:marsOrbital", dtn.getEidString());
            assertEquals("dtn:marsOrbital/pingservice", dtnping.getEidString());
            assertTrue(dtnping.matches(dtn));

            dtn = eidFactory.create("dtn:marsOrbital");
            assertEquals("dtn:marsOrbital", dtn.getEidString());
        } catch (EidFormatException efe) {
            fail();
        }
    }
}
