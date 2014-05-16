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

public enum RangeRelation {

    BEFORE(true, false, true),

    BEFORE_OVERLAPPING(true, false, false),

    CONTAINING(true, true, false),

    IDENTICAL(false, false, false),

    CONTAINED(false, false, false),

    AFTER_OVERLAPPING(false, true, false),

    AFTER(false, true, true);

    /**
     * Determines if the other range starts before the original range.
     */
    private boolean startsBefore;
    /**
     * Determines if the other range ends after the original range.
     */
    private boolean endsAfter;

    /**
     * Determines if the two ranges are distinct.
     */
    private boolean distinct;

    private RangeRelation(final boolean startsBefore, final boolean endsAfter, final boolean distinct) {
        this.startsBefore = startsBefore;
        this.endsAfter = endsAfter;
        this.distinct = distinct;
    }

    /**
     * Determines if the other range ends after the original range.
     */
    public boolean endsAfter() {
        return endsAfter;
    }

    /**
     * Determines if the two ranges are distinct.
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Determines if the other range starts before the original range.
     */
    public boolean startsBefore() {
        return startsBefore;
    }

}
