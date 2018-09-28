package io.left.rightmesh.libdtn.data;

import org.junit.Test;

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
        EID.IPN ipn = new EID.IPN(0, 0);
        assertEquals("ipn:0.0", ipn.eid);
        assertEquals(0, ipn.node_number);
        assertEquals(0, ipn.service_number);

        ipn = new EID.IPN(15, 32);
        assertEquals("ipn:15.32", ipn.eid);
        assertEquals(15, ipn.node_number);
        assertEquals(32, ipn.service_number);

        try {
            EID eid = EID.create( "ipn:0.0");
            assertEquals("ipn:0.0", eid.eid);
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }

    @Test
    public void testEIDDTN() {
        System.out.println("[+] eid: testing DTN Scheme");
        EID dtn = new EID.DTN("marsOrbital");
        EID dtnping = new EID.DTN("marsOrbital/pingservice");
        assertEquals("dtn:marsOrbital", dtn.eid);
        assertEquals("dtn:marsOrbital/pingservice", dtnping.eid);
        assertTrue(dtnping.matches(dtn));

        try {
            dtn = EID.create("dtn:marsOrbital");
            assertEquals("dtn:marsOrbital", dtn.eid);
        } catch (EID.EIDFormatException efe) {
            fail();
        }
    }

    @Test
    public void testEIDCLA() {
        System.out.println("[+] eid: testing CLA Scheme");

        EID.CLA cla = new EID.CLA("stcp", "google.com:4556");
        assertEquals("cla:stcp:google.com:4556", cla.eid);
        assertEquals("stcp", cla.cl_name);
        assertEquals("google.com:4556", cla.cl_specific);

        try {
            EID eid = EID.create( "cla:stcp:google.com:4556");
            EID path = EID.create( "cla:stcp:google.com:4556/pingservice");
            assertEquals("cla:stcp:google.com:4556", eid.eid);
            assertEquals("cla:stcp:google.com:4556/pingservice", path.eid);
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
            assertEquals("http://google.com:8080", dtn.eid);
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }
}
