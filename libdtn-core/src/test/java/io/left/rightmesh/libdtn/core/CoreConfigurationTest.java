package io.left.rightmesh.libdtn.core;

import org.junit.Test;

import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class CoreConfigurationTest {


    @Test
    public void testLocalEIDConfiguration() {
        try {
            EID testEID = new DTN("test");
            CoreConfiguration conf = new CoreConfiguration();
            conf.<EID>get(ConfigurationAPI.CoreEntry.LOCAL_EID).update(testEID);
            EID localEID = conf.<EID>get(ConfigurationAPI.CoreEntry.LOCAL_EID).value();
            assertEquals(testEID.getEIDString(), localEID.getEIDString());
        } catch(EIDFormatException ignore) {
            // sould not happen
            //todo: create a safe EID constructor by encoding URI
        }
    }

}
