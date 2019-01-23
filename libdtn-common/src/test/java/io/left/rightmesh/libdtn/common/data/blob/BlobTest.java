package io.left.rightmesh.libdtn.common.data.blob;

import org.junit.*;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory.BlobFactoryException;
import org.junit.rules.ExpectedException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * BlobTest.java
 *
 * Provides CRUD Data Integrity Coverage to: BaseBlobFactory.java and FileBlob.java
 *
 * @author Matthew Falkner on 22/01/2019
 */


public class BlobTest {

    private BaseBlobFactory blobFactory;
    private final int max_size = 100000;
    private String path = "./";

    private int getRandomBlobSize() {
        Random rand = new Random();
        int min = 10000;
        int max = 99999;
        return rand.nextInt((max - min) + 1) + min;
    }

    private String getRandomString(int size) {
        byte[] array = new byte[size];
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }

    @Test
    public void testVolatileBlobFromFactory() {
        blobFactory = new BaseBlobFactory().enableVolatile(max_size);
        int randomBlobSize = getRandomBlobSize();

        try {
            Blob blob = blobFactory.createVolatileBlob(randomBlobSize);
            assertEquals("Failed: Blob memory size is greater than inputted", blob.size(), (long) randomBlobSize);
            assertTrue("Failed: Blob is not set to volatile.", blobFactory.isVolatileEnabled());
        } catch (BlobFactoryException e) {
            e.printStackTrace();
            fail("Failed: Blob was not a File.");
        }
    }

    /* Data Integrity Check of the Persistent Blob Type.
     * Tests Creating, Writing, Reading and Destroying these Blobs.
     */
    @Test
    public void testCRUDPersistentBlob() {

        blobFactory = new BaseBlobFactory().enablePersistent(path);
        try {
            Blob blob = blobFactory.createFileBlob(max_size);
            String foundPath = blob.getFilePath();
            File blobFile = new File(foundPath);


            /* Checking blob file status */
            assertTrue("Failed: Blob File does not exist", blobFile.exists());
            assertTrue("Failed: Blob is not writable", blobFile.canWrite());
            assertTrue("Failed: Blob is not readable", blobFile.canRead());
            assertTrue("Failed: BlobFactory is not persistent", blobFactory.isPersistentEnabled());
            assertTrue("Failed: Blob is volatile, and not persistent, for some reason.", blob.isFileBlob());


            /* Checking the integrity of the Writable Blob. Write Data. */
            WritableBlob writableBlob = blob.getWritableBlob();

            int randomByteData = getRandomBlobSize();
            String randomStringForFile = getRandomString(randomByteData);
            byte[] bytesOfString = randomStringForFile.getBytes();
            final int amountOfBytesWritten = writableBlob.write(bytesOfString);

            /* Read that data back in */
            String[] payload = {null};

            blob.observe().subscribe(
                    buffer -> {
                        byte[] arr = new byte[buffer.remaining()];
                        buffer.get(arr);
                        payload[0] = new String(arr);
                    }, err -> {
                        payload[0] = null;

                    });

            assertNotNull("Error: could not read in data from the .blob file, it was null.", payload[0]);

            String msg = "Error: amount of bytes supposedly written is not the same as in the file. Written: " + amountOfBytesWritten + " !=   Found: " + blobFile.length();
            Assert.assertEquals(msg,amountOfBytesWritten, blobFile.length());

            String path = blob.getFilePath();
            blob.moveToFile(path);

            writableBlob.clear();
            Assert.assertFalse("Error: File was found after being deleted." , blobFile.exists());

        } catch (BlobFactoryException e) {
            e.printStackTrace();
            fail("Failed: Could not Create a Persistent Blob in Factory.");

        } catch (Blob.NotFileBlob e) {
            e.printStackTrace();
            fail("Failed: Blob was not a File.");

        }
        catch (WritableBlob.BlobOverflowException e) {
            e.printStackTrace();
            fail("Failed: Blob overflow occurred when trying to write a random string");

        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Failed: IO Exception Failed Either creating the file, or moving it. Check Stack Trace Printed.");
        }
    }


    /* Purposefully cause an exception in the BlobFactory
     * throwing the BlobFactoryException.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBlobFactoryException() throws BlobFactoryException {
        thrown.expect(BlobFactoryException.class);

        blobFactory = new BaseBlobFactory().enableVolatile(max_size);
        blobFactory = blobFactory.disablePersistent();
        Blob badBlob = blobFactory.createFileBlob(getRandomBlobSize());
    }

    /* Data Integrity Check for FileBlob.java.
     * Create a FileBlob. Call its getters for integrity.
     * Move the Blob and with moveToFile and test integrity.
     */
    @Test
    public void testCRUDFileBlob() {
        String commonPath = System.getProperty("java.io.tmpdir");
        int randomBlobSize = getRandomBlobSize();
        String path = commonPath + "/test-" + randomBlobSize + "-blob.blob";

        try {

            FileBlob fileBlob = new FileBlob(path);
            File blobFound = new File(fileBlob.getFilePath());

            /* Checking the integrity of the Writable Blob. Write Data. */
            WritableBlob writableBlob = fileBlob.getWritableBlob();
            String randomStringForFile = getRandomString(randomBlobSize);
            byte[] bytesOfString = randomStringForFile.getBytes();
            final int amountOfBytesWritten = writableBlob.write(bytesOfString);

            /* Read that data back in */
            String[] payload = {null};

            fileBlob.observe().subscribe( buffer -> {
                byte[] arr = new byte[buffer.remaining()];
                buffer.get(arr);
                payload[0] = new String(arr);

            }, err -> {
                payload[0] = null;
            });


            assertNotNull("Error: could not read in data from the .blob file, it was null.", payload[0]);

            String msg = "Error: amount of bytes supposedly written is not the same as in the file. Written: " + amountOfBytesWritten + " !=   Found: " + blobFound.length();
            Assert.assertEquals(msg,amountOfBytesWritten, blobFound.length());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed: IO Exception Failed Either creating the file, or moving it. Check Stack Trace Printed.");
        } catch (WritableBlob.BlobOverflowException e) {
            e.printStackTrace();
            fail("Failed: Blob overflow occurred when trying to write a random string");
        }

    }

}
