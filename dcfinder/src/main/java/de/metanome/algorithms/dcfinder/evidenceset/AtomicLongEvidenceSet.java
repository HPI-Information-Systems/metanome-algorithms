package de.metanome.algorithms.dcfinder.evidenceset;

import java.util.Iterator;
import java.util.Set;

import com.google.common.util.concurrent.AtomicLongMap;

import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

public class AtomicLongEvidenceSet implements IEvidenceSet {

	AtomicLongMap<PredicateSet> evidences = AtomicLongMap.create();

	private long sizeEvi = 0;

	@Override
	public boolean add(PredicateSet predicateSet) {
		return evidences.incrementAndGet(predicateSet) == 1;

	}

	@Override
	public boolean add(PredicateSet predicateSet, long count) {
		return evidences.addAndGet(predicateSet, count) == count;

	}

	@Override
	public long getCount(PredicateSet predicateSet) {
		return evidences.get(predicateSet);
	}

	public boolean contains(PredicateSet i) {
		return evidences.containsKey(i);
	}

	public int getSubsetCount(PredicateSet dcPredicates) {
		int error = 0;
		for (PredicateSet p : this) {
			if (dcPredicates.isSubsetOf(p))
				++error;
		}
		return error;
	}

	@Override
	public Iterator<PredicateSet> iterator() {
		return evidences.asMap().keySet().iterator();
	}

	@Override
	public Set<PredicateSet> getSetOfPredicateSets() {
		return evidences.asMap().keySet();
	}

	@Override
	public int size() {
		return evidences.size();
	}

	@Override
	public boolean isEmpty() {
		return evidences.isEmpty();
	}

	public long getTotalCountEvi() {
		if (sizeEvi == 0) {
			sizeEvi = evidences.sum();
		}
		return sizeEvi;
	}

	public AtomicLongMap<PredicateSet> getEvidences() {
		return evidences;
	}

	public void setEvidences(AtomicLongMap<PredicateSet> evidences) {
		this.evidences = evidences;
	}

	@Override
	public boolean adjustCount(PredicateSet predicateSet, long amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
