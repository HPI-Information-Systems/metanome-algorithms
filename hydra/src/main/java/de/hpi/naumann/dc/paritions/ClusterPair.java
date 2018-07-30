package de.hpi.naumann.dc.paritions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.PartitionRefiner;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicatePair;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

public class ClusterPair {
	private Cluster c1;

	private Cluster c2;

	public ClusterPair(Cluster c) {
		this(c, c);
	}

	public ClusterPair(Cluster c1, Cluster c2) {
		this.c1 = c1;
		this.c2 = c2;
	}

	public Cluster getC1() {
		return c1;
	}

	public Cluster getC2() {
		return c2;
	}

	public long getLinePairCount() {
		return ((long) c1.size()) * c2.size();
	}

	public Iterator<LinePair> getLinePairIterator() {
		return new Iterator<LinePair>() {
			TIntIterator iter = c1.iterator();
			int currentL1 = -1;
			TIntIterator iter2 = null;

			@Override
			public boolean hasNext() {
				if (iter2 != null && iter2.hasNext())
					return true;
				if (iter.hasNext()) {
					currentL1 = iter.next();
					iter2 = c2.iterator();
					return hasNext();
				}
				return false;
			}

			@Override
			public LinePair next() {
				if (iter2 != null && iter2.hasNext())
					return new LinePair(currentL1, iter2.next());
				if (iter != null && iter.hasNext()) {
					currentL1 = iter.next();
					iter2 = c2.iterator();
					return next();
				}
				return null;
			}
		};
	}

	public boolean filter(Predicate p, int index, Collection<ClusterPair> newClusters) {
		filter(p, index, pair -> newClusters.add(pair));
		return true;
	}

	public boolean fullCheck(List<Predicate> p, Consumer<ClusterPair> consumer) {
		TIntIterator iter = c1.iterator();
		while (iter.hasNext()) {
			int line1 = iter.next();
			Cluster newC = new Cluster();

			TIntIterator iter2 = c2.iterator();
			while (iter2.hasNext()) {
				int line2 = iter2.next();
				if (line1 != line2 && p.stream().allMatch(pr -> pr.satisfies(line1, line2))) {
					newC.add(line2);
				}
			}
			ClusterPair newPair = new ClusterPair(new Cluster(line1), newC);
			if (newPair.containsLinePair())
				consumer.accept(newPair);
		}

		return true;
	}

	public boolean fullCheck(List<Predicate> p, Collection<ClusterPair> newClusters) {
		TIntIterator iter = c1.iterator();
		while (iter.hasNext()) {
			int line1 = iter.next();
			Cluster newC = new Cluster();

			TIntIterator iter2 = c2.iterator();
			while (iter2.hasNext()) {
				int line2 = iter2.next();
				if (line1 != line2 && p.stream().allMatch(pr -> pr.satisfies(line1, line2))) {
					newC.add(line2);
				}
			}
			ClusterPair newPair = new ClusterPair(new Cluster(line1), newC);
			if (newPair.containsLinePair())
				newClusters.add(newPair);
		}

		return true;
	}

	public boolean fullCheck(Predicate p, Collection<ClusterPair> newClusters) {
		// System.out.println("full check " + getLinePairCount());
		List<Predicate> ps = new ArrayList<Predicate>();
		ps.add(p);
		return fullCheck(ps, newClusters);
	}

	public boolean equiJoin(ParsedColumn<?> column, ParsedColumn<?> column2, int[][] values,
			Collection<ClusterPair> newClusters) {
		equiJoin(column, column2, values, pair -> newClusters.add(pair));

		return true;
	}

	public boolean containsLinePair() {
		if (c1.size() < 1 || c2.size() < 1 || c1.size() == 1 && c2.size() == 1 && c1.equals(c2))
			return false;
		return true;
	}

	public boolean antiJoin(ParsedColumn<?> column, ParsedColumn<?> column2, Collection<ClusterPair> newClusters) {
		antiJoin(column, column2, pair -> newClusters.add(pair));

		return true;
	}

	public void refine(PartitionRefiner refiner, IEJoin iejoin, Consumer<ClusterPair> consumer) {
		if (refiner instanceof Predicate) {
			refinePs((Predicate) refiner, iejoin, consumer);
		} else if (refiner instanceof PredicatePair) {
			refinePP((PredicatePair) refiner, iejoin, consumer);
		}
	}


	private void refinePP(PredicatePair predicate, IEJoin iejoin, Consumer<ClusterPair> consumer) {


		if (getLinePairCount() > 100) {
			iejoin.calc(this, predicate.getP1(), predicate.getP2(), consumer);
		} else {
			List<Predicate> pList = new ArrayList<Predicate>();
			pList.add(predicate.getP1());
			pList.add(predicate.getP2());
			fullCheck(pList, consumer);
		}
	}

	private void refinePs(Predicate p, IEJoin iejoin, Consumer<ClusterPair> consumer) {
			ColumnOperand<?> o1 = p.getOperand1();
			ColumnOperand<?> o2 =  p.getOperand2();
			if (o1.getIndex() == o2.getIndex()) {
				// SIMPLE FILTER
				// EtmPoint point = etmMonitor.createPoint("FILTER");
				this.filter(p, o1.getIndex(), consumer);
				// point.collect();
			} else if (p.getOperator() == Operator.EQUAL) {
				// EQUI JOIN
				// EtmPoint point = etmMonitor.createPoint("EQUIJOIN");
				this.equiJoin(o1.getColumn(), o2.getColumn(), iejoin.values, consumer);
				// point.collect();
			} else if (p.getOperator() == Operator.UNEQUAL) {
				// ANTI JOIN
				// EtmPoint point = etmMonitor.createPoint("ANTIJOIN");
				this.antiJoin(o1.getColumn(), o2.getColumn(), consumer);
				// point.collect();
			} else {
				// EtmPoint point = etmMonitor.createPoint("IEJOIN SINGLE");
				iejoin.calc(this, p, consumer);
				// point.collect();
			}
	}

	private void antiJoin(ParsedColumn<?> column, ParsedColumn<?> column2, Consumer<ClusterPair> consumer) {
		Map<Object, Cluster> map1 = c1.refineBy(column);
		Map<Object, Cluster> map2 = c2.refineBy(column2);
		if (map1.size() < map2.size()) {
			Cluster noJoin = new Cluster();
			for (Entry<Object, Cluster> entry : map1.entrySet()) {
				Cluster cNot = map2.get(entry.getKey());
				if (cNot == null) {
					noJoin.addAll(entry.getValue());
					continue;
				}
				Cluster c2New = Cluster.minus(c2, cNot);
				if (c2New.equals(c2)) {
					noJoin.addAll(entry.getValue());
					continue;
				}

				if (c1.size() == 1) {
					TIntIterator iter = c1.iterator();
					int element = iter.next();
					c2New.add(element);
					if (c2New.equals(c2)) {
						noJoin.addAll(entry.getValue());
						continue;
					}
				}

				ClusterPair newPair = new ClusterPair(entry.getValue(), c2New);
				if (newPair.containsLinePair())
					consumer.accept(newPair);
			}
			ClusterPair newPair = new ClusterPair(noJoin, c2);
			if (newPair.containsLinePair())
				consumer.accept(newPair);
		} else {
			Cluster noJoin = new Cluster();
			for (Entry<Object, Cluster> entry : map2.entrySet()) {
				Cluster cNot = map1.get(entry.getKey());
				if (cNot == null) {
					noJoin.addAll(entry.getValue());
					continue;
				}
				Cluster c1New = Cluster.minus(c1, cNot);
				if (c1New.equals(c1)) {
					noJoin.addAll(entry.getValue());
					continue;
				}

				if (c2.size() == 1) {
					TIntIterator iter = c2.iterator();
					int element = iter.next();
					c1New.add(element);
					if (c1New.equals(c1)) {
						noJoin.addAll(entry.getValue());
						continue;
					}
				}

				ClusterPair newPair = new ClusterPair(c1New, entry.getValue());
				if (newPair.containsLinePair())
					consumer.accept(newPair);
			}
			ClusterPair newPair = new ClusterPair(c1, noJoin);
			if (newPair.containsLinePair())
				consumer.accept(newPair);
		}
	}

	private void equiJoin(ParsedColumn<?> column, ParsedColumn<?> column2, int[][] values,
			Consumer<ClusterPair> consumer) {
		TIntObjectHashMap<ClusterPair> mapClusterPairs = new TIntObjectHashMap<>();
		if (c1.size() < c2.size()) {
			TIntObjectHashMap<Cluster> map1 = c1.refineBy(column.getIndex(), values);
			TIntIterator iter = c2.iterator();
			int column2Index = column2.getIndex();
			while (iter.hasNext()) {
				int line = iter.next();
				int value = values[line][column2Index];
				if (mapClusterPairs.containsKey(value)) {
					mapClusterPairs.get(value).getC2().add(line);
				} else {
					if (map1.containsKey(value)) {
						ClusterPair pair = new ClusterPair(map1.get(value), new Cluster(line));
						mapClusterPairs.put(value, pair);
					}
				}
			}
		} else {
			TIntObjectHashMap<Cluster> map2 = c2.refineBy(column2.getIndex(), values);
			TIntIterator iter = c1.iterator();
			int column1Index = column.getIndex();
			while (iter.hasNext()) {
				int line = iter.next();
				int value = values[line][column1Index];
				if (mapClusterPairs.containsKey(value)) {
					mapClusterPairs.get(value).getC1().add(line);
				} else {
					if (map2.containsKey(value)) {
						ClusterPair pair = new ClusterPair(new Cluster(line), map2.get(value));
						mapClusterPairs.put(value, pair);
					}
				}
			}
		}

		if (mapClusterPairs.size() == 1) {
			ClusterPair pair = mapClusterPairs.valueCollection().iterator().next();
			if (pair.getC1().size() == c1.size() && pair.getC2().size() == c2.size()) {
				mapClusterPairs = null;
				pair = null;
				consumer.accept(this);
				return;
			}
		}

		mapClusterPairs.forEachValue(new TObjectProcedure<ClusterPair>() {
			@Override
			public boolean execute(ClusterPair object) {
				if (object.containsLinePair())
					consumer.accept(object);
				return true;
			}
		});

	}

	private void filter(Predicate p, int index, Consumer<ClusterPair> consumer) {
		Cluster c = index == 0 ? c1 : c2;
		boolean didRefine = false;
		Cluster newC = new Cluster();

		TIntIterator iter = c.iterator();
		while (iter.hasNext()) {
			int line = iter.next();
			if (p.satisfies(line, line))
				newC.add(line);
			else
				didRefine = true;

		}

		if (didRefine) {
			ClusterPair newPair = index == 0 ? new ClusterPair(newC, c2) : new ClusterPair(c1, newC);

			if (newPair.containsLinePair())
				consumer.accept(newPair);
		} else {
			newC = null;
			consumer.accept(this);
		}
	}

}
