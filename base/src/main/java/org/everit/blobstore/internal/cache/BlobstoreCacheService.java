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
package org.everit.blobstore.internal.cache;

import java.util.List;

/**
 * Caching service running under {@link BlobstoreStorage}.
 */
public interface BlobstoreCacheService {

    /**
     * Getting a part of the blob from the cache if available. This function starts at the given position and gives back
     * as many byes as there are available sequentially based on the available segments but not more than the maxLength
     * parameter.
     * 
     * @param blobId
     *            The id of the blob.
     * @param startPosition
     *            The starting position where the reading should start from inside the blob.
     * @param maxLength
     *            The maximum length of data that should be read from the cache.
     * @return A list of blobParts that were available in the cache ordered by the starting position of the parts.
     */
    List<CachedBlobPart> getBlobParts(long blobId, long startPosition, long maxLength);

    /**
     * Removing all parts from the cache by a blobId. This normally happens when a blob is deleted or updated.
     * 
     * @param blobId
     *            The id of the blob.
     */
    void removePartsByBlobId(long blobId);

    /**
     * Storing a new fragment in the cache.
     * 
     * @param blobId
     *            The id of the blob that the fragment belongs to.
     * @param startPosition
     *            The start position where the fragment begins inside the blob.
     * @param blobPart
     *            The data of the blobPart.
     * @throws org.everit.util.core.validation.ValidationException
     *             if any of the parameters is null.
     */
    void storeBlobPart(long blobId, long startPosition, byte[] blobPart);

}
