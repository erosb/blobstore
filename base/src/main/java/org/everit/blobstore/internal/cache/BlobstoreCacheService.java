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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.log.LogService;

public class BlobstoreCacheService {

    // private class KeyCacheMaintiner implements
    // CacheEntryRemovedListener<CacheKey, Fragment> {
    // @Override
    // public void entriesRemoved(
    // final Iterable<CacheEntryEvent<? extends CacheKey, ? extends Fragment>>
    // events) {
    // Map<Long, List<CacheKey>> removedKeys = new HashMap<Long,
    // List<CacheKey>>();
    // CacheKey key;
    // Iterator<CacheEntryEvent<? extends CacheKey, ? extends Fragment>>
    // iterator = events
    // .iterator();
    // while (iterator.hasNext()) {
    // CacheEntryEvent<? extends CacheKey, ? extends Fragment> next = iterator
    // .next();
    // key = next.getKey();
    // List<CacheKey> removedKeysOfBlob = removedKeys.get(key
    // .getBlobId());
    // if (removedKeysOfBlob == null) {
    // removedKeysOfBlob = new ArrayList<CacheKey>();
    // removedKeys.put(key.getBlobId(), removedKeysOfBlob);
    // }
    // removedKeysOfBlob.add(key);
    // }
    // for (Long blobId : removedKeys.keySet()) {
    // List<CacheKey> keyList = keyCache.get(blobId);
    // if (keyList != null) {
    // keyList.removeAll(removedKeys.get(blobId));
    // storeOrRemove(blobId, keyList);
    // }
    // }
    // }
    //
    // @Override
    // public void entryRemoved(
    // final CacheEntryEvent<? extends CacheKey, ? extends Fragment> event) {
    // CacheKey removedKey = event.getKey();
    // Long blobId = removedKey.getBlobId();
    // List<CacheKey> keyList = keyCache.get(blobId);
    // if (keyList != null) {
    // keyList.remove(removedKey);
    // storeOrRemove(blobId, keyList);
    // }
    // }
    //
    // private void storeOrRemove(final Long blobId,
    // final List<CacheKey> keyList) {
    // if (keyList.size() == 0) {
    // keyCache.remove(blobId);
    // } else {
    // keyCache.put(blobId, keyList);
    // }
    // }
    // }

    public static ClassLoader getClassLoader() {
        return BlobstoreCacheService.class.getClassLoader();
    }

    @Reference
    private LogService LOGGER;

    private final ConcurrentMap<CacheKey, Fragment> cache;

    private final ConcurrentMap<Long, List<CacheKey>> keyCache;

    private long fragmentSize = 1024l;

    public BlobstoreCacheService(
            final ConcurrentMap<CacheKey, Fragment> cache,
            final ConcurrentMap<Long, List<CacheKey>> keyCache) {
        super();
        this.cache = cache;
        this.keyCache = keyCache;
        // cache.registerCacheEntryListener(new KeyCacheMaintiner(),
        // NotificationScope.REMOTE, true);
    }

    public List<CachedBlobPart> getBlobParts(final long blobId,
            final long startPosition, final long maxLength) {
        List<CacheKey> keyList = keyCache.get(blobId);
        List<CachedBlobPart> rval = new ArrayList<CachedBlobPart>();
        if (keyList == null || keyList.size() == 0) {
            return rval;
        }
        long actualLength = 0;
        CacheKey key;
        Iterator<CacheKey> iterator = keyList.iterator();
        // flag marking if the process of collection the required blob parts is
        // finished
        boolean collectingFinished = false;
        while (iterator.hasNext() && !collectingFinished) {
            key = iterator.next();
            Fragment fragment = cache.get(key);
            if (fragment != null && fragment.getStartPosition() + fragmentSize >= startPosition) {
                long prevFragmentPartAbsEndPos = 0;
                for (FragmentPart part : fragment.getFragmentParts()) {
                    long fragmentPartAbsStartPos = fragment.getStartPosition() + part.getStartPositionInFragment();
                    long fragmentPartAbsEndPos = fragmentPartAbsStartPos + part.getData().length;
                    if (prevFragmentPartAbsEndPos > 0) {
                        if (fragmentPartAbsStartPos != prevFragmentPartAbsEndPos) {
                            // non-continuous fragment parts, the current part
                            // does not sequentially follow the
                            // previous part
                            collectingFinished = true;
                        }
                    }
                    if (!collectingFinished
                            // otherwise the given fragment part is before the blob part
                            // we are interested in
                            && fragmentPartAbsEndPos >= startPosition) {
                        int copyFrom = 0;
                        int copyLength = part.getData().length;

                        if (fragmentPartAbsStartPos < startPosition) {
                            long diff = startPosition - fragmentPartAbsStartPos;
                            copyFrom += diff;
                            copyLength -= diff;
                        }

                        long candidateNewLength = actualLength + copyLength;

                        if (candidateNewLength > maxLength) {
                            copyLength = (int) (maxLength - actualLength);
                        }
                        byte[] partData;
                        if (copyFrom != 0 || copyLength != part.getData().length) {
                            partData = new byte[copyLength];
                            System.arraycopy(part.getData(), copyFrom, partData, 0, copyLength);
                        } else {
                            partData = part.getData();
                        }
                        rval.add(new CachedBlobPart(blobId, fragmentPartAbsStartPos + copyFrom, partData));
                        actualLength = candidateNewLength;
                        if (actualLength >= maxLength) {
                            collectingFinished = true;
                        } else {
                            prevFragmentPartAbsEndPos = fragment.getStartPosition() + part.getEndPositionInFragment();
                        }
                    }
                }
            }
        }
        return rval;
    }

    Fragment getFragment(final long blobId, final long startPosition) {
        long fragmentStartPosition = getFragmentStartPosition(startPosition);
        CacheKey key = new CacheKey(blobId, fragmentStartPosition);
        Fragment result = cache.get(key);
        if (result == null) {
            result = new Fragment(blobId, fragmentStartPosition);
            storeFragment(result);
        }
        return result;
    }

    long getFragmentStartPosition(final long startPosition) {
        return startPosition / fragmentSize * fragmentSize;
    }

    void mergeInto(final long relStartPos, final FragmentByteArray blobPart, final FragmentPart part) {
        long relPosInFragment = relStartPos - part.getStartPositionInFragment();
        blobPart.copyTo(0, part.getData(), (int) relPosInFragment, blobPart.length());
    }

    private void partialUpdateBlobFragment(final long relStartPos,
            final FragmentByteArray fragmentBytes, final Fragment fragment) {
        long relEndPos = relStartPos + fragmentBytes.length();
        Range<Long> newRange = new Range<Long>(relStartPos, relEndPos);
        LinkedList<FragmentPart> fragmentParts = fragment.getFragmentParts();
        if (fragmentParts.size() > 0) {
            FragmentPart beforeOverlapping = null;
            FragmentPart afterOverlapping = null;
            boolean merged = false;
            List<FragmentPart> innerParts = new ArrayList<FragmentPart>();
            FragmentPart part;
            Iterator<FragmentPart> iterator = fragmentParts.iterator();
            while (iterator.hasNext() && !merged) {
                part = iterator.next();
                Range<Long> existingRange = part.asRange();
                RangeRelation relation = newRange.getRelationTo(existingRange);
                // the new range has nothing to do with this existing range
                if (!relation.isDistinct()) {
                    if (relation == RangeRelation.CONTAINING || relation == RangeRelation.IDENTICAL) {
                        mergeInto(relStartPos, fragmentBytes, part);
                        // the entire blobPart has been copied into the containing FragmentPart nothing else to do
                        merged = true;
                    } else if (relation == RangeRelation.BEFORE_OVERLAPPING) {
                        beforeOverlapping = part;
                    } else if (relation == RangeRelation.AFTER_OVERLAPPING) {
                        afterOverlapping = part;
                    } else {
                        assert (relation == RangeRelation.CONTAINED);
                        innerParts.add(part);
                    }
                }
            }
            if (!merged) {
                FragmentPart newPart = processOverlappings(relStartPos,
                        fragmentBytes, beforeOverlapping, afterOverlapping,
                        fragment);
                fragment.getFragmentParts().removeAll(innerParts);
                fragment.insertFragmentPart(newPart);

            }
        } else {
            FragmentPart newPart = new FragmentPart(relStartPos, fragmentBytes.asRawArray());
            fragment.insertFragmentPart(newPart);
        }
        storeFragment(fragment);
    }

    /**
     * Used during the blob part storing process to merge the {@link FragmentPart}s which already exist in the cache and
     * overlap (but aren't contained by) the new blob part to be stored. The <code>beforeOverlapping</code> fragment
     * part, the new fragment part and the <code>afterOverlapping</code> fragment part must be in the same
     * {@link Fragment} specified by <code>ownerFragment</code>, cross-fragment data storing is not taken into account
     * by this method.
     *
     * Merges the before-overlapping {@link FragmentPart}, after-overlapping {@link FragmentPart} (if they exist) and
     * the new blob data into a new array.
     *
     * If <code>beforeOverlapping</code> and <code>afterOverlapping</code> are both null then the method will return
     * <code>blobPart</code> otherwise it performs the merge.
     *
     * Example: if <code>beforeOverlapping</code> and <code>afterOverlapping</code> look this way (given by the start
     * and end position relatively in the <code>ownerFragment</code>):
     * <ul>
     * <li><code>beforeFragment</code>: [5, 20]</li>
     * <li><code>afterFragment</code>: [80, 95]</li>
     * </ul>
     * and <code>startPosition</code> = 10, and <code>blobPart.length</code> is 80, then the following steps will be
     * performed by this method:
     * <ul>
     * <li>creates a new <code>byte</code> array with size 95 - 5 = 90, this array will be returned</li>
     * <li>copies the first 5 bytes of {@link FragmentPart#getData() beforeOverlapping.getData()} to the beginning of
     * the result</li>
     * <li>copies <code>blobPart</code> to the result array, to position 5 (at this point the first 85 bytes of the
     * result will be loaded)</li>
     * <li>copies the last 5 bytes of {@link FragmentPart#getData() afterOverlapping.getData()} to the result</li>
     * <li>{@link LinkedList#remove() removes} both <code>beforeOverlapping</code> and <code>afterOverlapping</code>
     * from the {@link Fragment#getFragmentParts() the FragmentParts of ownerFragment}.</li>
     * <li>returns the new array</li>
     * </ul>
     *
     * @param startPositionInFragment
     * @param blobPart
     * @param beforeOverlapping
     * @param afterOverlapping
     * @param ownerFragment
     * @return
     */
    private FragmentPart processOverlappings(
            final long startPositionInFragment,
            final FragmentByteArray blobPart,
            final FragmentPart beforeOverlapping,
            final FragmentPart afterOverlapping, final Fragment ownerFragment) {
        boolean beforeIsNull = beforeOverlapping == null;
        boolean afterIsNull = afterOverlapping == null;
        if (beforeIsNull && afterIsNull) {
            return new FragmentPart(startPositionInFragment, blobPart.asRawArray());
        }

        long newStartPos = startPositionInFragment;
        int newSize = blobPart.length();
        int beforeRemaining = 0;
        byte[] beforeFragmentOldData = null;

        int afterCut = 0;
        int afterRemaining = 0;
        byte[] afterFragmentOldData = null;

        if (!beforeIsNull) {
            beforeFragmentOldData = beforeOverlapping.getData();
            beforeRemaining = (int) (startPositionInFragment - beforeOverlapping.getStartPositionInFragment());
            newSize += beforeRemaining;
            newStartPos = beforeOverlapping.getStartPositionInFragment();
        }

        if (!afterIsNull) {
            afterFragmentOldData = afterOverlapping.getData();
            long newDataEndPos = startPositionInFragment + blobPart.length();
            afterRemaining = (int) (afterOverlapping.getEndPositionInFragment() - newDataEndPos);
            afterCut = afterFragmentOldData.length - afterRemaining;
            newSize += afterRemaining;
        }

        byte[] newData = new byte[newSize];

        int loadedUpTo = 0;

        if (!beforeIsNull) {
            System.arraycopy(beforeFragmentOldData, 0, newData, 0, beforeRemaining);
            loadedUpTo = beforeRemaining;
            ownerFragment.getFragmentParts().remove(beforeOverlapping);
        }

        blobPart.copyTo(0, newData, loadedUpTo, blobPart.length());
        loadedUpTo += blobPart.length();

        if (!afterIsNull) {
            System.arraycopy(afterFragmentOldData, afterCut, newData, loadedUpTo, afterRemaining);
            ownerFragment.getFragmentParts().remove(afterOverlapping);
        }
        return new FragmentPart(newStartPos, newData);
    }

    public void removePartsByBlobId(final long blobId) {
        for (CacheKey key : keyCache.get(blobId)) {
            if (cache.remove(key) == null) {
                LOGGER.log(LogService.LOG_WARNING, "inconsistent cache state: " + key
                        + " exists in keyCache but not found in fragment cache");
            }
        }
        keyCache.remove(blobId);
    }

    public void setFragmentSize(final long fragmentSize) {
        this.fragmentSize = fragmentSize;
    }

    public void storeBlobPart(final long blobId, final long startPosition,
            final byte[] blobPart) {
        Objects.requireNonNull(blobPart, "blobPart cannot be null");
        if (blobPart.length == 0) {
            return;
        }
        long relStartPos = startPosition % fragmentSize;
        FragmentByteArray[] bytesByFragment = new FragmentableBlobPart(blobPart, (int) fragmentSize,
                (int) relStartPos).getFragments();

        long fragmentStartPosition = startPosition;
        Fragment fragment = getFragment(blobId, fragmentStartPosition);
        // storing the new bytes of the first fragment
        partialUpdateBlobFragment(relStartPos, bytesByFragment[0], fragment);
        fragmentStartPosition += fragmentSize;
        // replacing the entire content of the inner fragments if such fragments
        // exist
        if (bytesByFragment.length > 2) {
            for (int i = 1; i < bytesByFragment.length - 1; ++i) {
                fragment = getFragment(blobId, fragmentStartPosition);
                updateBlobFragment(bytesByFragment[i], fragment);
                fragmentStartPosition += fragmentSize;
            }
        }
        // storing the new bytes of the last fragment if the first and last
        // segments are not the same
        if (bytesByFragment.length > 1) {
            fragment = getFragment(blobId, fragmentStartPosition);
            partialUpdateBlobFragment(0, bytesByFragment[bytesByFragment.length - 1], fragment);
        }
    }

    private void storeFragment(final Fragment fragment) {
        CacheKey key = fragment.createCacheKey();
        cache.put(key, fragment);
        long blobId = fragment.getBlobId();
        List<CacheKey> keyList = keyCache.get(blobId);
        if (keyList == null) {
            keyList = new ArrayList<CacheKey>();
        }
        if (!keyList.contains(key)) {
            keyList.add(key);
        }
        keyCache.put(blobId, keyList);
    }

    private void updateBlobFragment(final FragmentByteArray fragmentByteArray,
            final Fragment fragment) {
        FragmentPart newPart = new FragmentPart(0, fragmentByteArray.asRawArray());
        LinkedList<FragmentPart> parts = new LinkedList<FragmentPart>();
        parts.add(newPart);
        fragment.setFragmentParts(parts);
        storeFragment(fragment);
    }
}
