package io.left.rightmesh.libdtn.common.data.blob;

import io.left.rightmesh.libdtn.common.utils.Function;
import io.left.rightmesh.libdtn.common.utils.Supplier;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * VersatileGrowingBuffer is a growing Blob that uses volatile or persistent buffer to grow
 * depending on what is available everytime it needs to instantiate a new Blob to grow.
 *
 * @author Lucien Loiseau on 01/11/18.
 */
public class VersatileGrowingBuffer extends VolatileBlob {

    private static final int VOLATILE_BLOB_SIZE = 20000;

    private BlobFactory factory;
    private LinkedList<Blob> blobs;
    private int blobSizeUnit = VOLATILE_BLOB_SIZE;

    /**
     * Constructor requires a factory to instantiate new Blob chunk as it grows.
     *
     * @param factory to instantiates new Blob.
     * @throws BlobFactory.BlobFactoryException if it cannot instantiate the first chunk.
     */
    public VersatileGrowingBuffer(BlobFactory factory) throws BlobFactory.BlobFactoryException {
        this.factory = factory;
        blobs = new LinkedList<>();
        allocateBlob();
    }

    /**
     * Constructor requires a factory to instantiate new Blob chunk as it grows.
     *
     * @param factory  to instantiates new Blob.
     * @param blobSize to set each chunk size.
     * @throws BlobFactory.BlobFactoryException if it cannot instantiate the first chunk.
     */
    public VersatileGrowingBuffer(BlobFactory factory, int blobSize)
            throws BlobFactory.BlobFactoryException {
        this.factory = factory;
        this.blobSizeUnit = blobSize;
        blobs = new LinkedList<>();
        allocateBlob();
    }

    private Blob allocateBlob() throws BlobFactory.BlobFactoryException {
        Blob blob = factory.createBlob(blobSizeUnit);
        blobs.add(blob);
        return blob;
    }

    @Override
    public long size() {
        return Observable.fromIterable(blobs)
                .map(Blob::size)
                .reduce(
                        0L,
                        (a, b) -> a + b)
                .blockingGet();
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.fromIterable(blobs).concatMap(Blob::observe, 1);
    }

    @Override
    public void map(Supplier<ByteBuffer> open,
                    Function<ByteBuffer, ByteBuffer> update,
                    Supplier<ByteBuffer> close) throws Exception {
        if (blobs.size() == 0) {
            return;
        }

        for (int i = 0; i < blobs.size(); i++) {
            if (i == 0) {
                // first chunk
                blobs.get(i).map(open, update, () -> ByteBuffer.allocate(0));
            }
            if (i == blobs.size() - 1) {
                // last chunk
                blobs.getLast().map(() -> ByteBuffer.allocate(0), update, close);
            }
            if ((i > 0) && (i < blobs.size() - 1)) {
                // intermediate chunk
                blobs.get(i).map(
                        () -> ByteBuffer.allocate(0),
                        update,
                        () -> ByteBuffer.allocate(0));
            }
        }
    }

    @Override
    public WritableBlob getWritableBlob() {
        return new WritableBlob() {
            {
                {
                    cur = blobs.getLast().getWritableBlob();
                }
            }

            private WritableBlob cur;

            @Override
            public void clear() {
                blobs.forEach(b -> b.getWritableBlob().clear());
                blobs.clear();
            }

            @Override
            public int write(byte b) throws IOException, BlobOverflowException {
                try {
                    cur.write(b);
                    return 1;
                } catch (IOException | BlobOverflowException e) {
                    cur.close();
                    /* create new blob */
                }

                try {
                    allocateBlob();
                    cur = blobs.getLast().getWritableBlob();
                } catch (BlobFactory.BlobFactoryException e) {
                    throw new IOException(e);
                }

                cur.write(b);
                return 1;
            }

            @Override
            public int write(byte[] a) throws IOException, BlobOverflowException {
                for (int i = 0; i < a.length; i++) {
                    write(a[i]);
                }
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws IOException, BlobOverflowException {
                int size = buffer.remaining();
                while (buffer.hasRemaining()) {
                    write(buffer.get());
                }
                return size;
            }

            @Override
            public int write(InputStream stream) throws IOException, BlobOverflowException {
                int read = 0;
                int b;
                while ((b = stream.read()) != -1) {
                    write((byte) b);
                    read++;
                }
                return read;
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BlobOverflowException {
                int read = 0;
                int b;
                while (((b = stream.read()) != -1) && (read < size)) {
                    write((byte) b);
                    read++;
                }
                return read;
            }

            @Override
            public void close() {
                cur.close();
            }
        };
    }

    @Override
    public boolean isFileBlob() {
        return blobs.size() == 1 && blobs.getLast() instanceof FileBlob;
    }

    @Override
    public String getFilePath() throws NotFileBlob {
        if (!isFileBlob()) {
            throw new NotFileBlob();
        }
        return blobs.getLast().getFilePath();
    }

    @Override
    public Completable moveToFile(String path) {
        if (isFileBlob()) {
            return blobs.getLast().moveToFile(path);
        } else {
            return super.moveToFile(path);
        }
    }
}
