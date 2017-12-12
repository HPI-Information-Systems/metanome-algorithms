package de.hpi.naumann.dc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.javasoft.bitset.search.NTreeSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;
import de.metanome.algorithm_integration.Operator;
import static org.mockito.Mockito.*;

public class DenialConstraintSetTest {

	@Test
	public void testImplicationTrivial() {

		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);

		DenialConstraintSet dcSet = new DenialConstraintSet();
		dcSet.add(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2), new Predicate(Operator.LESS, o1, o2)));
		dcSet.add(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2)));
		dcSet.minimize();
		assertEquals(1, dcSet.size());
		assertTrue(dcSet.contains(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2))));
	}

	@Test
	public void testImplication() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);

		NTreeSearch tree = new NTreeSearch();
		DenialConstraintSet dcSet = new DenialConstraintSet();
		dcSet.add(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2), new Predicate(Operator.LESS, o2, o3)));
		for (DenialConstraint i : dcSet)
			tree.add(PredicateSetFactory.create(i.getPredicateSet()).getBitset());
		assertFalse(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2)).isImpliedBy(tree));

		dcSet.add(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2)));
		for (DenialConstraint i : dcSet)
			tree.add(PredicateSetFactory.create(i.getPredicateSet()).getBitset());
		assertTrue(new DenialConstraint(new Predicate(Operator.GREATER, o1, o2)).isImpliedBy(tree));
	}

	@Test
	public void testSymmetric() {
		// example 6
		ParsedColumn<String> h = new ParsedColumn<>("table", "h", String.class, 0);
		ParsedColumn<String> l = new ParsedColumn<>("table", "l", String.class, 1);
		ColumnOperand<String> h0 = new ColumnOperand<>(h, 0);
		ColumnOperand<String> l0 = new ColumnOperand<>(l, 0);
		ColumnOperand<String> h1 = new ColumnOperand<>(h, 1);
		ColumnOperand<String> l1 = new ColumnOperand<>(l, 1);

		DenialConstraintSet dcSet = new DenialConstraintSet();
		dcSet.add(new DenialConstraint(new Predicate(Operator.LESS, h0, l0)));

		NTreeSearch tree = new NTreeSearch();
		for (DenialConstraint i : dcSet)
			tree.add(PredicateSetFactory.create(i.getPredicateSet()).getBitset());

		assertTrue(
				new DenialConstraint(new Predicate(Operator.GREATER, h0, h1), new Predicate(Operator.GREATER, l1, h0))
						.isImpliedBy(tree));

	}

	@Test
	public void testImp() {
		ParsedColumn<String> open = new ParsedColumn<>("table", "open", String.class, 0);
		ParsedColumn<String> close = new ParsedColumn<>("table", "close", String.class, 1);

		ColumnOperand<String> open0 = new ColumnOperand<>(open, 0);
		ColumnOperand<String> close0 = new ColumnOperand<>(close, 0);
		ColumnOperand<String> close1 = new ColumnOperand<>(close, 1);

		PredicateBitSet ps = new PredicateBitSet();
		ps.add(new Predicate(Operator.LESS_EQUAL, open0, close1)); // o0 <= c1
		ps.add(new Predicate(Operator.GREATER_EQUAL, close0, close1)); // c0 >= c1
		ps.add(new Predicate(Operator.UNEQUAL, open0, close0)); // o0 != c0

		Closure closure = new Closure(ps);
		assertTrue(closure.construct());
		assertTrue(closure.getClosure().containsPredicate(new Predicate(Operator.LESS, open0, close0)));
	}

	@Test
	public void testImp2() {
		ParsedColumn<String> volume = new ParsedColumn<>("table", "volume", String.class, 0);
		ParsedColumn<String> close = new ParsedColumn<>("table", "close", String.class, 1);
		ParsedColumn<String> high = new ParsedColumn<>("table", "high", String.class, 1);
		ParsedColumn<String> low = new ParsedColumn<>("table", "low", String.class, 1);
		ParsedColumn<String> open = new ParsedColumn<>("table", "open", String.class, 1);

		ColumnOperand<String> volumne0 = new ColumnOperand<>(volume, 0);
		ColumnOperand<String> volume1 = new ColumnOperand<>(volume, 1);
		ColumnOperand<String> high0 = new ColumnOperand<>(high, 0);
		ColumnOperand<String> high1 = new ColumnOperand<>(high, 1);
		ColumnOperand<String> low0 = new ColumnOperand<>(low, 0);
		ColumnOperand<String> low1 = new ColumnOperand<>(low, 1);
		ColumnOperand<String> open0 = new ColumnOperand<>(open, 0);
		ColumnOperand<String> close0 = new ColumnOperand<>(close, 0);

		PredicateBitSet ps = new PredicateBitSet();
		ps.add(new Predicate(Operator.EQUAL, volumne0, volume1));
		ps.add(new Predicate(Operator.GREATER, high0, low1));
		ps.add(new Predicate(Operator.EQUAL, low0, close0));
		ps.add(new Predicate(Operator.LESS, open0, high1));
		ps.add(new Predicate(Operator.EQUAL, open0, low0));
		// [Predicate: t0.SPStock.csv.Volume(Integer) ==
		// t1.SPStock.csv.Volume(Integer)],
		// [Predicate: t0.SPStock.csv.High(Double) >
		// t1.SPStock.csv.Low(Double)],
		// [Predicate: t0.SPStock.csv.Low(Double) ==
		// t0.SPStock.csv.Close(Double)],
		// [Predicate: t0.SPStock.csv.Open(Double) <
		// t1.SPStock.csv.High(Double)]]]
		// [Predicate: t0.SPStock.csv.Open(Double) ==
		// t0.SPStock.csv.Low(Double)],

		assertFalse(new DenialConstraint(ps).isTrivial());
	}
}
