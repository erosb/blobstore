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
package org.everit.blobstore.internal.api;
import java.util.List;
import java.util.Map;

import org.everit.blobstore.internal.cache.CachedBlobPart;

/**
 * The DTO for holding the cached and non-cached parts of the blob.
 */
public class BlobPartResolver {
    /**
     * The cached parts of the blob.
     */
    private final List<CachedBlobPart> cached;
    /**
     * The non-cached parts of the blog. The key of the map is the start of the non-cached part, and the value is the
     * length of the non-cached part starting at the location specified by the key.
     */
    private final Map<Long, Long> notcached;

    /**
     * The default constructor.
     * 
     * @param cached
     *            The cached parts of the blob.
     * @param notcached
     *            The non-cached parts of the blog. The key of the map is the start of the non-cached part, and the
     *            value is the length of the non-cached part starting at the location specified by the key.
     */
    public BlobPartResolver(final List<CachedBlobPart> cached, final Map<Long, Long> notcached) {
        super();
        this.cached = cached;
        this.notcached = notcached;
    }

    public List<CachedBlobPart> getCached() {
        return cached;
    }

    public Map<Long, Long> getNotcached() {
        return notcached;
    }
}