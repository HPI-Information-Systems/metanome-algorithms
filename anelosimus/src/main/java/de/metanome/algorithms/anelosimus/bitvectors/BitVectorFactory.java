package de.metanome.algorithms.anelosimus.bitvectors;

public class BitVectorFactory {
    boolean isFastVector = true;

    public BitVectorFactory(boolean isFastVector) {
        this.isFastVector = isFastVector;
    }

    public BitVector<?> createBitVector(int size) {
        if (isFastVector)
            return new LongArrayHierarchicalBitVector(size);
        else
            return new LongArrayBitVector(size);
    }

}
