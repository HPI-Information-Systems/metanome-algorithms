package de.hpi.naumann.dc.fastdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.paritions.Cluster;
import de.hpi.naumann.dc.paritions.ClusterPair;
import de.hpi.naumann.dc.paritions.IEJoin;
import de.hpi.naumann.dc.paritions.LinePair;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;

public class IEJoinTest {

	@Test
	public void testRandom() {
		for(int i = 0; i < 500; ++i)
			testIteration();
	}
	
	public void testIteration() {
		Cluster c1 = new Cluster(0);
		c1.add(1);
		c1.add(2);

		Cluster c2 = new Cluster(0);
		c2.add(1);
		c2.add(2);
		c2.add(3);

		ParsedColumn<Integer> dur = new ParsedColumn<>("east", "dur", Integer.TYPE, 0);
		dur.addLine(50);
		dur.addLine(100);
		dur.addLine(90);

		ParsedColumn<Integer> rev = new ParsedColumn<>("east", "rev", Integer.TYPE, 1);
		rev.addLine(12);
		rev.addLine(9);
		rev.addLine(5);

		ParsedColumn<Integer> time = new ParsedColumn<>("east", "time", Integer.TYPE, 2);
		time.addLine(100);
		time.addLine(140);
		time.addLine(80);
		time.addLine(90);

		ParsedColumn<Integer> cost = new ParsedColumn<>("east", "cost", Integer.TYPE, 3);
		cost.addLine(6);
		cost.addLine(11);
		cost.addLine(10);
		cost.addLine(5);

		Random r = new Random();
		Operator op1 = getRandomOperator(r);
		Operator op2 = getRandomOperator(r);
		for (int i = 3; i < 501; ++i) {
			c1.add(i);
			dur.addLine(r.nextInt(150));
			rev.addLine(r.nextInt(150));
		}
		for (int i = 4; i < 501; ++i) {
			c2.add(i);
			time.addLine(r.nextInt(150));
			cost.addLine(r.nextInt(150));
		}
		
		int[][] input2s = new int[501][4];
		for (int i = 0; i < 501; ++i) {
			input2s [i][0] = dur.getValue(i);
			input2s[i][1] = rev.getValue(i);
			input2s [i][2] = time.getValue(i);
			input2s[i][3] = cost.getValue(i);
		}
		
		IEJoin join = new IEJoin(input2s);

		test(c1, c2, dur, rev, time, cost, op1, op2, join);
	}

	public static Set<LinePair> bruteForce(ClusterPair clusters, Predicate p1, Predicate p2) {
		Iterator<LinePair> iter = clusters.getLinePairIterator();
		Set<LinePair> result = new HashSet<>();
		while (iter.hasNext()) {
			LinePair next = iter.next();
			if (next.getLine1() != next.getLine2() && p1.satisfies(next.getLine1(), next.getLine2()) && p2.satisfies(next.getLine1(), next.getLine2()))
				result.add(new LinePair(next.getLine1(), next.getLine2()));
		}
		return result;

	}

	private static Operator getRandomOperator(Random r) {
		switch (r.nextInt(4)) {
		case 0:
			return Operator.GREATER;
		case 1:
			return Operator.GREATER_EQUAL;
		case 2:
			return Operator.LESS;
		case 3:
		default:
			return Operator.LESS_EQUAL;
		}
	}

	private static void test(Cluster c1, Cluster c2, ParsedColumn<Integer> dur, ParsedColumn<Integer> rev,
			ParsedColumn<Integer> time, ParsedColumn<Integer> cost, Operator op1, Operator op2, IEJoin join) {
		Predicate p1 = new Predicate(op1, new ColumnOperand<>(dur, 0), new ColumnOperand<>(time, 1));
		Predicate p2 = new Predicate(op2, new ColumnOperand<>(rev, 0), new ColumnOperand<>(cost, 1));

		Set<LinePair> gold = bruteForce(new ClusterPair(c1, c2), p1, p2);
		Set<LinePair> result = new HashSet<LinePair>();
		for(ClusterPair p : join.calc(new ClusterPair(c1, c2), p1, p2)) {
			Iterator<LinePair> iter = p.getLinePairIterator();
			while(iter.hasNext()) {
				LinePair next = iter.next();
				if(next.getLine1() != next.getLine2())
					result.add(next);
			}
		}
		
		boolean valid = gold.equals(result);

		if (!valid) {
			int missing = 0;
			for (LinePair i : gold) {
				if (!result.contains(i))
					++missing;
			}

			int tooMuch = 0;
			for (LinePair i : result) {
				if (!gold.contains(i))
					++tooMuch;
			}
			
			System.out.println("missing: " + missing + " too much: " + tooMuch);
			System.out.println();
			
			assertTrue(valid);
		}
	}

	@Test
	public void testIndexOf2() {
		Integer[] sortedArray = { 1, 1, 3, 3, 3, 5, 5 };

		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 0, sortedArray.length, false, true));
		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 0, sortedArray.length, false, false));

		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 1, sortedArray.length, false, true));
		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 1, sortedArray.length, false, false));

		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 2, sortedArray.length, false, true));
		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 2, sortedArray.length, false, false));

		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 3, sortedArray.length, false, false));
		assertEquals(5, IEJoin.indexOf2(i -> sortedArray[i], 3, sortedArray.length, false, true));

		assertEquals(5, IEJoin.indexOf2(i -> sortedArray[i], 4, sortedArray.length, false, false));
		assertEquals(5, IEJoin.indexOf2(i -> sortedArray[i], 4, sortedArray.length, false, true));

		assertEquals(5, IEJoin.indexOf2(i -> sortedArray[i], 5, sortedArray.length, false, false));
		assertEquals(7, IEJoin.indexOf2(i -> sortedArray[i], 5, sortedArray.length, false, true));

		assertEquals(7, IEJoin.indexOf2(i -> sortedArray[i], 6, sortedArray.length, false, false));
		assertEquals(7, IEJoin.indexOf2(i -> sortedArray[i], 6, sortedArray.length, false, true));
	}

	@Test
	public void testIndexOf2Rev() {
		Integer[] sortedArray = { 5, 5, 3, 3, 1, 1 };

		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 6, sortedArray.length, true, true));
		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 6, sortedArray.length, true, false));

		assertEquals(0, IEJoin.indexOf2(i -> sortedArray[i], 5, sortedArray.length, true, false));
		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 5, sortedArray.length, true, true));

		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 4, sortedArray.length, true, true));
		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 4, sortedArray.length, true, false));

		assertEquals(2, IEJoin.indexOf2(i -> sortedArray[i], 3, sortedArray.length, true, false));
		assertEquals(4, IEJoin.indexOf2(i -> sortedArray[i], 3, sortedArray.length, true, true));

		assertEquals(4, IEJoin.indexOf2(i -> sortedArray[i], 2, sortedArray.length, true, false));
		assertEquals(4, IEJoin.indexOf2(i -> sortedArray[i], 2, sortedArray.length, true, true));

		assertEquals(4, IEJoin.indexOf2(i -> sortedArray[i], 1, sortedArray.length, true, false));
		assertEquals(6, IEJoin.indexOf2(i -> sortedArray[i], 1, sortedArray.length, true, true));

		assertEquals(6, IEJoin.indexOf2(i -> sortedArray[i], 0, sortedArray.length, true, false));
		assertEquals(6, IEJoin.indexOf2(i -> sortedArray[i], 0, sortedArray.length, true, true));
	}

	@Test
	public void testIndexOfUnequal() {
		Integer[] sortedArray = { 1, 1, 3, 3, 3, 5, 5 };

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 0, sortedArray.length, false, false));

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 1, sortedArray.length, false, false));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 2, sortedArray.length, false, false));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 3, sortedArray.length, false, false));

		assertEquals(5, IEJoin.indexOf(i -> sortedArray[i], 4, sortedArray.length, false, false));

		assertEquals(5, IEJoin.indexOf(i -> sortedArray[i], 5, sortedArray.length, false, false));

		assertEquals(7, IEJoin.indexOf(i -> sortedArray[i], 6, sortedArray.length, false, false));
	}

	@Test
	public void testIndexOfEqual() {
		Integer[] sortedArray = { 1, 1, 3, 3, 5, 5 };

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 0, sortedArray.length, false, true));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 1, sortedArray.length, false, true));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 2, sortedArray.length, false, true));

		assertEquals(4, IEJoin.indexOf(i -> sortedArray[i], 3, sortedArray.length, false, true));

		assertEquals(6, IEJoin.indexOf(i -> sortedArray[i], 5, sortedArray.length, false, true));

		assertEquals(6, IEJoin.indexOf(i -> sortedArray[i], 6, sortedArray.length, false, true));
	}

	@Test
	public void testIndexOfRevUnequal() {
		Integer[] sortedArray = { 5, 5, 3, 3, 3, 1, 1 };

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 6, sortedArray.length, true, false));

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 5, sortedArray.length, true, false));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 4, sortedArray.length, true, false));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 3, sortedArray.length, true, false));

		assertEquals(5, IEJoin.indexOf(i -> sortedArray[i], 2, sortedArray.length, true, false));

		assertEquals(5, IEJoin.indexOf(i -> sortedArray[i], 1, sortedArray.length, true, false));

		assertEquals(7, IEJoin.indexOf(i -> sortedArray[i], 0, sortedArray.length, true, false));
	}

	@Test
	public void testIndexOfRevEqual() {
		Integer[] sortedArray = { 5, 5, 3, 3, 1, 1 };

		assertEquals(0, IEJoin.indexOf(i -> sortedArray[i], 6, sortedArray.length, true, true));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 5, sortedArray.length, true, true));

		assertEquals(2, IEJoin.indexOf(i -> sortedArray[i], 4, sortedArray.length, true, true));

		assertEquals(4, IEJoin.indexOf(i -> sortedArray[i], 3, sortedArray.length, true, true));

		assertEquals(4, IEJoin.indexOf(i -> sortedArray[i], 2, sortedArray.length, true, true));

		assertEquals(6, IEJoin.indexOf(i -> sortedArray[i], 1, sortedArray.length, true, true));

		assertEquals(6, IEJoin.indexOf(i -> sortedArray[i], 0, sortedArray.length, true, true));
	}
}
