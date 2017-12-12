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

/**
 * The <code>LeafNode</code> contains exactly one bit set
 */
public class LeafNode implements Node {

	protected final IBitSet set;

	public LeafNode(IBitSet set) {
		this.set = set;
	}

	public Node add(IBitSet set, int bit) {
		if (this.set.equals(set)) {
			return null;
		}
		return InterNode.create(this, new LeafNode(set), bit);
	}

	public Node remove(IBitSet set) {
		return set.equals(this.set) ? Node.EMPTY : null;
	}

	public IBitSet findSubSet(IBitSet of) {
		return set.isSubSetOf(of) ? set : null;
	}

	public IBitSet findSubSet(IBitSet of, IBitSet after) {
		return set.isSubSetOf(of) && set.compareTo(after) > 0 ? set : null;
	}

	public IBitSet findSuperSet(IBitSet of) {
		return of.isSubSetOf(set) ? set : null;
	}

	public IBitSet findSuperSet(IBitSet of, IBitSet after) {
		return of.isSubSetOf(set) && set.compareTo(after) > 0 ? set : null;
	}

	public IBitSet union() {
		return set;
	}

	public IBitSet inter() {
		return set;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append('{');
		sb.append(set);
		return sb.append('}').toString();
	}

	@Override
	public IBitSet findSuperSet(IBitSet of, int without) {
		return !set.get(without) && of.isSubSetOf(set) ? set : null;
	}

	@Override
	public void each(Consumer<IBitSet> consumer) {
		consumer.accept(set);
	}

	@Override
	public void eachSubSet(IBitSet of, Consumer<IBitSet> consumer) {
		if (set.isSubSetOf(of))
			consumer.accept(set);
	}

	@Override
	public void eachSuperSet(IBitSet of, Consumer<IBitSet> consumer) {
		if (of.isSubSetOf(set))
			consumer.accept(set);
	}
}
