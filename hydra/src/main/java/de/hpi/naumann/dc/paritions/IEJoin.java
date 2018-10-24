package de.hpi.naumann.dc.paritions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

import ch.javasoft.bitset.LongBitSet;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.IntComparator;

public class IEJoin {
	private enum Order {
		ASCENDING, DESCENDING
	}

	int[][] values;

	public IEJoin(Input input) {
		values = input.getInts();
	}

	public IEJoin(int[][] input2s) {
		this.values = input2s;
	}

	public Collection<ClusterPair> calc(ClusterPair clusters, Predicate p1, Predicate p2) {
		Collection<ClusterPair> result = new ArrayList<>();
		calc(clusters, p1, p2, (pair) -> result.add(pair));
		return result;
	}
	public Collection<ClusterPair> calc(ClusterPair clusters, Predicate p1) {
		Collection<ClusterPair> result = new ArrayList<>();
		calc(clusters, p1, (pair) -> result.add(pair));
		return result;
	}

	private int[] getSortedArray(Cluster c, ParsedColumn<?> column, Order order) {
		int[] array = new int[c.size()];
		TIntIterator iter = c.iterator();
		int i = 0;
		while (iter.hasNext()) {
			array[i++] = iter.next();
		}
		final int cIndex = column.getIndex();

		IntComparator comp = (i1, i2) -> Integer.compare(values[order == Order.DESCENDING ? i2 : i1][cIndex], values[order == Order.DESCENDING ? i1 : i2][cIndex]);
		Primitive.sort(array,comp, false);
		return array;
	}

	private static Order getSortingOrder(int pos, Predicate p) {
		switch (p.getOperator()) {
		case GREATER:
		case GREATER_EQUAL:
			return pos == 0 ? Order.DESCENDING : Order.ASCENDING;
		case LESS:
		case LESS_EQUAL:
			return pos == 0 ? Order.ASCENDING : Order.DESCENDING;
		case EQUAL:
		case UNEQUAL:
		default:
			// WRONG STATE;
			break;
		}
		return null;
	}

	private static int[] getPermutationArray(int[] l2, int[] l1) {
		final int count = l1.length;
		TIntIntMap map = new TIntIntHashMap(count);
		for (int i = 0; i < count; ++i) {
			map.put(l1[i], i);
		}
		int[] result = new int[count];
		for (int i = 0; i < count; ++i) {
			result[i] = map.get(l2[i]);
		}
		return result;
	}

	private int[] getOffsetArray(int[] l2, int[] l2_, int column1, int column2, boolean c2Rev,
			boolean equal) {
		final int size = l2.length;
		int[] result = new int[size];
		for (int i = 0; i < size; ++i) {
			int value = values[l2[i]][column1];

			result[i] = indexOf(index -> values[l2_[index]][column2], value, l2_.length, c2Rev,
					equal);
		}
		return result;
	}

	public static int indexOf2(IntUnaryOperator a, int key, int count, boolean rev, boolean equal) {
		for (int i = 0; i < count; ++i) {
			int c = Integer.compare(key, a.applyAsInt(i));
			if (!rev && equal && c < 0)
				return i;
			if (!rev && !equal && c <= 0)
				return i;
			if (rev && !equal && c >= 0)
				return i;
			if (rev && equal && c > 0)
				return i;
		}
		return count;
	}

	public static int indexOf(IntUnaryOperator a, int key, int count, boolean rev, boolean equal) {
		int lo = 0;
		int hi = count - 1;
		while (lo <= hi) {
			// Key is in a[lo..hi] or not present.
			int mid = lo + (hi - lo) / 2;
			int value = a.applyAsInt(mid);
			int comp = Integer.compare(key, value);
			if (rev) {
				if (!equal && comp >= 0 || equal && comp > 0)
					hi = mid - 1;
				else if (!equal && comp < 0 || equal && comp <= 0)
					lo = mid + 1;
				else
					return mid;
			} else {
				if (!equal && comp <= 0 || equal && comp < 0)
					hi = mid - 1;
				else if (!equal && comp > 0 || equal && comp >= 0)
					lo = mid + 1;
				else
					return mid;
			}

		}
		return lo;
	}


	@SuppressWarnings("rawtypes")
	public void calc(ClusterPair clusters, Predicate p1, Predicate p2, Consumer<ClusterPair> consumer) {
		ColumnOperand op11 = p1.getOperand1();
		ParsedColumn<?> columnX = op11.getColumn();
		ColumnOperand op12 = p1.getOperand2();
		ParsedColumn<?> columnX_ = op12.getColumn();
		ColumnOperand op21 = p2.getOperand1();
		ParsedColumn<?> columnY = op21.getColumn();
		ColumnOperand op22 = p2.getOperand2();
		ParsedColumn<?> columnY_ = op22.getColumn();

		Order order1 = getSortingOrder(0, p1);
		Order order2 = getSortingOrder(1, p2);

//		EtmPoint pointS = etmMonitor.createPoint("sortings..");
		int[] L1 = getSortedArray(clusters.getC1(), columnX, order1);
		int[] L1_ = getSortedArray(clusters.getC2(), columnX_, order1);
		int[] L2 = getSortedArray(clusters.getC1(), columnY, order2);
		int[] L2_ = getSortedArray(clusters.getC2(), columnY_, order2);
//		pointS.collect();

//		EtmPoint pointP = etmMonitor.createPoint("permutations..");
		int[] P = getPermutationArray(L2, L1);
		int[] P_ = getPermutationArray(L2_, L1_);
//		pointP.collect();

//		EtmPoint pointO = etmMonitor.createPoint("offsets..");
		int[] O1 = getOffsetArray(L1, L1_, columnX.getIndex(), columnX_.getIndex(), order1 == Order.DESCENDING,
				p1.getOperator() == Operator.GREATER || p1.getOperator() == Operator.LESS);
		int[] O2 = getOffsetArray(L2, L2_, columnY.getIndex(), columnY_.getIndex(), order2 == Order.DESCENDING,
				p2.getOperator() == Operator.GREATER_EQUAL || p2.getOperator() == Operator.LESS_EQUAL);
//		pointO.collect();

		LongBitSet bitset = new LongBitSet(clusters.getC2().size());

//		EtmPoint pointJ = etmMonitor.createPoint("Join");
		Cluster lastC1 = null;
		Cluster lastC2 = null;
		for (int i = 0; i < clusters.getC1().size(); ++i) {
			// relative position of r_i in L2'
			int off2 = O2[i];
			int start = i > 0 ? O2[i - 1] : 0;
			for (int j = start; j < off2; ++j) {
				bitset.set(P_[j]);
			}
			int start2 = O1[P[i]];

			if (lastC2 != null && start >= off2 && bitset.nextSetBit(start2) == bitset.nextSetBit(O1[P[i - 1]])) {
				lastC1.add(L2[i]);
				continue;
			}

			Cluster c1 = new Cluster(L2[i]);

			int count = 0;
			for (int k = bitset.nextSetBit(start2); k >= 0; k = bitset.nextSetBit(k + 1))
				++count;
			if (count > 0) {
				Cluster c2 = new Cluster(new TIntArrayList(count));
				for (int k = bitset.nextSetBit(start2); k >= 0; k = bitset.nextSetBit(k + 1))
					c2.add(L1_[k]);

				ClusterPair pair = new ClusterPair(c1, c2);
				if (pair.containsLinePair()) {
					if (lastC2 != null && c2.equals(lastC2)) {
						lastC1.add(L2[i]);
					} else {
						if(lastC1 != null) {
							ClusterPair pairLast = new ClusterPair(lastC1, lastC2);
							consumer.accept(pairLast);
						}
						
						lastC2 = c2;
						lastC1 = c1;
					}
				} else {
					if(lastC1 != null) {
						ClusterPair pairLast = new ClusterPair(lastC1, lastC2);
						consumer.accept(pairLast);
					}
					
					lastC2 = lastC1 = null;
				}
			} else {
				if(lastC1 != null) {
					ClusterPair pairLast = new ClusterPair(lastC1, lastC2);
					consumer.accept(pairLast);
				}
				lastC2 = lastC1 = null;
			}
		}
		if(lastC1 != null) {
			ClusterPair pairLast = new ClusterPair(lastC1, lastC2);
			consumer.accept(pairLast);
		}
//		pointJ.collect();
	}

	public void calc(ClusterPair clusters, Predicate p1, Consumer<ClusterPair> consumer) {

		ColumnOperand<?> op11 = p1.getOperand1();
		ParsedColumn<?> columnX = op11.getColumn();
		ColumnOperand<?> op12 = p1.getOperand2();
		ParsedColumn<?> columnX_ = op12.getColumn();

		Order order1 = getSortingOrder(0, p1);

		int[] L1 = getSortedArray(clusters.getC1(), columnX, order1);
		int[] L1_ = getSortedArray(clusters.getC2(), columnX_, order1);

		
		int start2 = 0;
		for (int i = 0; i < L1.length; ++i) {
			while (start2 < L1_.length && !p1.satisfies(L1[i], L1_[start2]))
				++start2;
			if (start2 == L1_.length)
				break;

			Cluster c1 = new Cluster(L1[i]);
			while (i + 1 < L1.length && p1.satisfies(L1[i + 1], L1_[start2])) {
				++i;
				c1.add(L1[i]);
			}

			Cluster c2 = new Cluster();
			for (int j = start2; j < L1_.length; ++j) {
				c2.add(L1_[j]);
			}
			ClusterPair pair = new ClusterPair(c1, c2);
			if (pair.containsLinePair()) {
				consumer.accept(pair);
			}
		}
	}
}
