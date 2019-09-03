package de.metanome.algorithms.dcfinder.predicates.sets;

import java.util.Collection;

import ch.javasoft.bitset.IBitSet;
import de.metanome.algorithms.dcfinder.predicates.Predicate;


public class PredicateSetFactory {

	public static PredicateSet create(Predicate... predicates) {
		PredicateSet set = new PredicateSet();
		for (Predicate p : predicates)
			set.add(p);
		return set;
	}
	
	public static PredicateSet create(IBitSet bitset) {
		return new PredicateSet(bitset);
	}

	public static PredicateSet create(PredicateSet pS) {
		return new PredicateSet(pS);
	}
	
	
	public static PredicateSet create(Collection<Predicate> objects) {
		PredicateSet set = new PredicateSet();
		for (Predicate p : objects)
			set.add(p);
		return set;
	}
	
	
}
