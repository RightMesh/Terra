package io.left.rightmesh.libdtn.common.data.eid;

import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EidIpn;

import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    public void testClaEid() {
        System.out.println("[+] eid: testing ClaEid Scheme");
        String eidString = "cla:stcp:1.2.3.4:4557";
        try {
            Eid cla = eidFactory.create(eidString);
            assertEquals(eidString, cla.getEidString());
            assertEquals("cla", cla.getScheme());

            assertTrue(cla.matches(cla));
            assertFalse(cla.matches(null));

            Eid cla2 = cla.copy();
            assertTrue(cla.matches(cla2));
            assertEquals(ClaEid.EID_CLA_IANA_VALUE, cla2.ianaNumber());

            cla = ((BaseClaEid) cla).setPath("/test");
            assertEquals("/test", ((BaseClaEid) cla).getPath());
            assertEquals("", (((BaseClaEid) cla2).getPath()));

            cla2 = ((BaseClaEid) cla).setPath("test2");
            assertEquals("/test2", ((BaseClaEid) cla2).getPath());

            assertEquals("stcp", (((BaseClaEid) cla2).getClaName()));
            assertEquals("1.2.3.4:4557", (((BaseClaEid) cla2).getClaSpecificPart()));


            Eid cla3 = eidFactory.create(eidString + "0");
            assertFalse(cla.matches(cla3));

            Eid cla4 = eidFactory.create("dtn:marsOrbital");
            assertFalse(cla.matches(cla4));

        } catch (EidFormatException efe) {
            fail();
        }

        BaseEidFactory exceptionThrowerFactory = new BaseEidFactory(true);
        try {
            Eid cla = exceptionThrowerFactory.create(eidString);
        } catch (ClaEidParser.UnknownClaName e) {
            assertTrue(e.getMessage().contains("claName unknown"));
        } catch (EidFormatException efe) {
            fail();
        }


    }

    @Test
    public void testApiEid() {

    }

    @Test
    public void testBaseEidFactory() {

    }
}
