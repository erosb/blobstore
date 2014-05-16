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
import java.util.Arrays;

public class FragmentPart implements Serializable {

    private static final long serialVersionUID = -6366245907682133551L;

    private long startPositionInFragment;

    private byte[] data;

    public FragmentPart() {

    }

    public FragmentPart(final long startPositionInFragment, final byte[] data) {
        super();
        this.startPositionInFragment = startPositionInFragment;
        this.data = data;
    }

    public Range<Long> asRange() {
        return new Range<Long>(startPositionInFragment, startPositionInFragment
                + data.length, true, false);
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
        FragmentPart other = (FragmentPart) obj;
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        if (startPositionInFragment != other.startPositionInFragment) {
            return false;
        }
        return true;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * @return startPositionInFragment + data.length
     */
    public int getEndPositionInFragment() {
        return (int) (startPositionInFragment + data.length);
    }

    public long getStartPositionInFragment() {
        return startPositionInFragment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        result = prime
                * result
                + (int) (startPositionInFragment ^ (startPositionInFragment >>> 32));
        return result;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    public void setStartPositionInFragment(final long startPositionInFragment) {
        this.startPositionInFragment = startPositionInFragment;
    }

}
