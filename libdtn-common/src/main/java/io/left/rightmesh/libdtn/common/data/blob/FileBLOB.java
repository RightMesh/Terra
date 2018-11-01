package io.left.rightmesh.libdtn.common.data.blob;

import io.left.rightmesh.libdtn.common.data.Tag;
import io.reactivex.Completable;
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
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * FileBLOB holds a BLOB in a file saved in persistent storage. Useful for large BLOB that can't
 * fit in memory or if persistence over reboot is necessary for long caching strategy.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class FileBLOB extends Tag implements BLOB {

    private static final int BUFFER_SIZE = 4096;
    private File file;

    public String getPathToBLOB() {
        return file.getAbsolutePath();
    }

    /**
     * Constructor: creates a BLOB from a path. It will open the file and check for existence.
     * note that the file must be created beforehand!
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

    /**
     * Constructor: creates a BLOB out of an already created file.
     *
     * @param file to file
     * @throws IOException if the file cannot be accessed
     */
    public FileBLOB(File file) throws IOException {
        this.file = file;
        if (!file.exists()) {
            throw new IOException("Can't access file: " + file.getAbsolutePath());
        }
    }

    @Override
    public String getFilePath() {
        return file.getAbsolutePath();
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
                    return new FileInputStream(file).getChannel();
                },
                (channel, emitter) -> {
                    if (channel == null) {
                        emitter.onError(new Throwable("couldn't open File CoreBLOBFactory"));
                        return null;
                    }

                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(2048);
                        buffer.clear();
                        if (channel.read(buffer) == -1) {
                            emitter.onComplete();
                            channel.close();
                        } else {
                            buffer.flip();
                            emitter.onNext(buffer);
                            return channel;
                        }
                    } catch (NonReadableChannelException  | IOException io) {
                        emitter.onError(io);
                        channel.close();
                    }
                    return channel;
                },
                (channel) -> channel.close());
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
            public int write(ByteBuffer buffer) throws IOException {
                int length = buffer.remaining();
                while(buffer.hasRemaining()) {
                    int b = buffer.get();
                    System.out.println("b="+(char)b);
                    bos.write(b);
                }
                return length;
            }

            @Override
            public int write(InputStream stream) throws IOException {
                open();
                int total = 0;
                byte[] fileBuffer = new byte[BUFFER_SIZE];
                int count = stream.read(fileBuffer, 0, BUFFER_SIZE);
                while (count > 0) {
                    bos.write(fileBuffer, 0, count);
                    total += count;
                    count = stream.read(fileBuffer, 0, BUFFER_SIZE);
                }
                return total;
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
            public void close() {
                try {
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException io) {
                    // ignore
                }
            }
        };
    }

    @Override
    public boolean isFileBLOB() {
        return true;
    }

    @Override
    public Completable moveToFile(String newLocation) {
        return Completable.create(s -> {
            try {
                Path sourcePath = Paths.get(file.getAbsolutePath());
                Path destinationPath = Paths.get(newLocation);
                Files.move(sourcePath, destinationPath, REPLACE_EXISTING);
                file = new File(newLocation);
                if (!file.exists()) {
                    s.onError(new IOException("Can't access file: " + newLocation));
                } else {
                    s.onComplete();
                }
            } catch(IOException io) {
                s.onError(io);
            }
        });
    }
}
