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
        assertEquals(0, eidIpn.getNodeNumber());
        assertEquals(0, eidIpn.getServiceNumber());

        EidIpn eidIpn2 = new EidIpn(15, 32);
        assertEquals("ipn:15.32", eidIpn2.getEidString());
        assertEquals(15, eidIpn2.getNodeNumber());
        assertEquals(32, eidIpn2.getServiceNumber());
        assertFalse(eidIpn2.matches(null));
        assertFalse(eidIpn2.equals(null));
        assertTrue(eidIpn2.matches(eidIpn2));
        assertTrue(eidIpn2.equals(eidIpn2));
        assertFalse(eidIpn.matches(eidIpn2));
        assertFalse(eidIpn.equals(eidIpn2));

        assertEquals((17 * 31 + eidIpn2.getNodeNumber()) * 31 + eidIpn2.getServiceNumber(), eidIpn2.hashCode());
        assertEquals("ipn:15.32", eidIpn2.toString());

        try {
            Eid eid = eidFactory.create("ipn:0.0");
            Eid otherEid = eidFactory.create("dtn:marsOrbital");
            assertEquals("ipn:0.0", eid.getEidString());
            assertFalse(eid.matches(otherEid));
            assertFalse(eid.equals(otherEid));
            otherEid = eid.copy();
            assertTrue(eid.matches(otherEid));
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
            assertTrue(e.getMessage().contains("unknown"));
        } catch (EidFormatException efe) {
            fail();
        }


    }

    @Test
    public void testApiEid() {
        System.out.println("[+] eid: testing ApiEid Scheme");
        String eidString = "api:me";

        try {
            Eid apiEid = eidFactory.create(eidString);
            assertEquals(eidString, apiEid.getEidString());
            assertFalse(apiEid.matches(null));
            assertTrue(apiEid.matches(apiEid));

            Eid apiEid2 = apiEid.copy();
            assertEquals("", ((ApiEid) apiEid2).getPath());
            assertEquals(ApiEid.EID_API_IANA_VALUE, apiEid2.ianaNumber());

            Eid otherEid = eidFactory.create("dtn:marsOrbital");
            assertFalse(apiEid.matches(otherEid));

        } catch (EidFormatException efe) {
            fail();
        }
    }

    @Test
    public void testBaseEidFactory() {
        try {
            assertEquals(ApiEid.EID_API_SCHEME, eidFactory.getIanaScheme(ApiEid.EID_API_IANA_VALUE));
            assertEquals(DtnEid.EID_DTN_SCHEME, eidFactory.getIanaScheme(DtnEid.EID_DTN_IANA_VALUE));
            assertEquals(EidIpn.EID_IPN_SCHEME, eidFactory.getIanaScheme(EidIpn.EID_IPN_IANA_VALUE));
            assertEquals(ClaEid.EID_CLA_SCHEME, eidFactory.getIanaScheme(ClaEid.EID_CLA_IANA_VALUE));
        } catch (EidFactory.UnknownIanaNumber uin) {
            fail();
        }

        try {
            eidFactory.getIanaScheme(-1);
            fail();
        } catch (EidFactory.UnknownIanaNumber uin) {
            // This should happen
        }
    }

    @Test
    public void testUnknownEid() {
        String ssp = "1.2.3.9";
        try {
            UnknownEid ueid = new UnknownEid(17, ssp);
            assertEquals(17, ueid.ianaNumber());
            assertEquals(UnknownEid.EID_UNK_SCHEME, ueid.getScheme());
            assertEquals(ssp, ueid.getSsp());

            Eid ueid2 = ueid.copy();
            assertFalse(ueid.matches(null));
            assertTrue(ueid.matches(ueid));
            assertTrue(ueid2.matches(ueid));
        } catch (EidFormatException efe) {
            fail();
        }


    }
}
