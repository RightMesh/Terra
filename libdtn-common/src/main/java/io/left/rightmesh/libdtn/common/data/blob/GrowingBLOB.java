package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class GrowingBLOB extends VolatileBLOB {

    private static final int CHUNK_SIZE = 2048;

    private VolatileMemory memory;
    private LinkedList<ByteBuffer> buffers;
    private int total_size = 0;
    private int limit = -1;

    public GrowingBLOB(VolatileMemory memory) throws IOException {
        this.memory = memory;
        buffers = new LinkedList<>();
        buffers.add(memory.malloc(CHUNK_SIZE));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        memory.free(CHUNK_SIZE*buffers.size());
        buffers.clear();
    }

    @Override
    public long size() {
        return total_size;
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.generate(
                () -> buffers.iterator(),
                (it, s) -> {
                    if(it.hasNext()) {
                        ByteBuffer dup = it.next().duplicate();
                        dup.limit(dup.position());
                        dup.position(0);
                        s.onNext(dup);
                    } else {
                        s.onComplete();
                    }
                    return it;
                });
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ReadableBLOB() {
            @Override
            public void read(OutputStream stream) throws IOException {
                for(ByteBuffer buf : buffers) {
                    ByteBuffer dup = buf.duplicate();
                    dup.limit(dup.position());
                    dup.position(0);
                    while(dup.hasRemaining()) {
                        stream.write(dup.get());
                    }
                }
            }

            @Override
            public void close() {
                // do nothing
            }
        };
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new WritableBLOB() {
            @Override
            public void clear() {
                buffers.clear();
            }

            @Override
            public int write(byte b) throws IOException, BLOBOverflowException {
                if((limit >= 0) && (total_size > limit)) {
                    throw new BLOBOverflowException();
                }
                if (buffers.getLast().hasRemaining()) {
                    buffers.getLast().put(b);
                } else {
                    buffers.add(memory.malloc(CHUNK_SIZE));
                    buffers.getLast().put(b);
                }
                total_size++;
                return 1;
            }

            @Override
            public int write(byte[] a) throws IOException, BLOBOverflowException {
                int position = 0;
                while (position < a.length) {
                    write(a[position++]);
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
            public int write(InputStream stream)
                    throws IOException, BLOBOverflowException {
                int size = 0;
                int b;
                while ((b = stream.read()) != -1) {
                    write((byte)b);
                    size++;
                }
                return size;
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BLOBOverflowException {
                int read = 0;
                int b;
                while (((b = stream.read()) != -1) && (read < size)) {
                    write((byte)b);
                    read++;
                }
                return size;
            }

            @Override
            public void close() {
                /* do nothing */
            }
        };
    }

}
