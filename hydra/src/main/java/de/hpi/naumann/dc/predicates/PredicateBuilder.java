package de.hpi.naumann.dc.predicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;

public class PredicateBuilder {

	private double COMPARE_AVG_RATIO = 0.1d;

	private double minimumSharedValue = 0.15d;
	
	private boolean noCrossColumn = false;

	private Set<Predicate> predicates;

	private Collection<Collection<Predicate>> predicateGroups;

	public PredicateBuilder(Input input, boolean noCrossColumn, double minimumSharedValue) {
		predicates = new HashSet<>();
		predicateGroups = new ArrayList<>();
		this.noCrossColumn = noCrossColumn;
		this.minimumSharedValue = minimumSharedValue;

		constructColumnPairs(input).forEach(pair -> {
			ColumnOperand<?> o1 = new ColumnOperand<>(pair.getC1(), 0);
			addPredicates(o1, new ColumnOperand<>(pair.getC2(), 1), pair.isJoinable(), pair.isComparable());
			if (pair.getC1() != pair.getC2()) {
				addPredicates(o1, new ColumnOperand<>(pair.getC2(), 0), pair.isJoinable(), false);
			}
		});
	}

	private ArrayList<ColumnPair> constructColumnPairs(Input input) {
		ArrayList<ColumnPair> pairs = new ArrayList<ColumnPair>();
		for (int i = 0; i < input.getColumns().length; ++i) {
			ParsedColumn<?> c1 = input.getColumns()[i];
			for (int j = i; j < input.getColumns().length; ++j) {
				ParsedColumn<?> c2 = input.getColumns()[j];
				boolean joinable = isJoinable(c1, c2);
				boolean comparable = isComparable(c1, c2);
				if (joinable || comparable)
					pairs.add(new ColumnPair(c1, c2, joinable, comparable));
			}
		}
		return pairs;
	}

	private boolean isJoinable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2);

		if (!c1.getType().equals(c2.getType()))
			return false;

		return c1.getSharedPercentage(c2) > minimumSharedValue;
	}

	private boolean isComparable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2) && (c1.getType().equals(Double.class) || c1.getType().equals(Long.class));

		if (!c1.getType().equals(c2.getType()))
			return false;

		if (c1.getType().equals(Double.class) || c1.getType().equals(Long.class)) {
			if (c1.equals(c2))
				return true;

			double avg1 = c1.getAverage();
			double avg2 = c2.getAverage();
			return Math.min(avg1, avg2) / Math.max(avg1, avg2) > COMPARE_AVG_RATIO;
		}
		return false;
	}

	public Set<Predicate> getPredicates() {
		return predicates;
	}

	public Collection<Collection<Predicate>> getPredicateGroups() {
		return predicateGroups;
	}

	public Collection<ColumnPair> getColumnPairs() {
		Set<List<ParsedColumn<?>>> joinable = new HashSet<>();
		Set<List<ParsedColumn<?>>> comparable = new HashSet<>();
		Set<List<ParsedColumn<?>>> all = new HashSet<>();
		for (Predicate p : predicates) {
			List<ParsedColumn<?>> pair = new ArrayList<>();
			pair.add(p.getOperand1().getColumn());
			pair.add(p.getOperand2().getColumn());

			if (p.getOperator() == Operator.EQUAL)
				joinable.add(pair);

			if (p.getOperator() == Operator.LESS)
				comparable.add(pair);

			all.add(pair);
		}

		Set<ColumnPair> pairs = new HashSet<>();
		for (List<ParsedColumn<?>> pair : all) {
			pairs.add(new ColumnPair(pair.get(0), pair.get(1), joinable.contains(pair), comparable.contains(pair)));
		}
		return pairs;
	}

	private void addPredicates(ColumnOperand<?> o1, ColumnOperand<?> o2, boolean joinable, boolean comparable) {
		Set<Predicate> predicates = new HashSet<Predicate>();
		for (Operator op : Operator.values()) {
			// EQUAL and UNEQUAL must be joinable, all other comparable
			if (op == Operator.EQUAL || op == Operator.UNEQUAL) {
				if (joinable) {
					predicates.add(predicateProvider.getPredicate(op, o1, o2));
				}
			} else if (comparable) {
				predicates.add(predicateProvider.getPredicate(op, o1, o2));
			}
		}
		this.predicates.addAll(predicates);
		this.predicateGroups.add(predicates);
	}

	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();
}
