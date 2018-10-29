package io.left.rightmesh.libdtn.common.data;

import org.junit.Test;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.IPN;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class EIDTest {

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
            EID eid = EID.create("ipn:0.0");
            assertEquals("ipn:0.0", eid.getEIDString());
        } catch (EID.EIDFormatException eid) {
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

            dtn = EID.create("dtn:marsOrbital");
            assertEquals("dtn:marsOrbital", dtn.getEIDString());
        } catch (EID.EIDFormatException efe) {
            fail();
        }
    }

    @Test
    public void testEIDCLA() {
        System.out.println("[+] eid: testing CLA Scheme");

        try {
            CLA cla = new CLASTCP("google.com", 4556, "/");
            assertEquals("cla:stcp:google.com:4556/", cla.getEIDString());
            assertEquals("stcp", cla.cl_name);
            assertEquals("google.com:4556", cla.cl_specific);
            assertEquals("/", cla.cl_sink);

            EID eid = EID.create("cla:stcp:google.com:4556");
            EID path = EID.create("cla:stcp:google.com:4556/pingservice");
            assertEquals("cla:stcp:google.com:4556", eid.getEIDString());
            assertEquals("cla:stcp:google.com:4556/pingservice", path.getEIDString());
            assertTrue(path.matches(eid));
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }

    @Test
    public void testEIDhttp() {
        System.out.println("[+] eid: testing Unknown Scheme");

        try {
            EID dtn = EID.create("http://google.com:8080");
            assertEquals("http://google.com:8080", dtn.getEIDString());
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }
}
