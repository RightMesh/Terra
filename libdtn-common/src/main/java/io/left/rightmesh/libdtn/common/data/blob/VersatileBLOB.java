package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class VersatileBLOB extends VolatileBLOB {

    private BLOB volatileBLOB;
    private BLOB fileBLOB;

    VersatileBLOB(BLOB volatileBLOB, BLOB fileBLOB) {
        this.volatileBLOB = volatileBLOB;
        this.fileBLOB = fileBLOB;
    }

    @Override
    public long size() {
        return volatileBLOB.size() + fileBLOB.size();
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return volatileBLOB.observe().concatWith(fileBLOB.observe());
    }

    public class VersatileReadableBLOB implements ReadableBLOB {
        ReadableBLOB volatileReadableBLOB;
        ReadableBLOB fileReadableBLOB;

        VersatileReadableBLOB() {
            volatileReadableBLOB = volatileBLOB.getReadableBLOB();
            fileReadableBLOB = fileBLOB.getReadableBLOB();
        }

        @Override
        public void read(OutputStream stream) throws IOException {
            volatileReadableBLOB.read(stream);
            fileReadableBLOB.read(stream);
        }

        @Override
        public void close() {
            volatileReadableBLOB.close();
            fileReadableBLOB.close();
        }
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new VersatileReadableBLOB();
    }

    public class VersatileWritableBLOB implements WritableBLOB {


        WritableBLOB volatileWritableBLOB;
        WritableBLOB fileWritableBLOB;
        boolean mode_volatile = true;

        VersatileWritableBLOB() {
            volatileWritableBLOB = volatileBLOB.getWritableBLOB();
            fileWritableBLOB = fileBLOB.getWritableBLOB();
        }

        @Override
        public void clear() {
            volatileWritableBLOB.clear();
            fileWritableBLOB.clear();
        }

        @Override
        public int write(byte b) throws IOException, BLOBOverflowException {
            if(mode_volatile) {
                try {
                    volatileWritableBLOB.write(b);
                    return 1;
                } catch(IOException | BLOBOverflowException e) {
                    mode_volatile = false;
                }
            }
            fileWritableBLOB.write(b);
            return 1;
        }

        @Override
        public int write(byte[] a) throws IOException, BLOBOverflowException {
            for(byte b : a) {
                write(b);
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
            int read = 0;
            int b;
            while((b = stream.read()) != -1) {
                write((byte)b);
                read++;
            }
            return read;
        }

        @Override
        public int write(InputStream stream, int size)
                throws IOException, BLOBOverflowException {
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
            volatileWritableBLOB.close();
            fileWritableBLOB.close();
        }
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new VersatileWritableBLOB();
    }

    @Override
    public boolean isFileBLOB() {
        return (volatileBLOB instanceof ZeroBLOB && fileBLOB instanceof FileBLOB);
    }

    @Override
    public String getFilePath() throws NotFileBLOB {
        if(isFileBLOB()) {
            return fileBLOB.getFilePath();
        }
        throw new NotFileBLOB();
    }

    @Override
    public Completable moveToFile(String path) {
        if(isFileBLOB()) {
            return fileBLOB.moveToFile(path);
        } else {
            return super.moveToFile(path);
        }
    }
}
