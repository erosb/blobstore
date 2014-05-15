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

import java.io.Serializable;
import java.util.LinkedList;

public class Fragment implements Serializable {

    private static final long serialVersionUID = 4411177167585354588L;

    private long blobId;

    private long startPosition;

    private LinkedList<FragmentPart> fragmentParts = new LinkedList<FragmentPart>();

    public Fragment() {

    }

    public Fragment(final long blobId, final long startPosition) {
        super();
        this.blobId = blobId;
        this.startPosition = startPosition;
    }

    public CacheKey createCacheKey() {
        return new CacheKey(blobId, startPosition);
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
        Fragment other = (Fragment) obj;
        if (blobId != other.blobId) {
            return false;
        }
        if (fragmentParts == null) {
            if (other.fragmentParts != null) {
                return false;
            }
        } else if (!fragmentParts.equals(other.fragmentParts)) {
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

    /**
     * Please only use these fragment parts for traversing, do not add items to the list itself. Use {@link
     * insertFragmentPart(FragmentPart) } instead, since it will maintain list ordering.
     * 
     * @return
     */
    public LinkedList<FragmentPart> getFragmentParts() {
        return fragmentParts;
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
                + ((fragmentParts == null) ? 0 : fragmentParts.hashCode());
        result = prime * result
                + (int) (startPosition ^ (startPosition >>> 32));
        return result;
    }

    public void insertFragmentPart(final FragmentPart newPart) {
        long newPartPos = newPart.getStartPositionInFragment();
        int insertIdx = 0;
        for (FragmentPart part : fragmentParts) {
            if (part.getStartPositionInFragment() > newPartPos) {
                break;
            }
            ++insertIdx;
        }
        fragmentParts.add(insertIdx, newPart);
    }

    public void setBlobId(final long blobId) {
        this.blobId = blobId;
    }

    public void setFragmentParts(final LinkedList<FragmentPart> fragmentParts) {
        this.fragmentParts = fragmentParts;
    }

    public void setStartPosition(final long startPosition) {
        this.startPosition = startPosition;
    }

}
