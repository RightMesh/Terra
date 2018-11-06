package io.left.rightmesh.libdtn.common.data.security;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.CanonicalBlockSerializer;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock.TAG;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public class BlockIntegrityBlock extends AbstractSecurityBlock {


    public static final int type = 193;

    public BlockIntegrityBlock() {
        super(type);
    }

    BlockIntegrityBlock(BlockIntegrityBlock bib) {
        super(bib);
    }

    public void setDigestSuite(CipherSuites cipherSuite) {
        this.cipherSuiteId = cipherSuite.id;
    }

    private void checkBCBInteraction(BlockConfidentialityBlock bcb) throws ForbiddenOperationException {
        LinkedList<CanonicalBlock> matches = new LinkedList<>();
        for (int st : bcb.securityTargets) {

            /* 3.10 - condition 3 */
            if (this.securityTargets.contains(st)) {
                throw new ForbiddenOperationException();
            }
        }
    }

    @Override
    public void addTo(Bundle bundle) throws ForbiddenOperationException, NoSuchBlockException {
        for (int i : this.securityTargets) {
            if (bundle.getBlock(i) == null) {
                throw new NoSuchBlockException();
            }

            /* 3.10 - cond 6 */
            if (bundle.getBlock(i).type == BlockConfidentialityBlock.type) {
                throw new ForbiddenOperationException();
            }
        }

        for (CanonicalBlock block : bundle.getBlocks()) {
            if (block.type == BlockConfidentialityBlock.type) {
                checkBCBInteraction((BlockConfidentialityBlock) block);
            }
        }
        bundle.addBlock(this);
    }

    @Override
    public void applyTo(Bundle bundle, SecurityContext context, Log logger) throws SecurityOperationException {
        for (int block_number : securityTargets) {
            logger.v(TAG, ".. computing integrity for block number: " + block_number);
            CanonicalBlock block = bundle.getBlock(block_number);
            LinkedList<SecurityResult> results = new LinkedList<>();
            securityResults.add(results);
            if (block != null) {
                MessageDigest digest;
                try {
                    digest = context.initDigestForIntegrity(this.cipherSuiteId, this.securitySource);
                } catch (SecurityContext.NoSecurityContextFound | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                    throw new SecurityOperationException(e.getMessage());
                }


                CborEncoder encoder = CanonicalBlockSerializer.encodePayload(block);
                encoder.observe()
                        .subscribe( /* same thread */
                                byteBuffer -> digest.update(byteBuffer.array()),
                                e -> {
                                },
                                () -> {
                                });
                results.add(new IntegrityResult(digest.digest()));
            } else {
                /* should we thrown a NoSuchBlockException ? probably means that it was removed
                   along the way */
            }
        }
    }

    @Override
    public void applyFrom(Bundle bundle, SecurityContext context, Log logger) throws SecurityOperationException {
        if(securityResults.size() != securityTargets.size()) {
            throw new SecurityOperationException("There should be as many results as there is targets");
        }

        for (int i = 0; i < securityTargets.size(); i++) {
            int block_number = securityTargets.get(i);
            List<SecurityResult> results = securityResults.get(i);

            logger.v(TAG, ".. checking integrity for block number: " + block_number);
            CanonicalBlock block = bundle.getBlock(block_number);
            if (block != null) {
                MessageDigest digest;

                try {
                    digest = context.initDigestForVerification(this.cipherSuiteId, this.securitySource);
                    if(results.size() < CipherSuites.expectedResults(this.cipherSuiteId)) {
                        throw new SecurityOperationException("wrong number of result for this " +
                                "cipherId, id="+cipherSuiteId+" results="+results.size());
                    }
                    if(results.get(0).getResultId() != 1) {
                        throw new SecurityOperationException("ResultId should have been 1");
                    }
                } catch (SecurityContext.NoSecurityContextFound | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                    throw new SecurityOperationException(e.getMessage());
                }

                IntegrityResult ir = (IntegrityResult)results.get(0);

                CborEncoder encoder = CanonicalBlockSerializer.encodePayload(block);
                encoder.observe()
                        .subscribe( /* same thread */
                                byteBuffer -> {
                                    if(byteBuffer.remaining() == byteBuffer.capacity()) {
                                        digest.update(byteBuffer.array());
                                    } else {
                                        byte[] array = new byte[byteBuffer.remaining()];
                                        byteBuffer.get(array);
                                        digest.update(array);
                                    }
                                },
                                e -> {
                                },
                                () -> {
                                });

                byte[] checkDigest = digest.digest();
                if(!Arrays.equals(ir.getChecksum(), checkDigest)) {
                    logger.v(TAG, ".. integrity failed for target block=" + block_number);
                    throw new SecurityOperationException("checksum doesn't match: "
                            +"\nbib_result="+new String(ir.getChecksum())
                            +"\ndigest="+new String(checkDigest));
                } else {
                    logger.v(TAG, ".. integrity ok for target block=" + block_number);
                }
            } else {
                /* should we thrown a NoSuchBlockException ? probably means that it was removed
                   along the way */
            }
        }
    }

}
