package de.hpi.mpss2015n.approxind.datastructures.bloomfilter;

import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BitVector;
import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.LongArrayBitVector;
import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.LongArrayHierarchicalBitVector;

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
