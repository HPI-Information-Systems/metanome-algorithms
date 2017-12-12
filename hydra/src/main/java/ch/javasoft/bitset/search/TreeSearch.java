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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.tree.Node;

/**
 * The <code>TreeSearch</code> constructs a bit pattern tree to find a subset or
 * a subset.
 */
public class TreeSearch implements SuperSetSearch, SubSetSearch, ITreeSearch {

	private Node root = Node.EMPTY;

	/**
	 * Adds the given set to the structure, or returns false if such a set is
	 * already contained within this search set
	 * 
	 * @param set
	 *            the set to add
	 * @return true if it has been added, and false if it was already contained
	 *         in the structure
	 */
	public boolean add(IBitSet set) {
		final Node newRoot = root.add(set, 0);
		if (newRoot != null) {
			root = newRoot;
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified set from the structure and returns true if such as
	 * set was found and removed. False indicates that no set was found and the
	 * structure was not modified.
	 * 
	 * @param set
	 *            the set to remove
	 * @return true if the structure was modified.
	 */
	public void remove(IBitSet set) {
		final Node newRoot = root.remove(set);
		if (newRoot != null) {
			root = newRoot;
//			return true;
		}
//		return false;
	}

	public IBitSet findSuperSet(IBitSet of) {
		return root.findSuperSet(of);
	}

	public IBitSet findSuperSet(IBitSet of, int without) {
		if (of.get(without))
			return null;

		return root.findSuperSet(of, without);
	}

	public IBitSet findSuperSet(IBitSet of, IBitSet after) {
		return root.findSuperSet(of, after);
	}

	public IBitSet findSubSet(IBitSet of) {
		return root.findSubSet(of);
	}

	public IBitSet findSubSet(IBitSet of, IBitSet after) {
		return root.findSubSet(of, after);
	}

	public void forEach(Consumer<IBitSet> consumer) {
		root.each(consumer);
	}

	public void forEachSubSet(IBitSet of, Consumer<IBitSet> consumer) {
		root.eachSubSet(of, consumer);
	}

	public void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		root.eachSuperSet(bitset, consumer);
	}

	public boolean containsSubset(IBitSet add) {
		return root.findSubSet(add) != null;
	}

	@Override
	public Collection<IBitSet> getAndRemoveGeneralizations(IBitSet bitset) {
		List<IBitSet> result = new ArrayList<IBitSet>();
		forEachSubSet(bitset, b -> result.add(b));
		return result;
	}
}
