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

import java.util.BitSet;

/**
 * The <code>IBitSet</code> interface defines bit set operations common to all
 * implementors. The functionality of <code>IBitSet</code> is similar to that of
 * Java's {@code BitSet}, but some missing important methods have been added,
 * like {@link #isSubSetOf(IBitSet)}.
 */
public interface IBitSet extends Comparable<IBitSet> {

	/**
	 * Set the specified {@code bit} to true
	 * 
	 * @param bit
	 *            The index of the bit to set
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	void set(int bit);

	/**
	 * Set the specified {@code bit} to the given {@code value}
	 * 
	 * @param bit
	 *            the index of the bit to set
	 * @param value
	 *            the value to be set
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	void set(int bit, boolean value);

	/**
	 * Set the specified bit to false
	 * 
	 * @param bit
	 *            The index of the bit to clear
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	void clear(int bit);

	/**
	 * Set all bits to false
	 */
	void clear();

	/**
	 * Set the specified bit to the opposite of the current value (new=not old)
	 * 
	 * @param bit
	 *            The index of the bit to flip
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	void flip(int bit);

	/**
	 * Returns the specified
	 * 
	 * @param bit
	 *            The index of the asked bit
	 * @return true if the bit is set, false otherwise
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	boolean get(int bit);

	/**
	 * Returns true if this bit set is a subset of the given bit set. A bit set
	 * is a subset of another bit set if the all bits which are true in this bit
	 * set are also true in the other bit set. If two bit sets are equal, each
	 * is a subset of the other.
	 * 
	 * @param of
	 *            The bit set, which is a superset of this bit set if the latter
	 *            is a subset of it
	 * @return true if this bit set is a subset of <code>of</code>
	 */
	boolean isSubSetOf(IBitSet of);

	/**
	 * Returns true if this bit set is a superset of the intersection of the
	 * given bit sets. This method returns true if for every bit {@code i}, the
	 * following condition holds:
	 * {@code this.bit[i] >= (interA.bit[i] AND with.bit[i])}. The condition is
	 * equivalent to {@code this.bit[i] OR NOT interA.bit[i] OR NOT with.bit[i]}
	 * .
	 * 
	 * @param interA
	 *            The first part of the intersection set
	 * @param interB
	 *            The second part of the intersection set
	 * @return true if this bit set is a superset of {@code (interA AND interB)}
	 */
	boolean isSuperSetOfIntersection(IBitSet interA, IBitSet interB);

	/**
	 * The current bit set is logically and-ed with the given bit set:<br>
	 * {@code this.bit[i] = this.bit[i] AND with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically and-ed
	 */
	void and(IBitSet with);

	/**
	 * The current bit set is logically and-ed with the given bit set, and the
	 * cardinality of the result is returned:<br>
	 * {@code result = |this.bit[i] AND with.bit[i]|}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically and-ed
	 * @return the cardinality of the result
	 */
	int getAndCardinality(IBitSet with);

	/**
	 * The current bit set is logically and-ed with the given bit set, and the
	 * result is returned as a new bit set instance:<br>
	 * {@code result.bit[i] = this.bit[i] AND with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically and-ed
	 * @return the resulting bit set
	 */
	IBitSet getAnd(IBitSet with);

	/**
	 * The current bit set is logically and-ed with the complement of the given
	 * bit set:<br>
	 * {@code this.bit[i] = this.bit[i] AND (NOT with.bit[i])}
	 * 
	 * @param with
	 *            The bit set with which's complement this bit set is logically
	 *            and-ed
	 */
	void andNot(IBitSet with);

	/**
	 * The current bit set is logically and-ed with the complement of the given
	 * bit set, and the result is returned as a new bit set instance:<br>
	 * {@code result.bit[i] = this.bit[i] AND (NOT with.bit[i])}
	 * 
	 * @param with
	 *            The bit set with which's complement this bit set is logically
	 *            and-ed
	 * @return the resulting bit set
	 */
	IBitSet getAndNot(IBitSet with);

	/**
	 * The current bit set is logically or-ed with the given bit set:<br>
	 * {@code this.bit[i] = this.bit[i] OR with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically or-ed
	 */
	void or(IBitSet with);

	/**
	 * The current bit set is logically or-ed with the given bit set, and the
	 * result is returned as a new bit set instance:<br>
	 * {@code result.bit[i] = this.bit[i] OR with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically or-ed
	 * @return the resulting bit set
	 */
	IBitSet getOr(IBitSet with);

	/**
	 * The current bit set is logically xor-ed with the given bit set:<br>
	 * {@code this.bit[i] = this.bit[i] XOR with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically xor-ed
	 */
	void xor(IBitSet with);

	/**
	 * The current bit set is logically xor-ed with the given bit set, and the
	 * result is returned as a new bit set instance:<br>
	 * {@code result.bit[i] = this.bit[i] XOR with.bit[i]}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically xor-ed
	 * @return the resulting bit set
	 */
	IBitSet getXor(IBitSet with);

	/**
	 * The current bit set is logically xor-ed with the given bit set, and the
	 * cardinality of the result is returned:<br>
	 * {@code result = |this.bit[i] XOR with.bit[i]|}
	 * 
	 * @param with
	 *            The bit set with which this bit set is logically xor-ed
	 * @return the cardinality of the result
	 */
	int getXorCardinality(IBitSet with);

	/**
	 * Returns <code>index + 1</code>, with the index of the highest bit set to
	 * true.
	 * 
	 * @return <code>index + 1</code> of the highest true bit
	 */
	int length();

	/**
	 * Returns the number of true bits in this bit set
	 * 
	 * @return the number of set bits
	 */
	int cardinality();

	/**
	 * Returns the number of true bits in this bit set, starting from
	 * {@code fromBit} (inclusive), ending at {@code toBit} (exclusive).
	 * 
	 * @param fromBit
	 *            the start bit, inclusive
	 * @param toBit
	 *            the end bit, exclusive
	 * 
	 * @return the number of set bits
	 */
	int cardinality(int fromBit, int toBit);

	/**
	 * Returns the index of the first bit that is set to <code>true</code> that
	 * occurs on or after the specified starting index. If no such bit exists
	 * then -1 is returned.
	 *
	 * To iterate over the <code>true</code> bits in a <code>BitSet</code>, use
	 * the following loop:
	 * 
	 * <pre>
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
	 * 	// operate on index i here
	 * }
	 * </pre>
	 * 
	 * @param from
	 *            the index to start checking from (inclusive).
	 * @return the index of the next set bit.
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	int nextSetBit(int from);

	/**
	 * Returns the index of the first bit that is set to <code>false</code> that
	 * occurs on or after the specified starting index.
	 * 
	 * @param from
	 *            the index to start checking from (inclusive).
	 * @return the index of the next clear bit.
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	int nextClearBit(int from);

	/**
	 * Returns a clone of this bit set
	 * 
	 * @return A cloned (new) instance of this bit set
	 */
	IBitSet clone();

	/**
	 * Converts this bit set to a Java bit set and returns it
	 * 
	 * @return a java bit set representing the same bits as this bit set
	 */
	BitSet toBitSet();

	/**
	 * Returns the factory for this bit set implementation.
	 * 
	 * @return the factory for this bit set implementation
	 */
	BitSetFactory factory();

	// /**
	// * Returns the hash code as it would be returned by {@link
	// Object#hashCode()}. Useful
	// * if equality for bit sets should only return true for same instance.
	// *
	// * @return hash code as it would be returned by {@link Object#hashCode()}
	// */
	// int hashCodeObj();
	//

	boolean isEmpty();
}
