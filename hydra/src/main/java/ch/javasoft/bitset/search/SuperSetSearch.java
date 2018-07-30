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
package ch.javasoft.bitset.search;

import ch.javasoft.bitset.IBitSet;

/**
 * The <code>SuperSetSearch</code> data structure allows fast searching for a
 * superset in a given set of bit sets.
 */
public interface SuperSetSearch {

	/**
	 * Finds a superset of the specified set and returns it, if one is found. If
	 * no superset is found, {@code null} is returned.
	 * <p>
	 * Another bitset is a superset of the specified set if it contains all its
	 * {@code true} bits and possibly additional ones.
	 *
	 * @param of
	 *            the set for which a superset is to be searched
	 * @return the superset, if one is found, and {@code null} otherwise
	 */
	IBitSet findSuperSet(IBitSet of);

	IBitSet findSuperSet(IBitSet of, int without);

	/**
	 * Finds a superset of the specified set and returns it, if one is found. If
	 * no superset is found, {@code null} is returned. Only sets occuring after
	 * the set {@code after} are considered, other sets are ignored.
	 * <p>
	 * This method allows the enumeration of all supersets of a given set. The
	 * enumeration is started with {@link #findSuperSet(IBitSet)}. Then, the
	 * found set is passed to {@link #findSuperSet(IBitSet, IBitSet)} as second
	 * parameter to retrieve the next set in the enumeration. A for-loop
	 * statement looks as follows:
	 * 
	 * <pre>
	 * IBitSet of=...
	 * SuperSetSearch search = ...;
	 * for (IBitSet s=search.findSuperSet(of);s!= null;s=search.findSuperSet(of,s)) {
	 *     ...
	 * }
	 * </pre>
	 * <p>
	 * Another bitset is a superset of the specified set if it contains all its
	 * {@code true} bits and possibly additional ones.
	 *
	 * @param of
	 *            the set for which a superset is to be searched
	 * @return the superset, if one is found, and {@code null} otherwise
	 */
	IBitSet findSuperSet(IBitSet of, IBitSet after);

}
