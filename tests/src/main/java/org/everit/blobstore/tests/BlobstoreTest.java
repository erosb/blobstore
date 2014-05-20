/**
 * This file is part of Everit - Blobstore Tests.
 *
 * Everit - Blobstore Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.blobstore.api.BlobReader;
import org.everit.osgi.blobstore.api.Blobstore;
import org.everit.osgi.blobstore.api.BlobstoreException;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.log.LogService;

@Component(name = "BlobstoreTest",
        immediate = true,
        metatype = true,
        policy = ConfigurationPolicy.REQUIRE,
        configurationFactory = true)
@Service(value = BlobstoreTest.class)
@Properties({
        @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "blobstoreTest"),
})
public class BlobstoreTest {

    @Reference
    private LogService log;

    @Reference
    private Blobstore blobstore;

    private final Object lock = new Object();

    public void bindBlobstore(final Blobstore blobstore) {
        this.blobstore = blobstore;
    }

    public void bindLog(final LogService log) {
        this.log = log;
    }

    public Blobstore getBlobstore() {
        return blobstore;
    }

    public LogService getLog() {
        return log;
    }

    private long storeBlobSupport(final int length, final byte constantByte) {
        byte[] byteArray = new byte[length];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = constantByte;
        }
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        return blobstore.storeBlob(inputStream, Long.valueOf(length), "");
    }

    public void stressTestBlobstoreService() {
        final int blobNum = 1000;
        final int minBlobLength = 10000;
        final int maxBlobLength = 300000;
        final int readNum = 100000;
        final int workerThreads = 5;
        log.log(LogService.LOG_INFO, "Starting stress test with the following properties:\n"
                + " Number of Blobs: " + blobNum + "\n Minimal blob length: " + minBlobLength
                + "\n Maximal blob length: " + maxBlobLength + "\n Number of random reads: " + readNum);
        final List<Long> blobIds = Collections.synchronizedList(new ArrayList<Long>());

        Date blobCreationStart = new Date();
        log.log(LogService.LOG_INFO, "Starting blob creation");

        final AtomicInteger workingThreads = new AtomicInteger(workerThreads);
        for (int t = 0; t < workerThreads; t++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final Random random = new Random();
                    for (int i = 0, n = blobNum / workerThreads; i < n; i++) {
                        Long blobId = blobstore.storeBlob(new DummyInputStream(Long.MAX_VALUE, 0),
                                (long) (random.nextInt(maxBlobLength - minBlobLength) + minBlobLength), "Dummy");
                        blobIds.add(blobId);
                    }
                    workingThreads.decrementAndGet();
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }).start();
        }
        waitUntilThreadsAreFinished(workingThreads);
        Date blobReadingStart = new Date();
        log.log(LogService.LOG_INFO, "Blob creation took " + (blobReadingStart.getTime() - blobCreationStart.getTime())
                + "ms");

        workingThreads.set(workerThreads);

        for (int t = 0; t < workerThreads; t++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final Random random = new Random();
                    for (int i = 0, n = readNum / workerThreads; i < n; i++) {
                        int blobIdIndex = random.nextInt(blobNum);
                        Long blobId = blobIds.get(blobIdIndex);
                        blobstore.readBlob(blobId, random.nextInt(minBlobLength), new BlobReader() {

                            @Override
                            public void readBlob(final InputStream blobStream) {
                                try {
                                    int available = blobStream.available();
                                    int byteToReadNum = random.nextInt(available);
                                    for (int j = 0; j < byteToReadNum; j++) {
                                        blobStream.read();
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException("Could not get available byte num from blob stream", e);
                                }
                            }
                        });
                    }
                    workingThreads.decrementAndGet();
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }).start();
        }
        waitUntilThreadsAreFinished(workingThreads);
        Date blobDeletingStart = new Date();
        log.log(LogService.LOG_INFO, "Blob reading took " + (blobDeletingStart.getTime() - blobReadingStart.getTime())
                + "ms");
        for (Long blobId : blobIds) {
            blobstore.deleteBlob(blobId);
        }
        Date blobDeletingEnd = new Date();
        log.log(LogService.LOG_INFO, "Blob deleting took " + (blobDeletingEnd.getTime() - blobDeletingStart.getTime())
                + "ms");
    }

    @Test
    @TestDuringDevelopment
    public void testBlobSize() {
        final int length = 5000000;
        final byte constantByte = 34;
        long blobId = storeBlobSupport(length, constantByte);
        long size = blobstore.getBlobSizeByBlobId(blobId);
        Assert.assertEquals(length, size);
    }

    @Test
    @TestDuringDevelopment
    public void testBlobstoreService() {
        final int dummyStreamLength = 500;
        final int dummyStreamStartingPoint = 5;
        final int readingBlobStartinPoint = 490;
        DummyInputStream is = new DummyInputStream(dummyStreamLength, dummyStreamStartingPoint);
        Long blobId = blobstore.storeBlob(is, null, "Dummy");
        blobstore.readBlob(blobId, readingBlobStartinPoint, new BlobReader() {

            @Override
            public void readBlob(final InputStream blobStream) {
                try {
                    final int readingBlobShiftedStartingPoint = readingBlobStartinPoint
                            + dummyStreamStartingPoint;
                    Assert.assertTrue(DummyInputStream.couldBeFromDummyStream(blobStream,
                            readingBlobShiftedStartingPoint));
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected error during reading from blobStream", e);
                }
            }
        });
        String description = blobstore.getDescriptionByBlobId(blobId);
        if (!"Dummy".equals(description)) {
            Assert.fail("Description of blob is not the same as the one that was stored.");
        }
        blobstore.deleteBlob(blobId);
    }

    @Test
    @TestDuringDevelopment
    public void testNullInputStream() {
        try {
            blobstore.storeBlob(null, (long) 0, "");
            Assert.fail("should have been thrown.");
        } catch (NullPointerException e) {
            Assert.assertEquals("blobStream cannot be null", e.getMessage());
        }
    }

    @Test
    @TestDuringDevelopment
    public void testReadAfterWrite() {
        final int length = 5000000;
        final byte constantByte = 34;
        long blobId = storeBlobSupport(length, constantByte);
        blobstore.readBlob(blobId, 0, new BlobReader() {
            @Override
            public void readBlob(final InputStream inputStream) {
                try {
                    for (int i = 0; i < length; i++) {
                        int read = inputStream.read();
                        if (read == -1) {
                            Assert.fail("Unexpected end of stream, the index of the current byte is: " + i
                                    + ", the length is " + length);
                        }
                        Assert.assertEquals(constantByte, read);
                    }
                    int read = inputStream.read();
                    if (read != -1) {
                        Assert.fail("inputStream end reached, the value should be -1");
                    }
                } catch (IOException e) {
                    Assert.fail("Can not read from inputStream");
                    throw new RuntimeException("Cannot read from inputStream", e);
                }
            }
        });
    }

    @Test
    @TestDuringDevelopment
    public void testTooLongDescription() {
        char[] tooLongDescriptionCharArray = new char[Blobstore.BLOB_DESCRIPTION_MAX_LENGTH + 1];
        Arrays.fill(tooLongDescriptionCharArray, '1');
        try {
            blobstore.storeBlob(new DummyInputStream(0, 0), (long) 0,
                    String.valueOf(tooLongDescriptionCharArray));
            Assert.fail("storeBlob() did not throw exception for too long description");
        } catch (BlobstoreException e) {
            String expected = "description length must be at most 255, actual length: 256";
            Assert.assertEquals(expected, e.getMessage());
        }
    }

    @Test
    @TestDuringDevelopment
    public void testUknownBlobId() {
        final long blobIdThatDoesNotExist = 332322223;
        String expectedExceptionMsg = "blob [" + blobIdThatDoesNotExist + "] does not exist";
        try {
            blobstore.readBlob(blobIdThatDoesNotExist, 0, new DummyBlobReader());
            Assert.fail("ReadBlob should have failed with unknown blob id");
        } catch (BlobstoreException e) {
            Assert.assertEquals(expectedExceptionMsg, e.getMessage());
        }
        try {
            blobstore.getDescriptionByBlobId(blobIdThatDoesNotExist);
            Assert.fail("Getting description should have failed with unknown blob id");
        } catch (BlobstoreException e) {
            Assert.assertEquals(expectedExceptionMsg, e.getMessage());
        }
    }

    @Test
    @TestDuringDevelopment
    public void testZeroLengthBlobAndBlobstoreTooHighExceptionBlob() {
        DummyInputStream is = new DummyInputStream(0, 0);
        Long blobId = blobstore.storeBlob(is, null, "Dummy");
        try {
            blobstore.readBlob(blobId, 1, new DummyBlobReader());
            Assert.fail("Calling readBlob with higher startposition than the "
                    + "size of the blob should have died with exception");
        } catch (BlobstoreException e) {
            Assert.assertEquals("startPosition(=1) cannot be higher than totalSize(=0) of blob #" + blobId,
                    e.getMessage());
        }
        blobstore.deleteBlob(blobId);
    }

    private void waitUntilThreadsAreFinished(final AtomicInteger workingThreads) {
        final int waitPeriodForRunningThreads = 100;
        synchronized (lock) {
            while (workingThreads.get() > 0) {
                try {
                    lock.wait(waitPeriodForRunningThreads);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
