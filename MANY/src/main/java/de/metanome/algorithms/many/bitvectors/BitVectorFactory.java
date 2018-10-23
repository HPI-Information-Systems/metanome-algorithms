package de.metanome.algorithms.many.bitvectors;

public class BitVectorFactory {
    boolean isFastVector = true;

    public BitVectorFactory(boolean isFastVector) {
        this.isFastVector = isFastVector;
    }

    public BitVector<?> createBitVector(int size) {
        if (isFastVector)
            return new LongArrayHierarchicalBitVector(size);
		return new LongArrayBitVector(size);
    }

}
