package io.left.rightmesh.libdtn.common.data.blob;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.left.rightmesh.libdtn.common.data.Tag;
import io.reactivex.Completable;

/**
 * @author Lucien Loiseau on 31/10/18.
 */
public abstract class VolatileBLOB extends Tag implements BLOB {

    @Override
    public boolean isFileBLOB() {
        return false;
    }

    @Override
    public String getFilePath() throws NotFileBLOB {
        throw new NotFileBLOB();
    }

    @Override
    public Completable moveToFile(String path) {
        return Completable.create(s -> {
            File file = new File(path);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("could not create file: " + path);
                    }
                } catch(IOException io) {
                    s.onError(io);
                }
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            observe().subscribe(
                    b -> {
                        try {
                            while (b.hasRemaining()) {
                                bos.write(b.get());
                            }
                        } catch(IOException io) {
                            s.onError(io);
                        }
                    },
                    e -> {
                        try {
                            bos.close();
                        } catch(IOException io) {
                            /* silently ignore */
                        }
                        file.delete();
                        s.onError(e);
                    },
                    () -> {
                        try {
                            bos.close();
                        } catch(IOException io) {
                            /* silently ignore */
                        }
                        s.onComplete();
                    }
            );
        });
    }

}
