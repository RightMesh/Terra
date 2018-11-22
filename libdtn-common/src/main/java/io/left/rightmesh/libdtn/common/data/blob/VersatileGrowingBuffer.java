package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import io.left.rightmesh.libdtn.common.utils.Function;
import io.left.rightmesh.libdtn.common.utils.Supplier;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * @author Lucien Loiseau on 01/11/18.
 */
public class VersatileGrowingBuffer extends VolatileBLOB {

    private static final int VOLATILE_BLOB_SIZE = 20000;

    private BLOBFactory factory;
    private LinkedList<BLOB> blobs;
    private int blobSizeUnit = VOLATILE_BLOB_SIZE;

    public VersatileGrowingBuffer(BLOBFactory factory) throws BLOBFactory.BLOBFactoryException {
        this.factory = factory;
        blobs = new LinkedList<>();
        allocateBLOB();
    }

    public VersatileGrowingBuffer(BLOBFactory factory, int blob_size) throws BLOBFactory.BLOBFactoryException {
        this.factory = factory;
        this.blobSizeUnit = blob_size;
        blobs = new LinkedList<>();
        allocateBLOB();
    }

    private BLOB allocateBLOB() throws BLOBFactory.BLOBFactoryException {
        BLOB blob = factory.createBLOB(blobSizeUnit);
        blobs.add(blob);
        return blob;
    }

    @Override
    public long size() {
        return Observable.fromIterable(blobs)
                .map(BLOB::size)
                .reduce(
                        0L,
                        (a,b) -> a+b)
                .blockingGet();
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.fromIterable(blobs).concatMap(BLOB::observe, 1);
    }

    @Override
    public void map(Supplier<ByteBuffer> open, Function<ByteBuffer, ByteBuffer> update, Supplier<ByteBuffer> close) throws Exception {
        if(blobs.size() == 0) {
            return;
        }

        for(int i = 0; i < blobs.size(); i++) {
            if(i == 0) {
                // first
                blobs.get(i).map(open, update, () -> ByteBuffer.allocate(0));
            }
            if(i == blobs.size()-1) {
                // last
                blobs.getLast().map(() -> ByteBuffer.allocate(0), update, close);
            }
            if((i > 0) && (i < blobs.size()-1)){
                // intermediate
                blobs.get(i).map(() -> ByteBuffer.allocate(0), update, () -> ByteBuffer.allocate(0));
            }
        }
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new WritableBLOB() {
            {{
                cur = blobs.getLast().getWritableBLOB();
            }}

            private WritableBLOB cur;

            @Override
            public void clear() {
                blobs.forEach(b -> b.getWritableBLOB().clear());
                blobs.clear();
            }

            @Override
            public int write(byte b) throws IOException, BLOBOverflowException {
                try {
                    cur.write(b);
                    return 1;
                } catch(IOException | BLOBOverflowException e) {
                    cur.close();
                    /* create new blob */
                }

                try {
                    allocateBLOB();
                    cur = blobs.getLast().getWritableBLOB();
                } catch( BLOBFactory.BLOBFactoryException e) {
                    throw new IOException(e);
                }

                cur.write(b);
                return 1;
            }

            @Override
            public int write(byte[] a) throws IOException, BLOBOverflowException {
                for(int i = 0; i < a.length; i++) {
                    write(a[i]);
                }
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws IOException, BLOBOverflowException {
                int size = buffer.remaining();
                while(buffer.hasRemaining()) {
                    write(buffer.get());
                }
                return size;
            }

            @Override
            public int write(InputStream stream) throws IOException, BLOBOverflowException {
                int read = 0;
                int b;
                while((b = stream.read()) != -1) {
                    write((byte)b);
                    read++;
                }
                return read;
            }

            @Override
            public int write(InputStream stream, int size) throws IOException, BLOBOverflowException {
                int read = 0;
                int b;
                while(((b = stream.read()) != -1) && (read < size)) {
                    write((byte)b);
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
    public boolean isFileBLOB() {
        return blobs.size() == 1 && blobs.getLast() instanceof FileBLOB;
    }

    @Override
    public String getFilePath() throws NotFileBLOB {
        if(!isFileBLOB()) {
            throw new NotFileBLOB();
        }
        return blobs.getLast().getFilePath();
    }

    @Override
    public Completable moveToFile(String path) {
        if(isFileBLOB()) {
            return blobs.getLast().moveToFile(path);
        } else {
            return super.moveToFile(path);
        }
    }
}
