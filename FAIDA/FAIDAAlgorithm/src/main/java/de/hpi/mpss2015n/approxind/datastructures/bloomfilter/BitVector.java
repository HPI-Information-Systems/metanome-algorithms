package de.hpi.mpss2015n.approxind.datastructures.bloomfilter;

public interface BitVector<T> {

    long[] getBits();

    void set(int i);

    void clear(int i);

    boolean get(int i);

    BitVector<T> and(BitVector<?> other);

    BitVector<T> flip();

    int size();

    BitVector<T> or(BitVector<?> other);

    BitVector<T> copy();

    int next(int i);

    int count();
}
