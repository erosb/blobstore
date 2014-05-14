/**
 * This file is part of Everit - Blobstore Base.
 *
 * Everit - Blobstore Base is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore Base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore Base.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.base;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.BlobstoreStorage;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;

@Component
@Service
public class BlobstoreImpl implements Blobstore {

    @Reference
    private Map<Long, Boolean> clusteredCache;

    @Reference
    private BlobstoreStorage storage;

    private BlobstoreCacheService cache;

    @Activate
    public void activate() {
        // TODO
        cache = new BlobstoreCacheService(null, null);
    }

    public void bindClusteredCache(final Map<Long, Boolean> clusteredCache) {
        this.clusteredCache = clusteredCache;
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
            return storage.createInputStream(cache, blobId, 0).getTotalSize();
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
        AbstractBlobReaderInputStream inputStream = null;
        Objects.requireNonNull(blobReader, "blobReader cannot be null");
        try {
            inputStream = storage.createInputStream(cache, blobId, startPosition);
            long totalSize = inputStream.getTotalSize();
            if (totalSize < startPosition) {
                throw new BlobstoreException("startPosition(=" + startPosition
                        + ") cannot be higher than totalSize(=" + totalSize + ") of blob #" + blobId);
            }
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            blobReader.readBlob(bis);
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new BlobstoreException(e);
            }
        }
    }

    @Override
    public long storeBlob(final InputStream blobStream, final Long length, final String description) {
        Objects.requireNonNull(blobStream, "blobStream cannot be null");
        if ((description != null) && (description.length() > Blobstore.BLOB_DESCRIPTION_MAX_LENGTH)) {
            throw new BlobstoreException("description length must be at most " +
                    Blobstore.BLOB_DESCRIPTION_MAX_LENGTH + ", actual length: " + description.length());
        }
        return storage.storeBlobNoParamCheck(blobStream, length, description);
    }

}
