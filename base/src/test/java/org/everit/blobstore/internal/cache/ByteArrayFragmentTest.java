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

import static org.everit.blobstore.internal.cache.BlobstoreCacheTestUtil.createDataRange;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ByteArrayFragmentTest {

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] { { 0, 10, 12, 1, 10, 0, 10 },
				{ 2, 3, 10, 1, 3, 0, 3 }, { 0, 30, 10, 3, 10, 10, 10 },
				{ 0, 32, 10, 4, 10, 10, 2 }, { 4, 30, 10, 4, 6, 10, 4 },
				{ 4, 33, 10, 4, 6, 10, 7 }

		});
	}

	private final int increment = 1;

	private final int offset;

	private final int elemCount;

	private final int fragmentSize;

	private final int expectedFragmentCount;

	private final int expectedFirstFragmentLength;

	private final int expectedInnerFragmentLength;

	private final int expectedLastFragmentLength;

	public ByteArrayFragmentTest(final int offset, final int elemCount,
			final int fragmentSize, final int expectedFragmentCount,
			final int expectedFirstFragmentLength,
			final int expectedInnerFragmentLength,
			final int expectedLastFragmentLength) {
		super();
		this.offset = offset;
		this.elemCount = elemCount;
		this.fragmentSize = fragmentSize;
		this.expectedFragmentCount = expectedFragmentCount;
		this.expectedFirstFragmentLength = expectedFirstFragmentLength;
		this.expectedInnerFragmentLength = expectedInnerFragmentLength;
		this.expectedLastFragmentLength = expectedLastFragmentLength;
	}

	private byte assertByteArray(final FragmentByteArray fragment,
			final int expectedLength, final byte expectedInitialValue,
			final int fragmentIdx) {
		Assert.assertNotNull(fragment);
		Assert.assertEquals("fragment[ " + fragmentIdx + " ].length",
				expectedLength, fragment.length());
		byte expectedValue = expectedInitialValue;
		byte[] rawArray = fragment.asRawArray();
		Assert.assertEquals(expectedLength, rawArray.length);
		int byteIdx = 0;
		for (byte b : rawArray) {
			Assert.assertEquals("fragment[ " + fragmentIdx + " ][ " + byteIdx
					+ " ]", expectedValue, b);
			expectedValue += increment;
			++byteIdx;
		}
		return expectedValue;
	}

	@Test
	public void testGetFragments() {
		byte[] data = createDataRange((byte) 0, increment, elemCount);
		FragmentableBlobPart fragmentable = new FragmentableBlobPart(data,
				fragmentSize, offset);

		FragmentByteArray[] fragments = fragmentable.getFragments();
		Assert.assertNotNull(fragments);

		Assert.assertEquals(expectedFragmentCount, fragments.length);
		if (expectedFragmentCount == 0) {
			return;
		}
		byte expected = 0;
		FragmentByteArray firstFragment = fragments[0];
		expected = assertByteArray(firstFragment, expectedFirstFragmentLength,
				expected, 0);
		if (expectedFragmentCount == 1) {
			return;
		}
		int lastFragmentIdx = fragments.length - 1;
		for (int i = 1; i < lastFragmentIdx; ++i) {
			expected = assertByteArray(fragments[i],
					expectedInnerFragmentLength, expected, i);
		}
		assertByteArray(fragments[lastFragmentIdx], expectedLastFragmentLength,
				expected, lastFragmentIdx);
	}

}
