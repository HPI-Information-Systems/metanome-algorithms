package de.hpi.naumann.dc.predicates;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.hpi.naumann.dc.predicates.sets.Closure;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.metanome.algorithm_integration.Operator;

public class PredicateSetTest {

	@Test
	public void testTransitivity() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);
		ColumnOperand<?> o4 = mock(ColumnOperand.class);

		Predicate p1 = new Predicate(Operator.GREATER, o1, o2);
		Predicate p2 = new Predicate(Operator.GREATER, o2, o3);
		Predicate p3 = new Predicate(Operator.GREATER, o3, o4);
		PredicateBitSet set = new PredicateBitSet();
		set.add(p1);
		set.add(p2);
		set.add(p3);
		Closure closure = new Closure(set);
		assertTrue(closure.construct());

		Assert.assertEquals(36, closure.getClosure().size());
	}

	@Test
	public void testTransitivity2() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);
		ColumnOperand<?> o4 = mock(ColumnOperand.class);
		ColumnOperand<?> o5 = mock(ColumnOperand.class);

		Predicate p1 = new Predicate(Operator.GREATER_EQUAL, o1, o2);
		Predicate p2 = new Predicate(Operator.GREATER_EQUAL, o2, o3);
		Predicate p3 = new Predicate(Operator.GREATER_EQUAL, o3, o4);
		Predicate p4 = new Predicate(Operator.GREATER_EQUAL, o4, o5);
		PredicateBitSet set = new PredicateBitSet();
		set.add(p1);
		set.add(p2);
		set.add(p3);
		set.add(p4);
		Closure closure = new Closure(set);
		assertTrue(closure.construct());

		Assert.assertEquals(20, closure.getClosure().size());
	}

	@Test
	public void testTransitivity3() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);

		Predicate p1 = new Predicate(Operator.UNEQUAL, o1, o2);
		Predicate p2 = new Predicate(Operator.UNEQUAL, o2, o3);
		PredicateBitSet set = new PredicateBitSet();
		set.add(p1);
		set.add(p2);
		Closure closure = new Closure(set);
		assertTrue(closure.construct());

		Assert.assertEquals(4, closure.getClosure().size());
	}

	@Test
	public void testTransitivityMixed() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);

		Predicate p1 = new Predicate(Operator.GREATER_EQUAL, o1, o2);
		Predicate p2 = new Predicate(Operator.GREATER, o2, o3);
		PredicateBitSet set = new PredicateBitSet();
		set.add(p1);
		set.add(p2);
		Closure closure = new Closure(set);
		assertTrue(closure.construct());
		Assert.assertEquals(14, closure.getClosure().size());
	}

	@Test
	public void testTransitivityMixed2() {
		ColumnOperand<?> o1 = mock(ColumnOperand.class);
		ColumnOperand<?> o2 = mock(ColumnOperand.class);
		ColumnOperand<?> o3 = mock(ColumnOperand.class);

		Predicate p1 = new Predicate(Operator.GREATER, o1, o2);
		Predicate p2 = new Predicate(Operator.EQUAL, o2, o3);
		PredicateBitSet set = new PredicateBitSet();
		set.add(p1);
		set.add(p2);
		Closure closure = new Closure(set);
		assertTrue(closure.construct());
		Assert.assertEquals(24, closure.getClosure().size());
	}
}
