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
import java.io.InputStream;
import java.util.Arrays;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.blobstore.api.Blobstore;
import org.everit.osgi.blobstore.api.BlobstoreException;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.log.LogService;

@Component(name = "BlobstoreTest",
immediate = true,
        metatype = true)
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

}
