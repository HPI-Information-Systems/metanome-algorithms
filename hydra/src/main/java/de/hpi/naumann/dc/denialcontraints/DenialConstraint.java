package de.hpi.naumann.dc.denialcontraints;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;
import de.metanome.algorithm_integration.PredicateVariable;

public class DenialConstraint {

	private PredicateBitSet predicateSet;

	public DenialConstraint(Predicate... predicates) {
		predicateSet = PredicateSetFactory.create(predicates);
	}

	public DenialConstraint(PredicateBitSet predicateSet) {
		this.predicateSet = predicateSet;
	}

	public boolean isTrivial() {
		return !new Closure(predicateSet).construct();
	}

	public boolean isImpliedBy(NTreeSearch tree) {
		Closure c = new Closure(predicateSet);
		if (!c.construct())
			return true;

		return isImpliedBy(tree, c.getClosure());
	}

	public boolean isImpliedBy(NTreeSearch tree, PredicateBitSet closure) {
		IBitSet subset = tree.getSubset(PredicateSetFactory.create(closure).getBitset());
		if (subset != null) {
			return true;
		}

		DenialConstraint sym = getInvT1T2DC();
		if (sym != null) {
			Closure c = new Closure(sym.getPredicateSet());
			if (!c.construct())
				return true;
			IBitSet subset2 = tree.getSubset(PredicateSetFactory.create(c.getClosure()).getBitset());
			return subset2 != null;
		}

		return false;

	}

	public boolean containsPredicate(Predicate p) {
		return predicateSet.containsPredicate(p) || predicateSet.containsPredicate(p.getSymmetric());
	}

	public DenialConstraint getInvT1T2DC() {
		PredicateBitSet invT1T2 = PredicateSetFactory.create();
		for (Predicate predicate : predicateSet) {
			Predicate sym = predicate.getInvT1T2();
			if (sym == null)
				return null;
			invT1T2.add(sym);
		}
		return new DenialConstraint(invT1T2);
	}

	public PredicateBitSet getPredicateSet() {
		return predicateSet;
	}

	public int getPredicateCount() {
		return predicateSet.size();
	}

	private boolean containedIn(PredicateBitSet otherPS) {
		for (Predicate p : predicateSet) {
			if (!otherPS.containsPredicate(p) && !otherPS.containsPredicate(p.getSymmetric()))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "[DC: " + predicateSet.toString() + "]";
	}

	@Override
	public int hashCode() {
		// final int prime = 31;
		int result1 = 0;
		for (Predicate p : predicateSet) {
			result1 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
		}
		int result2 = 0;
		if (getInvT1T2DC() != null)
			for (Predicate p : getInvT1T2DC().predicateSet) {
				result2 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
			}
		return Math.max(result1, result2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DenialConstraint other = (DenialConstraint) obj;
		if (predicateSet == null) {
			return other.predicateSet == null;
		} else if (predicateSet.size() != other.predicateSet.size()) {
			return false;
		} else {
			PredicateBitSet otherPS = other.predicateSet;
			return containedIn(otherPS) || getInvT1T2DC().containedIn(otherPS)
					|| containedIn(other.getInvT1T2DC().predicateSet);
		}
	}

	public de.metanome.algorithm_integration.results.DenialConstraint toResult() {
		PredicateVariable[] predicates = new PredicateVariable[predicateSet.size()];
		int i = 0;
		for (Predicate p : predicateSet) {
			predicates[i] = new PredicateVariable(p.getOperand1().getColumn().getColumnIdentifier(),
					p.getOperand1().getIndex(), p.getOperator(), p.getOperand2().getColumn().getColumnIdentifier(),
					p.getOperand2().getIndex());
			++i;
		}

		return new de.metanome.algorithm_integration.results.DenialConstraint(predicates);
	}

}
