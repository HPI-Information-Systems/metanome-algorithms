package de.metanome.algorithms.dcfinder.helpers;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class BitSetTranslator {
	private Integer[] indexes;

	public BitSetTranslator(Integer[] indexes) {
		this.indexes = indexes;
	}

	public IBitSet bitsetRetransform(IBitSet bitset) {
		IBitSet valid = LongBitSet.FACTORY.create();
		// TODO: check trivial
		for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
			valid.set(indexes[i].intValue());
		}
		return valid;
	}
	
	public int retransform(int i) {
		return indexes[i].intValue();
	}

	public IBitSet bitsetTransform(IBitSet bitset) {
		IBitSet bitset2 = LongBitSet.FACTORY.create();
		for (Integer i : indexes) {
			if (bitset.get(indexes[i.intValue()].intValue())) {
				bitset2.set(i.intValue());
			}
		}
		return bitset2;
	}

	public Collection<IBitSet> transform(Collection<IBitSet> bitsets) {
		return bitsets.stream().map(bitset -> bitsetTransform(bitset)).collect(Collectors.toList());
	}

	public Collection<IBitSet> retransform(Set<IBitSet> bitsets) {
		return bitsets.stream().map(bitset -> bitsetRetransform(bitset)).collect(Collectors.toList());
	}
}
