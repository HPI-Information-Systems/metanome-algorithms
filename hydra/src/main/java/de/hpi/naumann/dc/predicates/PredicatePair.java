package de.hpi.naumann.dc.predicates;

import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;

public class PredicatePair implements PartitionRefiner {

	private Predicate p1;
	private Predicate p2;

	public PredicatePair(Predicate p1, Predicate p2) {
		super();
		this.p1 = p1;
		this.p2 = p2;
	}

	public Predicate getP1() {
		return p1;
	}

	public Predicate getP2() {
		return p2;
	}

	public boolean bothContainedIn(PredicateBitSet ps) {
		return ps.containsPredicate(p1) && ps.containsPredicate(p2);
	}

	@Override
	public boolean satisfies(int line1, int line2) {
		return p1.satisfies(line1, line2) && p2.satisfies(line1, line2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
		result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
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
		PredicatePair other = (PredicatePair) obj;
		if (p1 == null) {
			if (other.p1 != null)
				return false;
		} else if (!p1.equals(other.p1))
			return false;
		if (p2 == null) {
			if (other.p2 != null)
				return false;
		} else if (!p2.equals(other.p2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PredicatePair [p1=" + p1 + ", p2=" + p2 + "]";
	}

}
