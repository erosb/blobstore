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
package org.everit.blobstore.internal.cache;

class FragmentByteArray {

	private final byte[] origArray;

	private final int startPosition;

	private final int length;

	private final int offset;

	FragmentByteArray(final byte[] origArray, final int startPosition,
			final int length, final int offset) {
		super();
		if (startPosition + length > origArray.length) {
			throw new IllegalArgumentException("failed to create fragment [ "
					+ startPosition + " , " + length + " ]");
		}
		this.origArray = origArray;
		this.startPosition = startPosition;
		this.length = length;
		this.offset = offset;
	}

	byte[] asRawArray() {
		if (startPosition == 0 && origArray.length == length) {
			return origArray;
		}
		byte[] rval = new byte[length];
		System.arraycopy(origArray, startPosition, rval, 0, length);
		return rval;
	}

	public void copyTo(final int srcPos, final byte[] dest, final int destPos,
			final int length) {
		System.arraycopy(origArray, startPosition + srcPos, dest, destPos,
				length);
	}

	public int getOffset() {
		return offset;
	}

	int length() {
		return length;
	}

}
