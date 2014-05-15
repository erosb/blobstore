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
package org.everit.blobstore.api.storage;

import java.io.IOException;
import java.sql.SQLException;

public interface BlobstoreStorageReader {

    void close() throws IOException;

    /**
     * Get the total size of the blob.
     *
     * @return The total size of the blob.
     * @throws SQLException
     *             If the total size cannot be retrieved.
     */
    long getTotalSize() throws SQLException;

    /**
     * Read byte array of length amount from the database from the given offset from the database.
     *
     * @param startPosition
     *            The offset from which the data is to be read.
     * @param amount
     *            Length of the array to be read.
     * @return The read byte array.
     * @throws SQLException
     *             If a db error occurred.
     */
    byte[] readDataFromStorage(long startPosition, int amount) throws SQLException;

}
