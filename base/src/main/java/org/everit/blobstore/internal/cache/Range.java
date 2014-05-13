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

import java.io.Serializable;

public class Range<T extends Comparable<? super T>> implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4567444380490553673L;

    protected static int alignComparisionResultBasedOnInclusive(int value, final boolean forLowerBound,
            final boolean thisInclusive, final boolean otherInclusive) {
        if (value == 0) {
            if (!thisInclusive && otherInclusive) {
                value = (forLowerBound ? 1 : -1);
            } else if (thisInclusive && !otherInclusive) {
                value = (forLowerBound ? -1 : 1);
            }
        }
        return value;
    }

    protected final T lowerEndpoint;

    protected final T upperEndpoint;

    protected boolean lowerInclusive = true;

    protected boolean upperInclusive = true;

    /**
     * Creates a range with the given bounding values.
     *
     * The created range will be open at both bounds.
     *
     * @param lowerEndpoint
     * @param upperEndpoint
     */
    public Range(final T lowerEndpoint, final T upperEndpoint) {
        super();
        if ((lowerEndpoint != null) && (upperEndpoint != null) && (lowerEndpoint.compareTo(upperEndpoint) > 0)) {
            throw new IllegalArgumentException("lowerBound [" + lowerEndpoint + "] is not lower than higherBound ["
                    + upperEndpoint + "]");
        }
        this.lowerEndpoint = lowerEndpoint;
        this.upperEndpoint = upperEndpoint;
    }

    public Range(final T lowerEndpoint, final T upperEndpoint, final boolean lowerInclusive,
            final boolean upperInclusive) {
        this(lowerEndpoint, upperEndpoint);
        this.lowerInclusive = lowerInclusive;
        this.upperInclusive = upperInclusive;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        Range<T> other = (Range<T>) obj;
        if ((upperInclusive != other.upperInclusive) || (lowerInclusive != other.lowerInclusive)) {
            return false;
        }
        if (((upperEndpoint != null) && !upperEndpoint.equals(other.upperEndpoint))
                || ((lowerEndpoint != null) && !lowerEndpoint.equals(other.lowerEndpoint))) {
            return false;
        }
        if (((upperEndpoint == null) && (other.upperEndpoint != null))
                || ((lowerEndpoint == null) && (other.lowerEndpoint != null))) {
            return false;
        }
        return true;
    }

    public T getLowerEndpoint() {
        return lowerEndpoint;
    }

    /**
     * Determines the relation of this range to an other range.
     *
     * Returns
     * <ul>
     * <li>{@link RangeRelation#BEFORE} if the <code>other</code> is before this range and they don't have common
     * element.</li>
     * <li>{@link RangeRelation#BEFORE_OVERLAPPING} if <code>other</code> starts before this range but they do have
     * common elements.</li>
     * <li>{@link RangeRelation#CONTAINING} if <code>other</code> starts before this range and ends after this interval
     * (practically <code>other</code> contains <code>this</code>).</li>
     * <li>{@link RangeRelation#IDENTICAL} if the two range are equal.</li>
     * <li>{@link RangeRelation#CONTAINED} if this range starts before <code>other</code> and ends after
     * <code>other</code> (practically <code>this</code> contains <code>other</code>).</li>
     * <li>{@link RangeRelation#AFTER_OVERLAPPING} if <code>other</code> starts before the end of this range and
     * <code>other</code> ends after the end of this range.</li>
     * <li>{@link RangeRelation#AFTER} if <code>other</code> starts after the end of this range.</li>
     * </ul>
     *
     * @param other
     * @return
     */
    public RangeRelation getRelationTo(final Range<T> other) {
        if (this == other) {
            return RangeRelation.IDENTICAL;
        }
        int lowerToOtherLower = lowerEndpoint.compareTo(other.getLowerEndpoint());
        lowerToOtherLower = Range.alignComparisionResultBasedOnInclusive(lowerToOtherLower, true, lowerInclusive,
                other.isLowerInclusive());

        int higherToOtherHigher = upperEndpoint.compareTo(other.getUpperEndpoint());
        higherToOtherHigher = Range.alignComparisionResultBasedOnInclusive(higherToOtherHigher, false,
                upperInclusive,
                other.isUpperInclusive());

        if ((lowerToOtherLower == 0) && (higherToOtherHigher == 0)) {
            return RangeRelation.IDENTICAL;
        }

        if ((lowerToOtherLower >= 0) && (higherToOtherHigher <= 0)) {
            return RangeRelation.CONTAINING;
        }

        if ((lowerToOtherLower <= 0) && (higherToOtherHigher >= 0)) {
            return RangeRelation.CONTAINED;
        }

        if (lowerToOtherLower > 0) {
            int lowerToOtherHigher = lowerEndpoint.compareTo(other.getUpperEndpoint());
            if ((lowerToOtherHigher == 0) && !(other.isUpperInclusive() && lowerInclusive)) {
                lowerToOtherHigher = 1;
            }
            if (lowerToOtherHigher > 0) {
                return RangeRelation.BEFORE;
            } else {
                return RangeRelation.BEFORE_OVERLAPPING;
            }
        }

        if (higherToOtherHigher < 0) {
            int higherToOtherLower = upperEndpoint.compareTo(other.getLowerEndpoint());
            if ((higherToOtherLower == 0) && !(other.isLowerInclusive() && upperInclusive)) {
                higherToOtherLower = -1;
            }
            if (higherToOtherLower < 0) {
                return RangeRelation.AFTER;
            } else {
                return RangeRelation.AFTER_OVERLAPPING;
            }
        }

        throw new RangeException("failed to determine intervals' relation of this [" + toString() + "] and other ["
                + other.toString() + "]");
    }

    public T getUpperEndpoint() {
        return upperEndpoint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((upperEndpoint == null) ? 0 : upperEndpoint.hashCode());
        result = (prime * result) + (upperInclusive ? 1231 : 1237);
        result = (prime * result) + ((lowerEndpoint == null) ? 0 : lowerEndpoint.hashCode());
        result = (prime * result) + (lowerInclusive ? 1231 : 1237);
        return result;
    }

    public Range<T> intersect(final Range<T> other) {
        if (getRelationTo(other).isDistinct()) {
            return null;
        }
        T lowerBound, higherBound;
        boolean lowerInclusive, higherInclusive;
        int lowerComp = this.lowerEndpoint.compareTo(other.lowerEndpoint);
        if (lowerComp > 0) {
            lowerBound = this.lowerEndpoint;
            lowerInclusive = this.lowerInclusive;
        } else if (lowerComp < 0) {
            lowerBound = other.lowerEndpoint;
            lowerInclusive = other.lowerInclusive;
        } else {
            lowerBound = this.lowerEndpoint;
            lowerInclusive = this.lowerInclusive || other.lowerInclusive;
        }
        int higherComp = this.upperEndpoint.compareTo(other.upperEndpoint);
        if (higherComp < 0) {
            higherBound = this.upperEndpoint;
            higherInclusive = this.upperInclusive;
        } else if (higherComp > 0) {
            higherBound = other.upperEndpoint;
            higherInclusive = other.upperInclusive;
        } else {
            higherBound = this.upperEndpoint;
            higherInclusive = this.upperInclusive || other.upperInclusive;
        }
        return new Range<T>(lowerBound, higherBound, lowerInclusive, higherInclusive);
    }

    public boolean isLowerInclusive() {
        return lowerInclusive;
    }

    public boolean isUpperInclusive() {
        return upperInclusive;
    }

    @Override
    public String toString() {
        StringBuilder sb;
        if (lowerInclusive) {
            sb = new StringBuilder("[");
        } else {
            sb = new StringBuilder("(");
        }
        sb.append(lowerEndpoint.toString())
        .append(',')
        .append(upperEndpoint.toString());
        if (upperInclusive) {
            sb.append(']');
        } else {
            sb.append(')');
        }
        return sb.toString();
        // return "Range [lowerBound=" + lowerEndpoint + ", lowerClosed=" + lowerInclusive + ", higherBound="
        // + upperEndpoint + ", higherClosed=" + upperInclusive + "]";
    }

    /**
     * Returns the union of <code>this</code> and <code>other</code>. The method does not modify neither
     * <code>this</code> or <code>other</code>.
     *
     * @param other
     * @return
     */
    public Range<T> union(final Range<T> other) {
        T lowerBound, higherBound;
        boolean lowerInclusive, higherInclusive;
        int lowerComp = this.lowerEndpoint.compareTo(other.lowerEndpoint);
        if (lowerComp < 0) {
            lowerBound = this.lowerEndpoint;
            lowerInclusive = this.lowerInclusive;
        } else if (lowerComp > 0) {
            lowerBound = other.lowerEndpoint;
            lowerInclusive = other.lowerInclusive;
        } else {
            lowerBound = this.lowerEndpoint;
            lowerInclusive = this.lowerInclusive || other.lowerInclusive;
        }
        int higherComp = this.upperEndpoint.compareTo(other.upperEndpoint);
        if (higherComp > 0) {
            higherBound = this.upperEndpoint;
            higherInclusive = this.upperInclusive;
        } else if (higherComp < 0) {
            higherBound = other.upperEndpoint;
            higherInclusive = other.upperInclusive;
        } else {
            higherBound = this.upperEndpoint;
            higherInclusive = this.upperInclusive || other.upperInclusive;
        }
        return new Range<T>(lowerBound, higherBound, lowerInclusive, higherInclusive);
    }

}
