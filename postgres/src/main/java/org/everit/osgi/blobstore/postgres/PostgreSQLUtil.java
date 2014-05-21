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
package org.everit.osgi.blobstore.postgres;


import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.PGConnection;
/**
 * Util functions for the PostgreSQL database.
 */
public final class PostgreSQLUtil {

    /**
     * Getting a PGConnection instance out of the provided connection. If the connection is a wrapper the function tries
     * to unwrap the PGConnection object.
     * 
     * @param connection
     *            The original connection object that may be a wrapper.
     * @return The PGConnection instance.
     * @throws SQLException
     *             if the connection is neither an instance of PGConnection nor a wrapper of it or although the
     *             connection is a wrapper it does not support the unwrap functionality.
     */
    public static PGConnection getPGConnection(final Connection connection) throws SQLException {
        if (connection instanceof PGConnection) {
            return (PGConnection) connection;
        } else {
            if (connection.isWrapperFor(PGConnection.class)) {
                return connection.unwrap(PGConnection.class);
            } else {
                throw new SQLException(
                        "Connection is not instance of PGConnection and is not a wrapper of a PGConnection: "
                                + connection.getClass().getName());
            }
        }

    }

    /**
     * Private constructor for util class.
     */
    private PostgreSQLUtil() {
    }
}
