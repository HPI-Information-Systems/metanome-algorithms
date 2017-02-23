package de.hpi.mpss2015n.approxind.datastructures;

/**
 * Reduced Bloom filter implementation for FAIDACore.
 */
public class BloomFilter {

    private long[] bits;

    private long bitCapacity;

    public BloomFilter(int capacityInBytes) {
        this.bits = new long[(capacityInBytes + 7) / 8];
        this.bitCapacity = capacityInBytes * 8L;
    }

    public void setHash(long hash) {
        hash = Math.abs(hash);
        this.setBit(hash % this.bitCapacity);
    }

    private void setBit(long position) {
        int field = (int) (position / 64);
        int offset = (int) (position % 64);
        this.bits[field] |= (1L << offset);
    }

    public boolean containsAll(BloomFilter that) {
        if (this.bitCapacity != that.bitCapacity) throw new IllegalArgumentException();

        for (int i = 0; i < this.bits.length; i++) {
            if ((that.bits[i] & ~this.bits[i]) != 0L) return false;
        }
        return true;
    }

}
