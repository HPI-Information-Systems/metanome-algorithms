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
package ch.javasoft.bitset.search.tree;

import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.SubSetSearch;
import ch.javasoft.bitset.search.SuperSetSearch;

/**
 * The <code>Node</code> interface is implemented by tree nodes of binary bit
 * pattern trees.
 */
public interface Node extends SuperSetSearch, SubSetSearch {
	/**
	 * Adds the given set to the tree and returns the new root node of the tree.
	 * If the tree already contains the specified set, it is not added and
	 * {@code null} is returned.
	 * 
	 * @param set
	 *            the set to add
	 * @param bit
	 *            the bits to be used in the given order for left and right
	 *            subtree separation
	 * @return the new root node of the tree, or {@code null} if the set was
	 *         already contained in the tree
	 */
	Node add(IBitSet set, int bit);

	/**
	 * Removes the specified set from the tree and returns the new root node of
	 * the tree. If the tree did not contain the specified set, the tree is not
	 * changed and {@code null} is returned.
	 * 
	 * @param set
	 *            the set to remove
	 * @return the new root node of the tree, or {@code null} if the set was not
	 *         contained in the tree
	 */
	Node remove(IBitSet set);

	/**
	 * Returns the union set, meaning the union of all sets contained in the
	 * subtree. A null value indicates that the tree is empty.
	 */
	IBitSet union();

	/**
	 * Returns the intersection set, meaning the intersection of all sets
	 * contained in the subtree. A null value indicates that the tree is empty.
	 */
	IBitSet inter();

	/**
	 * An empty node
	 */
	Node EMPTY = new Node() {
		public IBitSet findSubSet(IBitSet of, IBitSet after) {
			return null;
		}

		public IBitSet findSubSet(IBitSet of) {
			return null;
		}

		public IBitSet findSuperSet(IBitSet of, IBitSet after) {
			return null;
		}

		public IBitSet findSuperSet(IBitSet of) {
			return null;
		}

		public Node add(IBitSet set, int bit) {
			return new LeafNode(set);
		}

		public Node remove(IBitSet set) {
			return null;// null indicates that the set was not in the tree
		}

		public IBitSet union() {
			return null;// empty tree
		}

		public IBitSet inter() {
			return null;// empty tree
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(getClass().getSimpleName()).append('{');
			return sb.append('}').toString();
		}

		@Override
		public IBitSet findSuperSet(IBitSet of, int without) {
			return null;
		}

		@Override
		public void each(Consumer<IBitSet> consumer) {
			// nothing to do
		}

		@Override
		public void eachSubSet(IBitSet of, Consumer<IBitSet> consumer) {
			// nothing to do
		}

		@Override
		public void eachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
			// nothing to do
		}
	};

	void each(Consumer<IBitSet> consumer);

	void eachSubSet(IBitSet of, Consumer<IBitSet> consumer);

	void eachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer);
}
