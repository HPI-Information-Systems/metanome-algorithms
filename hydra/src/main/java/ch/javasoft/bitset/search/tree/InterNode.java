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
 * The <code>InterNode</code> contains a left and right subtree.
 * 
 */
public class InterNode implements Node {

	private final int bit;
	private final IBitSet union, inter;

	private Node left, right;

	public InterNode(int bit, Node left, Node right, IBitSet union, IBitSet inter) {
		this.bit = bit;
		this.left = left;
		this.right = right;
		this.union = union;
		this.inter = inter;
	}

	public InterNode(int bit, LeafNode left, LeafNode right) {
		this(bit, left, right, left.set.getOr(right.set), left.set.getAnd(right.set));
	}

	public static InterNode create(LeafNode leaf1, LeafNode leaf2, int bit) {
		boolean val1 = leaf1.set.get(bit);
		boolean val2 = leaf2.set.get(bit);
		while (val1 == val2) {
			bit++;
			val1 = leaf1.set.get(bit);
			val2 = leaf2.set.get(bit);
		}
		return new InterNode(bit, val1 ? leaf2 : leaf1, val1 ? leaf1 : leaf2);
	}

	public IBitSet union() {
		return union;
	}

	public IBitSet inter() {
		return inter;
	}

	public Node add(IBitSet set, int bit) {
		while (bit < this.bit) {
			final boolean val = set.get(bit);
			final boolean uni = union.get(bit);
			if (val != uni) {
				final Node left, right;
				left = val ? this : new LeafNode(set);
				right = val ? new LeafNode(set) : this;
				final IBitSet union = this.union.getOr(set);
				final IBitSet inter = this.inter.getAnd(set);
				return new InterNode(bit, left, right, union, inter);
			}
			bit++;
		}
		assert (bit == this.bit);
		if (bit != this.bit) {
			throw new RuntimeException("assert: bitIndex == this.bitIndex");
		}
		if (set.get(bit)) {
			final Node node = right.add(set, bit + 1);
			if (node != null) {
				right = node;
				union.or(set);
				inter.and(set);
				return this;
			}
			return null;
		}
		final Node node = left.add(set, bit + 1);
		if (node != null) {
			left = node;
			union.or(set);
			inter.and(set);
			return this;
		}
		return null;
	}

	public Node remove(IBitSet set) {
		if (set.isSubSetOf(union) && inter.isSubSetOf(set)) {
			Node node;
			node = left.remove(set);
			if (node != null) {
				if (Node.EMPTY.equals(node)) {
					return right;
				}
				final IBitSet union = node.union().getOr(right.union());
				final IBitSet inter = node.inter().getAnd(right.inter());
				return new InterNode(bit, node, right, union, inter);
			}
			node = right.remove(set);
			if (node != null) {
				if (Node.EMPTY.equals(node)) {
					return left;
				}
				final IBitSet union = node.union().getOr(left.union());
				final IBitSet inter = node.inter().getAnd(left.inter());
				return new InterNode(bit, left, node, union, inter);
			}
		}
		return null;
	}

	public IBitSet findSuperSet(IBitSet of) {
		if (of.isSubSetOf(union)) {
			final IBitSet set;
			set = left.findSuperSet(of);
			return set == null ? right.findSuperSet(of) : set;
		}
		return null;
	}

	public IBitSet findSuperSet(IBitSet of, int without) {
		if (of.isSubSetOf(union) && !inter.get(without)) {
			final IBitSet set;
			set = left.findSuperSet(of, without);
			return set == null ? right.findSuperSet(of, without) : set;
		}
		return null;
	}

	public IBitSet findSuperSet(IBitSet of, IBitSet after) {
		if (of.isSubSetOf(union) && union.compareTo(after) > 0) {
			final IBitSet set;
			set = left.findSuperSet(of, after);
			return set == null ? right.findSuperSet(of, after) : set;
		}
		return null;
	}

	public IBitSet findSubSet(IBitSet of) {
		if (inter.isSubSetOf(of)) {
			final IBitSet set;
			set = left.findSubSet(of);
			return set == null ? right.findSubSet(of) : set;
		}
		return null;
	}

	public IBitSet findSubSet(IBitSet of, IBitSet after) {
		if (inter.isSubSetOf(of) && union.compareTo(after) > 0) {
			final IBitSet set;
			set = left.findSubSet(of, after);
			return set == null ? right.findSubSet(of, after) : set;
		}
		return null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append('{');
		sb.append(bit);
		sb.append(", U=").append(union);
		sb.append(", I=").append(inter);
		sb.append(", L=").append(left);
		sb.append(", R=").append(right);
		return sb.append('}').toString();
	}

	@Override
	public void each(Consumer<IBitSet> consumer) {
		right.each(consumer);
		left.each(consumer);
	}

	@Override
	public void eachSubSet(IBitSet of, Consumer<IBitSet> consumer) {
		if (inter.isSubSetOf(of)) {
			left.eachSubSet(of, consumer);
			right.eachSubSet(of, consumer);
		}
	}

	@Override
	public void eachSuperSet(IBitSet of, Consumer<IBitSet> consumer) {
		if (of.isSubSetOf(union)) {
			left.eachSuperSet(of, consumer);
			right.eachSuperSet(of, consumer);
		}
	}
}
