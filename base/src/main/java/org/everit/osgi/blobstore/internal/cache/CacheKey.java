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
package org.everit.osgi.blobstore.internal.cache;

import java.io.Serializable;

public class CacheKey implements Serializable {

    private static final long serialVersionUID = -1432400795747622080L;

    private long blobId;

    private long startPosition;

    public CacheKey() {

    }

    public CacheKey(final long blobId, final long startPosition) {
        this.blobId = blobId;
        this.startPosition = startPosition;
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
        CacheKey other = (CacheKey) obj;
        if (blobId != other.blobId) {
            return false;
        }
        if (startPosition != other.startPosition) {
            return false;
        }
        return true;
    }

    public long getBlobId() {
        return blobId;
    }

    public long getStartPosition() {
        return startPosition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (blobId ^ (blobId >>> 32));
        result = prime * result
                + (int) (startPosition ^ (startPosition >>> 32));
        return result;
    }

    public void setBlobId(final long blobId) {
        this.blobId = blobId;
    }

    public void setStartPosition(final long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CacheKey{");
        sb.append("blobId=").append(blobId).append(";")
                .append("startPosition=").append(startPosition).append("}");
        return sb.toString();
    }

}
