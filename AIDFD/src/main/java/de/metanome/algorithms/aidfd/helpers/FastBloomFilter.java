package de.metanome.algorithms.aidfd.helpers;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.BitSet;
import java.util.Random;

public class FastBloomFilter {

	private final BitSet bs;

	final HashFunction[] hashFunctions;

	final int capacity;

	public FastBloomFilter(int bits, int numberHashFunctions) {
		bs = new BitSet(bits);
		Random r = new Random(System.currentTimeMillis());
		hashFunctions = new HashFunction[numberHashFunctions];
		for (int i=0; i<numberHashFunctions; ++i) {
			hashFunctions[i] = Hashing.murmur3_128(r.nextInt());
		}
		capacity = bits;
	}

	public void add(long value) {
		for (HashFunction f : hashFunctions) {
			int h = f.hashLong(value).asInt();
			bs.set(Math.abs(h)%capacity, true);
		}
	}

	public void clear() {
		bs.clear();
	}

	public boolean mightContain(long value) {
		for (HashFunction f: hashFunctions) {
			int h = f.hashLong(value).asInt();

			if(!bs.get(Math.abs(h)%capacity)) {
				return false;
			}
		}

		return true;
	}
	
	public boolean containsAndAdd(long value) {
		boolean result = true;

		for (HashFunction f: hashFunctions) {
			int h = f.hashLong(value).asInt();
			int index = Math.abs(h)%capacity;
			
			result &= bs.get(index);
			bs.set(index, true);
		}

		return result;
	}
}
