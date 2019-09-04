package de.metanome.algorithms.dcfinder.evidenceset;

import java.util.Iterator;
import java.util.Set;

import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

public interface IEvidenceSet extends Iterable<PredicateSet> {

	boolean add(PredicateSet predicateSet);

	boolean add(PredicateSet create, long count);

	long getCount(PredicateSet predicateSet);
	
	boolean adjustCount(PredicateSet predicateSet, long amount ) ;

	Iterator<PredicateSet> iterator();

	Set<PredicateSet> getSetOfPredicateSets();

	int size();

	boolean isEmpty();

}