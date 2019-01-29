package io.left.rightmesh.libdtn.common.data.bundlev7;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.parser.RxParserException;
import io.left.rightmesh.libdtn.common.BaseExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.*;
import io.left.rightmesh.libdtn.common.data.blob.BaseBlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.AdministrativeRecordSerializer;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.StatusReportSerializer;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.EidIpn;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Test class to test serialization and parsing of a Bundle.
 *
 * @author Lucien Loiseau on 20/09/18.
 */
public class BundleV7Test {

    public static String testPayload = "This is a test for bundle serialization";

    /**
     * create a simple test Bundle with no payload.
     * @return a Bundle
     */
    public static Bundle testBundle0() {
        Bundle bundle = new Bundle();
        bundle.setDestination(new EidIpn(5, 12));
        bundle.setSource(DtnEid.unsafe("source"));
        bundle.setReportto(DtnEid.nullEid());
        bundle.bid = BundleId.create(bundle);
        return bundle;
    }

    /**
     * create a simple test Bundle with a payload.
     * @return a Bundle
     */
    public static Bundle testBundle1() {
        Bundle bundle = testBundle0();
        bundle.addBlock(new PayloadBlock(testPayload));
        return bundle;
    }

    /**
     * create a simple test Bundle with payload and an ageblock.
     * @return a Bundle
     */
    public static Bundle testBundle2() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        return bundle;
    }

    /**
     * create a simple test Bundle with payload, an ageblock and a scopecontrolhoplimit.
     * @return a Bundle.
     */
    public static Bundle testBundle3() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        return bundle;
    }

    /**
     * create a simple test Bundle with payload, an ageblock and a scopecontrolhoplimit
     * and previous node block.
     * @return a Bundle
     */
    public static Bundle testBundle4() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(DtnEid.generate()));
        return bundle;
    }

    /**
     * create a simple test Bundle with payload, an ageblock and a scopecontrolhoplimit,
     * previous node block and enable crc on primary block.
     * @return a Bundle
     */
    public static Bundle testBundle5() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(DtnEid.generate()));
        bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_32);
        return bundle;
    }

    /**
     * create a simple test Bundle with payload, an ageblock and a scopecontrolhoplimit,
     * previous node block and enable crc on all block.
     * @return a Bundle
     */
    public static Bundle testBundle6() {
        Bundle bundle = testBundle0();
        bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_32);

        CanonicalBlock age = new AgeBlock();
        age.crcType = BlockHeader.CrcFieldType.CRC_16;

        CanonicalBlock scope = new ScopeControlHopLimitBlock();
        scope.crcType = BlockHeader.CrcFieldType.CRC_16;

        CanonicalBlock payload = new PayloadBlock(testPayload);
        payload.crcType = BlockHeader.CrcFieldType.CRC_32;

        CanonicalBlock previous = new PreviousNodeBlock();
        previous.crcType = BlockHeader.CrcFieldType.CRC_32;

        bundle.addBlock(age);
        bundle.addBlock(scope);
        bundle.addBlock(payload);
        bundle.addBlock(previous);
        return bundle;
    }


    /*
     * Unit test for the AdministrativeRecordSerializer.
     * Tests the serializer by creating a report with each type of 'reason'
     * followed by an observation that the report exists/was created.
     */
    @Test
    public void testAdministrativeRecordSerializer() {

        for (StatusReport.ReasonCode reason: StatusReport.ReasonCode.values()) {
            Bundle bundle = new Bundle();
            String payload = "This is a test for testing block operations";

            bundle.addBlock(new PayloadBlock(payload));
            bundle.addBlock(new AgeBlock());
            bundle.addBlock(new ScopeControlHopLimitBlock());

            StatusReport statusReport = new StatusReport(reason);
            statusReport.source = bundle.getSource();
            statusReport.creationTimestamp = bundle.getCreationTimestamp();

            CborEncoder enc = AdministrativeRecordSerializer.encode(statusReport);

            long size = enc.observe()
                    .map(ByteBuffer::remaining)
                    .reduce(0, (a, b) -> a + b)
                    .blockingGet();

            Assert.assertTrue("Serialized object has no serialized data", size > 0);
        }
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

        for (Bundle bundle : bundles) {
            Bundle[] res = {null};

            // prepare serializer
            CborEncoder enc = BundleV7Serializer.encode(bundle,
                    new BaseBlockDataSerializerFactory());

            // prepare parser
            CborParser parser = CBOR.parser().cbor_parse_custom_item(
                    () -> new BundleV7Item(
                            new SimpleLogger(),
                            new BaseExtensionToolbox(),
                            new BaseBlobFactory().enableVolatile(100000).disablePersistent()),
                    (p, t, item) ->
                            res[0] = item.bundle);

            // serialize and parse
            enc.observe(10).subscribe(
                    buf -> {
                        try {
                            if (parser.read(buf)) {
                                assertEquals(false, buf.hasRemaining());
                            }
                        } catch (RxParserException rpe) {
                            rpe.printStackTrace();
                            fail();
                        }
                    },
                    e -> {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    });

            // check payload
            checkBundlePayload(res[0]);
        }
    }


    /**
     * check that the payload of the bundle is correct.
     * @param bundle to check
     */
    public static void checkBundlePayload(Bundle bundle) {
        // assert
        assertEquals(true, bundle != null);
        String[] payload = {null};
        if (bundle != null) {
            for (CanonicalBlock block : bundle.getBlocks()) {
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
            assertEquals(testPayload, payload[0]);
        }
    }
}
