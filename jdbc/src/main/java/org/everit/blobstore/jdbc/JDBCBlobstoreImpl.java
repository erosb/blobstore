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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;
import org.everit.blobstore.internal.impl.AbstractBlobReaderInputStream;
import org.everit.blobstore.internal.impl.AbstractBlobstoreImpl;
import org.everit.blobstore.internal.impl.StreamUtil;

/**
 * JDBC specific implementation of {@link org.everit.blobstore.api.Blobstore}. This implementation handles a cache based
 * on {@link org.everit.blobstore.api.BlobstoreCacheService} if available.
 */
public class JDBCBlobstoreImpl extends AbstractBlobstoreImpl {

    /**
     * Name of the table the blob is stored.
     */
    public static final String TABLE_NAME = "BS_BLOB";

    /**
     * Name of the column the blob is stored.
     */
    public static final String COLUMN_BLOB_DATA = "BLOB_DATA";
    /**
     * Name of the column the blob is stored.
     */
    public static final String COLUMN_BLOB_ID = "BLOB_ID";

    /**
     * Name of the blob description column.
     */
    public static final String COLUMN_DESCRIPTION = "BLOB_DESCRIPTION";
    /**
     * Default cache size.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;

    /**
     * SQL query to get the description of a blob.
     */
    public static final String SQL_QUERY_DESCRIPTION = "select " + COLUMN_DESCRIPTION + " from " + TABLE_NAME
            + " where " + COLUMN_BLOB_ID + "= ?";

    /**
     * SQL statement to insert a new blob into the blob table.
     */
    public static final String SQL_INSERT_BLOB = "INSERT INTO " + TABLE_NAME + " ("
            + COLUMN_BLOB_DATA + ", " + COLUMN_DESCRIPTION + ") VALUES (?, ?)";

    /**
     * Simple constructor that does nothing else but calls the constructor of the superclass.
     *
     * @param dataSource
     *            See the constructor of the superclass.
     * @param xaDataSource
     *            See the constructor of the superclass.
     * @param blobstoreCacheService
     *            See the constructor of the superclass.
     */
    public JDBCBlobstoreImpl(final DataSource dataSource, final BlobstoreCacheService blobstoreCacheService) {
        super(dataSource, blobstoreCacheService);
    }

    /**
     * Cleanup method for closing the connection and the large object handler.
     *
     * @param connection
     *            The connection to be closed.
     * @param binaryStream
     *            The {@link OutputStream} to be released.
     */
    private void cleanup(final Connection connection, final OutputStream binaryStream) {
        try {
            if (binaryStream != null) {
                binaryStream.close();
            }
        } catch (IOException e) {
            throw new BlobstoreException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new BlobstoreException(e);
                }
            }
        }
    }

    @Override
    protected AbstractBlobReaderInputStream createBlobInputStream(final long blobId,
            final long startPosition) throws SQLException {
        return new JDBCBlobReaderInputStream(getDataSource(), blobId, startPosition);
    }

    @Override
    public void deleteBlob(final long blobId) {
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement preparedStatement = connection
                    .prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_BLOB_ID + " = ?");
            preparedStatement.setLong(1, blobId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {
            cleanup(connection, null);
        }
    }

    @Override
    public String getDescriptionByBlobId(final long blobId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(SQL_QUERY_DESCRIPTION);
            preparedStatement.setLong(1, blobId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                throw new BlobstoreException("blob [" + blobId + "] does not exist");
            }
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {

            try {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                throw new BlobstoreException(e);
            }
        }
    }

    @Override
    public long storeBlobNoParamCheck(final InputStream blobStream, final Long length, final String description) {
        Connection connection = null;
        OutputStream binaryStream = null;
        PreparedStatement preparedStatement = null;
        ResultSet keyset = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(SQL_INSERT_BLOB, Statement.RETURN_GENERATED_KEYS);
            if (length == null) {
                preparedStatement.setBinaryStream(1, blobStream);
            } else {
                if (length.longValue() > StreamUtil.countBytesProcessed(blobStream, length.longValue(),
                        DEFAULT_BUFFER_SIZE)) {
                    throw new BlobstoreException("too short stream");
                }
                preparedStatement.setBinaryStream(1, blobStream, length);
            }
            preparedStatement.setString(2, description);
            preparedStatement.executeUpdate();
            keyset = preparedStatement.getGeneratedKeys();
            int lastKey = 1;
            while (keyset.next()) {
                lastKey = keyset.getInt(1);
            }
            return Long.valueOf(lastKey);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                try {
                    if (keyset != null) {
                        keyset.close();
                    }
                } finally {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                cleanup(connection, binaryStream);
            }
        }
    }
}
