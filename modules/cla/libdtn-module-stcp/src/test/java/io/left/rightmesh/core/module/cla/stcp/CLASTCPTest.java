package io.left.rightmesh.core.module.cla.stcp;

import org.junit.Test;

import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class CLASTCPTest {

    @Test
    public void testEIDCLA() {
        System.out.println("[+] eid: testing BaseClaEid Scheme");

        try {
            BaseClaEid cla = new CLASTCP("google.com", 4556, "/");
            assertEquals("cla:stcp:google.com:4556/", cla.getEidString());
            assertEquals("stcp", cla.claName);
            assertEquals("google.com:4556", cla.claSpecific);
            assertEquals("/", cla.claSink);

            Eid eid = (new CLASTCPParser()).create("stcp","google.com:4556", "");
            Eid path = (new CLASTCPParser()).create("stcp", "google.com:4556", "/pingservice");
            assertEquals("cla:stcp:google.com:4556", eid.getEidString());
            assertEquals("cla:stcp:google.com:4556/pingservice", path.getEidString());
            assertTrue(path.matches(eid));
        } catch (EidFormatException eid) {
            fail(eid.getMessage());
        }
    }

}
