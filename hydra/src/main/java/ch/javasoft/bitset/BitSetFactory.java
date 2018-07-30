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
 * The <code>BitSetFactory</code> class offers generic instantiation methods for
 * different {@link IBitSet} implementations.
 */
public interface BitSetFactory {
	/**
	 * Creates an empty bit set with default initial capacity. All bits are
	 * initially false.
	 */
	IBitSet create();

	/**
	 * Creates an empty bit set with the specified initial capacity. All bits
	 * are initially false.
	 */
	IBitSet create(int capacity);

	/**
	 * Creates a bit set using the specified bits to initialize the new
	 * instance. The bits are copied even if the specified bit set is an
	 * instance of the same class as the instances returned by this factory.
	 * 
	 * @param bits
	 *            the bit set used which's bits are used to initialize the
	 *            returned bit set
	 */
	IBitSet create(IBitSet bits);

	/**
	 * Creates a bit set using the specified bits to initialize the new
	 * instance.
	 * 
	 * @param bits
	 *            the bit set used which's bits are used to initialize the
	 *            returned bit set
	 */
	IBitSet create(BitSet bits);

	/**
	 * If the given bit set is an instance of the same class as the instances
	 * returned by this factory, it is simply returned. Otherwise, a new bit set
	 * instance is created using the specified bits to initialize the new
	 * instance.
	 * 
	 * @param bitSet
	 *            the bit set to convert if necessary
	 * @return the {@code bitSet}, if conversion is not necessary, and a new bit
	 *         set instance with the same true and false bits otherwise
	 */
	IBitSet convert(IBitSet bitSet);

	/**
	 * Returns the bit set class instantiated by this factory
	 */
	Class<? extends IBitSet> getBitSetClass();
}
