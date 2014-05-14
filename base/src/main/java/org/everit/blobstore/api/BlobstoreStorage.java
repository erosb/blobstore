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
package org.everit.blobstore.api;

import java.io.InputStream;
import java.sql.SQLException;

import org.everit.blobstore.base.AbstractBlobReaderInputStream;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;

/**
 * The service of a blob store that may be used to stored binary values. The service must be implemented in the way that
 * if a {@link BlobstoreCacheService} is available it will be used first to find the fragments of a blob and the
 * database will be queried only if the fragments are not available in the cache.
 */
public interface BlobstoreStorage {

    /**
     * The maximum length of a blob description.
     */
    public static final int BLOB_DESCRIPTION_MAX_LENGTH = 255;

    public static final int DEFAULT_BUFFER_SIZE = 2048;

    AbstractBlobReaderInputStream createInputStream(BlobstoreCacheService cache, long blobId, long startPosition)
            throws SQLException;

    /**
     * Deleting a blob from the store.
     *
     * @param blobId
     *            The unique id of the blob that will be deleted.
     * @throws org.everit.util.core.validation.ValidationException
     *             if the blobId parameter is null.
     */
    void deleteBlob(long blobId);

    /**
     * Getting the description of a blob by it's id.
     *
     * @param blobId
     *            The id of the blob.
     * @return The description.
     * @throws BlobstoreException
     *             from the following reasons: {@link ErrorCode#BLOB_DOES_NOT_EXIST}, {@link ErrorCode#SQL_EXCEPTION}.
     */
    String getDescriptionByBlobId(long blobId);

    long storeBlobNoParamCheck(InputStream blobStream, Long length, String description);
}
