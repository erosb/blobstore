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
package org.everit.blobstore.internal.api;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Providing database connections to the low level classes.
 */
public interface ConnectionProvider {

    /**
     * Getting a database connection.
     * 
     * @return The database connection.
     * @throws SQLException
     *             if an error occures during connecting to the database or the
     */
    Connection getConnection() throws SQLException;
}
