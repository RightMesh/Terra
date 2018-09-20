package io.left.rightmesh.libdtn.data.bundleV7;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Formatter;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.CRC;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PayloadBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class BundleV7Test {

    String testPayload = "This is a test for bundle serialization";


    Bundle testBundle1() {
        Bundle bundle = new Bundle();
        bundle.addBlock(new PayloadBlock(testPayload));
        bundle.destination = EID.createIPN(5, 12);
        bundle.source = EID.createDTN("source");
        bundle.reportto = EID.NullEID();
        return bundle;
    }

    Bundle testBundle2() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        return bundle;
    }

    Bundle testBundle3() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        return bundle;
    }

    Bundle testBundle4() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        return bundle;
    }


    Bundle testBundle5() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        bundle.crcType = PrimaryBlock.CRCFieldType.CRC_16;
        return bundle;
    }

    @Test
    public void testSimpleBundleSerialization() {
        Bundle[] bundles = {
                testBundle1(),
                testBundle2(),
                testBundle3(),
                testBundle4(),
                testBundle5()
        };

        for(Bundle bundle : bundles) {
            Bundle[] res = {null};

            // prepare serializer
            CborEncoder enc = BundleV7Serializer.encode(bundle);

            // prepare parser
            BundleV7Parser p = BundleV7Parser.create(b -> {
                res[0] = b;
            });

            // serialize and parse
            enc.observe(10).subscribe(buf -> {
                try {
                    if (p.read(buf)) {
                        assertEquals(false, buf.hasRemaining());
                    }
                } catch (RxParserException rpe) {
                    rpe.printStackTrace();
                    fail();
                }
            });

            // check payload
            checkBundlePayload(res[0]);
        }
    }



    void checkBundlePayload(Bundle bundle) {
        // assert
        assertEquals(true, bundle != null);
        String[] payload = {null};
        if (bundle != null) {
            bundle.getPayloadBlock().data.observe().subscribe(
                    buffer -> {
                        byte[] arr = new byte[buffer.remaining()];
                        buffer.get(arr);
                        payload[0] = new String(arr);
                    });

            assertEquals(true, payload[0] != null);
            if (payload[0] != null) {
                assertEquals(testPayload, payload[0]);
            }
        }
    }


    // debug

    private String getEncodedString(CborEncoder enc) {
        // get all in one buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enc.observe().subscribe(b -> {
            while (b.hasRemaining()) {
                baos.write(b.get());
            }
        });

        // return the string
        Formatter formatter = new Formatter();
        formatter.format("0x");
        for (byte b : baos.toByteArray()) {
            formatter.format("%02x", b);
        }
        return (formatter.toString());
    }

    private void showRemaining(String prefix, ByteBuffer buf) {
        Formatter formatter = new Formatter();
        formatter.format(prefix + " remaining (" + buf.remaining() + "): 0x");
        while (buf.hasRemaining()) {
            formatter.format("%02x", buf.get());
        }
        System.out.println(formatter.toString());
    }

}
