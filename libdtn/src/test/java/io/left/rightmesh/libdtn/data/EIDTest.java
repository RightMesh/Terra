package io.left.rightmesh.libdtn.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class EIDTest {

    @Test
    public void testEIDIPN() {
        System.out.println("[+] testing EID IPN Scheme");
        EID.IPN ipn = EID.createIPN(0, 0);
        assertEquals("ipn:0.0", ipn.eid);
        assertEquals(0, ipn.node_number);
        assertEquals(0, ipn.service_number);

        ipn = EID.createIPN(15, 32);
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
        System.out.println("[+] testing EID DTN Scheme");
        EID dtn = EID.createDTN("marsOrbital");
        assertEquals("dtn:marsOrbital", dtn.eid);

        try {
            dtn = EID.create("dtn:marsOrbital");
            assertEquals("dtn:marsOrbital", dtn.eid);
        } catch (EID.EIDFormatException efe) {
            fail();
        }
    }

    @Test
    public void testEIDCLA() {
        System.out.println("[+] testing EID CLA Scheme");

        EID.CLA cla = EID.createCLA("stcp", "tcp://google.com:4556");
        assertEquals("cla:stcp:tcp://google.com:4556", cla.eid);
        assertEquals("stcp", cla.cl_name);
        assertEquals("tcp://google.com:4556", cla.cl_specific);

        try {
            EID eid = EID.create( "cla:stcp:tcp://google.com:4556");
            assertEquals("cla:stcp:tcp://google.com:4556", eid.eid);
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }

    @Test
    public void testEIDhttp() {
        System.out.println("[+] testing EID Unknown Scheme");

        try {
            EID dtn = EID.create("http", "//google.com:8080");
            assertEquals("http://google.com:8080", dtn.eid);
        } catch (EID.EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }
}
