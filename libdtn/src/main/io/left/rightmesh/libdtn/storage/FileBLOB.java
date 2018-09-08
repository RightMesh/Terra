package io.left.rightmesh.libdtn.storage;

import io.reactivex.Flowable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * FileBLOB holds a BLOB in a file saved in persistent storage. Useful for large BLOB that can't
 * fit in memory or if persistence over reboot is necessary for long caching strategy.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class FileBLOB extends BLOB {

    private static final int BUFFER_SIZE = 4096;
    private File file;

    /**
     * Constructor: creates a BLOB out of a file present on the disk.
     *
     * @param absolutePath to file
     * @throws IOException if the file cannot be accessed
     */
    public FileBLOB(String absolutePath) throws IOException {
        file = new File(absolutePath);
        if (!file.exists()) {
            throw new IOException("Can't access file: " + absolutePath);
        }
    }

    @Override
    public long size() {
        if (!file.exists()) {
            return 0;
        } else {
            return file.length();
        }
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.generate(
                () -> {
                    if (!file.exists()) {
                        return null;
                    }
                    FileInputStream fis = new FileInputStream(file);
                    return new ImmutablePair<>(fis, ByteBuffer.allocate(2048));
                },
                (state, emitter) -> {
                    if (state == null) {
                        emitter.onError(new Throwable("couldn't open File BLOB"));
                        return state;
                    }

                    try {
                        FileChannel channel = state.left.getChannel();
                        ByteBuffer buffer = state.right;
                        buffer.clear();
                        if (channel.read(buffer) == -1) {
                            emitter.onComplete();
                            state.left.close();
                        } else {
                            buffer.flip();
                            emitter.onNext(buffer);
                        }
                    } catch (NonReadableChannelException nrce) {
                        emitter.onError(nrce);
                        state.left.close();
                    } catch (ClosedChannelException cce) {
                        emitter.onError(cce);
                        state.left.close();
                    } catch (IOException io) {
                        emitter.onError(io);
                        state.left.close();
                    }
                    return state;
                });
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ReadableBLOB() {

            private FileInputStream fis = null;
            private BufferedInputStream bis = null;

            @Override
            public void read(OutputStream stream) throws IOException {
                if (!file.exists()) {
                    throw new IOException("can't access file");
                }
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);

                byte[] fileBuffer = new byte[BUFFER_SIZE];
                int count = bis.read(fileBuffer, 0, BUFFER_SIZE);
                while (count > 0) {
                    stream.write(fileBuffer, 0, count);
                    count = bis.read(fileBuffer, 0, BUFFER_SIZE);
                }
            }

            @Override
            public void close() {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
            }
        };
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new WritableBLOB() {

            private FileOutputStream fos = null;
            private BufferedOutputStream bos = null;
            private boolean open = false;

            @Override
            public void clear() {
                if (!file.exists()) {
                    return;
                }
                file.delete();
            }

            private void open() throws IOException {
                if (open) {
                    return;
                }

                if (!file.exists()) {
                    throw new IOException("can't access file");
                }
                fos = new FileOutputStream(file, true);
                bos = new BufferedOutputStream(fos);
                open = true;
            }

            @Override
            public int write(InputStream stream, int size) throws IOException {
                open();
                int total = 0;
                byte[] fileBuffer = new byte[BUFFER_SIZE];
                int count = stream.read(fileBuffer, 0, Math.min(BUFFER_SIZE, size));
                while (count > 0) {
                    bos.write(fileBuffer, 0, count);
                    total += count;
                    count = stream.read(fileBuffer, 0, Math.min(BUFFER_SIZE, size - total));
                }

                if (total != size) {
                    throw new IOException("We read " + (total < size ? "less" : "more")
                            + " bytes than expected");
                }
                return total;
            }

            @Override
            public int write(byte b) throws IOException {
                open();
                bos.write(b);
                return 1;
            }

            @Override
            public int write(byte[] a) throws IOException {
                open();
                bos.write(a);
                return a.length;
            }

            @Override
            public void close() {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
                try {
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
            }
        };
    }


    /**
     * Tries to move the file embedded in this FileBLOB to the destination file. It is useful if
     * a bundle needs to be put in storage and the payload BLOB is already a FileBLOB, by moving it
     * we avoid the entire read/write of the file that can be quite large.
     *
     * @param destinationPath path to destination
     * @throws IOException if we can't access the source file or if it is busy reading or writing.
     */
    public void moveTo(String destinationPath) throws IOException {
        if (!file.exists()) {
            throw new IOException("Can't access file: " + this.file);
        }
        //Files.move(file, destinationPath, REPLACE_EXISTING);
    }
}
