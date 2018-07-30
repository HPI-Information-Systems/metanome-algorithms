package de.hpi.naumann.dc.paritions;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.paritions.Cluster;
import de.hpi.naumann.dc.paritions.ClusterPair;
import de.hpi.naumann.dc.paritions.StrippedPartition;
import de.hpi.naumann.dc.predicates.PartitionRefiner;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;

public class RefiningTest {
	
	

	@Test
	public void testInequalityJoin() {
		for(int i = 1; i < 50; ++i)
			testRandom(100);

	}

	private void testRandom(int limit) {
		final int ROWS = 200;
		
		Cluster c1 = new Cluster(0);
		for (int i = 0; i < ROWS; ++i)
			c1.add(i);

		Cluster c2 = new Cluster(0);
		for (int i = ROWS - 1; i > 0; i--)
			c2.add(i);

		List<ClusterPair> pair = new ArrayList<>();
		pair.add(new ClusterPair(c1, c2));

		Random r = new Random();
		
		int[][] input2s = new int[ROWS][2];
		ParsedColumn<Integer> time = new ParsedColumn<>("west", "time", Integer.TYPE, 0);
		time.addLine(100);
		time.addLine(140);
		time.addLine(80);
		time.addLine(90);
		time.addLine(10);
		time.addLine(11);
		for(int i = 6; i < ROWS; ++i)
			time.addLine(r.nextInt(limit));

		ParsedColumn<Integer> cost = new ParsedColumn<>("west", "cost", Integer.TYPE, 1);
		cost.addLine(6);
		cost.addLine(11);
		cost.addLine(10);
		cost.addLine(5);
		cost.addLine(20);
		cost.addLine(21);
		for(int i = 6; i < ROWS; ++i)
			cost.addLine(r.nextInt(limit));

		for (int i = 0; i < ROWS; ++i) {
			input2s[i][0] = time.getValue(i);
			input2s[i][1] = cost.getValue(i);
		}
		IEJoin join = new IEJoin(input2s);

		for (Operator op : new Operator[] { Operator.GREATER, Operator.LESS, Operator.LESS_EQUAL,
				Operator.GREATER_EQUAL, Operator.EQUAL, Operator.UNEQUAL }) {
			System.out.println(op);
			Predicate p = new Predicate(op, new ColumnOperand<>(time, 0), new ColumnOperand<>(cost, 1));
			List<PartitionRefiner> predicates = new ArrayList<>();
			predicates.add(p);
			
			StrippedPartition partition = new StrippedPartition(pair);
			long start = System.currentTimeMillis();
			StrippedPartition refined = partition.refine(p, join);
			long timeRefine = System.currentTimeMillis() - start;
			
			Set<LinePair> pairs = new HashSet<LinePair>();
			for (LinePair linePair : refined.getLinePairIterator()) {
				if (linePair.getLine1() != linePair.getLine2())
					pairs.add(linePair);
			}

			long startGold = System.currentTimeMillis();
			StrippedPartition refinedGold = partition.refineFull(predicates, join);
			long timeGold = System.currentTimeMillis() - startGold;
			Set<LinePair> pairsGold = new HashSet<LinePair>();
			for (LinePair linePair : refinedGold.getLinePairIterator()) {
				if (linePair.getLine1() != linePair.getLine2())
					pairsGold.add(linePair);
			}
			System.out.println(pairs.size() + " " + pairsGold.size() + " " + timeRefine + " " + timeGold + " " + refined.clusterCount() + " " + refinedGold.clusterCount());
			assertTrue(pairs.equals(pairsGold));
		}
	}
}
