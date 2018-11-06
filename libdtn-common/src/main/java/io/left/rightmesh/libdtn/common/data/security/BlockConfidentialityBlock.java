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
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.CanonicalBlockSerializer;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public class BlockConfidentialityBlock extends AbstractSecurityBlock {

    public static final String TAG = "BlockConfidentialityBlock";
    public static final int type = 194;

    public BlockConfidentialityBlock() {
        super(type);
    }

    public void setCipherSuite(CipherSuites cipherSuite) {
        this.cipherSuiteId = cipherSuite.id;
    }

    private void checkBIBInteraction(Bundle bundle, BlockIntegrityBlock bib) {
        LinkedList<CanonicalBlock> matches = new LinkedList<>();
        for (int st : bib.securityTargets) {
            if (this.securityTargets.contains(st)) {
                matches.add(bundle.getBlock(st));
            }
        }

        /* 3.10 - cond 1 */
        if (matches.size() == bib.securityTargets.size()) {
            securityTargets.add(bib.number);
            return;
        }

        /* 3.10 - cond 2 */
        if (matches.size() > 0) {
            BlockIntegrityBlock newBib = new BlockIntegrityBlock(bib);
            newBib.securityTargets.clear();
            bundle.addBlock(newBib);
            for (CanonicalBlock b : matches) {
                bib.securityTargets.remove(b.number);
                newBib.securityTargets.add(b.number);
                this.securityTargets.add(newBib.number);
            }
        }
    }

    @Override
    public void addTo(Bundle bundle) throws NoSuchBlockException {
        for (Integer i : this.securityTargets) {
            if (bundle.getBlock(i) == null) {
                throw new NoSuchBlockException();
            }
        }

        bundle.addBlock(this);
        for (CanonicalBlock block : bundle.getBlocks()) {
            if (block.type == BlockIntegrityBlock.type) {
                checkBIBInteraction(bundle, (BlockIntegrityBlock) block);
            }
        }
    }

    @Override
    public void applyTo(Bundle bundle, SecurityContext context, Log logger) throws SecurityOperationException {
        for (int block_number : securityTargets) {
            logger.v(TAG, ".. applying encryption to: "+block_number);
            CanonicalBlock block = bundle.getBlock(block_number);
            if (block != null) {
                // init cipher
                Cipher cipher;
                try {
                    cipher = context.initCipherForEncryption(this.cipherSuiteId, this.securitySource);
                } catch (SecurityContext.NoSecurityContextFound | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                    throw new SecurityOperationException(e.getMessage());
                }

                if (block instanceof BlockBLOB) {
                    // encrypt in place
                    try {
                        block.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);
                        ((BlockBLOB) block).data.map(
                                byteBuffer -> ByteBuffer.wrap(cipher.update(byteBuffer.array())),
                                () -> ByteBuffer.wrap(cipher.doFinal()));
                    } catch (Exception e) {
                        throw new SecurityOperationException(e.getMessage());
                    }
                } else {
                    // create new block to hold the encrypted blob
                    EncryptedBlock encryptedBlock = new EncryptedBlock(block);
                    encryptedBlock.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);

                    // prepare block serializer
                    CborEncoder encoder = CanonicalBlockSerializer.encodePayload(block);
                    int size = encoder.observe(1024)
                            .map(ByteBuffer::remaining)
                            .reduce(0, (a, b) -> a + b)
                            .blockingGet();

                    // malloc space for the new encrypted block
                    encryptedBlock.data = new UntrackedByteBufferBLOB(size + cipher.getBlockSize());
                    WritableBLOB wblob = encryptedBlock.data.getWritableBLOB();

                    // encrypt serialized content
                    encoder.observe(cipher.getBlockSize() == 0 ? 128 : cipher.getBlockSize())
                            .subscribe( /* same thread */
                                    byteBuffer -> wblob.write(ByteBuffer.wrap(cipher.update(byteBuffer.array()))),
                                    e -> { },
                                    () -> wblob.write(ByteBuffer.wrap(cipher.doFinal())));
                    wblob.close();

                    // replace the current block with encrypted blob
                    bundle.updateBlock(block_number, encryptedBlock);
                }
            } else {
                /* should we thrown a NoSuchBlockException ? probably means that it was removed
                   along the way */
            }
        }
    }

    @Override
    public void applyFrom(Bundle bundle, SecurityContext context, Log logger) throws SecurityOperationException {
        for (int block_number : securityTargets) {
            CanonicalBlock block = bundle.getBlock(block_number);
            logger.v(TAG, ".. applying decryption to: "+block_number);
            if (block instanceof BlockBLOB && block.getV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED)) {

                // init cipher
                Cipher cipher;
                try {
                    cipher = context.initCipherForDecryption(this.cipherSuiteId, this.securitySource);
                } catch (SecurityContext.NoSecurityContextFound | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    throw new SecurityOperationException(e.getMessage());
                }

                try {
                    // decrypt
                    ((BlockBLOB) block).data.map(
                            byteBuffer -> {
                                if(byteBuffer.remaining() == byteBuffer.capacity()) {
                                    return ByteBuffer.wrap(cipher.update(byteBuffer.array()));
                                } else {
                                    byte[] array = new byte[byteBuffer.remaining()];
                                    byteBuffer.get(array);
                                    return ByteBuffer.wrap(cipher.update(array));
                                }
                            },
                            () -> ByteBuffer.wrap(cipher.doFinal()));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SecurityOperationException(e.getMessage());
                }
            } else {
                throw new SecurityOperationException("BCB target should be an encrypted BLOB");
            }

        }
    }

}
