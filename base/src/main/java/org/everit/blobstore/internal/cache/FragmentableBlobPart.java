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

class FragmentableBlobPart {

	private final byte[] rawByteArray;

	private final int fragmentSize;

	private final int offset;

	FragmentableBlobPart(final byte[] rawByteArray, final int fragmentSize,
			final int offset) {
		super();
		this.rawByteArray = rawByteArray;
		this.fragmentSize = fragmentSize;
		this.offset = offset;
	}

	public FragmentByteArray[] getFragments() {
		if (rawByteArray.length < fragmentSize) {
			return new FragmentByteArray[] { new FragmentByteArray(
					rawByteArray, 0, rawByteArray.length, offset) };
		}
		int offsetPlusLen = offset + rawByteArray.length;
		int fragmentCount = offsetPlusLen / fragmentSize;
		int lastFragmentSize = offsetPlusLen % fragmentSize;
		if (lastFragmentSize > 0) {
			++fragmentCount;
		}
		FragmentByteArray[] rval = new FragmentByteArray[fragmentCount];

		if (rawByteArray.length == 0) {
			return new FragmentByteArray[0];
		}

		rval[0] = new FragmentByteArray(rawByteArray, 0, fragmentSize - offset,
				offset);

		int startPos = fragmentSize - offset;
		for (int i = 1; i < fragmentCount - 1; ++i) {
			rval[i] = new FragmentByteArray(rawByteArray, startPos,
					fragmentSize, 0);
			startPos += fragmentSize;
		}
		rval[fragmentCount - 1] = new FragmentByteArray(rawByteArray, startPos,
				rawByteArray.length - startPos, 0);
		return rval;
	}

	long getFragmentSize() {
		return fragmentSize;
	}

	int getOffset() {
		return offset;
	}

}
