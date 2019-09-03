package de.metanome.algorithms.dcfinder.evidenceset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

public class HashEvidenceSet implements IEvidenceSet {

	private Set<PredicateSet> evidences = new HashSet<>();

	@Override
	public boolean add(PredicateSet predicateSet) {
		return evidences.add(predicateSet);
	}

	@Override
	public boolean add(PredicateSet predicateSet, long count) {
		return evidences.add(predicateSet);
	}

	@Override
	public long getCount(PredicateSet predicateSet) {
		return evidences.contains(predicateSet) ? 1 : 0;// do not used
	}

	@Override
	public Iterator<PredicateSet> iterator() {
		return evidences.iterator();
	}

	@Override
	public Set<PredicateSet> getSetOfPredicateSets() {
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

	@Override
	public boolean adjustCount(PredicateSet predicateSet, long amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
