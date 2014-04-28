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

/**
 * Blob store error codes.
 */
public enum ErrorCode {
    /**
     * Blob reading exception.
     */
    BLOB_READING_EXCEPTION,

    /**
     * Sql exception.
     */
    SQL_EXCEPTION,

    /**
     * I/O exception.
     */
    IO_EXCEPTION,

    /**
     * Error during finding out the type of the blobstore.
     */
    BLOBSTORE_TYPE_DERIVATION,

    /**
     * Incoming stream had less bytes than the length that was specified to read out into blob.
     */
    SHORT_STREAM,

    /**
     * The value of start position specified to read the blob is higher than the total size of the blob. Parameters:
     * blobId, totalSize, startPosition.
     */
    TOO_HIGH_START_POSITION,

    /**
     * Blob with the defined id does not exist. Params: blobId
     */
    BLOB_DOES_NOT_EXIST,

    /**
     * The description of the blob is too long.
     */
    TOO_LONG_DESCRIPTION;

    @Override
    public String toString() {
        return ErrorCode.class.getName() + "." + super.toString();
    }
}
