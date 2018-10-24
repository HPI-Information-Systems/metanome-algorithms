package de.hpi.naumann.dc.paritions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.hpi.naumann.dc.predicates.PartitionRefiner;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicatePair;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;

public class StrippedPartition {
	private static final int FULL_CHECK_LINE_LIMIT = 300;

	private final Collection<ClusterPair> clustersPairs;

	protected StrippedPartition(Collection<ClusterPair> collection) {
		this.clustersPairs = Collections.unmodifiableCollection(collection);
	}

	public static ClusterPair getFullParition(int lineCount) {
		Cluster c = new Cluster();
		for (int i = 0; i < lineCount; ++i) {
			c.add(i);
		}
		ClusterPair pair = new ClusterPair(c);
		return pair;
	}

	public static boolean isSingleSupported(Predicate p) {
		if (p.getOperand1() != null && p.getOperand2() != null) {
			ColumnOperand<?> o1 = p.getOperand1();
			ColumnOperand<?> o2 = p.getOperand2();
			if (o1.getIndex() == o2.getIndex())
				return true;

			if (p.getOperator() == Operator.EQUAL || p.getOperator() == Operator.UNEQUAL)
				return true;
		}

		return false;
	}

	public static boolean isPairSupported(Predicate p) {
		if (p.getOperand1() != null && p.getOperand2() != null) {
			ColumnOperand<?> o1 = p.getOperand1();
			ColumnOperand<?> o2 = p.getOperand2();
			if (o1.getIndex() != o2.getIndex() && p.getOperator() != Operator.EQUAL
					&& p.getOperator() != Operator.UNEQUAL)
				return true;
		}

		return false;
	}

	public Iterable<LinePair> getLinePairIterator() {
		return new Iterable<LinePair>() {

			@Override
			public Iterator<LinePair> iterator() {
				return new Iterator<LinePair>() {
					Iterator<ClusterPair> cIter = clustersPairs.iterator();
					Iterator<LinePair> currentC = null;

					@Override
					public boolean hasNext() {
						if (currentC != null && currentC.hasNext()) {
							return true;
						}
						if (cIter != null && cIter.hasNext()) {
							currentC = cIter.next().getLinePairIterator();
							return hasNext();
						}
						return false;
					}

					@Override
					public LinePair next() {
						if (currentC != null && currentC.hasNext())
							return currentC.next();
						if (cIter != null && cIter.hasNext()) {
							currentC = cIter.next().getLinePairIterator();
							return next();
						}
						return null;
					}
				};
			}
		};
	}

	public StrippedPartition refine(PartitionRefiner refiner, IEJoin iejoin) {
		if (refiner instanceof Predicate) {
			return refinePs((Predicate) refiner, iejoin);
		} else if (refiner instanceof PredicatePair) {
			return refinePP((PredicatePair) refiner, iejoin);
		}
		return null;
	}

	private StrippedPartition refinePs(Predicate p, IEJoin join) {
		Collection<ClusterPair> newClusters = new ArrayList<>();
		boolean didRefine = false;
		if (p.getOperand1() != null && p.getOperand2() != null) {
			ColumnOperand<?> o1 = p.getOperand1();
			ColumnOperand<?> o2 = p.getOperand2();
			if (o1.getIndex() == o2.getIndex()) {
				// SIMPLE FILTER
				for (ClusterPair clusterPair : clustersPairs) {
					didRefine |= clusterPair.filter(p, o1.getIndex(), newClusters);
				}
			} else if (p.getOperator() == Operator.EQUAL) {
				// EQUI JOIN
				for (ClusterPair clusterPair : clustersPairs) {
					didRefine |= clusterPair.equiJoin(o1.getColumn(), o2.getColumn(), join.values, newClusters);
				}
			} else if (p.getOperator() == Operator.UNEQUAL) {
				// ANTI JOIN
				for (ClusterPair clusterPair : clustersPairs) {
					didRefine |= clusterPair.antiJoin(o1.getColumn(), o2.getColumn(), newClusters);
				}
			} else {
				for (ClusterPair clusterPair : clustersPairs)
					newClusters.addAll(join.calc(clusterPair, p));
				didRefine = true;
			}
		}

		StrippedPartition result = this;
		if (didRefine) {
			result = new StrippedPartition(newClusters);
		}

		return result;
	}

	private StrippedPartition refinePP(PredicatePair predicate, IEJoin join) {
		Collection<ClusterPair> newClusters = new ArrayList<>();
		List<Predicate> pList = new ArrayList<Predicate>();
		pList.add(predicate.getP1());
		pList.add(predicate.getP2());
		for (ClusterPair clusterPair : clustersPairs) {
			if (clusterPair.getLinePairCount() < FULL_CHECK_LINE_LIMIT)
				clusterPair.fullCheck(pList, newClusters);
			else
				newClusters.addAll(join.calc(clusterPair, predicate.getP1(), predicate.getP2()));
		}
		return new StrippedPartition(newClusters);
	}

	public long getLinePairCount() {
		long size = 0;
		for (ClusterPair c : clustersPairs) {
			size += c.getLinePairCount();
		}
		return size;
	}

	public int clusterCount() {
		return clustersPairs.size();
	}

	public boolean isEmpty() {
		return clustersPairs.size() == 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + getLinePairCount());
		result = prime * result + clusterCount();
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
		StrippedPartition other = (StrippedPartition) obj;
		if (getLinePairCount() != other.getLinePairCount() || clusterCount() != clusterCount())
			return false;

		// check clusters match
		return false;
	}

	public StrippedPartition refineFull(List<PartitionRefiner> delayed, IEJoin iejoin) {
		List<Predicate> ps = new ArrayList<Predicate>();
		for (PartitionRefiner refiner : delayed) {
			if (refiner instanceof Predicate)
				ps.add((Predicate) refiner);
			else if (refiner instanceof PredicatePair) {
				ps.add(((PredicatePair) refiner).getP1());
				ps.add(((PredicatePair) refiner).getP2());
			}
		}
		Collection<ClusterPair> newClusters = new ArrayList<>();
		boolean didRefine = false;
		for (ClusterPair clusterPair : clustersPairs) {
			didRefine |= clusterPair.fullCheck(ps, newClusters);
		}
		StrippedPartition result = this;
		if (didRefine) {
			result = new StrippedPartition(newClusters);
		}
		return result;
	}


}
