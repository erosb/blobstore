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

import org.everit.blobstore.internal.api.ConnectionProvider;
import org.everit.blobstore.internal.impl.AbstractBlobReaderInputStream;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

/**
 * The {@link AbstractCachedInputStream} implementation for PostgreSQL database.
 */
public class PostgresBlobReaderInputStream extends AbstractBlobReaderInputStream {
    /**
     * Logger for this class.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(PostgresBlobReaderInputStream.class);

    /**
     * The connection provider to the database.
     */
    private ConnectionProvider connectionProvider;

    /**
     * Currently opened connection to the database.
     */
    private Connection connection;

    /**
     * The {@link LargeObject} to be read.
     */
    private LargeObject obj;

    /**
     * The lazily initialized total size of the blob.
     */
    private Integer totalSize = null;

    /**
     * Constructor for the {@link AbstractCachedInputStream} implementation for PostgreSQL database.
     * 
     * @param connectionProvider
     *            The connection provider to the database.
     * @param blobId
     *            The id of the {@link LargeObject} to be read.
     * @param startPosition
     *            The offset at which the blob reading starts.
     * @throws SQLException
     *             If the db cannot be accessed.
     */
    public PostgresBlobReaderInputStream(final ConnectionProvider connectionProvider, final Long blobId,
            final Long startPosition)
            throws SQLException {
        super(blobId, startPosition);

        this.connectionProvider = connectionProvider;
        try {
            totalSize = getObj().size();
        } finally {
            cleanUp(true, true);
        }
    }

    /**
     * Cleaning up the connection and large object if created.
     * 
     * @param cleanObj
     *            whether to clean the obj or not.
     * @param cleanConnection
     *            whether to clean the connection or not.
     * @throws SQLException
     *             if a database error occurs.
     */
    protected void cleanUp(final boolean cleanObj, final boolean cleanConnection) throws SQLException {
        try {
            if (cleanObj && (obj != null)) {
                obj.close();
                obj = null;
            }
        } finally {
            if (cleanConnection && (connection != null)) {
                connection.close();
                connection = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            cleanUp(true, true);
        } catch (SQLException e) {
            LOGGER.error("Error during cleaning up large object and connection from postgres", e);
            throw new IOException(e);
        }
    }

    /**
     * Lazily initiating an exception.
     * 
     * @return The created database connection.
     * @throws SQLException
     *             if a database error occurs.
     */
    protected Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = connectionProvider.getConnection();
        }
        return connection;
    }

    /**
     * Lazily getting a large object based on the {@link #getLargeObjectId()}.
     * 
     * @return The large object.
     * @throws SQLException
     *             if a database error occurs.
     */
    protected LargeObject getObj() throws SQLException {
        if (obj == null) {
            Connection conn = getConnection();
            LargeObjectManager largeObjectAPI = PostgreSQLUtil.getPGConnection(conn).getLargeObjectAPI();
            obj = largeObjectAPI.open(PostgresBlobstoreServiceImpl.getLargeObjectId(getBlobId(), conn),
                    LargeObjectManager.READ);
        }
        return obj;
    }

    @Override
    public long getTotalSize() throws SQLException {
        boolean cleanObj = (obj == null);
        boolean cleanConnection = (connection == null);
        if (totalSize == null) {
            totalSize = getObj().size();
            cleanUp(cleanObj, cleanConnection);
        }
        return totalSize;
    }

    @Override
    public byte[] readDataFromDb(final long startPosition, final int amount) throws SQLException {
        byte[] output = new byte[amount];

        LargeObject lObj = getObj();
        if (lObj.tell() != startPosition) {
            lObj.seek((int) startPosition);
        }
        lObj.read(output, 0, amount);
        return output;
    }
}
