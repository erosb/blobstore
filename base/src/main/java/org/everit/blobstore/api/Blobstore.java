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
package org.everit.blobstore.api;

import java.io.InputStream;
import java.sql.SQLException;

public interface Blobstore {

    final int BLOB_DESCRIPTION_MAX_LENGTH = 255;

    void deleteBlob(long blobId);

    /**
     * Getting the size of a blob by it's id.
     *
     * @param blobId
     *            The id of the blob.
     * @return The size in bytes.
     * @throws BlobstoreException
     *             if no blob found for {@code blobId} or an {@link SQLException} is thrown
     */
    long getBlobSizeByBlobId(long blobId);

    String getDescriptionByBlobId(long blobId);

    /**
     * Reading the content of a blob from the given position.
     *
     * @param blobId
     *            The unique id of the blob.
     * @param startPosition
     *            The position where the blob reading will be started from.
     * @param blobReader
     *            The {@link BlobReader#readBlob(InputStream)} function will be called to let the programmer read the
     *            content of the blob. The function has one InputStream parameter that should not be closed as it is
     *            handled automatically. This encapsulation is necessary to be sure that the current transaction,
     *            connection and resultSet is opened until the end of reading of the inputStream.
     * @throws BlobstoreException
     *             if a blob cannot be read due to one of the reasons
     */
    void readBlob(long blobId, long startPosition, BlobReader blobReader);

    /**
     * Storing a blob with the data coming from the given inputStream.
     *
     * @param blobStream
     *            The stream where data will be read from when the blob is stored.
     * @param length
     *            The length of data that is read from the stream or if null the input stream will be read until the
     *            end.
     * @param description
     *            The description of the blob. Could be the identity of the system that created the blob concatenated
     *            with with some external identifier. If multiple systems connect to the same blobstore it is important
     *            to be able to do database cleanings. This field should help doing that if the caller fills it well.
     *            The description may be any string with the maximum length 255.
     * @return The unique id of this blob.
     * @throws BlobstoreException
     *             with the following reasons
     * @throws org.everit.util.core.validation.ValidationException
     *             if the blobStream parameter is null.
     */
    long storeBlob(InputStream blobStream, Long length, String description);

}
