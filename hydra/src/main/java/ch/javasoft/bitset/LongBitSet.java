/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008-2009, Marco Terzer, Zurich, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Swiss Federal Institute of Technology Zurich 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */
package ch.javasoft.bitset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A <code>LongBitSet</code> stores bits using a long integer array.
 */
public class LongBitSet implements IBitSet, Serializable {

	private static final long serialVersionUID = 3961087336068687071L;
	
	/**
	 * Default static factory for {@link LongBitSet} instances.
	 */
	public static final LongBitSetFactory FACTORY = new LongBitSetFactory();

	/**
	 * <code>LongBitSetFactory</code> is the {@link BitSetFactory} for
	 * {@code LongBitSet} instances.
	 */
	public static final class LongBitSetFactory implements BitSetFactory {
		public LongBitSet create() {
			return new LongBitSet();
		}

		public LongBitSet create(int capacity) {
			return new LongBitSet(capacity);
		}

		public LongBitSet create(IBitSet bits) {
			return new LongBitSet(bits);
		}

		public LongBitSet convert(IBitSet bitSet) {
			return bitSet instanceof LongBitSet ? (LongBitSet) bitSet : new LongBitSet(bitSet);
		}

		public LongBitSet create(BitSet bits) {
			return new LongBitSet(bits);
		}

		public Class<LongBitSet> getBitSetClass() {
			return LongBitSet.class;
		}
	};

	private static final int BITS_PER_UNIT = Long.SIZE;
	private long[] mUnits;

	/**
	 * Creates an empty bit set with initial capacity for 64 bits. All bits are
	 * initially false.
	 */
	public LongBitSet() {
		this(BITS_PER_UNIT);
	}

	/**
	 * Creates an empty bit set with the specified initial capacity. All bits
	 * are initially false.
	 */
	public LongBitSet(int bitCapacity) {
		mUnits = new long[1 + (bitCapacity - 1) / BITS_PER_UNIT];
	}

	public LongBitSet(BitSet bitSet) {
		this(bitSet.length());
		for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
			set(bit);
		}
	}

	/**
	 * Constructor using the specified bits to initialize this bit set. The bits
	 * are copied, meaning that changes to the given bit set are <b>not</b>
	 * reflected in {@code this} bit set.
	 * 
	 * @param bitSet
	 *            the bit set used which's bits are used to initialize this bit
	 *            set
	 */
	public LongBitSet(IBitSet bitSet) {
		this(bitSet.length());
		for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
			set(bit);
		}
	}

	/**
	 * Constructor using the specified string to initialize this bit set. One
	 * characters in the string are interpreted as {@code true} bits, meaning
	 * that a '1' in the string at position {@code i} causes that bit {@code i}
	 * is set to {@code true}. All other characters are interpreted as false.
	 * 
	 * @param bitString
	 *            the string used which's '1' characters are used to set the one
	 *            bits of this bit set
	 */
	public LongBitSet(String bitString) {
		this(bitString.length());
		for (int ii = 0; ii < bitString.length(); ii++) {
			if (bitString.charAt(ii) == '1')
				set(ii);
		}
	}

	/**
	 * Constructor for <code>LongBitSet</code> with units. The units array is
	 * cloned if {@code cloneArray} is true.
	 * 
	 * @param units
	 *            the long array with the bits
	 * @param cloneArray
	 *            true if {@code units} should be cloned
	 */
	public LongBitSet(long[] units, boolean cloneArray) {
		mUnits = cloneArray ? Arrays.copyOf(units, units.length) : units;
	}

	private void ensureCapacity(int unitLen) {
		if (mUnits.length < unitLen) {
			final long[] newUnits = new long[unitLen];
			System.arraycopy(mUnits, 0, newUnits, 0, mUnits.length);
			mUnits = newUnits;
		}
	}

	public void set(int bit, boolean value) {
		if (value)
			set(bit);
		else
			clear(bit);
	}

	public void set(int bit) {
		int unit = bit / BITS_PER_UNIT;
		int index = bit % BITS_PER_UNIT;
		long mask = 1L << index;
		ensureCapacity(unit + 1);
		mUnits[unit] |= mask;
	}

	public void clear(int bit) {
		int unit = bit / BITS_PER_UNIT;
		int index = bit % BITS_PER_UNIT;
		long mask = 1L << index;
		mUnits[unit] &= ~mask;
	}

	public void clear() {
		for (int ii = 0; ii < mUnits.length; ii++) {
			mUnits[ii] = 0L;
		}
	}

	public void flip(int bit) {
		int unit = bit / BITS_PER_UNIT;
		int index = bit % BITS_PER_UNIT;
		long mask = 1L << index;
		ensureCapacity(unit + 1);
		mUnits[unit] ^= (mUnits[unit] & mask);
	}

	public boolean get(int bit) {
		int unit = bit / BITS_PER_UNIT;
		if (unit >= mUnits.length)
			return false;
		int index = bit % BITS_PER_UNIT;
		long mask = 1L << index;
		return 0 != (mUnits[unit] & mask);
	}

	public boolean isSubSetOf(IBitSet of) {
		return isSubSetOf(of instanceof LongBitSet ? (LongBitSet) of : new LongBitSet(of));
	}

	public boolean isSubSetOf(LongBitSet of) {
		if (this == of)
			return true;

		int min = Math.min(mUnits.length, of.mUnits.length);
		for (int ii = 0; ii < min; ii++) {
			// classical
			long and = mUnits[ii] & of.mUnits[ii];
			if (and != mUnits[ii])
				return false;

			// this -> of == not this OR of
			// if (-1L != (~mUnits[ii] | of.mUnits[ii])) return false;

			// not (this -> of) == this AND not of
			// if (0L != (mUnits[ii] & ~of.mUnits[ii])) return false;
		}
		for (int i = min; i < mUnits.length; i++) {
			if (mUnits[i] != 0L)
				return false;
		}
		return true;
	}

	public boolean isSuperSetOfIntersection(IBitSet interA, IBitSet interB) {
		final LongBitSet longA = interA instanceof LongBitSet ? (LongBitSet) interA : new LongBitSet(interA);
		final LongBitSet longB = interB instanceof LongBitSet ? (LongBitSet) interB : new LongBitSet(interB);
		return isSuperSetOfIntersection(longA, longB);
	}

	public boolean isSuperSetOfIntersection(LongBitSet interA, LongBitSet interB) {
		if (this == interA || this == interB)
			return true;

		int minInter = Math.min(interA.mUnits.length, interB.mUnits.length);
		int minAll = Math.min(mUnits.length, minInter);
		for (int ii = 0; ii < minAll; ii++) {
			long inter = interA.mUnits[ii] & interB.mUnits[ii];
			if (inter != (inter & mUnits[ii]))
				return false;
		}
		for (int i = minAll; i < minInter; i++) {
			if (0L != (interA.mUnits[i] & interB.mUnits[i]))
				return false;
		}
		return true;
	}

	public void and(IBitSet with) {
		and(with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public void and(LongBitSet with) {
		if (this == with)
			return;

		int len = Math.min(mUnits.length, with.mUnits.length);
		for (int ii = 0; ii < len; ii++) {
			mUnits[ii] &= with.mUnits[ii];
		}
		for (int ii = len; ii < mUnits.length; ii++) {
			mUnits[ii] = 0L;
		}
	}

	public LongBitSet getAnd(IBitSet with) {
		return getAnd(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public LongBitSet getAnd(LongBitSet with) {
		return getAnd(this, with);
	}

	public int getAndCardinality(IBitSet with) {
		return getAndCardinality(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public int getAndCardinality(LongBitSet with) {
		return getAndCardinality(this, with);
	}

	public void or(IBitSet with) {
		or(with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public void or(LongBitSet with) {
		if (this == with)
			return;

		if (with.mUnits.length > mUnits.length) {
			long[] newUnits = new long[with.mUnits.length];
			for (int i = 0; i < mUnits.length; i++) {
				newUnits[i] = mUnits[i] | with.mUnits[i];
			}
			for (int i = mUnits.length; i < with.mUnits.length; i++) {
				newUnits[i] = with.mUnits[i];
			}
			mUnits = newUnits;
		} else {
			int len = with.mUnits.length;
			for (int i = 0; i < len; i++) {
				mUnits[i] |= with.mUnits[i];
			}
		}
	}

	public LongBitSet getOr(IBitSet with) {
		return getOr(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public LongBitSet getOr(LongBitSet with) {
		return getOr(this, with);
	}

	public void xor(IBitSet with) {
		xor(with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public void xor(LongBitSet with) {
		int newlen = Math.max(mUnits.length, with.mUnits.length);
		if (mUnits.length == with.mUnits.length) {
			while (newlen > 0 && 0L == (mUnits[newlen - 1] ^ with.mUnits[newlen - 1])) {
				newlen--;
			}
		}
		if (newlen != mUnits.length) {
			final long[] newUnits = new long[newlen];
			System.arraycopy(mUnits, 0, newUnits, 0, Math.min(newlen, mUnits.length));
			mUnits = newUnits;
		}
		final int len = Math.min(with.mUnits.length, newlen);
		for (int i = 0; i < len; i++) {
			mUnits[i] ^= with.mUnits[i];
		}
	}

	public LongBitSet getXor(IBitSet with) {
		return getXor(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public LongBitSet getXor(LongBitSet with) {
		return getXor(this, with);
	}

	public int getXorCardinality(IBitSet with) {
		return getXorCardinality(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public int getXorCardinality(LongBitSet with) {
		return getXorCardinality(this, with);
	}

	public void andNot(IBitSet with) {
		andNot(with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public void andNot(LongBitSet with) {
		// with is always true in the large parts, and thus always larger
		// thus, this is always directing the new length
		final int len = Math.min(mUnits.length, with.mUnits.length);
		for (int i = 0; i < len; i++) {
			mUnits[i] &= ~with.mUnits[i];
		}
	}

	public LongBitSet getAndNot(IBitSet with) {
		return getAndNot(this, with instanceof LongBitSet ? (LongBitSet) with : new LongBitSet(with));
	}

	public LongBitSet getAndNot(LongBitSet with) {
		return getAndNot(this, with);
	}

	public static LongBitSet getXor(LongBitSet setA, LongBitSet setB) {
		final LongBitSet res;
		if (setA.mUnits.length > setB.mUnits.length) {
			res = setA.clone();
			res.xor(setB);
		} else {
			res = setB.clone();
			res.xor(setA);
		}
		return res;
	}

	/**
	 * Return the number of one bits in the xor of the two sets, that is, the
	 * number of bits which differ in the two sets
	 */
	public static int getXorCardinality(LongBitSet setA, LongBitSet setB) {
		int card = 0;
		final int minLen = Math.min(setA.mUnits.length, setB.mUnits.length);
		for (int i = 0; i < minLen; i++) {
			card += Long.bitCount(setA.mUnits[i] ^ setB.mUnits[i]);
		}
		for (int i = minLen; i < setA.mUnits.length; i++) {
			card += Long.bitCount(setA.mUnits[i]);
		}
		for (int i = minLen; i < setB.mUnits.length; i++) {
			card += Long.bitCount(setB.mUnits[i]);
		}
		return card;
	}

	public static LongBitSet getOr(LongBitSet setA, LongBitSet setB) {
		LongBitSet larger, smaller;
		if (setA.mUnits.length >= setB.mUnits.length) {
			larger = setA;
			smaller = setB;
		} else {
			larger = setB;
			smaller = setA;
		}
		long[] units = new long[larger.mUnits.length];
		for (int i = 0; i < smaller.mUnits.length; i++) {
			units[i] = smaller.mUnits[i] | larger.mUnits[i];
		}
		for (int i = smaller.mUnits.length; i < larger.mUnits.length; i++) {
			units[i] = larger.mUnits[i];
		}
		return new LongBitSet(units, false);
	}

	public static LongBitSet getAnd(LongBitSet setA, LongBitSet setB) {
		LongBitSet larger, smaller;
		if (setA.mUnits.length >= setB.mUnits.length) {
			larger = setA;
			smaller = setB;
		} else {
			larger = setB;
			smaller = setA;
		}
		long[] units = new long[smaller.mUnits.length];
		for (int i = 0; i < smaller.mUnits.length; i++) {
			units[i] = smaller.mUnits[i] & larger.mUnits[i];
		}
		return new LongBitSet(units, false);
	}

	/**
	 * Return the number of one bits in the and of the two sets, that is, the
	 * number of bits which are common in the two sets
	 */
	public static int getAndCardinality(LongBitSet setA, LongBitSet setB) {
		int card = 0;
		final int minLen = Math.min(setA.mUnits.length, setB.mUnits.length);
		for (int i = 0; i < minLen; i++) {
			card += Long.bitCount(setA.mUnits[i] & setB.mUnits[i]);
		}
		return card;
	}

	/**
	 * Returns setA and not setB
	 */
	public static LongBitSet getAndNot(LongBitSet setA, LongBitSet setB) {
		// set b is always true in the large parts, and thus always larger
		// thus, set a is always directing the new length
		final long[] units = new long[setA.mUnits.length];
		for (int i = 0; i < setB.mUnits.length; i++) {
			units[i] = setA.mUnits[i] & ~setA.mUnits[i];
		}
		for (int i = setB.mUnits.length; i < setA.mUnits.length; i++) {
			units[i] = setA.mUnits[i];
		}
		return new LongBitSet(units, false);
	}

	public static LongBitSet getAnd(LongBitSet... bitSets) {
		// handle special cases 0 - 2 sets
		if (bitSets.length == 0)
			return new LongBitSet();
		else if (bitSets.length == 1)
			return bitSets[0].clone();
		else if (bitSets.length == 2)
			return getAnd(bitSets[0], bitSets[1]);
		int smallest = 0;
		for (int i = 1; i < bitSets.length; i++) {
			if (bitSets[i].length() < bitSets[smallest].length()) {
				smallest = i;
			}
		}
		final LongBitSet result = bitSets[smallest].clone();
		for (int i = 0; i < bitSets.length; i++) {
			if (i != smallest)
				result.and(bitSets[i]);
		}
		return result;
	}

	public int compareTo(IBitSet o) {
		return compareTo(o instanceof LongBitSet ? (LongBitSet) o : new LongBitSet(o));
	}

	public int compareTo(LongBitSet o) {
		final int min = Math.min(mUnits.length, o.mUnits.length);
		for (int i = 0; i < min; i++) {
			// make unsigned comparison
			final boolean bitA = 0 != (0x1 & mUnits[i]);
			final boolean bitB = 0 != (0x1 & o.mUnits[i]);
			if (bitA != bitB) {
				return bitA ? 1 : -1;
			}
			final long revA = Long.reverse(0xfffffffffffffffeL & mUnits[i]);
			final long revB = Long.reverse(0xfffffffffffffffeL & o.mUnits[i]);
			final long cmp = revA - revB;

			if (cmp < 0)
				return -1;
			if (cmp > 0)
				return 1;
		}
		for (int i = min; i < mUnits.length; i++) {
			if (mUnits[i] != 0)
				return 1;
		}
		for (int i = min; i < o.mUnits.length; i++) {
			if (o.mUnits[i] != 0)
				return -1;
		}
		return 0;
	}

	protected int unitLength() {
		int index = mUnits.length;
		do
			index--;
		while (index >= 0 && mUnits[index] == 0L);
		return index + 1;
	}

	public int length() {
		int index = mUnits.length;
		do
			index--;
		while (index >= 0 && mUnits[index] == 0L);
		return index < 0 ? 0 : index * BITS_PER_UNIT + BITS_PER_UNIT - Long.numberOfLeadingZeros(mUnits[index]);
	}

	public boolean isEmpty() {
		int index = mUnits.length;
		do
			index--;
		while (index >= 0 && mUnits[index] == 0L);
		return index < 0;
	}

	@Override
	public LongBitSet clone() {
		return new LongBitSet(mUnits.clone(), false);
	}

	public int cardinality() {
		int card = 0;
		for (int ii = 0; ii < mUnits.length; ii++) {
			card += Long.bitCount(mUnits[ii]);
		}
		return card;
	}

	public int cardinality(int fromBit, int toBit) {
		int fromUnit = fromBit / BITS_PER_UNIT;
		int toUnit = (toBit + BITS_PER_UNIT - 1) / BITS_PER_UNIT;
		int unitStart = Math.max(0, fromUnit);
		int unitLen = Math.min(mUnits.length, toUnit);
		int card = 0;
		for (int ii = unitStart; ii < unitLen; ii++) {
			if (mUnits[ii] != 0) {
				long unit = mUnits[ii];
				if (ii == fromUnit) {
					int bit = fromBit % BITS_PER_UNIT;
					unit &= (0xffffffffffffffffL >>> bit);
				}
				if (ii == toUnit - 1) {
					int bit = fromBit % BITS_PER_UNIT;
					unit &= (0xffffffffffffffffL << (BITS_PER_UNIT - bit));
				}
				card += Long.bitCount(unit);
			}
		}
		return card;
	}

	@Override
	public int hashCode() {
		int code = 0;
		for (int ii = 0; ii < mUnits.length; ii++) {
			code ^= (0x00000000ffffffffL & mUnits[ii]);
			code ^= (0x00000000ffffffffL & (mUnits[ii] >>> 32));
		}
		return code;
	}

	public int hashCodeObj() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof LongBitSet) {
			final LongBitSet bitSet = (LongBitSet) obj;
			final int len = Math.min(mUnits.length, bitSet.mUnits.length);
			for (int ii = 0; ii < len; ii++) {
				if (mUnits[ii] != bitSet.mUnits[ii])
					return false;
			}
			for (int ii = len; ii < mUnits.length; ii++) {
				if (mUnits[ii] != 0L)
					return false;
			}
			for (int ii = len; ii < bitSet.mUnits.length; ii++) {
				if (bitSet.mUnits[ii] != 0L)
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		int unitLen = unitLength();
		for (int ii = 0; ii < unitLen; ii++) {
			int len = ii == unitLen - 1 ? 1 + (length() - 1) % BITS_PER_UNIT : BITS_PER_UNIT;
			if (mUnits[ii] != 0) {
				long bit = 1L;
				for (int jj = 0; jj < len; jj++, bit <<= 1) {
					if ((mUnits[ii] & bit) != 0)
						sb.append('1');
					else
						sb.append('0');
				}
			} else {
				sb.append("0000000000000000000000000000000000000000000000000000000000000000", 0, len);
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public int nextSetBit(int from) {
		int fromBit = from % BITS_PER_UNIT;
		int fromUnit = from / BITS_PER_UNIT;
		for (int ii = Math.max(0, fromUnit); ii < mUnits.length; ii++) {
			if (mUnits[ii] != 0L) {
				long unit = mUnits[ii];
				if (ii == fromUnit)
					unit &= (0xffffffffffffffffL << fromBit);
				if (unit != 0L)
					return ii * BITS_PER_UNIT + Long.numberOfTrailingZeros(unit);
			}
		}
		return -1;
	}

	public int nextClearBit(int from) {
		int fromBit = from % BITS_PER_UNIT;
		int fromUnit = from / BITS_PER_UNIT;
		for (int ii = fromUnit; ii < mUnits.length; ii++) {
			if (mUnits[ii] != -1L) {
				long unit = ~mUnits[ii];
				if (ii == fromUnit)
					unit &= (0xffffffffffffffffL << fromBit);
				if (unit != 0)
					return ii * BITS_PER_UNIT + Long.numberOfTrailingZeros(unit);
			}
		}
		return Math.max(from, length());
	}

	public BitSet toBitSet() {
		BitSet bitSet = new BitSet(length());
		for (int bit = nextSetBit(0); bit >= 0; bit = nextSetBit(bit + 1)) {
			bitSet.set(bit);
		}
		return bitSet;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream dout) throws IOException {
		dout.writeInt(mUnits.length);
		for (int i = 0; i < mUnits.length; i++) {
			dout.writeLong(mUnits[i]);
		}
	}

	private synchronized void readObject(java.io.ObjectInputStream din) throws IOException, ClassNotFoundException {
		final int len = din.readInt();
		this.mUnits = new long[len];
		for (int i = 0; i < len; i++) {
			this.mUnits[i] = din.readLong();
		}
	}

	public byte[] compress() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			compress(out);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return out.toByteArray();
	}

	public void compress(OutputStream out) throws IOException {
		ZipOutputStream zOut = new ZipOutputStream(out);
		// zOut.setLevel(Deflater.BEST_SPEED);
		zOut.putNextEntry(new ZipEntry("A"));
		DataOutputStream dOut = new DataOutputStream(zOut);
		dOut.writeInt(mUnits.length);
		for (int ii = 0; ii < mUnits.length; ii++) {
			dOut.writeLong(mUnits[ii]);
		}
		dOut.flush();
		zOut.closeEntry();
		zOut.flush();
	}

	public static LongBitSet uncompress(byte[] bytes) {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
		try {
			return uncompress(byteIn);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static LongBitSet uncompress(InputStream in) throws IOException {
		ZipInputStream zIn = new ZipInputStream(in);
		zIn.getNextEntry();
		DataInputStream dIn = new DataInputStream(zIn);
		int unitLen = dIn.readInt();
		long[] units = new long[unitLen];
		for (int ii = 0; ii < unitLen; ii++) {
			units[ii] = dIn.readLong();
		}
		return new LongBitSet(units, false);
	}

	public BitSetFactory factory() {
		return FACTORY;
	}

	public long[] toLongArray() {
		return toLongArray(null, 0);
	}

	public long[] toLongArray(long[] arr, int offset) {
		int len = unitLength();
		if (arr == null || arr.length < len + offset) {
			arr = new long[len + offset];
		}
		System.arraycopy(mUnits, 0, arr, offset, len);
		return arr;
	}

}
