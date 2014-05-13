/**
 * This file is part of Everit - Blobstore Postgres.
 *
 * Everit - Blobstore Postgres is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore Postgres is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore Postgres.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.postgres;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Reference;
import org.everit.blobstore.api.BlobstoreException;
import org.everit.blobstore.internal.cache.BlobstoreCacheService;
import org.everit.blobstore.internal.impl.AbstractBlobReaderInputStream;
import org.everit.blobstore.internal.impl.AbstractBlobstoreImpl;
import org.everit.blobstore.internal.impl.StreamUtil;
import org.osgi.service.log.LogService;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

/**
 * PostgreSQL specific implementation of {@link org.everit.blobstore.api.BlobstoreService}. This implementation handles
 * a cache based on {@link org.everit.blobstore.api.BlobstoreCacheService} if available.
 */
public class PostgresBlobstoreImpl extends AbstractBlobstoreImpl {

    /**
     * Name of the table the blob is stored.
     */
    public static final String TABLE_NAME = "bs_blob";

    /**
     * Name of the column that stores the large object id associated to this blob.
     */
    public static final String COLUMN_LARGE_OBJECT_ID = "large_object_id";
    /**
     * Name of the column the blob is stored.
     */
    public static final String COLUMN_BLOB_ID = "blob_id";

    /**
     * Name of the blob description column.
     */
    public static final String COLUMN_DESCRIPTION = "blob_description";

    /**
     * Name of the sequence where the blob ids will get value from.
     */
    public static final String SEQUENCE_NAME = "bs_seq";

    /**
     * Getting the large object id based on the blob id.
     */
    public static final String SQL_GET_LOID_BY_BLOBID = "select " + COLUMN_LARGE_OBJECT_ID
            + " from " + TABLE_NAME + " where " + COLUMN_BLOB_ID + " = ?";

    /**
     * Insert statement which a new blob can be inserted into the blob table.
     */
    public static final String SQL_INSERT_BLOB = "insert into " + TABLE_NAME + " (" + COLUMN_BLOB_ID + ", "
            + COLUMN_LARGE_OBJECT_ID + ", " + COLUMN_DESCRIPTION + ") values (nextval('" + SEQUENCE_NAME + "'), ?, ?"
            + ") returning " + COLUMN_BLOB_ID;

    /**
     * Query to get the description of the blob.
     */
    public static final String SQL_QUERY_DESCRIPTION = "select " + COLUMN_DESCRIPTION + " from " + TABLE_NAME
            + " where " + COLUMN_BLOB_ID + " = ?";

    /**
     * SQL command to delete blob record from database.
     */
    public static final String SQL_DELETE_BLOB_RECORD = "delete from " + TABLE_NAME + " where " + COLUMN_BLOB_ID
            + " = ?";

    /**
     * Getting the large object id based on the blob id.
     *
     * @param conn
     *            The database connection to use to get the large object id.
     * @param blobId
     *            The id of the blob.
     * @return The large object id.
     * @throws SQLException
     *             if a database error occurs.
     */
    static long getLargeObjectId(final long blobId, final Connection conn) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(SQL_GET_LOID_BY_BLOBID);
            preparedStatement.setLong(1, blobId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                throw new BlobstoreException("blob [" + blobId + "] does not exist");
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    /**
     * Logger of this class.
     */
    @Reference
    private LogService log;

    /**
     * Constructor that calls the superclass constructor.
     *
     * @param dataSource
     *            See the superclass constructor.
     * @param xaDataSource
     *            See the superclass constructor.
     * @param blobstoreCacheService
     *            See the superclass constructor.
     */
    public PostgresBlobstoreImpl(final DataSource dataSource, final BlobstoreCacheService blobstoreCacheService) {
        super(dataSource, blobstoreCacheService);
    }

    /**
     * Cleanup method for closing the connection and the large object handler.
     *
     * @param connection
     *            The connection to be closed.
     * @param obj
     *            The {@link LargeObject} to be released.
     */
    private void cleanup(final Connection connection, final LargeObject obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (SQLException e) {
            log.log(LogService.LOG_ERROR, e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.log(LogService.LOG_ERROR, e.getMessage());
                }
            }
        }
    }

    @Override
    protected AbstractBlobReaderInputStream createBlobInputStream(final long blobId,
            final long startPosition) throws SQLException {
        return new PostgresBlobReaderInputStream(getDataSource(), blobId, startPosition);
    }

    @Override
    public void deleteBlob(final long blobId) {
        Connection connection = null;
        LargeObject obj = null;
        try {
            connection = getConnection();
            PreparedStatement deleteStatement = null;
            long largeObjectId = PostgresBlobstoreImpl.getLargeObjectId(blobId, connection);
            try {
                deleteStatement = connection.prepareStatement(SQL_DELETE_BLOB_RECORD);
                deleteStatement.setLong(1, blobId);
                int deletedRecordNum = deleteStatement.executeUpdate();
                if (deletedRecordNum < 1) {
                    throw new BlobstoreException("blob [" + blobId + "] does not exist");
                }
            } finally {
                if (deleteStatement != null) {
                    deleteStatement.close();
                }
            }
            LargeObjectManager loManager = PostgreSQLUtil.getPGConnection(connection).getLargeObjectAPI();
            loManager.delete(largeObjectId);
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {
            cleanup(connection, obj);
        }
    }

    @Override
    public String getDescriptionByBlobId(final long blobId) {
        Connection connection = null;
        PreparedStatement query = null;
        try {
            connection = getConnection();
            query = connection.prepareStatement(SQL_QUERY_DESCRIPTION);
            query.setLong(1, blobId);
            ResultSet resultSet = query.executeQuery();
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
                    if (query != null) {
                        query.close();
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                log.log(LogService.LOG_ERROR, "Could not close database connection");
            }
        }
    }

    /**
     * Inserting a newly created blob into the blob table.
     *
     * @param oid
     *            The id of the postgres large object.
     * @param description
     *            The description of the blob.
     * @param connection
     *            The database connection which we can run the query on.
     * @return The generated id of the blob.
     */
    protected long insertBlobIntoTable(final long oid, final String description, final Connection connection) {
        PreparedStatement insertStatement = null;
        try {
            insertStatement = connection.prepareStatement(SQL_INSERT_BLOB);
            insertStatement.setLong(1, oid);
            insertStatement.setString(2, description);
            ResultSet resultSet = insertStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                throw new BlobstoreException("Blob id was not returned from database after running insert statement.");
            }
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } finally {
            if (insertStatement != null) {
                try {
                    insertStatement.close();
                } catch (SQLException e) {
                    log.log(LogService.LOG_ERROR, "Could not close prepared statement for database: " + e.getMessage());
                }
            }
        }

    }

    @Override
    protected long storeBlobNoParamCheck(final InputStream blobStream, final Long length, final String description) {
        Connection connection = null;
        LargeObject obj = null;
        try {
            connection = getConnection();

            LargeObjectManager loManager = PostgreSQLUtil.getPGConnection(connection).getLargeObjectAPI();
            Long oid = loManager.createLO();
            obj = loManager.open(oid, LargeObjectManager.WRITE);
            long bytesProcessed = StreamUtil.copyStream(blobStream, obj.getOutputStream(), length, DEFAULT_BUFFER_SIZE);

            if ((length != null) && (length.longValue() != bytesProcessed)) {
                throw new BlobstoreException("too short stream");
            }

            return insertBlobIntoTable(oid, description, connection);
        } catch (SQLException e) {
            throw new BlobstoreException(e);
        } catch (IOException e) {
            throw new BlobstoreException(e);
        } finally {
            cleanup(connection, obj);
        }
    }
}
