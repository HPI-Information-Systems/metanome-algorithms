package de.hpi.naumann.dc.evidenceset.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.PredicateProvider;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;
import de.metanome.algorithm_integration.Operator;

public abstract class EvidenceSetBuilder {

	protected PredicateBuilder predicates;

	public EvidenceSetBuilder(PredicateBuilder predicates2) {
		this.predicates = predicates2;
	}


	protected PredicateBitSet getStatic(Collection<ColumnPair> pairs, int i) {
		PredicateBitSet set = PredicateSetFactory.create();
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			if (p.getC1().equals(p.getC2()))
				continue;

			PredicateBitSet[] list = map.get(p);
			if (p.isJoinable()) {
				if (equals(i, i, p))
					set.addAll(list[2]);
				else
					set.addAll(list[3]);
			}
			if (p.isComparable()) {
				int compare2 = compare(i, i, p);
				if (compare2 < 0) {
					set.addAll(list[7]);
				} else if (compare2 == 0) {
					set.addAll(list[8]);
				} else {
					set.addAll(list[9]);
				}
			}

		}
		return set;
	}

	protected PredicateBitSet getPredicateSet(PredicateBitSet staticSet, Collection<ColumnPair> pairs, int i, int j) {
		PredicateBitSet set = PredicateSetFactory.create(staticSet);
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			PredicateBitSet[] list = map.get(p);
			if (p.isJoinable()) {
				if (equals(i, j, p))
					set.addAll(list[0]);
				else
					set.addAll(list[1]);
			}
			if (p.isComparable()) {
				int compare = compare(i, j, p);
				if (compare < 0) {
					set.addAll(list[4]);
				} else if (compare == 0) {
					set.addAll(list[5]);
				} else {
					set.addAll(list[6]);
				}

			}

		}
		return set;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int compare(int i, int j, ColumnPair p) {
		return ((Comparable) getValue(i, p.getC1())).compareTo((getValue(j, p.getC2())));
	}

	private boolean equals(int i, int j, ColumnPair p) {
		return getValue(i, p.getC1()) != null && getValue(i, p.getC1()).equals(getValue(j, p.getC2()));
	}

	private Object getValue(int i, ParsedColumn<?> p) {
		return p.getValue(i);
	}

	protected Map<ColumnPair, PredicateBitSet[]> map;

	protected void createSets(Collection<ColumnPair> pairs) {
		map = new HashMap<>();
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			PredicateBitSet[] list = new PredicateBitSet[10];
			for(int i = 0; i < list.length; ++i)
				list[i] = PredicateSetFactory.create();
			map.put(p, list);
			if (p.isJoinable()) {
				addIfValid(p, list[0], Operator.EQUAL, 1);
				addIfValid(p, list[1], Operator.UNEQUAL, 1);
				if (!p.getC1().equals(p.getC2())) {
					addIfValid(p, list[2], Operator.EQUAL, 0);
					addIfValid(p, list[3], Operator.UNEQUAL, 0);
				}
			}
			if (p.isComparable()) {

				addIfValid(p, list[4], Operator.LESS, 1);
				addIfValid(p, list[4], Operator.LESS_EQUAL, 1);
				
				addIfValid(p, list[5], Operator.LESS_EQUAL, 1);
				addIfValid(p, list[5], Operator.GREATER_EQUAL, 1);
				
				addIfValid(p, list[6], Operator.GREATER_EQUAL, 1);
				addIfValid(p, list[6], Operator.GREATER, 1);

				if (!p.getC1().equals(p.getC2())) {
					addIfValid(p, list[7], Operator.LESS, 0);
					addIfValid(p, list[7], Operator.LESS_EQUAL, 0);
					
					addIfValid(p, list[8], Operator.LESS_EQUAL, 0);
					addIfValid(p, list[8], Operator.GREATER_EQUAL, 0);
					
					addIfValid(p, list[9], Operator.GREATER_EQUAL, 0);
					addIfValid(p, list[9], Operator.GREATER, 0);
				}
			}

		}
	}

	private void addIfValid(ColumnPair p, PredicateBitSet list, Operator op, int index2) {
		Predicate pr = predicateProvider.getPredicate(op, new ColumnOperand<>(p.getC1(), 0),
				new ColumnOperand<>(p.getC2(), index2));
		if(predicates.getPredicates().contains(pr))
			list.add(pr);
	}

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EvidenceSetBuilder.class);
	
	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();  
}
