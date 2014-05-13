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

import javax.sql.DataSource;

import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.internal.impl.AbstractBlobReaderInputStream;
import org.everit.blobstore.internal.impl.StreamUtil;
import org.osgi.service.log.LogService;

public class JDBCBlobReaderInputStream extends AbstractBlobReaderInputStream {
    /**
     * Logger for this class.
     */
    protected LogService LOGGER;

    /**
     * The statement which we can query a blob out from the database.
     */
    public static final String BLOB_SELECT_STATEMENT = "SELECT " + JDBCBlobstoreImpl.COLUMN_BLOB_DATA
            + " FROM "
            + JDBCBlobstoreImpl.TABLE_NAME + " WHERE " + JDBCBlobstoreImpl.COLUMN_BLOB_ID + "= ?";
    /**
     * The connection to the database.
     */
    private Connection connection;

    /**
     * The connection provider to get database connections.
     */
    private DataSource dataSource = null;

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
    public JDBCBlobReaderInputStream(final DataSource connectionProvider, final Long blobId,
            final Long startPosition)
                    throws SQLException {
        super(blobId, startPosition);
        this.dataSource = connectionProvider;
        try {
            Blob lBlob = getBlob();
            if (lBlob == null) {
                throw new BlobstoreException("blob [" + blobId + "] does not exist");
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
     *             If a database error occurs.
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
                        blob = rs.getBlob(JDBCBlobstoreImpl.COLUMN_BLOB_DATA);
                    } else {
                        throw new BlobstoreException("blob [" + blobId + "] does not exist");
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
                throw new BlobstoreException(e);
            }
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream(amount);

        try {
            long copiedBytes = StreamUtil.copyStream(is, bout, (long) amount, JDBCBlobstoreImpl.IO_BUFFER_SIZE);
            currentDbStreamPosition = currentDbStreamPosition + copiedBytes;
            if (copiedBytes != amount) {
                throw new BlobstoreException("failed to copy byte array");
            }
            return bout.toByteArray();
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
