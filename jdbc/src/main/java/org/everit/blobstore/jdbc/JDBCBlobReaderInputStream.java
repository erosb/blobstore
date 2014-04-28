/**
 * This file is part of Everit - Blobstore JDBC.
 *
 * Everit - Blobstore JDBC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore JDBC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore JDBC.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.api.ErrorCode;
import org.everit.blobstore.internal.api.ConnectionProvider;
import org.everit.blobstore.internal.impl.AbstractBlobReaderInputStream;
import org.everit.blobstore.internal.impl.StreamUtil;
import org.everit.serviceutil.api.exception.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractCachedInputStream} implementation for JDBC database.
 */
public class JDBCBlobReaderInputStream extends AbstractBlobReaderInputStream {
    /**
     * Logger for this class.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(JDBCBlobReaderInputStream.class);

    /**
     * The statement which we can query a blob out from the database.
     */
    public static final String BLOB_SELECT_STATEMENT = "SELECT " + JDBCBlobstoreServiceImpl.COLUMN_BLOB_DATA
            + " FROM "
            + JDBCBlobstoreServiceImpl.TABLE_NAME + " WHERE " + JDBCBlobstoreServiceImpl.COLUMN_BLOB_ID + "= ?";
    /**
     * The connection to the database.
     */
    private Connection connection;

    /**
     * The connection provider to get database connections.
     */
    private ConnectionProvider connectionProvider = null;

    /**
     * The {@link InputStream} for the {@link Blob}.
     */
    private InputStream binaryStream = null;

    /**
     * The {@link Blob} to be read.
     */
    private Blob blob;

    /**
     * The current position of the binaryStream.
     */
    private long currentDbStreamPosition = 0;

    /**
     * The size of the blob.
     */
    private Long totalSize = null;

    /**
     * Constructor for the {@link AbstractCachedInputStream} implementation for JDBC database.
     * 
     * @param connectionProvider
     *            The connection provider that allows us getting database connections.
     * @param blobId
     *            The id of the {@link Blob} to be read.
     * @param startPosition
     *            The offset at which the blob reading starts.
     * @throws SQLException
     *             If the db cannot be accessed.
     */
    public JDBCBlobReaderInputStream(final ConnectionProvider connectionProvider, final Long blobId,
            final Long startPosition)
            throws SQLException {
        super(blobId, startPosition);
        this.connectionProvider = connectionProvider;
        try {
            Blob lBlob = getBlob();
            if (lBlob == null) {
                throw new BlobstoreException(ErrorCode.BLOB_DOES_NOT_EXIST, new Param(blobId));
            }
            totalSize = lBlob.length();
        } finally {
            try {
                close();
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (binaryStream != null) {
                binaryStream.close();
                binaryStream = null;
            }
        } finally {
            try {
                try {
                    if (blob != null) {
                        blob.free();
                        blob = null;
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                        connection = null;
                    }
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Getting a binary stream lazily.
     * 
     * @return The binary stream of the blob.
     * @throws SQLException
     *             if a database error occurs.
     */
    public InputStream getBinaryStream() throws SQLException {
        if (binaryStream == null) {
            binaryStream = getBlob().getBinaryStream();
        }
        return binaryStream;
    }

    /**
     * Getting the blob lazily.
     * 
     * @return The blob instance.
     * @throws SQLException
     *             If a database error occures.
     */
    public Blob getBlob() throws SQLException {
        if (blob == null) {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = getConnection().prepareStatement(BLOB_SELECT_STATEMENT);
                long blobId = getBlobId();
                preparedStatement.setLong(1, blobId);
                ResultSet rs = null;
                try {
                    rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        blob = rs.getBlob(JDBCBlobstoreServiceImpl.COLUMN_BLOB_DATA);
                    } else {
                        throw new BlobstoreException(ErrorCode.BLOB_DOES_NOT_EXIST, new Param(blobId));
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
        }
        return blob;
    }

    /**
     * Getting a connection lazily.
     * 
     * @return The database connection.
     * @throws SQLException
     *             if a database error occurs.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = connectionProvider.getConnection();
        }
        return connection;
    }

    @Override
    public long getTotalSize() throws SQLException {
        return totalSize;
    }

    @Override
    public byte[] readDataFromDb(final long startPosition, final int amount) throws SQLException {
        if (startPosition < currentDbStreamPosition) {
            throw new SQLException("Startposition [" + startPosition
                    + "] cannot be lower than the current position of the stream [" + currentDbStreamPosition + "]");
        }
        InputStream is = getBinaryStream();
        if (startPosition > currentDbStreamPosition) {
            try {
                StreamUtil.skip(is, startPosition - currentDbStreamPosition);
                currentDbStreamPosition = startPosition;
            } catch (IOException e) {
                throw new BlobstoreException(ErrorCode.SQL_EXCEPTION, e);
            }
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream(amount);

        try {
            long copiedBytes = StreamUtil.copyStream(is, bout, (long) amount, JDBCBlobstoreServiceImpl.IO_BUFFER_SIZE);
            currentDbStreamPosition = currentDbStreamPosition + copiedBytes;
            if (copiedBytes != amount) {
                Param blobIdParam = new Param(getBlobId());
                Param positionParam = new Param(Long.valueOf(currentDbStreamPosition - copiedBytes));
                Param amountParam = new Param(Integer.valueOf(amount));
                Param copiedBytesParam = new Param(Long.valueOf(copiedBytes));
                throw new BlobstoreException(ErrorCode.BLOB_READING_EXCEPTION,
                        blobIdParam, positionParam, amountParam, copiedBytesParam);
            }
            return bout.toByteArray();
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
