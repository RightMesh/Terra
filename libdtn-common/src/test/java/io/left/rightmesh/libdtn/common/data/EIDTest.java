package io.left.rightmesh.libdtn.common.data;

import org.junit.Test;

import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.common.data.eid.IPN;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class EIDTest {

    EIDFactory EidFactory = new BaseEIDFactory();

    @Test
    public void testEIDIPN() {
        System.out.println("[+] eid: testing IPN Scheme");
        IPN ipn = new IPN(0, 0);
        assertEquals("ipn:0.0", ipn.getEIDString());
        assertEquals(0, ipn.node_number);
        assertEquals(0, ipn.service_number);

        ipn = new IPN(15, 32);
        assertEquals("ipn:15.32", ipn.getEIDString());
        assertEquals(15, ipn.node_number);
        assertEquals(32, ipn.service_number);

        try {
            EID eid = EidFactory.create("ipn:0.0");
            assertEquals("ipn:0.0", eid.getEIDString());
        } catch (EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }

    @Test
    public void testEIDDTN() {
        System.out.println("[+] eid: testing DTN Scheme");
        try {
            EID dtn = new DTN("marsOrbital");
            EID dtnping = new DTN("marsOrbital/pingservice");
            assertEquals("dtn:marsOrbital", dtn.getEIDString());
            assertEquals("dtn:marsOrbital/pingservice", dtnping.getEIDString());
            assertTrue(dtnping.matches(dtn));

            dtn = EidFactory.create("dtn:marsOrbital");
            assertEquals("dtn:marsOrbital", dtn.getEIDString());
        } catch (EIDFormatException efe) {
            fail();
        }
    }
}
