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
package org.everit.blobstore.internal.cache;

import java.util.Arrays;

/**
 * Part of the blob that could have been read from the cache.
 */
public class CachedBlobPart {

    /**
     * Id of the blob.
     */
    private Long blobId;

    /**
     * The starting position where this part starts in the blob.
     */
    private Long startPosition;

    /**
     * The data of the blob part.
     */
    private byte[] blobPartData;

    public CachedBlobPart(final Long blobId, final Long startPosition, final byte[] blobPartData) {
        super();
        this.blobId = blobId;
        this.startPosition = startPosition;
        this.blobPartData = blobPartData;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CachedBlobPart other = (CachedBlobPart) obj;
        if (blobId == null) {
            if (other.blobId != null) {
                return false;
            }
        } else if (!blobId.equals(other.blobId)) {
            return false;
        }
        if (!Arrays.equals(blobPartData, other.blobPartData)) {
            return false;
        }
        if (startPosition == null) {
            if (other.startPosition != null) {
                return false;
            }
        } else if (!startPosition.equals(other.startPosition)) {
            return false;
        }
        return true;
    }

    public Long getBlobId() {
        return blobId;
    }

    public byte[] getBlobPartData() {
        return blobPartData;
    }

    public Long getStartPosition() {
        return startPosition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blobId == null) ? 0 : blobId.hashCode());
        result = prime * result + Arrays.hashCode(blobPartData);
        result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
        return result;
    }

    public void setBlobId(final Long blobId) {
        this.blobId = blobId;
    }

    public void setBlobPartData(final byte[] blobPartData) {
        this.blobPartData = blobPartData;
    }

    public void setStartPosition(final Long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public String toString() {
        return "CachedBlobPart [blobId=" + blobId + ", startPosition=" + startPosition + ", blobPartData="
                + blobPartData.length + "]";
    }

}
