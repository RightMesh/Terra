package io.left.rightmesh.libdtn.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class DTNConfigurationTest {


    @Test
    public void testLocalEIDConfiguration() {
        String testEID = "dtn:test";
        DTNConfiguration conf = new DTNConfiguration();
        conf.<String>get(DTNConfiguration.Entry.LOCAL_EID).update(testEID);
        String localEID = conf.<String>get(DTNConfiguration.Entry.LOCAL_EID).value();
        assertEquals(testEID, localEID);
    }

}
