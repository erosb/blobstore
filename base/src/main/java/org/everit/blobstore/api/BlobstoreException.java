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
package org.everit.blobstore.api;

import org.everit.serviceutil.api.exception.AbstractServiceException;
import org.everit.serviceutil.api.exception.Param;

/**
 * Exception class for Blobstore.
 */
public class BlobstoreException extends AbstractServiceException {

    /**
     * Default serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with {@link ErrorCode}.
     * 
     * @param errorCode
     *            The {@link ErrorCode}.
     */
    public BlobstoreException(final ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructor with {@link ErrorCode} and object array as parameters.
     * 
     * @param errorCode
     *            The {@link ErrorCode}.
     * @param params
     *            The parameters.
     */
    public BlobstoreException(final ErrorCode errorCode, final Param... params) {
        super(errorCode, params);
    }

    /**
     * Constructor with {@link ErrorCode} and {@link Throwable} as cause.
     * 
     * @param errorCode
     *            The {@link ErrorCode}.
     * @param cause
     *            The {@link Throwable} as cause.
     */
    public BlobstoreException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * Constructor with {@link ErrorCode}, {@link Throwable} as cause and object array as parameters.
     * 
     * @param errorCode
     *            The {@link ErrorCode}.
     * @param cause
     *            The {@link Throwable} as cause.
     * @param params
     *            The parameters.
     */
    public BlobstoreException(final ErrorCode errorCode, final Throwable cause, final Param... params) {
        super(errorCode, cause, params);
    }
}
