package de.metanome.algorithms.dcfinder.evidenceset;

import java.util.Iterator;
import java.util.Set;

import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class TroveEvidenceSet implements IEvidenceSet {

	private TObjectLongMap<PredicateSet> sets = new TObjectLongHashMap<>();

	@Override
	public boolean add(PredicateSet predicateSet) {
		return this.add(predicateSet, 1);
	}

	@Override
	public boolean add(PredicateSet create, long count) {
		return sets.adjustOrPutValue(create, count, count) == count;
	}

	@Override
	public long getCount(PredicateSet predicateSet) {
		return sets.get(predicateSet);
	}

	public void incrementCount(PredicateSet predicateSet, long newEvidences) {

		sets.adjustOrPutValue(predicateSet, newEvidences, 0);

	}

	@Override
	public boolean adjustCount(PredicateSet predicateSet, long amount) {
		return sets.adjustValue(predicateSet, amount);
	}

	@Override
	public Iterator<PredicateSet> iterator() {
		return sets.keySet().iterator();
	}

	@Override
	public Set<PredicateSet> getSetOfPredicateSets() {
		return sets.keySet();
	}

	@Override
	public int size() {
		return sets.size();
	}

	@Override
	public boolean isEmpty() {
		return sets.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sets == null) ? 0 : sets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TroveEvidenceSet other = (TroveEvidenceSet) obj;
		if (sets == null) {
			if (other.sets != null)
				return false;
		} else if (!sets.equals(other.sets))
			return false;
		return true;
	}

}
