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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.everit.osgi.blobstore.internal.cache.BlobstoreCacheService;
import org.everit.osgi.blobstore.internal.cache.CacheKey;
import org.everit.osgi.blobstore.internal.cache.CachedBlobPart;
import org.everit.osgi.blobstore.internal.cache.Fragment;
import org.everit.osgi.blobstore.internal.cache.FragmentByteArray;
import org.everit.osgi.blobstore.internal.cache.FragmentPart;
import org.everit.osgi.blobstore.internal.cache.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BlobstoreCacheServiceImplTest {

    private static final long FRAGMENT_SIZE = 1024l;

    private static long BLOB_ID = 10l;

    private BlobstoreCacheService cacheService;

    private ConcurrentMap<CacheKey, Fragment> cache;

    private ConcurrentMap<Long, List<CacheKey>> keyCache;

    private void createPartsInFragment(final long fragmentStartPosition,
            final Range<Long>[] ranges) {
        CacheKey fragmentCacheKey = new CacheKey(BLOB_ID, fragmentStartPosition);
        Fragment fragment = cache.get(fragmentCacheKey);
        if (fragment == null) {
            fragment = new Fragment(BLOB_ID, fragmentStartPosition);
        }
        for (Range<Long> range : ranges) {
            int size = range.getUpperEndpoint().intValue()
                    - range.getLowerEndpoint().intValue() - 1;
            if (range.isUpperInclusive()) {
                ++size;
            }
            if (range.isLowerInclusive()) {
                ++size;
            }
            FragmentPart newPart = new FragmentPart(range.getLowerEndpoint(),
                    BlobstoreCacheTestUtil.createData((byte) 1, size));
            fragment.insertFragmentPart(newPart);
        }
        cache.put(fragmentCacheKey, fragment);
        List<CacheKey> keyList = keyCache.get(BLOB_ID);
        if (keyList == null) {
            keyList = new ArrayList<CacheKey>();
        }
        keyList.add(fragmentCacheKey);
        keyCache.put(BLOB_ID, keyList);
    }

    @Before
    public void setUp() {
        cache = new ConcurrentHashMap<CacheKey, Fragment>();
        keyCache = new ConcurrentHashMap<Long, List<CacheKey>>();
        cacheService = new BlobstoreCacheService(cache, keyCache);
        cacheService.setFragmentSize(FRAGMENT_SIZE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetBlobParts() {
        createPartsInFragment(0, new Range[] {
                new Range<Long>(200l, 400l, true, false),
                new Range<Long>(600l, 800l, true, false),
                new Range<Long>(800l, 1024l, true, false) });
        createPartsInFragment(FRAGMENT_SIZE, new Range[] {
                new Range<Long>(0l, 76l, true, false),
                new Range<Long>(76l, 176l, true, false),
                new Range<Long>(176l, 276l, true, false) });
        List<CachedBlobPart> expectedList = Arrays.asList(new CachedBlobPart[] {
                new CachedBlobPart(BLOB_ID, 700l, BlobstoreCacheTestUtil
                        .createData((byte) 1, 100)),
                        new CachedBlobPart(BLOB_ID, 800l, BlobstoreCacheTestUtil
                                .createData((byte) 1, 224)),
                                new CachedBlobPart(BLOB_ID, 1024l, BlobstoreCacheTestUtil
                                        .createData((byte) 1, 76)),
                                        new CachedBlobPart(BLOB_ID, 1100l, BlobstoreCacheTestUtil
                                                .createData((byte) 1, 100)),
                                                new CachedBlobPart(BLOB_ID, 1200l, BlobstoreCacheTestUtil
                                                        .createData((byte) 1, 50)) });
        List<CachedBlobPart> result = cacheService.getBlobParts(BLOB_ID, 700l,
                550l);
        Assert.assertEquals(expectedList.size(), result.size());
        int idx = 0;
        for (CachedBlobPart expected : expectedList) {
            Assert.assertEquals(expected, result.get(idx));
            ++idx;
        }

        expectedList = Arrays.asList(new CachedBlobPart[] {
                new CachedBlobPart(BLOB_ID, 700l, BlobstoreCacheTestUtil
                        .createData((byte) 1, 100)),
                        new CachedBlobPart(BLOB_ID, 800l, BlobstoreCacheTestUtil
                                .createData((byte) 1, 224)),
                                new CachedBlobPart(BLOB_ID, 1024l, BlobstoreCacheTestUtil
                                        .createData((byte) 1, 76)),
                                        new CachedBlobPart(BLOB_ID, 1100l, BlobstoreCacheTestUtil
                                                .createData((byte) 1, 100)),
                                                new CachedBlobPart(BLOB_ID, 1200l, BlobstoreCacheTestUtil
                                                        .createData((byte) 1, 100)) });
        result = cacheService.getBlobParts(BLOB_ID, 700l, 640l);
        Assert.assertEquals(expectedList.size(), result.size());
        idx = 0;
        for (CachedBlobPart expected : expectedList) {
            Assert.assertEquals(expected, result.get(idx));
            ++idx;
        }

        expectedList = Arrays.asList(new CachedBlobPart[] {
                new CachedBlobPart(BLOB_ID, 700l, BlobstoreCacheTestUtil
                        .createData((byte) 1, 100)),
                        new CachedBlobPart(BLOB_ID, 800l, BlobstoreCacheTestUtil
                                .createData((byte) 1, 120)) });
        result = cacheService.getBlobParts(BLOB_ID, 700l, 220l);
        Assert.assertEquals(expectedList.size(), result.size());
        idx = 0;
        for (CachedBlobPart expected : expectedList) {
            Assert.assertEquals(expected, result.get(idx));
            ++idx;
        }
    }

    @Test
    public void testGetBlobPartsEmptyResult() {
        List<CachedBlobPart> result = cacheService.getBlobParts(BLOB_ID, 10l,
                20l);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testGetFragment() {
        Fragment fragment = new Fragment(BLOB_ID, FRAGMENT_SIZE);
        cache.put(new CacheKey(BLOB_ID, FRAGMENT_SIZE), fragment);
        Assert.assertEquals(fragment,
                cacheService.getFragment(BLOB_ID, FRAGMENT_SIZE));
        Assert.assertEquals(fragment,
                cacheService.getFragment(BLOB_ID, FRAGMENT_SIZE + 2));
    }

    @Test
    public void testGetFragmentStartPosition() {
        Assert.assertEquals(0, cacheService.getFragmentStartPosition(1l));
        Assert.assertEquals(0,
                cacheService.getFragmentStartPosition(FRAGMENT_SIZE - 1));
        Assert.assertEquals(FRAGMENT_SIZE,
                cacheService.getFragmentStartPosition(FRAGMENT_SIZE));
        Assert.assertEquals(FRAGMENT_SIZE,
                cacheService.getFragmentStartPosition(FRAGMENT_SIZE * 2 - 1));
        Assert.assertEquals(FRAGMENT_SIZE * 2,
                cacheService.getFragmentStartPosition(FRAGMENT_SIZE * 2));
    }

    @Test
    public void testGetNonexistentFragment() {
        long fragmentSize = FRAGMENT_SIZE * 3;
        Fragment expectedFragment = new Fragment(BLOB_ID, fragmentSize);
        Assert.assertEquals(expectedFragment,
                cacheService.getFragment(BLOB_ID, fragmentSize));

        CacheKey key = new CacheKey(BLOB_ID, fragmentSize);
        Assert.assertEquals(expectedFragment, cache.get(key));

        List<CacheKey> keyList = keyCache.get(BLOB_ID);
        Assert.assertNotNull("key list created in keyCache", keyList);
        Assert.assertEquals(1, keyList.size());
        Assert.assertEquals(new CacheKey(BLOB_ID, fragmentSize), keyList.get(0));
    }

    @Test
    public void testMergeInto() {
        byte[] partData = BlobstoreCacheTestUtil.createData((byte) 1, 30);
        FragmentByteArray newData = new FragmentByteArray(
                BlobstoreCacheTestUtil.createData((byte) 2, 20), 0, 20, 0);
        FragmentPart part = new FragmentPart(0, partData);
        cacheService.mergeInto(0, newData, part);
        int i;
        byte[] rawNewData = newData.asRawArray();
        for (i = 0; i < rawNewData.length; ++i) {
            Assert.assertEquals("byte[" + i + "] copied", rawNewData[i],
                    part.getData()[i]);
        }
        for (; i < partData.length; ++i) {
            Assert.assertEquals("byte[" + i + "] not affected", (byte) 1,
                    part.getData()[i]);
        }

        partData = BlobstoreCacheTestUtil.createData((byte) 1, 30);
        part = new FragmentPart(0, partData);
        cacheService.mergeInto(5, newData, part);
        for (i = 0; i < 5; ++i) {
            Assert.assertEquals("byte[ " + i + " ] not affected", 1,
                    part.getData()[i]);
        }
        for (; i < 25; ++i) {
            Assert.assertEquals("byte[ " + i + "] copied", 2, part.getData()[i]);
        }
        for (; i < 30; ++i) {
            Assert.assertEquals("byte[ " + i + " ] not affected", 1,
                    part.getData()[i]);
        }

        partData = BlobstoreCacheTestUtil.createData((byte) 1, 30);
        part = new FragmentPart(10, partData);
        cacheService.mergeInto(15, newData, part);
        for (i = 0; i < 5; ++i) {
            Assert.assertEquals("byte[ " + i + " ] not affected", 1,
                    part.getData()[i]);
        }
        for (; i < 25; ++i) {
            Assert.assertEquals("byte[ " + i + " ] copied", 2,
                    part.getData()[i]);
        }
        for (; i < 30; ++i) {
            Assert.assertEquals("byte[ " + i + " ] not affected", 1,
                    part.getData()[i]);
        }

    }

    @Test
    public void testRemovePartsByBlobId() {
        // populating cache with 10 fragments for the blob
        List<CacheKey> keyList = new ArrayList<CacheKey>();
        for (int i = 0; i < 10; ++i) {
            long startPosition = FRAGMENT_SIZE * i;
            CacheKey key = new CacheKey(BLOB_ID, startPosition);
            Fragment fragment = new Fragment(BLOB_ID, startPosition);
            cache.put(key, fragment);
            keyList.add(key);
        }
        keyCache.put(BLOB_ID, keyList);

        // populating cache with 10 fragments for an other blob too
        List<CacheKey> otherKeyList = new ArrayList<CacheKey>();
        long otherBlobId = BLOB_ID + 1;
        for (int i = 0; i < 10; ++i) {
            long startPosition = FRAGMENT_SIZE * i;
            CacheKey key = new CacheKey(otherBlobId, startPosition);
            Fragment fragment = new Fragment(otherBlobId, startPosition);
            cache.put(key, fragment);
            otherKeyList.add(key);
        }
        keyCache.put(otherBlobId, otherKeyList);

        cacheService.removePartsByBlobId(BLOB_ID);

        // checking if the fragments have been properly removed
        for (CacheKey key : keyList) {
            Assert.assertNull(cache.get(key));
        }
        List<CacheKey> cachedKeyList = keyCache.get(BLOB_ID);
        Assert.assertNull(cachedKeyList);

        // checking if the other fragments are not affected
        for (CacheKey key : otherKeyList) {
            Assert.assertNotNull(cache.get(key));
        }
        List<CacheKey> otherCachedKeyList = keyCache.get(otherBlobId);
        Assert.assertNotNull(otherCachedKeyList);
        Assert.assertEquals(10, otherCachedKeyList.size());

    }

    @Test(expected = NullPointerException.class)
    public void testStoreBlobPartException() {
        cacheService.storeBlobPart(BLOB_ID, FRAGMENT_SIZE, null);
    }

    @Test
    public void testStoreBlobPartOneFragment() {
        byte[] data = BlobstoreCacheTestUtil.createData((byte) 1,
                (int) FRAGMENT_SIZE / 3);
        cacheService.storeBlobPart(BLOB_ID, FRAGMENT_SIZE + 10, data);
        Fragment createdFragment = cache.get(new CacheKey(BLOB_ID,
                FRAGMENT_SIZE));
        Assert.assertNotNull(createdFragment);
        LinkedList<FragmentPart> fragmentParts = createdFragment
                .getFragmentParts();
        Assert.assertNotNull(fragmentParts);
        Assert.assertEquals(1, fragmentParts.size());
        FragmentPart fragmentPart = fragmentParts.get(0);
        Assert.assertEquals(data, fragmentPart.getData());
        Assert.assertEquals(10, fragmentPart.getStartPositionInFragment());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStoreBlobPartOneFragmentContaining() {
        byte[] newData = BlobstoreCacheTestUtil.createData((byte) 2, 800);
        long offset = 100l;
        createPartsInFragment(FRAGMENT_SIZE,
                new Range[] { new Range<Long>(50l, 950l, true, false) });
        List<CacheKey> keyList = keyCache.get(BLOB_ID);
        Assert.assertEquals(1, keyList.size());
        cacheService.storeBlobPart(BLOB_ID, FRAGMENT_SIZE + offset, newData);
        keyList = keyCache.get(BLOB_ID);
        Assert.assertEquals(1, keyList.size());
        CacheKey key = keyList.get(0);
        Fragment fragment = cache.get(key);
        Assert.assertNotNull(fragment);
        Assert.assertEquals(1, fragment.getFragmentParts().size());
        FragmentPart firstPart = fragment.getFragmentParts().get(0);
        byte[] actualData = firstPart.getData();
        Assert.assertEquals(900, actualData.length);
        int i;
        for (i = 0; i < 50; ++i) {
            Assert.assertEquals((byte) 1, actualData[i]);
        }
        for (; i < 850; ++i) {
            Assert.assertEquals((byte) 2, actualData[i]);
        }
        for (; i < 900; ++i) {
            Assert.assertEquals((byte) 1, actualData[i]);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStoreBlobPartOneFragmentOverlapping() {
        byte[] newData = BlobstoreCacheTestUtil.createData((byte) 2, 80);
        long offset = 10l;
        createPartsInFragment(FRAGMENT_SIZE, new Range[] {
                new Range<Long>(5l, 20l, true, false),
                new Range<Long>(25l, 40l, true, false),
                new Range<Long>(50l, 65l, true, false),
                new Range<Long>(80l, 95l, true, false),
                new Range<Long>(98l, 102l, true, false) });
        cacheService.storeBlobPart(BLOB_ID, FRAGMENT_SIZE + offset, newData);
        List<CacheKey> keyList = keyCache.get(BLOB_ID);
        Assert.assertEquals(1, keyList.size());
        CacheKey key = keyList.get(0);
        Fragment fragment = cache.get(key);
        Assert.assertNotNull(fragment);
        Assert.assertEquals(2, fragment.getFragmentParts().size());
        FragmentPart firstPart = fragment.getFragmentParts().get(0);
        Assert.assertEquals(5, firstPart.getStartPositionInFragment());
        byte[] data = firstPart.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(90, data.length);
        int i = 0;
        for (; i < 5; ++i) {
            Assert.assertEquals("data[ " + i + " ] olddata", (byte) 1, data[i]);
        }
        for (; i < 85; ++i) {
            Assert.assertEquals("data[ " + i + " ] newdata", (byte) 2, data[i]);
        }
        for (; i < 90; ++i) {
            Assert.assertEquals("data[ " + i + " ] olddata", (byte) 1, data[i]);
        }

        FragmentPart secondPart = fragment.getFragmentParts().get(1);
        Assert.assertEquals(98, secondPart.getStartPositionInFragment());
        data = secondPart.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(4, data.length);
        for (i = 0; i < 4; ++i) {
            Assert.assertEquals((byte) 1, data[i]);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStoreBlobPartThreeFragments() {
        byte[] newData = BlobstoreCacheTestUtil.createData((byte) 2,
                (int) FRAGMENT_SIZE + 200);
        createPartsInFragment(FRAGMENT_SIZE, new Range[] {
                new Range<Long>(60l, 90l, true, false),
                new Range<Long>(600l, 950l, true, false),
                new Range<Long>(960l, 1000l, true, false) });
        createPartsInFragment(FRAGMENT_SIZE * 2, new Range[] {
                new Range<Long>(300l, 500l, true, false),
                new Range<Long>(600l, 950l, true, false),
                new Range<Long>(960l, 1000l, true, false) });
        createPartsInFragment(FRAGMENT_SIZE * 3, new Range[] {
                new Range<Long>(0l, 40l, true, false),
                new Range<Long>(50l, 350l, true, false),
                new Range<Long>(960l, 1000l, true, false) });
        cacheService.storeBlobPart(BLOB_ID, FRAGMENT_SIZE * 2 - 100, newData);
        List<CacheKey> keyList = keyCache.get(BLOB_ID);
        Assert.assertEquals(3, keyList.size());
        Fragment firstFragment = cache.get(keyList.get(0));
        Assert.assertNotNull("firstFragment is not null", firstFragment);
        Assert.assertEquals("firstFragment has 2 FragmentParts", 2,
                firstFragment.getFragmentParts().size());
        FragmentPart firstPart = firstFragment.getFragmentParts().get(0);
        Assert.assertEquals("startPositionInFragment", 60l,
                firstPart.getStartPositionInFragment());
        Assert.assertEquals("data.length", 30, firstPart.getData().length);

        FragmentPart secondPart = firstFragment.getFragmentParts().get(1);
        Assert.assertNotNull("secondPart is not null", secondPart);
        Assert.assertEquals("startPositionInFragment", 600l,
                secondPart.getStartPositionInFragment());
        Assert.assertEquals("data.length", 424, secondPart.getData().length);
        int i;
        for (i = 0; i < 324; ++i) {
            Assert.assertEquals("data[ " + i + " ] olddata", (byte) 1,
                    secondPart.getData()[i]);
        }
        for (; i < 424; ++i) {
            Assert.assertEquals("data[ " + i + " ] newdata", (byte) 2,
                    secondPart.getData()[i]);
        }

        Fragment secondFragment = cache.get(keyList.get(1));
        Assert.assertNotNull("secondFragment is not null", secondFragment);
        Assert.assertEquals("secondFragment has 1 FragmentPart", 1,
                secondFragment.getFragmentParts().size());
        firstPart = secondFragment.getFragmentParts().get(0);
        Assert.assertEquals(0, firstPart.getStartPositionInFragment());
        Assert.assertEquals(FRAGMENT_SIZE, firstPart.getData().length);
        for (byte b : firstPart.getData()) {
            Assert.assertEquals((byte) 2, b);
        }

        Fragment thirdFragment = cache.get(keyList.get(2));
        Assert.assertNotNull("thirdFragment is not null", thirdFragment);
        Assert.assertEquals("thirdFragment has 2 FragmentParts", 2,
                thirdFragment.getFragmentParts().size());

        firstPart = thirdFragment.getFragmentParts().get(0);
        Assert.assertEquals("startPositionInFragment", 0l,
                firstPart.getStartPositionInFragment());
        Assert.assertEquals("data.length", 350, firstPart.getData().length);
        for (i = 0; i < 100; ++i) {
            Assert.assertEquals("data[ " + i + " ] olddata", (byte) 2,
                    firstPart.getData()[i]);
        }
        for (; i < 350; ++i) {
            Assert.assertEquals("data[ " + i + " ] newdata", (byte) 1,
                    firstPart.getData()[i]);
        }

        secondPart = thirdFragment.getFragmentParts().get(1);
        Assert.assertNotNull("secondPart is not null", secondPart);
        Assert.assertEquals("startPositionInFragment", 960l,
                secondPart.getStartPositionInFragment());
        Assert.assertEquals("data.length", 40, secondPart.getData().length);
    }
}
