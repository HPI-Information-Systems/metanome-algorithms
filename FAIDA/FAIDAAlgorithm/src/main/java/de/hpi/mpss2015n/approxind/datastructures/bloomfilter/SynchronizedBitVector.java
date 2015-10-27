package de.hpi.mpss2015n.approxind.datastructures.bloomfilter;

import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BitVector;

public class SynchronizedBitVector implements BitVector<SynchronizedBitVector> {

    BitVector<?> backing;

    public SynchronizedBitVector(BitVector<?> bitVector) {
        this.backing = bitVector;
    }

    @Override
    public long[] getBits() {
        synchronized (backing) {
            return this.backing.getBits();
        }
    }

    @Override
    public void set(int i) {
        synchronized (backing) {
            this.backing.set(i);
        }
    }

    @Override
    public synchronized void clear(int i) {
        synchronized (backing) {
            this.backing.clear(i);
        }
    }

    @Override
    public boolean get(int i) {
        synchronized (backing) {
            return this.backing.get(i);
        }
    }

    @Override
    public BitVector<SynchronizedBitVector> and(BitVector<?> other) {
        synchronized (backing) {
            this.backing.and(other);
            return this;
        }
    }

    @Override
    public BitVector<SynchronizedBitVector> flip() {
        synchronized (backing) {
            this.backing.flip();
            return this;
        }
    }

    @Override
    public int size() {
        synchronized (backing) {
            return this.backing.size();
        }
    }

    @Override
    public BitVector<SynchronizedBitVector> or(BitVector<?> other) {
        synchronized (backing) {
            this.backing.or(other);
            return this;
        }
    }

    @Override
    public BitVector<SynchronizedBitVector> copy() {
        synchronized (backing) {
            SynchronizedBitVector copy = new SynchronizedBitVector(this.backing.copy());
            return copy;
        }
    }

    @Override
    public int next(int i) {
        synchronized (backing) {
            return this.backing.next(i);
        }
    }

    @Override
    public int count() {
        synchronized (backing) {
            return this.backing.count();
        }
    }
}
