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
        EID ipn = EID.createIPN(0,0);
        assertEquals("ipn:0.0",ipn.eid);
    }

    @Test
    public void testEIDDTN() {
        EID dtn = EID.createDTN("marsOrbital");
        assertEquals("dtn:marsOrbital",dtn.eid);
    }

    @Test
    public void testEIDhttp() {
        try {
            EID dtn = EID.create("http", "//google.com:8080");
            assertEquals("http://google.com:8080", dtn.eid);
        } catch(EID.EIDFormatException eid) {
            fail();
        }
    }
}
