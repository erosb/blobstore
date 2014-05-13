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
package org.everit.blobstore.internal.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Reference;
import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.Blobstore;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;
import org.osgi.service.log.LogService;

/**
 * Abstract class for common features needed by the {@link Blobstore}s.
 */
public abstract class AbstractBlobstoreImpl implements Blobstore {

    /**
     * The label of the "blobReader" parameter.
     */
    private static final String BLOB_READER_LABEL = "blobReader";

    /**
     * The label of the ""blobStrean" parameter.
     */
    private static final String BLOB_STREAM_LABEL = "blobStream";

    /**
     * Logger for this class.
     */
    @Reference
    private LogService LOGGER;

    /**
     * Default buffer size.
     */
    protected static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Connection provider for this blobstore service. By default a {@link DataSourceConnectionProvider} is instantiated
     * that may be overridden in the constructor of a subclass.
     */
    private final DataSource dataSource;

    /**
     * BlobstoreCacheService.
     */
    protected BlobstoreCacheService blobstoreCacheService = null;

    /**
     * Constructor.
     *
     * @param dataSource
     *            Simple dataSource. If null xaDataSource has to be provided.
     * @param xaDataSource
     *            XA enabled datasource. If null simple dataSource has to be provided. During creating a connection XA
     *            enabled datasource is checked first.
     * @param blobstoreCacheService
     *            The cache that is used during reading blobs.
     */
    public AbstractBlobstoreImpl(final DataSource dataSource,
            final BlobstoreCacheService blobstoreCacheService) {
        this.dataSource = dataSource;
        this.blobstoreCacheService = blobstoreCacheService;
    }

    /**
     * Creating a database dependent blob reading inputstream instance.
     *
     * @param dataSource
     *            Supports getting connections to the database.
     * @param blobId
     *            The id of the blob.
     * @param startPosition
     *            The starting position where the blob should start.
     * @return The input stream instance.
     * @throws SQLException
     *             If a database error occurs.
     */
    protected abstract AbstractBlobReaderInputStream createBlobInputStream(
            long blobId, long startPosition) throws SQLException;

    @Override
    public long getBlobSizeByBlobId(final long blobId) {
        try (AbstractBlobReaderInputStream inputStream = createBlobInputStream(blobId, 0)) {
            return inputStream.getTotalSize();
        } catch (SQLException | IOException e) {
            throw new BlobstoreException(e);
        }
    }

    public BlobstoreCacheService getBlobstoreCacheService() {
        return blobstoreCacheService;
    }

    protected Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void readBlob(final long blobId, final long startPosition, final BlobReader blobReader) {
        AbstractBlobReaderInputStream inputStream = null;
        Objects.requireNonNull(blobReader, "blobReader cannot be null");
        try {
            inputStream = createBlobInputStream(blobId, startPosition);
            long totalSize = inputStream.getTotalSize();
            if (totalSize < startPosition) {
                throw new BlobstoreException("startPosition(=" + startPosition
                        + ") cannot be higher than totalSize(=" + totalSize + ") of blob #" + blobId);
            }
            inputStream.setCacheService(getBlobstoreCacheService());
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
        if ((description != null) && (description.length() > Blobstore.BLOB_DESCRIPTION_MAX_LENGTH)) {
            throw new BlobstoreException("description length must be at most " +
                    Blobstore.BLOB_DESCRIPTION_MAX_LENGTH + ", actual length: " + description.length());
        }
        Objects.requireNonNull(blobStream, "blobStream cannot be null");
        return storeBlobNoParamCheck(blobStream, length, description);
    }

    /**
     * Subclasses may should override this method without the necessity of checking null blobStream or too long
     * description.
     *
     * @param blobStream
     *            The stream where the blob data comes from.
     * @param length
     *            The length that should be read from the blobstream.
     * @param description
     *            The description of the blob.
     * @return the id of the blob.
     */
    protected abstract long storeBlobNoParamCheck(final InputStream blobStream, final Long length,
            final String description);
}
