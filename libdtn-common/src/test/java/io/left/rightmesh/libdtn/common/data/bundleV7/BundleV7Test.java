package io.left.rightmesh.libdtn.common.data.bundleV7;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Formatter;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.eid.IPN;
import io.left.rightmesh.libdtn.common.utils.NullLogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class BundleV7Test {

    public static String testPayload = "This is a test for bundle serialization";

    public static Bundle testBundle0() {
        Bundle bundle = new Bundle();
        bundle.destination = new IPN(5, 12);
        bundle.source = new DTN("source");
        bundle.reportto = DTN.NullEID();
        bundle.bid = BundleID.create(bundle);
        return bundle;
    }

    public static Bundle testBundle1() {
        Bundle bundle = testBundle0();
        bundle.addBlock(new PayloadBlock(new String(testPayload)));
        return bundle;
    }

    public static Bundle testBundle2() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        return bundle;
    }

    public static Bundle testBundle3() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        return bundle;
    }

    public static Bundle testBundle4() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        return bundle;
    }


    public static Bundle testBundle5() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        bundle.crcType = PrimaryBlock.CRCFieldType.CRC_32;
        return bundle;
    }

    public static Bundle testBundle6() {
        Bundle bundle = testBundle0();
        bundle.crcType = PrimaryBlock.CRCFieldType.CRC_32;

        CanonicalBlock age = new AgeBlock();
        CanonicalBlock scope = new ScopeControlHopLimitBlock();
        CanonicalBlock payload = new PayloadBlock(testPayload);
        CanonicalBlock previous = new PreviousNodeBlock();

        age.crcType = BlockHeader.CRCFieldType.CRC_16;
        scope.crcType = BlockHeader.CRCFieldType.CRC_16;
        payload.crcType = BlockHeader.CRCFieldType.CRC_32;
        previous.crcType = BlockHeader.CRCFieldType.CRC_32;

        bundle.addBlock(age);
        bundle.addBlock(scope);
        bundle.addBlock(payload);
        bundle.addBlock(previous);
        return bundle;
    }

    @Test
    public void testSimpleBundleSerialization() {
        System.out.println("[+] bundle: testing serialization and parsing with 6 test bundles");

        Bundle[] bundles = {
                testBundle1(),
                testBundle2(),
                testBundle3(),
                testBundle4(),
                testBundle5(),
                testBundle6()
        };

        for(Bundle bundle : bundles) {
            Bundle[] res = {null};

            // prepare serializer
            CborEncoder enc = BundleV7Serializer.encode(bundle);

            // prepare parser
            BundleV7Parser bundleParser = new BundleV7Parser(new NullLogger(), new BaseBLOBFactory().disablePersistent());
            CborParser p = bundleParser.createBundleParser(b -> {
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



    public static void checkBundlePayload(Bundle bundle) {
        // assert
        assertEquals(true, bundle != null);
        String[] payload = {null};
        if (bundle != null) {
            for(CanonicalBlock block : bundle.getBlocks()) {
                assertEquals(true, block.isTagged("crc_check"));
                assertEquals(true, block.<Boolean>getTagAttachment("crc_check"));
            }

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

    public static void showRemaining(String prefix, ByteBuffer buf) {
        buf.mark();
        Formatter formatter = new Formatter();
        formatter.format(prefix + " remaining (" + buf.remaining() + "): 0x");
        while (buf.hasRemaining()) {
            formatter.format("%02x", buf.get());
        }
        System.out.println(formatter.toString());
        buf.reset();
    }

}
