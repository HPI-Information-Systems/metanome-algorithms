package de.hpi.naumann.dc.evidenceset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;

public class HashEvidenceSet implements IEvidenceSet {

	private Set<PredicateBitSet> evidences = new HashSet<>();

	@Override
	public boolean add(PredicateBitSet predicateSet) {
		return evidences.add(predicateSet);
	}

	@Override
	public boolean add(PredicateBitSet predicateSet, long count) {
		return evidences.add(predicateSet);
	}

	@Override
	public long getCount(PredicateBitSet predicateSet) {
		return evidences.contains(predicateSet) ? 1 : 0;
	}


	@Override
	public Iterator<PredicateBitSet> iterator() {
		return evidences.iterator();
	}

	@Override
	public Set<PredicateBitSet> getSetOfPredicateSets() {
		return evidences;
	}

	@Override
	public int size() {
		return evidences.size();
	}

	@Override
	public boolean isEmpty() {
		return evidences.isEmpty();
	}

}
