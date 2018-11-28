package io.left.rightmesh.core.module.cla.stcp;

import org.junit.Test;

import io.left.rightmesh.libdtn.common.data.eid.BaseCLAEID;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class CLASTCPTest {

    @Test
    public void testEIDCLA() {
        System.out.println("[+] eid: testing BaseCLAEID Scheme");

        try {
            BaseCLAEID cla = new CLASTCP("google.com", 4556, "/");
            assertEquals("cla:stcp:google.com:4556/", cla.getEIDString());
            assertEquals("stcp", cla.cl_name);
            assertEquals("google.com:4556", cla.cl_specific);
            assertEquals("/", cla.cl_sink);

            EID eid = (new CLASTCPParser()).create("stcp","google.com:4556", "");
            EID path = (new CLASTCPParser()).create("stcp", "google.com:4556", "/pingservice");
            assertEquals("cla:stcp:google.com:4556", eid.getEIDString());
            assertEquals("cla:stcp:google.com:4556/pingservice", path.getEIDString());
            assertTrue(path.matches(eid));
        } catch (EIDFormatException eid) {
            fail(eid.getMessage());
        }
    }

}
