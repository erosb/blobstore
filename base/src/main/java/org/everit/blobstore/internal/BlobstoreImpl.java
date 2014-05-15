/**
 * This file is part of Everit - Blobstore.
 *
 * Everit - Blobstore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.storage.BlobstoreStorage;
import org.everit.blobstore.api.storage.BlobstoreStorageReader;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;

@Component(name = "org.everit.blobstore.Blobstore", metatype = true)
@Properties({
        @Property(name = "storage.target"),
        @Property(name = "cache.target")
})
@Service
public class BlobstoreImpl implements Blobstore {

    @Reference
    private ConcurrentMap<Long, Boolean> cache;

    @Reference
    private BlobstoreStorage storage;

    private BlobstoreCacheService cacheService;

    @Activate
    public void activate() {
        // TODO
        cacheService = new BlobstoreCacheService(null, null);
    }

    public void bindCache(final ConcurrentMap<Long, Boolean> cache) {
        this.cache = cache;
    }

    public void bindStorage(final BlobstoreStorage storage) {
        this.storage = storage;
    }

    @Override
    public void deleteBlob(final long blobId) {
        storage.deleteBlob(blobId);
    }

    @Override
    public long getBlobSizeByBlobId(final long blobId) {
        try {
            return storage.createReader(cacheService, blobId, 0).getTotalSize();
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        }
    }

    @Override
    public String getDescriptionByBlobId(final long blobId) {
        return storage.getDescriptionByBlobId(blobId);
    }

    @Override
    public void readBlob(final long blobId, final long startPosition, final BlobReader blobReader) {
        Objects.requireNonNull(blobReader, "blobReader cannot be null");
        BlobstoreStorageReader storageReader = null;
        InputStream stream = null;
        try {
            storageReader = storage.createReader(cacheService, blobId, startPosition);
            stream = new BlobReaderInputStream(blobId, startPosition, storageReader);
            long totalSize = storageReader.getTotalSize();
            if (totalSize < startPosition) {
                throw new BlobstoreException("startPosition(=" + startPosition
                        + ") cannot be higher than totalSize(=" + totalSize + ") of blob #" + blobId);
            }
            BufferedInputStream bis = new BufferedInputStream(stream);
            blobReader.readBlob(bis);
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {
            try {
                if (storageReader != null) {
                    storageReader.close();
                }
            } catch (IOException e) {
                throw new BlobstoreException(e);
            }
        }
    }

    @Override
    public long storeBlob(final InputStream blobStream, final Long length, final String description) {
        return storage.storeBlob(blobStream, length, description);
    }

}
