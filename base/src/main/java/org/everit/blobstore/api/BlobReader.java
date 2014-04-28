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

/**
 * Interface for a BlobReader.
 */
public interface BlobReader {
    /**
     * A callback interface which can be used to read a stream.
     * 
     * @param blobStream
     *            The input stream to be read.
     * @throws BlobstoreException
     *             If the reading of the blob cannot be executed.
     */
    void readBlob(InputStream blobStream);

}
