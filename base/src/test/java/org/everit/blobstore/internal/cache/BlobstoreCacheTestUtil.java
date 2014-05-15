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

import java.util.Arrays;

public class BlobstoreCacheTestUtil {

	public static byte[] createData(final byte elems, final int size) {
		byte[] rval = new byte[size];
		Arrays.fill(rval, 0, size, elems);
		return rval;
	}

	public static byte[] createDataRange(final byte initial,
			final int increment, final int size) {
		byte[] rval = new byte[size];
		byte current = initial;
		for (int i = 0; i < size; ++i) {
			rval[i] = current;
			current += increment;
		}
		return rval;
	}

	private BlobstoreCacheTestUtil() {
		// TODO Auto-generated constructor stub
	}

}
