package de.hpi.mpss2015n.approxind.datastructures;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;

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

    public boolean containsAll(BottomKSketch that) {
        long[] thisSorted = this.values.toLongArray();
        long[] thatSorted = that.values.toLongArray();

        if (thisSorted.length < thatSorted.length) return false;

        int i = 0, j = 0;

        while (i < thisSorted.length && j < thatSorted.length) {
            int diff = Long.compare(thisSorted[i], thatSorted[j]);
            if (diff < 0) i++;
            else if (diff == 0) {
                i++;
                j++;
            } else return false;
        }

        return j >= thatSorted.length;
    }

}
