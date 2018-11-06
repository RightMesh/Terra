package io.left.rightmesh.libdtn.common.data.bundleV7;

import org.junit.Test;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityContext;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;

import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.checkBundlePayload;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle1;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle2;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle3;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle4;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle5;
import static io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Test.testBundle6;
import static io.left.rightmesh.libdtn.common.data.security.CipherSuites.BCB_AES128_CBC_PKCS5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 06/11/18.
 */
public class BundleEncryptionTest {


    @Test
    public void testSimpleBundleEncryption() {
        System.out.println("[+] bundle: testing encryption");
        Log logger = new NullLogger();
        Bundle[] bundles = {
                testBundle1(),
                testBundle2(),
                testBundle3(),
                testBundle4(),
                testBundle5(),
                testBundle6()
        };

        /* create security context */
        SecurityContext context = SecurityContextTest.mockSecurityContext();

        for(Bundle b : bundles) {
            /* encrypt the payload of the bundles */
            BlockConfidentialityBlock bcb = new BlockConfidentialityBlock();
            bcb.addTarget(0);
            bcb.setCipherSuite(BCB_AES128_CBC_PKCS5);

            try {
                // offer integrity block
                bcb.addTo(b);
            } catch (SecurityBlock.NoSuchBlockException foe) {
                fail();
            }

            try {
                // perform integrity sum
                bcb.applyTo(b, context, logger);
            } catch (SecurityBlock.SecurityOperationException foe) {
                System.out.println(foe.getMessage());
                foe.printStackTrace();
                fail();
            }
        }

        for (Bundle bundle : bundles) {
            Bundle[] res = {null};

            // prepare serializer
            CborEncoder enc = BundleV7Serializer.encode(bundle);

            // prepare parser
            CborParser p = CBOR.parser().cbor_parse_custom_item(
                    () -> new BundleV7Item(
                            logger,
                            new BaseBLOBFactory().enableVolatile(100000).disablePersistent()),
                    (__, ___, item) ->
                            res[0] = item.bundle);

            // serialize and parse
            enc.observe(10).subscribe(
                    buf -> {
                        try {
                            if (p.read(buf)) {
                                assertEquals(false, buf.hasRemaining());
                            }
                        } catch (RxParserException rpe) {
                            rpe.printStackTrace();
                        }
                    },
                    e -> {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    });

            for (CanonicalBlock block : res[0].blocks) {
                if (block.type == BlockConfidentialityBlock.type) {
                    try {
                        ((BlockConfidentialityBlock) block).applyFrom(res[0], context, logger);
                    } catch(SecurityBlock.SecurityOperationException e) {
                        e.printStackTrace();
                        fail();
                    }
                }
            }

            // check the payload
            checkBundlePayload(res[0]);
        }

    }

}
