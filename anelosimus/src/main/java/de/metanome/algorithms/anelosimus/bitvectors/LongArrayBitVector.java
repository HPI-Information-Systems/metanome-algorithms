package de.metanome.algorithms.anelosimus.bitvectors;

import java.util.Arrays;

public class LongArrayBitVector implements BitVector<LongArrayBitVector> {

    private long[] backing;
    private static final int b = Long.SIZE;
    private int size;

    public LongArrayBitVector(int capacity) {
        backing = new long[((capacity + b - 1) / b)];
        size = capacity;
    }

    public void set(int i) {
        check(i);
        backing[i / b] |= (1l << (i % b));
    }

    public boolean get(int i) {
        check(i);
        return 0 != (backing[(i / b)] & (1l << (i % b)));
    }

    public void clear(int i) {
        check(i);
        backing[i / b] &= ~(1l << (i % b));
    }

    @Override
    public String toString() {
        String result = "";
        for (long l : backing) {
            String resultPart = "";
            for (int i = 0; i < Long.numberOfLeadingZeros(l); i++) {
                resultPart += "0";
            }
            resultPart += Long.toBinaryString(l) + " ";
            result += new
                    StringBuilder(resultPart).reverse().toString();
        }
        result = result.substring(0, size + backing.length);

        return result;
    }

    @Override
    public int size() {
        return size;
    }

    private void check(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public BitVector<LongArrayBitVector> flip() {
        int words = backing.length;
        while (words-- != 0)
            backing[words] ^= 0xFFFFFFFFFFFFFFFFL;
        return this;
    }

    @Override
    public BitVector<LongArrayBitVector> and(BitVector<?> other) {
        int words = backing.length;
        while (words-- != 0)
            backing[words] &= other.getBits()[words];
        return this;
    }

    @Override
    public BitVector<LongArrayBitVector> or(BitVector<?> other) {
        int words = backing.length;
        while (words-- != 0)
            backing[words] |= other.getBits()[words];
        return this;
    }

    @Override
    public long[] getBits() {
        return backing;
    }

    @Override
    public BitVector<LongArrayBitVector> copy() {
        LongArrayBitVector copy = new LongArrayBitVector(this.size);
        long[] backingCopy = new long[this.backing.length];

        System.arraycopy(backing, 0, backingCopy, 0, backing.length);
        copy.backing = backingCopy;

        return copy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(backing);
        result = prime * result + size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LongArrayBitVector other = (LongArrayBitVector) obj;
        if (!Arrays.equals(backing, other.backing))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public int next(int i) {
        if (i - 1 >= this.size)
            return -1;
        int wordI = i / b;
        long word = backing[wordI];

        while (true) {
            while (word == 0) {
                if (++wordI < this.backing.length) {
                    word = backing[wordI];
                } else
                    return -1;
            }
            int ntzWord = Long.numberOfTrailingZeros(word);
            int j = ntzWord + wordI * b;
            if (j <= i) {
                word ^= (1l << ntzWord);
            } else {
                return j < this.size ? j : -1;
            }

        }
    }

    @Override
    public int count() {
        int count = 0;
        int lastRef = -1;
        while ((lastRef = this.next(lastRef)) > -1) {
            count++;
        }
        return count;
    }
}
