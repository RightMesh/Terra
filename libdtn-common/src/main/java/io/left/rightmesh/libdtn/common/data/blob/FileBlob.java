package io.left.rightmesh.libdtn.common.data.blob;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import io.left.rightmesh.libdtn.common.data.Tag;
import io.left.rightmesh.libdtn.common.utils.Function;
import io.left.rightmesh.libdtn.common.utils.Supplier;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileBlob holds a Blob in a file saved in persistent storage. Useful for large Blob that can't
 * fit in memory or if persistence over reboot is necessary for long caching strategy.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class FileBlob extends Tag implements Blob {

    private static final int BUFFER_SIZE = 4096;
    private File file;

    public String getPathToBlob() {
        return file.getAbsolutePath();
    }

    /**
     * Constructor: creates a Blob from a path. It will open the file and check for existence.
     * note that the file must be created beforehand!
     *
     * @param absolutePath to file
     * @throws IOException if the file cannot be accessed
     */
    public FileBlob(String absolutePath) throws IOException {
        file = new File(absolutePath);
        if (!file.exists()) {
            throw new IOException("Can't access file: " + absolutePath);
        }
    }

    /**
     * Constructor: creates a Blob out of an already created file.
     *
     * @param file to file
     * @throws IOException if the file cannot be accessed
     */
    public FileBlob(File file) throws IOException {
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
                        emitter.onError(new Throwable("couldn't open FileBlob"));
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
                    } catch (NonReadableChannelException | IOException io) {
                        emitter.onError(io);
                        channel.close();
                    }
                    return channel;
                },
                (channel) -> channel.close());
    }

    @Override
    public void map(Supplier<ByteBuffer> open,
                    Function<ByteBuffer, ByteBuffer> update,
                    Supplier<ByteBuffer> close) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("file not found: " + file.getAbsolutePath());
        }

        FileChannel readChannel = new RandomAccessFile(file, "r").getChannel();
        FileChannel writeChannel = new RandomAccessFile(file, "rw").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.clear();

        writeChannel.write(open.get());
        while ((readChannel.read(buffer) != -1)) {
            buffer.flip();
            writeChannel.write(update.apply(buffer));
            buffer.clear();
        }
        writeChannel.write(close.get());

        readChannel.close();
        writeChannel.close();
    }

    @Override
    public WritableBlob getWritableBlob() {
        return new WritableFileBlob();
    }

    @Override
    public boolean isFileBlob() {
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
            } catch (IOException io) {
                s.onError(io);
            }
        });
    }


    private class WritableFileBlob implements WritableBlob {

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
            while (buffer.hasRemaining()) {
                int b = buffer.get();
                System.out.println("bundle=" + (char) b);
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
    }
}
