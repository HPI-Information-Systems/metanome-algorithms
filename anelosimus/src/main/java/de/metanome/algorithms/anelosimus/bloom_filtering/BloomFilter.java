package de.metanome.algorithms.anelosimus.bloom_filtering;

import java.nio.charset.Charset;
import java.util.Collection;

import de.metanome.algorithms.anelosimus.bitvectors.BitVector;
import de.metanome.algorithms.anelosimus.bitvectors.BitVectorFactory;

/**
 *
 * @param <E>
 *        Object type that is to be inserted into the Bloom filter.
 * @author Fabian Tschirschnitz <fatschi@googlemail.com>
 */
public class BloomFilter<E> {

    private byte salt;

    private int[] createHashes(byte[] data, int hashes) {
        final int[] result = new int[hashes];

        int k = 0;
        while (k < hashes) {
            final byte[] digest = HashFactory.Instance.createHash(data, salt);

            for (int i = 0; i < digest.length / 4 && k < hashes; i++) {
                int h = 0;
                for (int j = i * 4; j < i * 4 + 4; j++) {
                    h <<= 8;
                    h |= digest[j] & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }

    protected BitVector<?> bits;
    protected int n; // number of elements actually added to

    // the Bloom filter
    protected int k; // number of hash functions

    static final Charset charset = Charset.forName("UTF-8");

    public BloomFilter(double falsePositiveProbability, int expectedNumberOfElements, BitVectorFactory factory,
            byte salt) {
        int m = getM(falsePositiveProbability, expectedNumberOfElements);
        int k = getK(falsePositiveProbability, expectedNumberOfElements);
        this.bits = factory.createBitVector(m);
        this.k = k;
        this.salt = salt;
    }

    public static int getM(double falsePositiveProbability, int expectedNumberOfElements) {
        double c = (Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))) / Math.log(2));
        return (int) Math.ceil(c * expectedNumberOfElements);
    }

    public static int getK(double falsePositiveProbability, int expectedNumberOfElements) {
        return (int) Math.ceil(getM(falsePositiveProbability, expectedNumberOfElements) / expectedNumberOfElements
                * Math.log(2));
    }

    public BloomFilter(double falsePositiveProbability, int expectedNumberOfElements, BitVectorFactory factory) {
        this(falsePositiveProbability, expectedNumberOfElements, factory, (byte) 0);

    }

    public BloomFilter(int m, int k, BitVectorFactory factory) {
        this(m, k, factory, (byte) 0);
    }

    public BloomFilter(int m, int k, BitVectorFactory factory, byte salt) {
        this.bits = factory.createBitVector(m);
        this.k = k;
        this.salt = salt;
    }

    private void add(byte[] bytes) {
        final int[] hashes = createHashes(bytes, this.k);
        for (final int hash : hashes) {
            this.bits.set(Math.abs(hash % this.bits.size()));
        }
        this.n++;
    }

    public void add(E element) {
        if (element != null) {
            this.add(element.toString().getBytes(charset));
        }
    }

    public void addAll(Collection<? extends E> c) {
        for (final E element : c) {
            this.add(element);
        }
    }

    private boolean contains(byte[] bytes) {
        final int[] hashes = createHashes(bytes, this.k);
        for (final int hash : hashes) {
            if (!this.bits.get(Math.abs(hash % this.bits.size()))) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(E element) {
        return this.contains(element.toString().getBytes(charset));
    }

    public boolean containsAll(Collection<? extends E> c) {
        for (final E element : c) {
            if (!this.contains(element)) {
                return false;
            }
        }
        return true;
    }

    public BitVector<?> getBits() {
        return this.bits;
    }

    @Override
    public String toString() {
        return this.bits.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bits == null) ? 0 : bits.hashCode());
        result = prime * result + k;
        result = prime * result + n;
        result = prime * result + salt;
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
        BloomFilter<?> other = (BloomFilter<?>) obj;
        if (bits == null) {
            if (other.bits != null)
                return false;
        } else if (!bits.equals(other.bits))
            return false;
        if (k != other.k)
            return false;
        if (n != other.n)
            return false;
        if (salt != other.salt)
            return false;
        return true;
    }
}
