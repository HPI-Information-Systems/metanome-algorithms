package de.hpi.mpss2015n.approxind.datastructures;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;

import java.util.Arrays;

/**
 * A bottom-k sketch is basically a {@link LongHeap} with some additional functionality.
 */
public class BottomKSketch {

    private static final int BYTES_PER_ENTRY = 8;

    private final LongAVLTreeSet values;

    private final int capacity;

    public BottomKSketch(int capacityInBytes) {
        this.values = new LongAVLTreeSet();
        this.capacity = capacityInBytes / BYTES_PER_ENTRY;
    }

    public void add(long element) {
        // Check if we need to add the element in the first place.
        if (this.values.size() >= this.capacity && this.values.lastLong() <= element) {
            return;
        }

        // Exchange the last element for the new one.
        this.values.add(element);
        if (this.values.size() > this.capacity) {
            this.values.remove(this.values.lastLong());
        }
    }

    /**
     * Tells whether this instance might describe a superset of what the other instance describes.
     */
    public boolean dominates(BottomKSketch that) {
        assert this.capacity == that.capacity;

        // We must for sure have at least as much values in the sketch for domination.
        if (this.values.size() < that.values.size()) return false;
        else if (that.values.isEmpty()) return true;

        long[] thisSorted = this.values.toLongArray();
        long[] thatSorted = that.values.toLongArray();
        if (this.values.size() < this.capacity) {
            // If we are not filled to capacity, we can do a regular subset test.
            int thisIndex = 0, thatIndex = 0;
            while (thisIndex < thisSorted.length && thatIndex < thatSorted.length) {
                int diff = Long.compare(thisSorted[thisIndex], thatSorted[thatIndex]);
                if (diff < 0) thisIndex++;
                else if (diff == 0) {
                    thisIndex++;
                    thatIndex++;
                } else return false;
            }

            return thatIndex >= thatSorted.length;

        } else {
            // Otherwise, we must only compare the "overlaps" of the sketches, where the dominating sketch should
            // contain exclusively smaller or equal hashes.
            long thisMax = thisSorted[thisSorted.length - 1];
            int thatLimit = Arrays.binarySearch(thatSorted, thisMax);
            if (thatLimit == -1 || thatLimit == 0) {
                // The sketches do not overlap but the dominating sketch only contains smaller hashes.
                // Or the overlap is exactly one.
                return true;
            } else if (thatLimit < -1) {
                thatLimit = -thatLimit - 1;
            }

            int thisStart = Arrays.binarySearch(thisSorted, thatSorted[0]);
            if (thisStart < 0) {
                // If the elements overlap, than every element of that sketch must be included in this sketch
                // within the overlap interval.
                return false;
            }

            int thisIndex = thisStart + 1, thatIndex = 1;
            while (thisIndex < thisSorted.length && thatIndex < thatLimit) {
                int diff = Long.compare(thisSorted[thisIndex], thatSorted[thatIndex]);
                if (diff < 0) thisIndex++;
                else if (diff == 0) {
                    thisIndex++;
                    thatIndex++;
                } else return false;
            }

            return thatIndex >= thatLimit;
        }


    }

}
