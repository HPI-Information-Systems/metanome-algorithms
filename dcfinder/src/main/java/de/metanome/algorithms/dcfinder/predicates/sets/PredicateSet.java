package de.metanome.algorithms.dcfinder.predicates.sets;

import java.util.Iterator;

import ch.javasoft.bitset.BitSetFactory;
import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet.LongBitSetFactory;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.predicates.Predicate;

public class PredicateSet implements Iterable<Predicate> {

	private IBitSet bitset;

	public PredicateSet() {
		this.bitset = bf.create();
	}

	public PredicateSet(int capacity) {
		this.bitset = bf.create(capacity);
	}

	public PredicateSet(IBitSet bitset) {
		this.bitset = bitset.clone();
	}

	public PredicateSet convert() {
		PredicateSet converted = PredicateSetFactory.create();
		for (int l = bitset.nextSetBit(0); l >= 0; l = bitset.nextSetBit(l + 1)) {
			converted.add(indexProvider.getObject(l));
		}
		return converted;
	}

	public PredicateSet(PredicateSet pS) {
		this.bitset = pS.getBitset().clone();
	}

	public PredicateSet(Predicate p) {
		this.bitset = getBitSet(p);
	}

	public void remove(Predicate predicate) {
		this.bitset.clear(indexProvider.getIndex(predicate).intValue());
	}

	public boolean containsPredicate(Predicate predicate) {
		return this.bitset.get(indexProvider.getIndex(predicate).intValue());
	}

	public boolean isSubsetOf(PredicateSet superset) {
		return this.bitset.isSubSetOf(superset.getBitset());
	}

	public IBitSet getBitset() {
		return bitset;
	}

	public void addAll(PredicateSet PredicateBitSet) {
		this.bitset.or(PredicateBitSet.getBitset());
	}

	public int size() {
		return this.bitset.cardinality();
	}

	public boolean add(Predicate predicate) {
		int index = getIndex(predicate);
		boolean newAdded = !bitset.get(index);
		this.bitset.set(index);
		return newAdded;
	}

	@Override
	public Iterator<Predicate> iterator() {
		return new Iterator<Predicate>() {
			private int currentIndex = bitset.nextSetBit(0);

			@Override
			public Predicate next() {
				int lastIndex = currentIndex;
				currentIndex = bitset.nextSetBit(currentIndex + 1);
				return indexProvider.getObject(lastIndex);
			}

			@Override
			public boolean hasNext() {
				return currentIndex >= 0;
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredicateSet other = (PredicateSet) obj;
		if (bitset == null) {
			if (other.bitset != null)
				return false;
		} else if (!bitset.equals(other.bitset))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitset == null) ? 0 : bitset.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.forEach(p -> sb.append(p + " "));
		// this.forEach(p -> sb.append(p.getOperand1().getColumn().getName() + "" +
		// p.getOperator().getShortString() + " "));
		return sb.toString();
	}

	public static IndexProvider<Predicate> indexProvider = new IndexProvider<>();
	private static BitSetFactory bf = new LongBitSetFactory();

	static public Predicate getPredicate(int index) {
		return indexProvider.getObject(index);
	}

	static public IBitSet getBitSet(Predicate p) {
		int index = indexProvider.getIndex(p).intValue();
		IBitSet bitset = bf.create();
		bitset.set(index);
		return bitset;
	}

	public static int getIndex(Predicate add) {
		return indexProvider.getIndex(add).intValue();
	}

}
