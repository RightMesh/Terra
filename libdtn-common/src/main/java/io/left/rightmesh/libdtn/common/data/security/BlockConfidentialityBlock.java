package io.left.rightmesh.libdtn.common.data.security;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.VersatileGrowingBuffer;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.CanonicalBlockItem;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockHeaderSerializer;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
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

    public void applyTo(Bundle bundle,
                        SecurityContext context,
                        BlockDataSerializerFactory serializerFactory,
                        Log logger) throws SecurityOperationException {
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
                    // we encrypt in place so we modify the BLOB to hold encrypted data instead of
                    // plaintext. However, a block data BLOB holds data but we have to encrypt the
                    // cbor_representation of the data. We could use data.observe() but it is not
                    // necessary, since a blob is just a byte string, we can simply prepend a cbor
                    // start byte string flag.
                    long blockDataSize = ((BlockBLOB) block).data.size();
                    CborEncoder enc = CBOR.encoder().cbor_start_byte_string(blockDataSize);
                    int headersize = enc.observe().map(ByteBuffer::remaining).reduce(0, (a,b)->a+b).blockingGet();
                    ByteBuffer cborHeader = ByteBuffer.allocate(headersize);
                    enc.observe().subscribe(cborHeader::put);
                    cborHeader.flip();

                    // actual encryption
                    try {
                        block.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);
                        ((BlockBLOB) block).data.map(
                                () -> ByteBuffer.wrap(cipher.update(cborHeader.array())),
                                byteBuffer -> ByteBuffer.wrap(cipher.update(byteBuffer.array())),
                                () -> ByteBuffer.wrap(cipher.doFinal()));
                    } catch (Exception e) {
                        throw new SecurityOperationException(e.getMessage());
                    }
                } else {
                    // The block is not a BLOB so it holds unserialized data. To encrypt a non-blob
                    // block. we will serialized the block in order to have the cbor representation
                    // that we will encrypt.

                    // create new block to hold the encrypted blob
                    EncryptedBlock encryptedBlock = new EncryptedBlock(block);
                    encryptedBlock.setV7Flag(BlockV7Flags.BLOCK_IS_ENCRYPTED, true);

                    // prepare block serializer
                    CborEncoder encoder;
                    try {
                        encoder = serializerFactory.create(block);
                    } catch(BlockDataSerializerFactory.UnknownBlockTypeException ubte) {
                        throw new SecurityOperationException("target block serializer not found");
                    }

                    int encodedSize = encoder.observe(1024)
                            .map(ByteBuffer::remaining)
                            .reduce(0, (a, b) -> a + b)
                            .blockingGet();

                    // malloc space for the new encrypted block
                    encryptedBlock.data = new UntrackedByteBufferBLOB(cipher.getOutputSize(encodedSize)+cipher.getBlockSize());
                    WritableBLOB wblob = encryptedBlock.data.getWritableBLOB();

                    // encrypt serialized content
                    encoder.observe(cipher.getBlockSize() == 0 ? 128 : cipher.getBlockSize())
                            .subscribe( /* same thread */
                                    byteBuffer -> {
                                        ByteBuffer bb =ByteBuffer.wrap(cipher.update(byteBuffer.array()));
                                        wblob.write(bb);
                                    },
                                    e -> { },
                                    () -> {
                                        ByteBuffer bb = ByteBuffer.wrap(cipher.doFinal());
                                        wblob.write(bb);
                                    });
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

    public void applyFrom(Bundle bundle,
                          SecurityContext context,
                          ExtensionToolbox toolbox,
                          Log logger) throws SecurityOperationException {
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
                    // decrypted block
                    CborParser parser = CBOR.parser()
                            .cbor_parse_custom_item(
                                    () -> new CanonicalBlockItem(logger, toolbox,
                                            (size) -> new VersatileGrowingBuffer(UntrackedByteBufferBLOB::new, 1024)),
                                    (__, ___, item) -> {
                                        bundle.updateBlock(block.number, item.block);
                                    });

                    // serialize block header
                    block.crcType = CRCFieldType.NO_CRC;
                    BlockHeaderSerializer.encode(block).observe().subscribe(
                            parser::read
                    );

                    // decrypt
                    ((BlockBLOB) block).data.map( // same thread
                            () -> ByteBuffer.allocate(0),
                            byteBuffer -> {
                                if(byteBuffer.remaining() == byteBuffer.capacity()) {
                                    byte[] out = cipher.update(byteBuffer.array());
                                    ByteBuffer bb;
                                    if(out != null) {
                                        bb = ByteBuffer.wrap(out);
                                    } else {
                                        bb = ByteBuffer.allocate(0);
                                    }
                                    parser.read(bb);
                                    return bb;
                                } else {
                                    byte[] array = new byte[byteBuffer.remaining()];
                                    byteBuffer.get(array);
                                    byte[] out = cipher.update(array);
                                    ByteBuffer bb;
                                    if(out != null) {
                                        bb = ByteBuffer.wrap(out);
                                    } else {
                                        bb = ByteBuffer.allocate(0);
                                    }
                                    parser.read(bb);
                                    return bb;
                                }
                            },
                            () -> {
                                ByteBuffer bb = ByteBuffer.wrap(cipher.doFinal());
                                parser.read(bb);
                                return bb;
                            });
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
