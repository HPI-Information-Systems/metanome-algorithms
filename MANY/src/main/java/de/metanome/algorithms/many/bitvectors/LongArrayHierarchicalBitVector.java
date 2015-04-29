package de.metanome.algorithms.many.bitvectors;

import java.util.Arrays;

public class LongArrayHierarchicalBitVector implements BitVector<LongArrayHierarchicalBitVector> {

    private long[] backing;
    private long[] actives;
    private static final int b = Long.SIZE;
    private int size;

    public LongArrayHierarchicalBitVector(int capacity) {
        backing = new long[((capacity + b - 1) / b)];
        actives = new long[((backing.length + b - 1) / b)];
        size = capacity;
        // Arrays.fill(actives, Long.MAX_VALUE);
    }

    public void set(int i) {
        check(i);
        backing[i / b] |= (1l << (i % b));
        actives[(i / b) / b] |= (1l << ((i / b) % b));
    }

    public void clear(int i) {
        check(i);
        backing[i / b] &= ~(1l << (i % b));
        if (backing[i / b] == 0) {
            actives[(i / b) / b] &= ~(1l << ((i / b) % b));
        }
    }

    public boolean get(int i) {
        check(i);
        return 0 != (backing[(i / b)] & (1l << (i % b)));
    }

    private void check(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public long[] getBits() {
        return backing;
    }

    @Override
    public BitVector<LongArrayHierarchicalBitVector> and(BitVector<?> other) {
        int activeI = actives.length;
        while (activeI-- != 0) {
            long active = actives[activeI];
            while (active != 0) {
                int ntz = Long.numberOfTrailingZeros(active);
                active ^= (1l << ntz);
                int word = ntz + activeI * b;
                backing[word] &= other.getBits()[word];
                if (backing[word] == 0)
                    actives[word / b] &= ~(1l << (word % b));
            }
        }

        return this;
    }

    @Override
    public BitVector<LongArrayHierarchicalBitVector> or(BitVector<?> other) {
        int words = backing.length;
        while (words-- != 0) {
            backing[words] |= other.getBits()[words];
            if (backing[words] != 0)
                actives[words / b] |= (1l << (words % b));
        }

        return this;
    }

    @Override
    public BitVector<LongArrayHierarchicalBitVector> flip() {
        int words = backing.length;
        while (words-- != 0)
            backing[words] ^= 0xFFFFFFFFFFFFFFFFL;
        updateIndex();
        return this;
    }

    private void updateIndex() {
        int words = backing.length;
        while (words-- != 0) {
            if (backing[words] != 0)
                actives[words / b] |= (1l << (words % b));
            else
                actives[words / b] &= ~(1l << (words % b));
        }
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

    public String toLongString() {
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
        result += " > ";

        for (long l : actives) {
            String resultPart = "";
            for (int i = 0; i < Long.numberOfLeadingZeros(l) && i < Long.SIZE - 1; i++) {
                resultPart += "0";
            }

            resultPart += Long.toBinaryString(l);
            String activesString = new StringBuilder(resultPart).reverse().toString();
            result += activesString;
        }

        return result;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(actives);
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
        LongArrayHierarchicalBitVector other = (LongArrayHierarchicalBitVector) obj;
        if (!Arrays.equals(actives, other.actives))
            return false;
        if (!Arrays.equals(backing, other.backing))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public BitVector<LongArrayHierarchicalBitVector> copy() {
        LongArrayHierarchicalBitVector copy = new LongArrayHierarchicalBitVector(this.size);
        long[] backingCopy = new long[this.backing.length];
        long[] activesCopy = new long[this.actives.length];

        System.arraycopy(backing, 0, backingCopy, 0, backing.length);
        System.arraycopy(actives, 0, activesCopy, 0, actives.length);
        copy.backing = backingCopy;
        copy.actives = activesCopy;

        return copy;
    }

    @Override
    public int next(int i) {
        if (i - 1 >= this.size)
            return -1;
        // pick the first useful active
        int activeI = (i / b) / b;
        // forward-wind this first active
        long active = actives[activeI];
        active = (active >> (i / b) % b) << (i / b) % b;
        // we break dirty
        while (true) {
            // as long there are words with bits set in that active block
            while (active != 0) {
                // skip zero parts in active
                int ntzActive = Long.numberOfTrailingZeros(active);
                active ^= (1l << ntzActive);
                int wordI = ntzActive + activeI * b;
                // pick the word, scan over the bits --> we can find values smaller than i, so we have to check
                long word = backing[wordI];
                while (word != 0) {
                    int ntzWord = Long.numberOfTrailingZeros(word);
                    int j = ntzWord + wordI * b;
                    if (j <= i) {
                        word ^= (1l << ntzWord);
                    } else {
                        return j < this.size ? j : -1;
                    }
                }
            }
            if (++activeI < this.actives.length) {
                active = actives[activeI];
            } else
                break;
        }
        return -1;
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
