/**
 * This file is part of Everit - Blobstore.
 *
 * Everit - Blobstore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.blobstore.api;

/**
 * Exception class for Blobstore.
 */
public class BlobstoreException extends RuntimeException {

    /**
     * Default serial UID.
     */
    private static final long serialVersionUID = 1L;

    public BlobstoreException(final String message) {
        super(message);
    }

    public BlobstoreException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BlobstoreException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BlobstoreException(final Throwable cause) {
        super(cause);
    }

}
