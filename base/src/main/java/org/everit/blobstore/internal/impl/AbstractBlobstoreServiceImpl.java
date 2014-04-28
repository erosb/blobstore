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
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.everit.blobstore.api.BlobReader;
import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.BlobstoreService;
import org.everit.blobstore.api.ErrorCode;
import org.everit.blobstore.internal.api.ConnectionProvider;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;
import org.everit.serviceutil.api.exception.Param;
import org.everit.util.core.validation.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for common features needed by the {@link BlobstoreService}s.
 */
public abstract class AbstractBlobstoreServiceImpl implements BlobstoreService {

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
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractBlobstoreServiceImpl.class);

    /**
     * Default buffer size.
     */
    protected static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Connection provider for this blobstore service. By default a {@link DataSourceConnectionProvider} is instantiated
     * that may be overridden in the constructor of a subclass.
     */
    private ConnectionProvider connectionProvider;

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
    public AbstractBlobstoreServiceImpl(final DataSource dataSource, final XADataSource xaDataSource,
            final BlobstoreCacheService blobstoreCacheService) {
        connectionProvider = new DataSourceConnectionProvider(dataSource, xaDataSource);
        this.blobstoreCacheService = blobstoreCacheService;
    }

    /**
     * Creating a database dependent blob reading inputstream instance.
     * 
     * @param pConnectionProvider
     *            Supports getting connections to the database.
     * @param blobId
     *            The id of the blob.
     * @param startPosition
     *            The starting position where the blob should start.
     * @return The input stream instance.
     * @throws SQLException
     *             If a database error occurs.
     */
    protected abstract AbstractBlobReaderInputStream createBlobInputStream(ConnectionProvider pConnectionProvider,
            long blobId, long startPosition) throws SQLException;

    @Override
    public long getBlobSizeByBlobId(final long blobId) {
        AbstractBlobReaderInputStream inputStream = null;
        try {
            inputStream = createBlobInputStream(connectionProvider, blobId, 0);
            return inputStream.getTotalSize();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BlobstoreException(ErrorCode.SQL_EXCEPTION, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new BlobstoreException(ErrorCode.IO_EXCEPTION, e);
            }
        }
    }

    public BlobstoreCacheService getBlobstoreCacheService() {
        return blobstoreCacheService;
    }

    protected ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public void readBlob(final long blobId, final long startPosition, final BlobReader blobReader) {
        AbstractBlobReaderInputStream inputStream = null;
        ValidationUtil.isNotNull(blobReader, BLOB_READER_LABEL);
        try {
            inputStream = createBlobInputStream(connectionProvider, blobId, startPosition);
            long totalSize = inputStream.getTotalSize();
            if (totalSize < startPosition) {
                Param blobIdParam = new Param(blobId);
                Param totalSizeParam = new Param(totalSize);
                Param startPositionParam = new Param(startPosition);
                throw new BlobstoreException(ErrorCode.TOO_HIGH_START_POSITION,
                        blobIdParam, totalSizeParam, startPositionParam);
            }
            inputStream.setCacheService(getBlobstoreCacheService());
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            blobReader.readBlob(bis);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BlobstoreException(ErrorCode.SQL_EXCEPTION, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new BlobstoreException(ErrorCode.IO_EXCEPTION, e);
            }
        }
    }

    @Override
    public long storeBlob(final InputStream blobStream, final Long length, final String description) {
        if ((description != null) && (description.length() > BlobstoreService.BLOB_DESCRIPTION_MAX_LENGTH)) {
            throw new BlobstoreException(ErrorCode.TOO_LONG_DESCRIPTION);
        }
        ValidationUtil.isNotNull(blobStream, BLOB_STREAM_LABEL);
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
