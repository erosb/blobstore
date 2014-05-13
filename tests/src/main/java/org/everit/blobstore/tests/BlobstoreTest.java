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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.blobstore.api.Blobstore;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.log.LogService;

@Component(name = "BlobstoreTest", immediate = true)
@Service(value = BlobstoreTest.class)
@Properties({
    @Property(name = "eosgi.testEngine", value = "junit4"),
    @Property(name = "eosgi.testId", value = "blobstoreTest"),
})
public class BlobstoreTest {

    @Reference
    private LogService log;

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

    @Test
    @TestDuringDevelopment
    public void testBlobSize() {
        Assert.assertNotNull(log);
        System.out.println("testBlobSize() running!");
    }

}
