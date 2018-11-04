package io.left.rightmesh.libdtn.common.data.security;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.CanonicalBlockSerializer;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public class BlockConfidentialityBlock extends AbstractSecurityBlock {

    public static int type = 194;

    BlockConfidentialityBlock() {
        super(type);
    }

    private void checkBIBInteraction(Bundle bundle, BlockIntegrityBlock bib) {
        LinkedList<CanonicalBlock> matches = new LinkedList<>();
        for(int st : bib.securityTargets) {
            if(this.securityTargets.contains(st)) {
                matches.add(bundle.getBlock(st));
            }
        }

        /* 3.10 - cond 1 */
        if(matches.size() == bib.securityTargets.size()) {
            securityTargets.add(bib.number);
            return;
        }

        /* 3.10 - cond 2 */
        if(matches.size() > 0) {
            BlockIntegrityBlock newBib = new BlockIntegrityBlock(bib);
            newBib.securityTargets.clear();
            bundle.addBlock(newBib);
            for(CanonicalBlock b : matches) {
                bib.securityTargets.remove(b.number);
                newBib.securityTargets.add(b.number);
                this.securityTargets.add(newBib.number);
            }
        }
    }

    @Override
    public void addTo(Bundle bundle) throws ForbiddenOperationException, NoSuchBlockException {
        for(Integer i : this.securityTargets) {
            if(bundle.getBlock(i) == null) {
                throw new NoSuchBlockException();
            }
        }

        bundle.addBlock(this);
        for(CanonicalBlock block : bundle.getBlocks()) {
            if(block.type == BlockIntegrityBlock.type) {
                checkBIBInteraction(bundle, (BlockIntegrityBlock)block);
            }
        }
    }

    @Override
    public void applyTo(Bundle bundle, SecurityContext context) throws SecurityOperationException {
        Cipher cipher;
        try {
            cipher = CipherSuites.BcbCipherSuites.fromId(this.cipherSuiteId).getCipher();
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityOperationException(e.getMessage());
        }

        for(int block_number : securityTargets) {
            CanonicalBlock block = bundle.getBlock(block_number);
            if(block != null) {
                try {
                    context.initCipher(cipher, this, bundle);
                } catch (SecurityContext.NoSecurityContextFound e) {
                    throw new SecurityOperationException(e.getMessage());
                }

                if(block instanceof BlockBLOB) {
                    try {
                        block.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);
                        ((BlockBLOB) block).data.map(
                                byteBuffer -> ByteBuffer.wrap(cipher.update(byteBuffer.array())),
                                () -> ByteBuffer.wrap(cipher.doFinal()));
                    } catch(Exception e) {
                        throw new SecurityOperationException(e.getMessage());
                    }
                } else {
                    EncryptedBlock encryptedBlock = new EncryptedBlock(block);
                    encryptedBlock.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);

                    CborEncoder encoder = CanonicalBlockSerializer.encodePayload(block);
                    int size = encoder.observe(1024)
                            .map(ByteBuffer::remaining)
                            .reduce(0, (a,b) -> a+b)
                            .blockingGet();

                    encryptedBlock.data = new UntrackedByteBufferBLOB(size);
                    WritableBLOB wblob = encryptedBlock.data.getWritableBLOB();
                    encoder.observe(cipher.getBlockSize() == 0 ? 128 : cipher.getBlockSize())
                            .map(byteBuffer ->
                                    wblob.write(ByteBuffer.wrap(cipher.update(byteBuffer.array()))))
                            .doOnComplete(
                                    () -> wblob.write(ByteBuffer.wrap(cipher.doFinal())))
                            .subscribe();
                    wblob.close();


                    // replace the block


                }
            } else {
                /* should we thrown a NoSuchBlockException ? probably means that it was removed
                   along the way */
            }
        }

    }

    @Override
    public void applyFrom(Bundle bundle, SecurityContext context) {
    }

}
