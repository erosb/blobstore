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

/**
 * Type of current blobstore implementations.
 */
public enum BlobstoreType {

    /**
     * The type will be discovered automatically from the {@link java.sql.Connection#getClientInfo()} result.
     */
    AUTO,

    /**
     * The PostgreSQL {@link org.postgresql.largeobject.LargeObject} API will be used.
     */
    POSTGRES,

    /**
     * Standard JDBC {@link java.sql.Blob} will be used.
     */
    JDBC
}
