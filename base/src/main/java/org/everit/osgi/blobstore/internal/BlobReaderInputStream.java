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
package org.everit.osgi.blobstore.internal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.everit.osgi.blobstore.api.storage.BlobstoreStorageReader;
import org.everit.osgi.blobstore.internal.cache.BlobstoreCacheService;
import org.everit.osgi.blobstore.internal.cache.CachedBlobPart;

/**
 * Abstract class that is the skeleton of the database-specific input streams that are used to retrieve blobs from the
 * database.
 */
public class BlobReaderInputStream extends InputStream {
    /**
     * Integer that contains only bits with value one.
     */
    private static final int ALL_BIT_ONE_INT = 0xFF;
    /**
     * The current position of the reader.
     */
    private long currentPosition = 0;

    private final BlobstoreStorageReader storageReader;

    /**
     * The id of the blob.
     */
    private final Long blobId;

    /**
     * The cache service implementation.
     */
    private BlobstoreCacheService cacheService;

    /**
     * The required constructor for the abstract class.
     *
     * @param blobId
     *            The id of the blob.
     * @param startPosition
     *            the position where this stream should start.
     */
    public BlobReaderInputStream(final Long blobId, final long startPosition, final BlobstoreStorageReader storageReader) {
        this.blobId = blobId;
        currentPosition = startPosition;
        this.storageReader = storageReader;
    }

    @Override
    public int available() throws IOException {
        try {
            return (int) (storageReader.getTotalSize() - currentPosition);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        storageReader.close();
    }

    public long getTotalSize() throws SQLException {
        return storageReader.getTotalSize();
    }

    @Override
    public final int read() throws IOException {
        byte[] b = new byte[1];
        int read = read(b);
        if (read == -1) {
            throw new IOException("End of stream reached.");
        }
        return b[0] & ALL_BIT_ONE_INT;
    }

    @Override
    public final int read(final byte[] buffer, final int off, final int len) throws IOException {

        if (len > (buffer.length - off)) {
            throw new IOException(
                    "Byte array length without the offset is smaller"
                            + " than the length that should be read from the stream");
        }
        int available = available();
        if (available == 0) {
            return -1;
        }

        int bytesToRead = len;
        if (available < len) {
            bytesToRead = available;
        }

        List<CachedBlobPart> cachedBlobParts = null;
        if (cacheService != null) {
            cachedBlobParts = cacheService.getBlobParts(blobId, currentPosition, bytesToRead);
        } else {
            cachedBlobParts = Collections.emptyList();
        }
        CachedBlobPart[] cachedBlobPartsArray = cachedBlobParts.toArray(new CachedBlobPart[0]);

        int currentBlobPartIndex = 0;

        int readBytes = 0;
        while (readBytes < bytesToRead) {

            if ((currentBlobPartIndex < cachedBlobPartsArray.length)
                    && ((currentPosition + readBytes)
                            == cachedBlobPartsArray[currentBlobPartIndex].getStartPosition())) {
                CachedBlobPart cachedBlobPart = cachedBlobPartsArray[currentBlobPartIndex];
                byte[] blobPartData = cachedBlobPart.getBlobPartData();
                System.arraycopy(cachedBlobPart.getBlobPartData(), 0, buffer, off + readBytes, blobPartData.length);
                readBytes = readBytes + blobPartData.length;
                currentBlobPartIndex++;
            } else {
                int amountToRead = bytesToRead - readBytes;
                if (currentBlobPartIndex < cachedBlobPartsArray.length) {
                    amountToRead = (int) (cachedBlobPartsArray[currentBlobPartIndex].getStartPosition() - readBytes
                            - currentPosition);
                }
                long dbReadStartPosition = currentPosition + readBytes;
                byte[] bytesFromDB;
                try {
                    bytesFromDB = storageReader.readDataFromStorage(dbReadStartPosition, amountToRead);
                } catch (SQLException e) {
                    throw new IOException(e);
                }
                System.arraycopy(bytesFromDB, 0, buffer, off + readBytes, bytesFromDB.length);
                readBytes = readBytes + amountToRead;
            }
        }
        currentPosition = currentPosition + readBytes;
        return readBytes;
    }

    public void setCacheService(final BlobstoreCacheService cacheService) {
        this.cacheService = cacheService;
    }

}
